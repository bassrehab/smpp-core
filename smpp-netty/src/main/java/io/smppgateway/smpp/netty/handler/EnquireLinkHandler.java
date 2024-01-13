package io.smppgateway.smpp.netty.handler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.smppgateway.smpp.pdu.EnquireLink;
import io.smppgateway.smpp.pdu.EnquireLinkResp;
import io.smppgateway.smpp.pdu.Pdu;
import io.smppgateway.smpp.types.CommandStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Dedicated handler for SMPP enquire_link keepalive functionality.
 *
 * <p>This handler:
 * <ul>
 *   <li>Responds to incoming enquire_link requests</li>
 *   <li>Sends enquire_link on write idle</li>
 *   <li>Tracks pending enquire_link responses</li>
 *   <li>Closes connection on read idle (no response)</li>
 * </ul>
 *
 * <p>Add this handler before the main SMPP handler in the pipeline for
 * automatic keepalive management.
 */
public class EnquireLinkHandler extends ChannelDuplexHandler {

    private static final Logger log = LoggerFactory.getLogger(EnquireLinkHandler.class);

    private final AtomicInteger sequenceNumber;
    private final int maxPendingLinks;
    private final AtomicInteger pendingLinks = new AtomicInteger(0);
    private final AtomicLong lastEnquireLinkTime = new AtomicLong(0);

    /**
     * Creates an enquire link handler with shared sequence number.
     *
     * @param sequenceNumber Shared sequence number generator
     * @param maxPendingLinks Maximum pending enquire_links before closing
     */
    public EnquireLinkHandler(AtomicInteger sequenceNumber, int maxPendingLinks) {
        this.sequenceNumber = sequenceNumber;
        this.maxPendingLinks = maxPendingLinks;
    }

    /**
     * Creates an enquire link handler with default settings.
     */
    public EnquireLinkHandler(AtomicInteger sequenceNumber) {
        this(sequenceNumber, 3);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof EnquireLink link) {
            handleEnquireLink(ctx, link);
        } else if (msg instanceof EnquireLinkResp resp) {
            handleEnquireLinkResp(ctx, resp);
        } else {
            // Pass through to next handler
            super.channelRead(ctx, msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent idleEvent) {
            handleIdleState(ctx, idleEvent.state());
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    private void handleEnquireLink(ChannelHandlerContext ctx, EnquireLink link) {
        log.trace("Received enquire_link seq={}", link.sequenceNumber());
        ctx.writeAndFlush(new EnquireLinkResp(link.sequenceNumber(), CommandStatus.ESME_ROK));
    }

    private void handleEnquireLinkResp(ChannelHandlerContext ctx, EnquireLinkResp resp) {
        int pending = pendingLinks.decrementAndGet();
        log.trace("Received enquire_link_resp seq={}, pending={}", resp.sequenceNumber(), pending);
    }

    private void handleIdleState(ChannelHandlerContext ctx, IdleState state) {
        switch (state) {
            case WRITER_IDLE -> {
                if (pendingLinks.get() >= maxPendingLinks) {
                    log.warn("Max pending enquire_links ({}) reached, closing connection",
                             maxPendingLinks);
                    ctx.close();
                } else {
                    sendEnquireLink(ctx);
                }
            }
            case READER_IDLE -> {
                log.debug("Read idle timeout, closing connection");
                ctx.close();
            }
            case ALL_IDLE -> { /* Not used */ }
        }
    }

    private void sendEnquireLink(ChannelHandlerContext ctx) {
        int seq = nextSequenceNumber();
        pendingLinks.incrementAndGet();
        lastEnquireLinkTime.set(System.currentTimeMillis());
        log.trace("Sending enquire_link seq={}", seq);
        ctx.writeAndFlush(new EnquireLink(seq));
    }

    private int nextSequenceNumber() {
        return sequenceNumber.getAndUpdate(n -> (n >= 0x7FFFFFFF) ? 1 : n + 1);
    }

    /**
     * Returns the number of pending enquire_link responses.
     */
    public int getPendingCount() {
        return pendingLinks.get();
    }

    /**
     * Returns the timestamp of the last sent enquire_link.
     */
    public long getLastEnquireLinkTime() {
        return lastEnquireLinkTime.get();
    }
}
