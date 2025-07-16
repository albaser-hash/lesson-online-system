class WebSocketService {
  constructor() {
    this.ws = null;
    this.userId = null;
    this.heartbeatInterval = 15000; // 15秒
    this.heartbeatTimer = null;
    this.lastPong = Date.now();
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 10;
    this.reconnectInterval = 3000;
    this.listeners = new Map();
  }

  // 连接到WebSocket
  connect(userId) {
    if (!userId) return;
    if (this.ws && this.ws.readyState === 1) return;
    this.userId = userId;

    // 自动适配开发/生产环境
    let wsUrl;
    const protocol  = window.location.protocol === 'https:' ? 'wss' : 'ws';
    let host;
    if (process.env.NODE_ENV === 'development') {
      // 生产环境：使用环境变量配置

      if (process.env.VUE_APP_WS_BASE) {

        // 从完整URL中提取域名
        try {
          const url = new URL(process.env.VUE_APP_WS_BASE);
          host = url.hostname || url.href

        } catch (e) {
          // 如果不是完整URL，直接使用
          host = process.env.VUE_APP_WS_BASE;
        }
      } else {
        host = window.location.host;
      }
      wsUrl = `${protocol}://${host}/ws/${userId}`;
    } else {
      // 生产环境：使用环境变量配置
      if (process.env.VUE_APP_WS_BASE) {
        // 从完整URL中提取域名
        try {
          const url = new URL(process.env.VUE_APP_WS_BASE);
          host = url.hostname || url.href;
        } catch (e) {
          // 如果不是完整URL，直接使用
          host = process.env.VUE_APP_WS_BASE;
        }
      } else {
        host = window.location.host;
      }
      wsUrl = `${protocol}://${host}/ws/${userId}`;
    }

    console.log('WebSocket连接地址:', wsUrl);
    this.ws = new WebSocket(wsUrl);

    this.ws.onopen = () => {
      this.reconnectAttempts = 0;
      this.startHeartbeat();
    };

    this.ws.onmessage = (event) => {
      if (event.data === 'PONG') {
        this.lastPong = Date.now();
        return;
      }
      this.handleMessage(event.data);
    };

    this.ws.onclose = () => {
      this.stopHeartbeat();
      this.reconnect();
    };

    this.ws.onerror = (error) => {
      console.error('WebSocket错误:', error);
      this.stopHeartbeat();
      this.reconnect();
    };
  }

  startHeartbeat() {
    this.stopHeartbeat();
    this.lastPong = Date.now();
    this.heartbeatTimer = setInterval(() => {
      if (this.ws && this.ws.readyState === 1) {
        this.ws.send(JSON.stringify({ type: 'PING' }));
        // 超时检测
        if (Date.now() - this.lastPong > this.heartbeatInterval * 2) {
          this.ws.close();
        }
      }
    }, this.heartbeatInterval);
  }

  stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer);
      this.heartbeatTimer = null;
    }
  }

  // 处理接收到的消息
  handleMessage(data) {
    try {
      const message = JSON.parse(data);

      // 根据消息类型分发处理
      switch (message.type) {
        case 'EXAM_NOTIFICATION':
          this.handleExamNotification(message);
          break;
        case 'EXAM_STATUS_UPDATE':
          this.handleExamStatusUpdate(message);
          break;
        default:
      }

      // 触发监听器
      this.triggerListeners(message.type, message);
    } catch (error) {
      console.error('解析WebSocket消息失败:', error);
    }
  }

  // 处理考试通知
  handleExamNotification(message) {
    // 显示通知
    if (window.ELEMENT) {
      window.ELEMENT.Message({
        message: message.message,
        type: 'info',
        duration: 5000,
        showClose: true
      });
    }
  }

  // 处理考试状态更新
  handleExamStatusUpdate(message) {
    // 显示状态更新通知
    if (window.ELEMENT) {
      window.ELEMENT.Message({
        message: `考试"${message.examName}"状态已更新为: ${this.getStatusText(message.status)}`,
        type: 'warning',
        duration: 3000
      });
    }
  }

  // 获取状态文本
  getStatusText(status) {
    const statusMap = {
      'UPCOMING': '未开始',
      'ONGOING': '进行中',
      'FINISHED': '已结束'
    };
    return statusMap[status] || status;
  }

  // 重连
  reconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts && this.userId) {
      this.reconnectAttempts++;
      setTimeout(() => {
        // 确保当前没有活跃连接
        if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
          this.connect(this.userId);
        }
      }, this.reconnectInterval);
    } else if (this.reconnectAttempts >= this.maxReconnectAttempts) {
    }
  }

  // 添加消息监听器
  addListener(type, callback) {
    if (!this.listeners.has(type)) {
      this.listeners.set(type, []);
    }
    this.listeners.get(type).push(callback);
  }

  // 移除消息监听器
  removeListener(type, callback) {
    if (this.listeners.has(type)) {
      const callbacks = this.listeners.get(type);
      const index = callbacks.indexOf(callback);
      if (index > -1) {
        callbacks.splice(index, 1);
      }
    }
  }

  // 触发监听器
  triggerListeners(type, message) {
    if (this.listeners.has(type)) {
      this.listeners.get(type).forEach(callback => {
        try {
          callback(message);
        } catch (error) {
          console.error('执行监听器回调失败:', error);
        }
      });
    }
  }

  // 关闭连接
  disconnect() {
    this.stopHeartbeat();
    if (this.ws) {
      try {
        if (this.ws.readyState === WebSocket.OPEN) {
          this.ws.close();
        }
      } catch (error) {
        console.error('关闭WebSocket连接时发生错误:', error);
      } finally {
        this.ws = null;
        this.userId = null;
        this.reconnectAttempts = 0;
      }
    }
  }

  // 发送消息
  send(message) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(message));
    } else {
      console.error('WebSocket未连接，无法发送消息');
    }
  }
}

// 创建全局实例
const webSocketService = new WebSocketService();

export default webSocketService;
