package me.zinch.is.islab2.models.events.ws;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;
import me.zinch.is.islab2.services.WebSocketService;

@ApplicationScoped
public class WsEventHandler {
    private WebSocketService wsService;

    public WsEventHandler() {}

    @Inject
    public WsEventHandler(WebSocketService wsService) {
        this.wsService = wsService;
    }

    public void onWsEvent(
            @Observes(during = TransactionPhase.AFTER_SUCCESS) WsEvent wsEvent
    ) {
        wsService.sendEvent(wsEvent);
    }
}
