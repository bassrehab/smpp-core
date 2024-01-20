package io.smppgateway.smpp.server;

import io.smppgateway.smpp.pdu.*;
import io.smppgateway.smpp.types.CommandStatus;

/**
 * Handler interface for server-side SMPP events.
 *
 * <p>Implement this interface to handle:
 * <ul>
 *   <li>Bind authentication</li>
 *   <li>Incoming message submissions</li>
 *   <li>Message query, cancel, and replace operations</li>
 *   <li>Session lifecycle events</li>
 * </ul>
 *
 * <p>Example:
 * <pre>
 * public class MyHandler implements SmppServerHandler {
 *     @Override
 *     public BindResult authenticate(SmppServerSession session, String systemId,
 *                                    String password, SmppBindType bindType) {
 *         if (isValidCredentials(systemId, password)) {
 *             return BindResult.success();
 *         }
 *         return BindResult.failure(CommandStatus.ESME_RINVPASWD);
 *     }
 *
 *     @Override
 *     public SubmitSmResult handleSubmitSm(SmppServerSession session, SubmitSm submitSm) {
 *         String messageId = messageStore.store(submitSm);
 *         return SubmitSmResult.success(messageId);
 *     }
 * }
 * </pre>
 *
 * <p>Handler methods are called on virtual threads (Java 21) for high concurrency.
 */
public interface SmppServerHandler {

    /**
     * Called when a client attempts to bind.
     *
     * @param session The session being established
     * @param systemId The client's system ID
     * @param password The client's password
     * @param bindRequest The original bind request
     * @return Authentication result with status and optional system ID
     */
    BindResult authenticate(SmppServerSession session, String systemId,
                           String password, PduRequest<?> bindRequest);

    /**
     * Called when a submit_sm is received.
     *
     * @param session The session that received the message
     * @param submitSm The submit request
     * @return Result with message ID or error status
     */
    SubmitSmResult handleSubmitSm(SmppServerSession session, SubmitSm submitSm);

    /**
     * Called when a deliver_sm response is received (for MT delivery receipts).
     *
     * @param session The session
     * @param response The deliver_sm response
     */
    default void handleDeliverSmResp(SmppServerSession session, DeliverSmResp response) {
        // Default: no-op
    }

    /**
     * Called when a data_sm is received.
     *
     * @param session The session
     * @param dataSm The data_sm request
     * @return Result with message ID or error status
     */
    default DataSmResult handleDataSm(SmppServerSession session, DataSm dataSm) {
        return DataSmResult.failure(CommandStatus.ESME_RINVCMDID);
    }

    /**
     * Called when a query_sm is received.
     *
     * @param session The session
     * @param querySm The query request
     * @return Query result with message state
     */
    default QuerySmResult handleQuerySm(SmppServerSession session, QuerySm querySm) {
        return QuerySmResult.failure(CommandStatus.ESME_RINVCMDID);
    }

    /**
     * Called when a cancel_sm is received.
     *
     * @param session The session
     * @param cancelSm The cancel request
     * @return Command status
     */
    default CommandStatus handleCancelSm(SmppServerSession session, CancelSm cancelSm) {
        return CommandStatus.ESME_RINVCMDID;
    }

    /**
     * Called when a replace_sm is received.
     *
     * @param session The session
     * @param replaceSm The replace request
     * @return Command status
     */
    default CommandStatus handleReplaceSm(SmppServerSession session, ReplaceSm replaceSm) {
        return CommandStatus.ESME_RINVCMDID;
    }

    /**
     * Called when a submit_multi is received.
     *
     * @param session The session
     * @param submitMulti The submit_multi request
     * @return Result with message ID and failed deliveries
     */
    default SubmitMultiResult handleSubmitMulti(SmppServerSession session, SubmitMulti submitMulti) {
        return SubmitMultiResult.failure(CommandStatus.ESME_RINVCMDID);
    }

    /**
     * Called when a new session is created.
     */
    default void sessionCreated(SmppServerSession session) {
        // Default: no-op
    }

    /**
     * Called when a session is bound.
     */
    default void sessionBound(SmppServerSession session) {
        // Default: no-op
    }

    /**
     * Called when a session is destroyed.
     */
    default void sessionDestroyed(SmppServerSession session) {
        // Default: no-op
    }

    /**
     * Result of bind authentication.
     */
    record BindResult(CommandStatus status, String systemId) {
        public static BindResult success() {
            return new BindResult(CommandStatus.ESME_ROK, null);
        }

        public static BindResult success(String systemId) {
            return new BindResult(CommandStatus.ESME_ROK, systemId);
        }

        public static BindResult failure(CommandStatus status) {
            return new BindResult(status, null);
        }

        public boolean isSuccess() {
            return status == CommandStatus.ESME_ROK;
        }
    }

    /**
     * Result of submit_sm handling.
     */
    record SubmitSmResult(CommandStatus status, String messageId) {
        public static SubmitSmResult success(String messageId) {
            return new SubmitSmResult(CommandStatus.ESME_ROK, messageId);
        }

        public static SubmitSmResult failure(CommandStatus status) {
            return new SubmitSmResult(status, null);
        }

        public boolean isSuccess() {
            return status == CommandStatus.ESME_ROK;
        }
    }

    /**
     * Result of data_sm handling.
     */
    record DataSmResult(CommandStatus status, String messageId) {
        public static DataSmResult success(String messageId) {
            return new DataSmResult(CommandStatus.ESME_ROK, messageId);
        }

        public static DataSmResult failure(CommandStatus status) {
            return new DataSmResult(status, null);
        }

        public boolean isSuccess() {
            return status == CommandStatus.ESME_ROK;
        }
    }

    /**
     * Result of query_sm handling.
     */
    record QuerySmResult(CommandStatus status, String messageId, String finalDate,
                         byte messageState, byte errorCode) {
        public static QuerySmResult success(String messageId, String finalDate,
                                           byte messageState, byte errorCode) {
            return new QuerySmResult(CommandStatus.ESME_ROK, messageId,
                                    finalDate, messageState, errorCode);
        }

        public static QuerySmResult failure(CommandStatus status) {
            return new QuerySmResult(status, null, null, (byte) 0, (byte) 0);
        }

        public boolean isSuccess() {
            return status == CommandStatus.ESME_ROK;
        }
    }

    /**
     * Result of submit_multi handling.
     */
    record SubmitMultiResult(CommandStatus status, String messageId,
                             java.util.List<SubmitMultiResp.UnsuccessfulDelivery> failures) {
        public static SubmitMultiResult success(String messageId) {
            return new SubmitMultiResult(CommandStatus.ESME_ROK, messageId, java.util.List.of());
        }

        public static SubmitMultiResult success(String messageId,
                java.util.List<SubmitMultiResp.UnsuccessfulDelivery> failures) {
            return new SubmitMultiResult(CommandStatus.ESME_ROK, messageId, failures);
        }

        public static SubmitMultiResult failure(CommandStatus status) {
            return new SubmitMultiResult(status, null, java.util.List.of());
        }

        public boolean isSuccess() {
            return status == CommandStatus.ESME_ROK;
        }
    }
}
