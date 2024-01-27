package io.smppgateway.smpp.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.smppgateway.smpp.exception.SmppException;
import io.smppgateway.smpp.netty.window.Window;
import io.smppgateway.smpp.netty.window.WindowFuture;
import io.smppgateway.smpp.pdu.*;
import io.smppgateway.smpp.pdu.tlv.Tlv;
import io.smppgateway.smpp.state.SessionState;
import io.smppgateway.smpp.types.*;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents a client-side SMPP session.
 *
 * <p>A session provides methods to send messages and track responses.
 * Use the synchronous methods for simple request-response patterns,
 * or the async methods for higher throughput.
 *
 * <p>Example:
 * <pre>
 * // Synchronous
 * SubmitSmResp resp = session.submitSm(submitSm, Duration.ofSeconds(10));
 *
 * // Asynchronous
 * WindowFuture&lt;SubmitSmResp&gt; future = session.submitSmAsync(submitSm);
 * future.getFuture().thenAccept(resp -> { ... });
 * </pre>
 */
public class SmppClientSession {

    private final String sessionId;
    private final Channel channel;
    private final Window<PduResponse> window;
    private final SmppClientConfiguration config;
    private final Instant createdAt;
    private final AtomicInteger sequenceNumber = new AtomicInteger(1);
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    // Session state
    private volatile SessionState state = SessionState.OPEN;
    private volatile SmppBindType bindType;
    private volatile String serverSystemId;

    // Statistics
    private final AtomicLong submitSmSent = new AtomicLong(0);
    private final AtomicLong deliverSmReceived = new AtomicLong(0);
    private final AtomicLong errors = new AtomicLong(0);

    public SmppClientSession(Channel channel, Window<PduResponse> window, SmppClientConfiguration config) {
        this.sessionId = UUID.randomUUID().toString();
        this.channel = channel;
        this.window = window;
        this.config = config;
        this.createdAt = Instant.now();
    }

    // Identity

    /**
     * Returns the unique session ID.
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Returns the remote server address.
     */
    public InetSocketAddress getRemoteAddress() {
        return (InetSocketAddress) channel.remoteAddress();
    }

    /**
     * Returns when the session was created.
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns the server's system ID from the bind response.
     */
    public String getServerSystemId() {
        return serverSystemId;
    }

    // State

    /**
     * Returns the current session state.
     */
    public SessionState getState() {
        return state;
    }

    /**
     * Returns the bind type.
     */
    public SmppBindType getBindType() {
        return bindType;
    }

    /**
     * Returns true if the session is bound.
     */
    public boolean isBound() {
        return state.isBound();
    }

    /**
     * Returns true if the channel is active.
     */
    public boolean isActive() {
        return channel.isActive();
    }

    // Submit SM

    /**
     * Sends a submit_sm and waits for the response.
     *
     * @param submitSm The submit request
     * @param timeout Maximum time to wait
     * @return The response
     * @throws TimeoutException If no response is received in time
     */
    public SubmitSmResp submitSm(SubmitSm submitSm, Duration timeout)
            throws TimeoutException, InterruptedException {
        WindowFuture<SubmitSmResp> future = submitSmAsync(submitSm);
        return future.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Sends a submit_sm asynchronously.
     *
     * @param submitSm The submit request
     * @return Future that completes with the response
     */
    @SuppressWarnings("unchecked")
    public WindowFuture<SubmitSmResp> submitSmAsync(SubmitSm submitSm)
            throws TimeoutException, InterruptedException {
        checkBound();

        int seq = nextSequenceNumber();
        SubmitSm pdu = new SubmitSm(seq, submitSm.serviceType(),
            submitSm.sourceAddress(), submitSm.destAddress(), submitSm.esmClass(),
            submitSm.protocolId(), submitSm.priorityFlag(), submitSm.scheduleDeliveryTime(),
            submitSm.validityPeriod(), submitSm.registeredDelivery(), submitSm.replaceIfPresent(),
            submitSm.dataCoding(), submitSm.smDefaultMsgId(), submitSm.shortMessage(),
            submitSm.optionalParameters());

        WindowFuture<SubmitSmResp> future = (WindowFuture<SubmitSmResp>)
            window.offer((PduRequest) pdu, config.requestTimeout());

        channel.writeAndFlush(pdu).addListener(f -> {
            if (f.isSuccess()) {
                submitSmSent.incrementAndGet();
            } else {
                window.fail(seq, f.cause());
                errors.incrementAndGet();
            }
        });

        return future;
    }

    // Data SM

    /**
     * Sends a data_sm and waits for the response.
     */
    public DataSmResp dataSm(DataSm dataSm, Duration timeout)
            throws TimeoutException, InterruptedException {
        WindowFuture<DataSmResp> future = dataSmAsync(dataSm);
        return future.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Sends a data_sm asynchronously.
     */
    @SuppressWarnings("unchecked")
    public WindowFuture<DataSmResp> dataSmAsync(DataSm dataSm)
            throws TimeoutException, InterruptedException {
        checkBound();

        int seq = nextSequenceNumber();
        DataSm pdu = new DataSm(seq, dataSm.serviceType(),
            dataSm.sourceAddress(), dataSm.destAddress(), dataSm.esmClass(),
            dataSm.registeredDelivery(), dataSm.dataCoding(), dataSm.optionalParameters());

        WindowFuture<DataSmResp> future = (WindowFuture<DataSmResp>)
            window.offer((PduRequest) pdu, config.requestTimeout());

        channel.writeAndFlush(pdu).addListener(f -> {
            if (!f.isSuccess()) {
                window.fail(seq, f.cause());
                errors.incrementAndGet();
            }
        });

        return future;
    }

    // Query SM

    /**
     * Queries the status of a previously submitted message.
     */
    public QuerySmResp querySm(String messageId, Address sourceAddress, Duration timeout)
            throws TimeoutException, InterruptedException {
        checkBound();

        int seq = nextSequenceNumber();
        QuerySm pdu = new QuerySm(seq, messageId, sourceAddress, List.of());

        @SuppressWarnings("unchecked")
        WindowFuture<QuerySmResp> future = (WindowFuture<QuerySmResp>)
            window.offer((PduRequest) pdu, config.requestTimeout());

        channel.writeAndFlush(pdu).addListener(f -> {
            if (!f.isSuccess()) {
                window.fail(seq, f.cause());
            }
        });

        return future.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    // Cancel SM

    /**
     * Cancels a previously submitted message.
     */
    public CancelSmResp cancelSm(String messageId, String serviceType,
                                 Address sourceAddress, Address destAddress, Duration timeout)
            throws TimeoutException, InterruptedException {
        checkBound();

        int seq = nextSequenceNumber();
        CancelSm pdu = new CancelSm(seq, serviceType, messageId, sourceAddress, destAddress, List.of());

        @SuppressWarnings("unchecked")
        WindowFuture<CancelSmResp> future = (WindowFuture<CancelSmResp>)
            window.offer((PduRequest) pdu, config.requestTimeout());

        channel.writeAndFlush(pdu).addListener(f -> {
            if (!f.isSuccess()) {
                window.fail(seq, f.cause());
            }
        });

        return future.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    // Replace SM

    /**
     * Replaces a previously submitted message.
     */
    public ReplaceSmResp replaceSm(ReplaceSm replaceSm, Duration timeout)
            throws TimeoutException, InterruptedException {
        checkBound();

        int seq = nextSequenceNumber();
        ReplaceSm pdu = new ReplaceSm(seq, replaceSm.messageId(),
            replaceSm.sourceAddress(), replaceSm.scheduleDeliveryTime(),
            replaceSm.validityPeriod(), replaceSm.registeredDelivery(),
            replaceSm.smDefaultMsgId(), replaceSm.shortMessage(), replaceSm.optionalParameters());

        @SuppressWarnings("unchecked")
        WindowFuture<ReplaceSmResp> future = (WindowFuture<ReplaceSmResp>)
            window.offer((PduRequest) pdu, config.requestTimeout());

        channel.writeAndFlush(pdu).addListener(f -> {
            if (!f.isSuccess()) {
                window.fail(seq, f.cause());
            }
        });

        return future.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    // Submit Multi

    /**
     * Sends a message to multiple destinations.
     */
    public SubmitMultiResp submitMulti(SubmitMulti submitMulti, Duration timeout)
            throws TimeoutException, InterruptedException {
        checkBound();

        int seq = nextSequenceNumber();
        SubmitMulti pdu = new SubmitMulti(seq, submitMulti.serviceType(),
            submitMulti.sourceAddress(), submitMulti.destAddresses(), submitMulti.esmClass(),
            submitMulti.protocolId(), submitMulti.priorityFlag(), submitMulti.scheduleDeliveryTime(),
            submitMulti.validityPeriod(), submitMulti.registeredDelivery(), submitMulti.replaceIfPresent(),
            submitMulti.dataCoding(), submitMulti.smDefaultMsgId(), submitMulti.shortMessage(),
            submitMulti.optionalParameters());

        @SuppressWarnings("unchecked")
        WindowFuture<SubmitMultiResp> future = (WindowFuture<SubmitMultiResp>)
            window.offer((PduRequest) pdu, config.requestTimeout());

        channel.writeAndFlush(pdu).addListener(f -> {
            if (!f.isSuccess()) {
                window.fail(seq, f.cause());
            }
        });

        return future.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    // Unbind

    /**
     * Unbinds from the server.
     */
    public void unbind() {
        int seq = nextSequenceNumber();
        channel.writeAndFlush(new Unbind(seq));
        state = SessionState.CLOSED;
    }

    /**
     * Closes the session immediately.
     */
    public ChannelFuture close() {
        window.close();
        return channel.close();
    }

    // State management (called by handler)

    void setBound(SmppBindType bindType, String serverSystemId) {
        this.bindType = bindType;
        this.serverSystemId = serverSystemId;
        this.state = switch (bindType) {
            case TRANSMITTER -> SessionState.BOUND_TX;
            case RECEIVER -> SessionState.BOUND_RX;
            case TRANSCEIVER -> SessionState.BOUND_TRX;
        };
    }

    void incrementDeliverSmReceived() {
        deliverSmReceived.incrementAndGet();
    }

    void incrementErrors() {
        errors.incrementAndGet();
    }

    /**
     * Completes a pending request with its response.
     */
    boolean completeRequest(int sequenceNumber, PduResponse response) {
        return window.complete(sequenceNumber, response);
    }

    // Window access

    /**
     * Returns the number of pending requests.
     */
    public int getPendingCount() {
        return window.size();
    }

    /**
     * Returns the number of available window slots.
     */
    public int getAvailableSlots() {
        return window.getAvailableSlots();
    }

    // Sequence number

    int nextSequenceNumber() {
        return sequenceNumber.getAndUpdate(n -> (n >= 0x7FFFFFFF) ? 1 : n + 1);
    }

    // Attributes

    /**
     * Gets a custom attribute.
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
    }

    /**
     * Sets a custom attribute.
     */
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    // Statistics

    /**
     * Returns the number of submit_sm sent.
     */
    public long getSubmitSmSent() {
        return submitSmSent.get();
    }

    /**
     * Returns the number of deliver_sm received.
     */
    public long getDeliverSmReceived() {
        return deliverSmReceived.get();
    }

    /**
     * Returns the error count.
     */
    public long getErrors() {
        return errors.get();
    }

    // Channel access

    Channel getChannel() {
        return channel;
    }

    private void checkBound() {
        if (!isBound()) {
            throw new SmppException(CommandStatus.ESME_RINVBNDSTS, "Session is not bound");
        }
    }

    @Override
    public String toString() {
        return "SmppClientSession{" +
               "id=" + sessionId +
               ", state=" + state +
               ", bindType=" + bindType +
               ", server=" + getRemoteAddress() +
               '}';
    }
}
