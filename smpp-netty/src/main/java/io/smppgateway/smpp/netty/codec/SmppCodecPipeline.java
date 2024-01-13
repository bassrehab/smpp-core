package io.smppgateway.smpp.netty.codec;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Factory for creating SMPP codec pipelines.
 *
 * <p>Sets up the standard SMPP codec chain:
 * <ol>
 *   <li>SSL handler (optional)</li>
 *   <li>Idle state handler (for keepalive)</li>
 *   <li>Frame decoder (length-prefix)</li>
 *   <li>PDU decoder (ByteBuf to Pdu)</li>
 *   <li>PDU encoder (Pdu to ByteBuf)</li>
 *   <li>Application handler</li>
 * </ol>
 */
public class SmppCodecPipeline extends ChannelInitializer<SocketChannel> {

    private final SslContext sslContext;
    private final int maxPduSize;
    private final long readIdleTimeMillis;
    private final long writeIdleTimeMillis;
    private final Supplier<ChannelHandler> handlerFactory;

    private SmppCodecPipeline(Builder builder) {
        this.sslContext = builder.sslContext;
        this.maxPduSize = builder.maxPduSize;
        this.readIdleTimeMillis = builder.readIdleTimeMillis;
        this.writeIdleTimeMillis = builder.writeIdleTimeMillis;
        this.handlerFactory = builder.handlerFactory;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        // SSL handler (optional)
        if (sslContext != null) {
            pipeline.addLast("ssl", sslContext.newHandler(ch.alloc()));
        }

        // Idle state handler for keepalive
        if (readIdleTimeMillis > 0 || writeIdleTimeMillis > 0) {
            pipeline.addLast("idleState", new IdleStateHandler(
                readIdleTimeMillis, writeIdleTimeMillis, 0, TimeUnit.MILLISECONDS));
        }

        // SMPP codec
        pipeline.addLast("frameDecoder", new SmppFrameDecoder(maxPduSize));
        pipeline.addLast("pduDecoder", new SmppPduDecoder());
        pipeline.addLast("pduEncoder", new SmppPduEncoder());

        // Application handler
        if (handlerFactory != null) {
            pipeline.addLast("handler", handlerFactory.get());
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private SslContext sslContext;
        private int maxPduSize = SmppFrameDecoder.DEFAULT_MAX_PDU_SIZE;
        private long readIdleTimeMillis = 60000;
        private long writeIdleTimeMillis = 30000;
        private Supplier<ChannelHandler> handlerFactory;

        /**
         * Sets the SSL context for TLS connections.
         */
        public Builder sslContext(SslContext sslContext) {
            this.sslContext = sslContext;
            return this;
        }

        /**
         * Sets the maximum PDU size in bytes.
         */
        public Builder maxPduSize(int maxPduSize) {
            this.maxPduSize = maxPduSize;
            return this;
        }

        /**
         * Sets the read idle timeout in milliseconds.
         */
        public Builder readIdleTimeMillis(long millis) {
            this.readIdleTimeMillis = millis;
            return this;
        }

        /**
         * Sets the write idle timeout in milliseconds.
         */
        public Builder writeIdleTimeMillis(long millis) {
            this.writeIdleTimeMillis = millis;
            return this;
        }

        /**
         * Sets the channel handler factory.
         */
        public Builder handlerFactory(Supplier<ChannelHandler> factory) {
            this.handlerFactory = factory;
            return this;
        }

        /**
         * Disables idle state detection.
         */
        public Builder noIdleTimeout() {
            this.readIdleTimeMillis = 0;
            this.writeIdleTimeMillis = 0;
            return this;
        }

        public SmppCodecPipeline build() {
            return new SmppCodecPipeline(this);
        }
    }
}
