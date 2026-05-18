
package com.example.campusmaster.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@ServerEndpoint("/ws/{userId}")
public class WebSocketServer {

    private static final Logger log = LoggerFactory.getLogger(WebSocketServer.class);
    private static final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private static final Map<String, AtomicInteger> connectionCounts = new ConcurrentHashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        sessions.put(userId, session);
        connectionCounts.put(userId, new AtomicInteger(0));
        log.info("WebSocket连接建立: userId={}, sessionId={}", userId, session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId") String userId) {
        log.info("收到消息: userId={}, message={}", userId, message);
    }

    @OnClose
    public void onClose(Session session, @PathParam("userId") String userId) {
        sessions.remove(userId);
        connectionCounts.remove(userId);
        log.info("WebSocket连接关闭: userId={}, sessionId={}", userId, session.getId());
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket错误: sessionId={}", session.getId(), error);
    }

    public static void sendToUser(String userId, Object message) {
        Session session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                String jsonMessage = objectMapper.writeValueAsString(message);
                session.getBasicRemote().sendText(jsonMessage);
                log.debug("发送消息给用户: userId={}, message={}", userId, jsonMessage);
            } catch (Exception e) {
                log.error("发送消息失败: userId={}", userId, e);
            }
        }
    }

    public static void broadcast(Object message) {
        sessions.forEach((userId, session) -> {
            if (session.isOpen()) {
                try {
                    String jsonMessage = objectMapper.writeValueAsString(message);
                    session.getBasicRemote().sendText(jsonMessage);
                } catch (Exception e) {
                    log.error("广播消息失败: userId={}", userId, e);
                }
            }
        });
    }

    public static int getOnlineCount() {
        return sessions.size();
    }
}
