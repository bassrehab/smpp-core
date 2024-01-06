package io.smppgateway.smpp.types;

/**
 * SMPP command status codes as defined in SMPP 3.4 and 5.0 specifications.
 */
public enum CommandStatus {
    // Success
    OK(0x00000000, "No Error"),

    // General errors
    ESME_RINVMSGLEN(0x00000001, "Invalid Message Length"),
    ESME_RINVCMDLEN(0x00000002, "Invalid Command Length"),
    ESME_RINVCMDID(0x00000003, "Invalid Command ID"),
    ESME_RINVBNDSTS(0x00000004, "Incorrect BIND Status for given command"),
    ESME_RALYBND(0x00000005, "ESME Already in Bound State"),
    ESME_RINVPRTFLG(0x00000006, "Invalid Priority Flag"),
    ESME_RINVREGDLVFLG(0x00000007, "Invalid Registered Delivery Flag"),
    ESME_RSYSERR(0x00000008, "System Error"),

    // Authentication errors
    ESME_RINVSRCADR(0x0000000A, "Invalid Source Address"),
    ESME_RINVDSTADR(0x0000000B, "Invalid Destination Address"),
    ESME_RINVMSGID(0x0000000C, "Invalid Message ID"),
    ESME_RBINDFAIL(0x0000000D, "Bind Failed"),
    ESME_RINVPASWD(0x0000000E, "Invalid Password"),
    ESME_RINVSYSID(0x0000000F, "Invalid System ID"),

    // Cancel errors
    ESME_RCANCELFAIL(0x00000011, "Cancel SM Failed"),

    // Replace errors
    ESME_RREPLACEFAIL(0x00000013, "Replace SM Failed"),

    // Message queue errors
    ESME_RMSGQFUL(0x00000014, "Message Queue Full"),
    ESME_RINVSERTYP(0x00000015, "Invalid Service Type"),

    // Address errors
    ESME_RINVNUMDESTS(0x00000033, "Invalid number of destinations"),
    ESME_RINVDLNAME(0x00000034, "Invalid Distribution List Name"),

    // Destination errors
    ESME_RINVDESTFLAG(0x00000040, "Invalid Destination Flag"),
    ESME_RINVSUBREP(0x00000042, "Invalid submit with replace request"),
    ESME_RINVESMCLASS(0x00000043, "Invalid esm_class field data"),
    ESME_RCNTSUBDL(0x00000044, "Cannot Submit to Distribution List"),
    ESME_RSUBMITFAIL(0x00000045, "Submit SM Failed"),

    // Source/Dest errors
    ESME_RINVSRCTON(0x00000048, "Invalid Source address TON"),
    ESME_RINVSRCNPI(0x00000049, "Invalid Source address NPI"),
    ESME_RINVDSTTON(0x00000050, "Invalid Destination address TON"),
    ESME_RINVDSTNPI(0x00000051, "Invalid Destination address NPI"),

    // System type error
    ESME_RINVSYSTYP(0x00000053, "Invalid system_type field"),

    // Replace password error
    ESME_RINVREPFLAG(0x00000054, "Invalid replace_if_present flag"),
    ESME_RINVNUMMSGS(0x00000055, "Invalid number of messages"),

    // Throttling errors
    ESME_RTHROTTLED(0x00000058, "Throttling error"),

    // Scheduled delivery errors
    ESME_RINVSCHED(0x00000061, "Invalid Scheduled Delivery Time"),
    ESME_RINVEXPIRY(0x00000062, "Invalid Expiry Time"),
    ESME_RINVDFTMSGID(0x00000063, "Invalid Default Message ID"),

    // Temporary errors
    ESME_RX_T_APPN(0x00000064, "ESME Receiver Temporary App Error"),
    ESME_RX_P_APPN(0x00000065, "ESME Receiver Permanent App Error"),
    ESME_RX_R_APPN(0x00000066, "ESME Receiver Reject Message Error"),

    // Query errors
    ESME_RQUERYFAIL(0x00000067, "Query SM Failed"),

    // Optional parameter errors
    ESME_RINVTLVSTREAM(0x000000C0, "Error in optional part of PDU Body"),
    ESME_RTLVNOTALLWD(0x000000C1, "TLV not allowed"),
    ESME_RINVTLVLEN(0x000000C2, "Invalid Parameter Length"),
    ESME_RMISSINGTLV(0x000000C3, "Expected TLV missing"),
    ESME_RINVTLVVAL(0x000000C4, "Invalid TLV Value"),

    // Delivery errors
    ESME_RDELIVERYFAILURE(0x000000FE, "Delivery Failure"),
    ESME_RUNKNOWNERR(0x000000FF, "Unknown Error"),

    // SMPP 5.0 specific
    ESME_RSERTYPUNAUTH(0x00000100, "ESME Not authorized to use specified service_type"),
    ESME_RPROHIBITED(0x00000101, "ESME Prohibited from using specified operation"),
    ESME_RSERTYPUNAVAIL(0x00000102, "Specified service_type is unavailable"),
    ESME_RSERTYPDENIED(0x00000103, "Specified service_type is denied"),
    ESME_RINVDCS(0x00000104, "Invalid Data Coding Scheme"),
    ESME_RINVSRCADDRSUBUNIT(0x00000105, "Source Address Sub unit is Invalid"),
    ESME_RINVDSTADDRSUBUNIT(0x00000106, "Destination Address Sub unit is Invalid"),
    ESME_RINVBCASTFREQINT(0x00000107, "Broadcast Frequency Interval is invalid"),
    ESME_RINVBCASTALIAS_NAME(0x00000108, "Broadcast Alias Name is invalid"),
    ESME_RINVBCASTAREAFMT(0x00000109, "Broadcast Area Format is invalid"),
    ESME_RINVNUMBCAST_AREAS(0x0000010A, "Number of Broadcast Areas is invalid"),
    ESME_RINVBCASTCNTTYPE(0x0000010B, "Broadcast Content Type is invalid"),
    ESME_RINVBCASTMSGCLASS(0x0000010C, "Broadcast Message Class is invalid"),
    ESME_RBCASTFAIL(0x0000010D, "broadcast_sm operation failed"),
    ESME_RBCASTQUERYFAIL(0x0000010E, "query_broadcast_sm failed"),
    ESME_RBCASTCANCELFAIL(0x0000010F, "cancel_broadcast_sm failed"),
    ESME_RINVBCAST_REP(0x00000110, "Number of Repeated Broadcasts is invalid"),
    ESME_RINVBCASTSRVGRP(0x00000111, "Broadcast Service Group is invalid"),
    ESME_RINVBCASTCHANIND(0x00000112, "Broadcast Channel Indicator is invalid");

    private final int code;
    private final String description;

    CommandStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Returns the integer status code.
     */
    public int code() {
        return code;
    }

    /**
     * Returns the human-readable description.
     */
    public String description() {
        return description;
    }

    /**
     * Returns true if this status indicates success.
     */
    public boolean isSuccess() {
        return this == OK;
    }

    /**
     * Returns true if this status indicates an error.
     */
    public boolean isError() {
        return !isSuccess();
    }

    /**
     * Finds the CommandStatus for the given code.
     *
     * @param code the status code
     * @return the CommandStatus, or ESME_RUNKNOWNERR if not found
     */
    public static CommandStatus fromCode(int code) {
        for (CommandStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return ESME_RUNKNOWNERR;
    }
}
