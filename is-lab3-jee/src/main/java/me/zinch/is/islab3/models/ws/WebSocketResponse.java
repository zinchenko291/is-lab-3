package me.zinch.is.islab3.models.ws;

public class WebSocketResponse<T> {
    private String type;
    private String entity;
    private String action;
    private Integer id;
    private T payload;

    public WebSocketResponse(String type, String entity, String action, Integer id, T payload) {
        this.type = type;
        this.entity = entity;
        this.action = action;
        this.id = id;
        this.payload = payload;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }
}
