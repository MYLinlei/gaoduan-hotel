package com.sky.websocket;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.EOFException;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 商家后台通知 WebSocket 端点。
 */
@Component
@ServerEndpoint("/ws/{clientId}")
@Slf4j
public class AdminWebSocketServer {

    private static final ConcurrentMap<String, AdminWebSocketServer> CLIENTS = new ConcurrentHashMap<>();

    private Session session;
    private String clientId;

    @OnOpen
    public void onOpen(Session session, @PathParam("clientId") String clientId) {
        this.session = session;
        this.clientId = clientId;

        AdminWebSocketServer previous = CLIENTS.put(clientId, this);
        if (previous != null && previous != this) {
            previous.closeSilently();
        }

        log.info("WebSocket connected: clientId={}, online={}", clientId, CLIENTS.size());
    }

    @OnClose
    public void onClose() {
        if (clientId != null) {
            CLIENTS.remove(clientId, this);
        }
        log.info("WebSocket closed: clientId={}, online={}", clientId, CLIENTS.size());
    }

    @OnMessage
    public void onMessage(String message) {
        log.debug("WebSocket message received: clientId={}, message={}", clientId, message);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        if (throwable instanceof EOFException) {
            log.debug("WebSocket disconnected by client: clientId={}", clientId);
            return;
        }
        log.warn("WebSocket error: clientId={}", clientId, throwable);
    }

    public static int getOnlineCount() {
        return CLIENTS.size();
    }

    public static void sendToClient(String clientId, String message) {
        AdminWebSocketServer client = CLIENTS.get(clientId);
        if (client == null) {
            log.debug("WebSocket client not connected: clientId={}", clientId);
            return;
        }
        client.sendText(message);
    }

    public static void broadcast(String message) {
        CLIENTS.values().forEach(client -> client.sendText(message));
    }

    private void sendText(String message) {
        Session currentSession = this.session;
        if (currentSession == null || !currentSession.isOpen()) {
            return;
        }
        currentSession.getAsyncRemote().sendText(message);
    }

    private void closeSilently() {
        Session currentSession = this.session;
        if (currentSession == null || !currentSession.isOpen()) {
            return;
        }
        try {
            currentSession.close();
        } catch (IOException e) {
            log.debug("Failed to close duplicate WebSocket session: clientId={}", clientId, e);
        }
    }
}
