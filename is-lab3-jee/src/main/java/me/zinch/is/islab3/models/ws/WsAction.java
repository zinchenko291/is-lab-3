package me.zinch.is.islab3.models.ws;

public enum WsAction {
    CREATE("CREATE"),
    UPDATE("UPDATE"),
    DELETE("DELETE"),
    STATUS("STATUS"),
    CONFLICT("CONFLICT")
    ;

    private final String value;

    private WsAction(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
