package me.zinch.is.islab2.models.events.ws;

import me.zinch.is.islab2.models.ws.WsAction;
import me.zinch.is.islab2.models.ws.WsEntity;

public class WsEvent {
    private WsEntity entity;
    private WsAction action;
    private Integer id;
    private Object payloadDto;

    public WsEvent() {
    }

    public WsEvent(WsEntity entity, WsAction action, Integer id, Object payloadDto) {
        this.entity = entity;
        this.action = action;
        this.id = id;
        this.payloadDto = payloadDto;
    }

    public WsEntity getEntity() {
        return entity;
    }

    public void setEntity(WsEntity entity) {
        this.entity = entity;
    }

    public WsAction getAction() {
        return action;
    }

    public void setAction(WsAction action) {
        this.action = action;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Object getPayloadDto() {
        return payloadDto;
    }

    public void setPayloadDto(Object payloadDto) {
        this.payloadDto = payloadDto;
    }
}
