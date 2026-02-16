package me.zinch.is.islab3.controllers;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import me.zinch.is.islab3.models.entities.User;
import me.zinch.is.islab3.server.context.CurrentUser;
import me.zinch.is.islab3.services.WebSocketService;

import java.io.StringReader;

@ServerEndpoint("/api/ws")
public class WebSocketController {
    private WebSocketService webSocketService;
    private CurrentUser currentUser;

    public WebSocketController() {}

    @Inject
    public WebSocketController(WebSocketService webSocketService, CurrentUser currentUser) {
        this.webSocketService = webSocketService;
        this.currentUser = currentUser;
    }

    @OnOpen
    public void onOpen(Session session) {
        User user = currentUser == null ? null : currentUser.getUser();
        if (user != null) {
            webSocketService.add(session, user);
        }
    }

    @OnClose
    public void onClose(Session session) {
        webSocketService.remove(session);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        webSocketService.remove(session);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try (JsonReader reader = Json.createReader(new StringReader(message))) {
            JsonObject json = reader.readObject();
            String type = json.getString("type", null);

            if ("ping".equals(type)) {
                String response = Json.createObjectBuilder()
                        .add("type", "pong")
                        .build()
                        .toString();
                session.getBasicRemote().sendText(response);
            }
        } catch (Exception ignored) { }
    }

}
