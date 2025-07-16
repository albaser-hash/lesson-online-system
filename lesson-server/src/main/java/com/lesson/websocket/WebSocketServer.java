package com.lesson.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * WebSocket服务
 */
@ServerEndpoint("/ws/{userId}")
@Component // userId 用于标识用户连接
public class WebSocketServer {

    // 所有连接
    private static final Map<String, Session> sessionMap = new ConcurrentHashMap<>();

    // 按用户ID分组
    private static final Map<Integer, Set<Session>> userSubscribers = new ConcurrentHashMap<>();

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 活跃时间
    private static final Map<Session, Long> lastActiveMap = new ConcurrentHashMap<>();

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userIdStr) {
        int userId;
        try {
            userId = Integer.parseInt(userIdStr);
        } catch (NumberFormatException e) {
            return;
        }
        sessionMap.put(userIdStr, session);
        lastActiveMap.put(session, System.currentTimeMillis());
        userSubscribers.computeIfAbsent(userId, k -> new HashSet<>()).add(session);
    }

    /**
     * 收到客户端消息后调用的方法
     */
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId") String userId) throws IOException {
        // 心跳处理
        if ("{\"type\":\"PING\"}".equals(message)) {
            session.getBasicRemote().sendText("PONG");
            lastActiveMap.put(session, System.currentTimeMillis());
            return;
        }
        lastActiveMap.put(session, System.currentTimeMillis());
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session, @PathParam("userId") String userId) {
        try {
            // 参数校验
            if (userId == null) {
                return;
            }

            // 清理活跃时间记录
            lastActiveMap.remove(session);

            // 清理用户订阅记录
            int id;
            try {
                id = Integer.parseInt(userId);
                Set<Session> sessions = userSubscribers.get(id);
                if (sessions != null) {
                    sessions.remove(session);
                    // 如果该用户没有其他连接了，清理用户记录
                    if (sessions.isEmpty()) {
                        userSubscribers.remove(id);
                    }
                }
            } catch (NumberFormatException e) {
                // 忽略格式错误
            }

            // 清理session记录 - 需要根据session找到对应的userId
            String sessionUserId = null;
            for (Map.Entry<String, Session> entry : sessionMap.entrySet()) {
                if (entry.getValue().equals(session)) {
                    sessionUserId = entry.getKey();
                    break;
                }
            }
            if (sessionUserId != null) {
                sessionMap.remove(sessionUserId);
            }

        } catch (Exception e) {
            System.err.println("WebSocket连接关闭时发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 发生错误时调用的方法
     */
    @OnError
    public void onError(Session session, Throwable error) {
        System.err.println("WebSocket发生错误: " + error.getMessage());
        error.printStackTrace();

        // 尝试清理相关资源
        try {
            if (session != null && session.isOpen()) {
                session.close();
            }
        } catch (IOException e) {
            System.err.println("关闭WebSocket连接时发生异常: " + e.getMessage());
        }
    }

    /**
     * 群发给所有客户端
     */
    public static void sendToAllClient(String message) {
        sessionMap.values().forEach(session -> {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 向特定用户发送消息
     */
    public static void sendMessageToUser(int userId, String message) {
        Set<Session> sessions = userSubscribers.get(userId);
        if (sessions == null) return;
        sessions.forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.getBasicRemote().sendText(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 发送考试通知给特定用户列表
     */
    public static void sendExamNotificationToUsers(List<Integer> userIds, String examName, String message) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "EXAM_NOTIFICATION");
            notification.put("examName", examName);
            notification.put("message", message);
            notification.put("timestamp", System.currentTimeMillis());

            String jsonMessage = objectMapper.writeValueAsString(notification);
            for (Integer userId : userIds) {
                sendMessageToUser(userId, jsonMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送考试状态更新通知给特定用户列表
     */
    public static void sendExamStatusUpdateToUsers(List<Integer> userIds, String examName, String status) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "EXAM_STATUS_UPDATE");
            notification.put("examName", examName);
            notification.put("status", status);
            notification.put("timestamp", System.currentTimeMillis());

            String jsonMessage = objectMapper.writeValueAsString(notification);
            for (Integer userId : userIds) {
                sendMessageToUser(userId, jsonMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送主观题批改通知给特定老师用户列表
     */
    public static void sendReviewNotificationToUsers(List<Integer> userIds, String examName, String message) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "REVIEW_NOTIFICATION");
            notification.put("examName", examName);
            notification.put("message", message);
            notification.put("timestamp", System.currentTimeMillis());

            String jsonMessage = objectMapper.writeValueAsString(notification);
            for (Integer userId : userIds) {
                sendMessageToUser(userId, jsonMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 注意：考试状态更新定时任务已移至 ExamStatusTask 中执行
    // 这里只保留WebSocket连接超时检查
    @Scheduled(fixedRate = 60000)
    public void checkWebSocketTimeout() {
        try {
            long now = System.currentTimeMillis();
            List<Session> timeoutSessions = new ArrayList<>();

            // 找出超时的session
            for (Map.Entry<Session, Long> entry : new HashMap<>(lastActiveMap).entrySet()) {
                if (now - entry.getValue() > 60000) { // 60秒无心跳
                    timeoutSessions.add(entry.getKey());
                }
            }

            // 清理超时的session
            for (Session session : timeoutSessions) {
                try {
                    if (session.isOpen()) {
                        session.close();
                    }
                } catch (IOException e) {
                    System.err.println("关闭超时WebSocket连接时发生异常: " + e.getMessage());
                } finally {
                    lastActiveMap.remove(session);
                }
            }



        } catch (Exception e) {
            System.err.println("定时检查WebSocket超时时发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
