# WebSocket 前后端通讯流程（含源码文件与行号）

## 1. 连接建立

- **前端**
  - `school-homework-ui/src/utils/websocket.js`
    - `WebSocketService.connect(userId)` 方法，约第 20~80 行
    - 关键代码：`this.ws = new WebSocket(wsUrl);`
- **后端**
  - `lesson-server/src/main/java/com/lesson/websocket/WebSocketServer.java`
    - `@OnOpen` 注解的方法，约第 23~38 行

---

## 2. 前端启动心跳定时器

- **前端**
  - `school-homework-ui/src/utils/websocket.js`
    - `WebSocketService.startHeartbeat()` 方法，约第 90~110 行
    - 关键代码：`this.heartbeatTimer = setInterval(() => { ... }, this.heartbeatInterval);`

---

## 3. 前端定时发送心跳包

- **前端**
  - `school-homework-ui/src/utils/websocket.js`
    - `startHeartbeat()` 方法内部
    - 关键代码：`this.ws.send(JSON.stringify({ type: 'PING' }));`（约第 97 行）

---

## 4. 后端收到心跳包并响应

- **后端**
  - `lesson-server/src/main/java/com/lesson/websocket/WebSocketServer.java`
    - `@OnMessage` 注解的方法，约第 49~61 行
    - 关键代码：
      ```java
      if ("{\"type\":\"PING\"}".equals(message)) {
          session.getBasicRemote().sendText("PONG");
          lastActiveMap.put(session, System.currentTimeMillis());
          return;
      }
      ```

---

## 5. 前端收到 PONG 并刷新时间

- **前端**
  - `school-homework-ui/src/utils/websocket.js`
    - `this.ws.onmessage = (event) => { ... }`，约第 67~73 行
    - 关键代码：
      ```js
      if (event.data === 'PONG') {
        this.lastPong = Date.now();
        return;
      }
      ```

---

## 6. 后端定时检查超时连接并自动清理无用 session

- **后端**
  - `lesson-server/src/main/java/com/lesson/websocket/WebSocketServer.java`
    - `@Scheduled(fixedRate = 60000) public void checkWebSocketTimeout()`，约第 220~254 行
    - 关键代码：
      ```java
      // 找出超时的session
      if (now - entry.getValue() > 60000) { // 60秒无心跳
          timeoutSessions.add(entry.getKey());
      }
      // 清理超时的session
      session.close();
      lastActiveMap.remove(session);
      ```
    - 说明：后端每分钟自动检查所有 session，若 60 秒未收到心跳或消息，则主动关闭并清理无用 session，防止资源泄漏。

---

## Mermaid 时序图（带文件和行号注释）

```mermaid
sequenceDiagram
    participant 前端
    participant 后端(WebSocketServer)

    %% 1. 用户登录后，前端自动连接
    前端->>后端(WebSocketServer): 建立WebSocket连接 (ws(s)://.../ws/{userId})
    Note right of 前端: websocket.js connect(userId)\n(约20~80行)\nthis.ws = new WebSocket(wsUrl)
    Note right of 后端(WebSocketServer): WebSocketServer.java @OnOpen\n(约23~38行)

    %% 2. 前端启动心跳定时器
    前端->>前端: 启动心跳定时器\nstartHeartbeat()\n(约90~110行)

    loop 每15秒
        %% 3. 前端定时发送心跳包
        前端->>后端(WebSocketServer): 发送 {"type":"PING"}
        Note right of 前端: websocket.js startHeartbeat()\nthis.ws.send(JSON.stringify({type:'PING'}))\n(约97行)
        Note right of 后端(WebSocketServer): WebSocketServer.java @OnMessage\n(约49~61行)
        后端(WebSocketServer)->>后端(WebSocketServer): 判断消息内容
        alt 如果是 {"type":"PING"}
            后端(WebSocketServer)->>前端: 回复 "PONG"
            Note right of 后端(WebSocketServer): session.getBasicRemote().sendText("PONG")\n(约51行)
            后端(WebSocketServer)->>后端(WebSocketServer): 刷新 lastActiveMap\n(约52行)
        else 其他消息
            后端(WebSocketServer)->>后端(WebSocketServer): 处理业务消息
            后端(WebSocketServer)->>后端(WebSocketServer): 刷新 lastActiveMap
        end
        %% 4. 前端收到PONG
        前端->>前端: 更新 lastPong 时间\nonmessage 事件\n(约67~73行)
    end

    %% 5. 后端定时检查超时连接并自动清理无用 session
    Note over 后端(WebSocketServer): WebSocketServer.java\n@Scheduled(fixedRate=60000)\ncheckWebSocketTimeout()\n(约220~254行)\n自动关闭和清理60秒无心跳的session，防止资源泄漏
``` 