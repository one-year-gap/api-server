package site.holliverse.shared.logging;

public final class LogFieldKeys {

    private LogFieldKeys() {
    }

    public static final String TIME_STAMP="timestamp";
    public static final String LEVEL="level";
    public static final String SEVERITY = "severity";
    public static final String SERVICE = "service";
    public static final String ERROR_CODE = "errorCode";
    public static final String VERSION = "version";
    public static final String TEAM = "team";
    public static final String TRACE_ID = "traceId";
    public static final String REQUEST_ID = "requestId";
    public static final String METHOD = "method";
    public static final String URI_TEMPLATE = "uriTemplate";
    public static final String STATUS = "status";
    public static final String DURATION_MS = "durationMs";
    public static final String ERROR_TYPE="errorType";
    public static final String STACKTRACE = "stacktrace";
    public static final String EVENT = "event";
    public static final String OUTCOME = "outcome";

    //internal context-only
    public static final String START_AT="startAt";
    public static final String MEMBER_KEY_HASH="memberKeyHash";
}
