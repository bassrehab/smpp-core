package io.smppgateway.smpp.client;

import io.smppgateway.smpp.pdu.*;
import io.smppgateway.smpp.types.CommandStatus;

/**
 * Handler interface for client-side SMPP events.
 *
 * <p>Implement this interface to handle:
 * <ul>
 *   <li>Incoming deliver_sm messages (MO messages, delivery receipts)</li>
 *   <li>Session lifecycle events</li>
 *   <li>Connection errors</li>
 * </ul>
 *
 * <p>Example:
 * <pre>
 * public class MyHandler implements SmppClientHandler {
 *     @Override
 *     public DeliverSmResult handleDeliverSm(SmppClientSession session, DeliverSm deliverSm) {
 *         // Process incoming message
 *         processMessage(deliverSm);
 *         return DeliverSmResult.success();
 *     }
 * }
 * </pre>
 */
public interface SmppClientHandler {

    /**
     * Called when a deliver_sm is received.
     *
     * @param session The session that received the message
     * @param deliverSm The deliver_sm PDU
     * @return Result with status for the response
     */
    DeliverSmResult handleDeliverSm(SmppClientSession session, DeliverSm deliverSm);

    /**
     * Called when a data_sm is received.
     *
     * @param session The session
     * @param dataSm The data_sm PDU
     * @return Result with status for the response
     */
    default DataSmResult handleDataSm(SmppClientSession session, DataSm dataSm) {
        return DataSmResult.success();
    }

    /**
     * Called when an alert_notification is received.
     *
     * @param session The session
     * @param alert The alert notification
     */
    default void handleAlertNotification(SmppClientSession session, AlertNotification alert) {
        // Default: no-op
    }

    /**
     * Called when the session is successfully bound.
     */
    default void sessionBound(SmppClientSession session) {
        // Default: no-op
    }

    /**
     * Called when the session is unbound.
     */
    default void sessionUnbound(SmppClientSession session) {
        // Default: no-op
    }

    /**
     * Called when the connection is lost.
     *
     * @param session The session (may be null if not yet connected)
     * @param cause The reason for disconnection
     */
    default void connectionLost(SmppClientSession session, Throwable cause) {
        // Default: no-op
    }

    /**
     * Called when reconnection is attempted.
     *
     * @param attempt The attempt number (1-based)
     * @param delay The delay before this attempt
     */
    default void reconnecting(int attempt, java.time.Duration delay) {
        // Default: no-op
    }

    /**
     * Called when reconnection succeeds.
     *
     * @param session The new session
     * @param attempts The number of attempts it took
     */
    default void reconnected(SmppClientSession session, int attempts) {
        // Default: no-op
    }

    /**
     * Result of deliver_sm handling.
     */
    record DeliverSmResult(CommandStatus status, String messageId) {
        public static DeliverSmResult success() {
            return new DeliverSmResult(CommandStatus.ESME_ROK, "");
        }

        public static DeliverSmResult success(String messageId) {
            return new DeliverSmResult(CommandStatus.ESME_ROK, messageId);
        }

        public static DeliverSmResult failure(CommandStatus status) {
            return new DeliverSmResult(status, "");
        }

        public boolean isSuccess() {
            return status == CommandStatus.ESME_ROK;
        }
    }

    /**
     * Result of data_sm handling.
     */
    record DataSmResult(CommandStatus status, String messageId) {
        public static DataSmResult success() {
            return new DataSmResult(CommandStatus.ESME_ROK, "");
        }

        public static DataSmResult success(String messageId) {
            return new DataSmResult(CommandStatus.ESME_ROK, messageId);
        }

        public static DataSmResult failure(CommandStatus status) {
            return new DataSmResult(status, "");
        }

        public boolean isSuccess() {
            return status == CommandStatus.ESME_ROK;
        }
    }
}
