package io.smppgateway.smpp.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.smppgateway.smpp.pdu.*;
import io.smppgateway.smpp.state.SessionState;
import io.smppgateway.smpp.types.Address;
import io.smppgateway.smpp.types.CommandStatus;
import io.smppgateway.smpp.types.SmppBindType;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents a server-side SMPP session.
 *
 * <p>Each client connection is represented by a session that tracks:
 * <ul>
 *   <li>Connection state and bind type</li>
 *   <li>Client identification (system ID, bind address)</li>
 *   <li>Message statistics</li>
 *   <li>Custom attributes</li>
 * </ul>
 *
 * <p>Sessions can send messages to the client (e.g., deliver_sm for MT messages).
 */
public class SmppServerSession {

    private final String sessionId;
    private final Channel channel;
    private final Instant createdAt;
    private final AtomicInteger sequenceNumber = new AtomicInteger(1);
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    // Session state
    private volatile SessionState state = SessionState.OPEN;
    private volatile SmppBindType bindType;
    private volatile String systemId;
    private volatile String password;
    private volatile String systemType;
    private volatile byte interfaceVersion;

    // Statistics
    private final AtomicLong submitSmReceived = new AtomicLong(0);
    private final AtomicLong deliverSmSent = new AtomicLong(0);
    private final AtomicLong errors = new AtomicLong(0);

    public SmppServerSession(Channel channel) {
        this.sessionId = UUID.randomUUID().toString();
        this.channel = channel;
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
     * Returns the system ID provided during bind.
     */
    public String getSystemId() {
        return systemId;
    }

    /**
     * Returns the remote address.
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

    // State

    /**
     * Returns the current session state.
     */
    public SessionState getState() {
        return state;
    }

    /**
     * Returns the bind type, or null if not bound.
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
     * Returns true if the session can receive messages (RX or TRX).
     */
    public boolean canReceive() {
        return bindType == SmppBindType.RECEIVER || bindType == SmppBindType.TRANSCEIVER;
    }

    /**
     * Returns true if the session can transmit messages (TX or TRX).
     */
    public boolean canTransmit() {
        return bindType == SmppBindType.TRANSMITTER || bindType == SmppBindType.TRANSCEIVER;
    }

    // Bind management (called by server handler)

    public void setBound(SmppBindType bindType, String systemId, String password,
                  String systemType, byte interfaceVersion) {
        this.bindType = bindType;
        this.systemId = systemId;
        this.password = password;
        this.systemType = systemType;
        this.interfaceVersion = interfaceVersion;
        this.state = switch (bindType) {
            case TRANSMITTER -> SessionState.BOUND_TX;
            case RECEIVER -> SessionState.BOUND_RX;
            case TRANSCEIVER -> SessionState.BOUND_TRX;
        };
    }

    public void setUnbound() {
        this.state = SessionState.CLOSED;
    }

    // Sending messages

    /**
     * Sends a deliver_sm to the client (for MT delivery).
     *
     * @param deliverSm The deliver_sm PDU
     * @return Future that completes when the response is received
     */
    public CompletableFuture<DeliverSmResp> sendDeliverSm(DeliverSm deliverSm) {
        if (!canReceive()) {
            return CompletableFuture.failedFuture(
                new IllegalStateException("Session cannot receive messages"));
        }

        int seq = nextSequenceNumber();
        DeliverSm pdu = new DeliverSm(seq, deliverSm.serviceType(),
            deliverSm.sourceAddress(), deliverSm.destAddress(), deliverSm.esmClass(),
            deliverSm.protocolId(), deliverSm.priorityFlag(), deliverSm.scheduleDeliveryTime(),
            deliverSm.validityPeriod(), deliverSm.registeredDelivery(), deliverSm.replaceIfPresent(),
            deliverSm.dataCoding(), deliverSm.smDefaultMsgId(), deliverSm.shortMessage(),
            deliverSm.optionalParameters());

        CompletableFuture<DeliverSmResp> future = new CompletableFuture<>();
        channel.writeAndFlush(pdu).addListener(f -> {
            if (f.isSuccess()) {
                deliverSmSent.incrementAndGet();
            } else {
                errors.incrementAndGet();
                future.completeExceptionally(f.cause());
            }
        });

        return future;
    }

    /**
     * Sends an alert_notification to the client.
     */
    public ChannelFuture sendAlertNotification(Address sourceAddr, Address esmeAddr) {
        if (!canReceive()) {
            throw new IllegalStateException("Session cannot receive messages");
        }

        AlertNotification alert = new AlertNotification(
            nextSequenceNumber(), sourceAddr, esmeAddr, java.util.List.of());
        return channel.writeAndFlush(alert);
    }

    /**
     * Sends an outbind to request a bind from the client.
     */
    public ChannelFuture sendOutbind(String systemId, String password) {
        Outbind outbind = new Outbind(nextSequenceNumber(), systemId, password, List.of());
        return channel.writeAndFlush(outbind);
    }

    /**
     * Unbinds and closes the session.
     */
    public ChannelFuture unbind() {
        Unbind unbind = new Unbind(nextSequenceNumber());
        return channel.writeAndFlush(unbind);
    }

    /**
     * Closes the session immediately.
     */
    public ChannelFuture close() {
        return channel.close();
    }

    // Sequence number

    private int nextSequenceNumber() {
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

    /**
     * Removes a custom attribute.
     */
    public void removeAttribute(String key) {
        attributes.remove(key);
    }

    // Statistics

    /**
     * Returns the number of submit_sm received.
     */
    public long getSubmitSmReceived() {
        return submitSmReceived.get();
    }

    /**
     * Returns the number of deliver_sm sent.
     */
    public long getDeliverSmSent() {
        return deliverSmSent.get();
    }

    /**
     * Returns the error count.
     */
    public long getErrors() {
        return errors.get();
    }

    public void incrementSubmitSmReceived() {
        submitSmReceived.incrementAndGet();
    }

    public void incrementErrors() {
        errors.incrementAndGet();
    }

    // Channel access

    Channel getChannel() {
        return channel;
    }

    @Override
    public String toString() {
        return "SmppServerSession{" +
               "id=" + sessionId +
               ", systemId=" + systemId +
               ", state=" + state +
               ", bindType=" + bindType +
               ", remote=" + getRemoteAddress() +
               '}';
    }
}
