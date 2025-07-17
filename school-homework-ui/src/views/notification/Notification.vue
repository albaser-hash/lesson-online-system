<template>
  <div class="notification-page">
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h2>我的消息</h2>
          <p>系统推送与通知</p>
        </div>
        <el-button
          type="primary"
          @click="goHome"
          class="back-btn pink-btn"
        >
          <i class="el-icon-back"></i>
          返回主页
        </el-button>
      </div>
    </div>
    <el-card v-loading="loading" class="notification-card pink-card">
      <div v-if="!isLogin" class="permission-error pink-gradient-bg">
        <i class="el-icon-warning permission-icon pink-icon"></i>
        <p>请先登录</p>
        <el-button type="primary" class="pink-btn" @click="goLogin">去登录</el-button>
      </div>
      <div v-else-if="notifications.length === 0 && !loading" class="empty-state pink-gradient-bg">
        <i class="el-icon-bell pink-icon"></i>
        <p>暂无消息</p>
      </div>
      <div v-else>
        <div class="notification-actions">
          <el-button type="text" class="pink-link" @click="markAllRead" :disabled="unreadCount === 0">全部标为已读</el-button>
          <el-button type="text" class="pink-link" @click="deleteAllNotifications" :disabled="notifications.length === 0">全部删除</el-button>
          <span class="unread-count" v-if="unreadCount > 0">未读 {{ unreadCount }} 条</span>
        </div>
        <el-table :data="notifications" border style="width: 100%;" class="notification-table pink-table" @row-click="showDetail">
          <el-table-column prop="content" label="内容">
            <template slot-scope="scope">
              <span :class="{'unread-msg': !scope.row.isRead, 'pink-unread': !scope.row.isRead}" @click.stop="showDetail(scope.row)">{{ scope.row.content }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="type" label="类型" width="120">
            <template slot-scope="scope">
              <el-tag :type="getTypeTag(scope.row.type)" class="pink-tag">{{ getTypeText(scope.row.type) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="时间" width="180">
            <template slot-scope="scope">
              <span>{{ formatTime(scope.row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="260">
            <template slot-scope="scope">
              <div class="action-btns">
                <el-button v-if="!scope.row.isRead" size="mini" type="primary" class="pink-btn" @click.stop="markRead(scope.row)">标为已读</el-button>
                <el-button size="mini" class="pink-btn-outline" @click.stop="showDetail(scope.row)">详情</el-button>
                <el-button v-if="scope.row.link" size="mini" type="success" class="pink-btn-outline" @click.stop="goLink(scope.row.link)">前往</el-button>
                <el-button size="mini" type="danger" class="pink-btn-outline" @click.stop="deleteNotificationRow(scope.row)">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination-container">
          <el-pagination
            background
            layout="total, sizes, prev, pager, next, jumper"
            :total="pagination.total"
            :page-size="pagination.pageSize"
            :current-page.sync="pagination.page"
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
            :page-sizes="[5, 10, 20, 50]"
            class="pink-pagination"
          />
        </div>
      </div>
      <el-dialog :visible.sync="detailDialogVisible" title="消息详情" width="500px" class="pink-dialog">
        <div v-if="currentDetail">
          <div class="detail-content">{{ currentDetail.content }}</div>
          <div class="detail-meta">
            <el-tag :type="getTypeTag(currentDetail.type)" class="pink-tag">{{ getTypeText(currentDetail.type) }}</el-tag>
            <span class="detail-time">{{ formatTime(currentDetail.createTime) }}</span>
            <el-button v-if="currentDetail.link" size="mini" type="success" class="pink-btn-outline" style="margin-left: 16px" @click="goLink(currentDetail.link)">前往相关页面</el-button>
          </div>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button class="pink-btn" @click="detailDialogVisible = false">关闭</el-button>
        </span>
      </el-dialog>
    </el-card>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import { getNotificationList, getUnreadCount, markNotificationRead, markAllNotificationsRead, deleteNotification, deleteAllNotifications as deleteAllNotificationsApi } from '@/api/notification'
import webSocketService from '@/utils/websocket'

export default {
  name: 'Notification',
  data() {
    return {
      loading: false,
      notifications: [],
      unreadCount: 0,
      pagination: {
        page: 1,
        pageSize: 10,
        total: 0
      },
      detailDialogVisible: false,
      currentDetail: null
    }
  },
  computed: {
    ...mapState({
      token: state => state.user.token,
      userId: state => state.user.id
    }),
    isLogin() {
      return !!this.token
    }
  },
  created() {
    if (this.isLogin) {
      this.loadNotifications()
    }
  },
  mounted() {
    if (this.isLogin) {
      // 监听 WebSocket 推送，确保回调能拿到消息体
      webSocketService.addListener('EXAM_NOTIFICATION', msg => this.onNewNotification(msg))
      webSocketService.addListener('SYSTEM_NOTIFICATION', msg => this.onNewNotification(msg))
      webSocketService.addListener('COURSE_NOTIFICATION', msg => this.onNewNotification(msg))
    }
  },
  beforeDestroy() {
    // 移除监听器，防止内存泄漏
    webSocketService.removeListener('EXAM_NOTIFICATION', this.onNewNotification)
    webSocketService.removeListener('SYSTEM_NOTIFICATION', this.onNewNotification)
    webSocketService.removeListener('COURSE_NOTIFICATION', this.onNewNotification)
  },
  methods: {
    goHome() {
      this.$router.push('/')
    },
    goLogin() {
      this.$router.push('/login')
    },
    async loadNotifications() {
      this.loading = true
      try {
        const response = await getNotificationList(this.userId, this.pagination.page, this.pagination.pageSize)
        const res = response.data // 统一解包
        if (res && res.code === 200 && res.data) {
          this.notifications = (res.data.records || []).sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
          this.pagination.total = res.data.total || 0
          this.fetchUnreadCount()
        } else {
          this.notifications = []
          this.pagination.total = 0
          this.$message.error(res?.msg || '加载消息失败')
        }
      } catch (e) {
        this.$message.error('加载消息失败')
      } finally {
        this.loading = false
      }
    },
    async fetchUnreadCount() {
      try {
        const response = await getUnreadCount(this.userId)
        const res = response.data // 统一解包
        if (res && res.code === 200) {
          this.unreadCount = res.data
        } else {
          this.unreadCount = 0
        }
      } catch {
        this.unreadCount = 0
      }
    },
    async markRead(row) {
      const response = await markNotificationRead(row.notificationId)
      const res = response.data // 统一解包
      if (res && res.code === 200) {
        row.isRead = true
        this.$message.success(res.data || res.msg || '标记已读成功')
        this.fetchUnreadCount()
      } else {
        this.$message.error(res?.msg || '标记已读失败')
      }
    },
    async markAllRead() {
      const response = await markAllNotificationsRead(this.userId)
      const res = response.data // 统一解包
      if (res && res.code === 200) {
        this.notifications.forEach(n => { n.isRead = true })
        this.$message.success(res.data || res.msg || '全部标记已读成功')
        this.fetchUnreadCount()
      } else {
        this.$message.error(res?.msg || '全部标记已读失败')
      }
    },
    handlePageChange(page) {
      this.pagination.page = page
      this.loadNotifications()
    },
    handleSizeChange(size) {
      this.pagination.pageSize = size
      this.pagination.page = 1
      this.loadNotifications()
    },
    showDetail(row) {
      this.currentDetail = row
      this.detailDialogVisible = true
      // 自动标记为已读
      if (!row.isRead) {
        this.markRead(row)
      }
    },
    formatTime(ts) {
      if (!ts) return ''
      const date = new Date(ts)
      return date.toLocaleString()
    },
    getTypeText(type) {
      switch (type) {
        case 'EXAM': return '考试';
        case 'COURSE': return '课程';
        case 'SYSTEM': return '系统';
        default: return '其他';
      }
    },
    getTypeTag(type) {
      switch (type) {
        case 'EXAM': return 'success';
        case 'COURSE': return 'info';
        case 'SYSTEM': return 'warning';
        default: return 'default';
      }
    },
    // WebSocket推送时可调用此方法自动刷新
    onNewNotification(msg) {
      this.fetchUnreadCount()
      this.loadNotifications()
      // 新增：收到WebSocket推送时弹窗提示
      if (msg && msg.message) {
        this.$notify({
          title: msg.examName ? '考试通知' : '新消息',
          message: msg.message,
          type: 'info',
          duration: 6000
        })
      }
    },
    goLink(link) {
      if (!link) return;
      if (/^https?:\/\//.test(link)) {
        // 外部链接，新窗口打开
        window.open(link, '_blank');
      } else {
        // 内部路由，跳转并关闭弹窗
        this.$router.push(link);
        this.detailDialogVisible = false;
      }
    },
    async deleteNotificationRow(row) {
      this.$confirm('确定要删除这条消息吗？', '提示', { type: 'warning' })
        .then(async () => {
          await deleteNotification(row.notificationId)
          this.$message.success('删除成功')
          this.loadNotifications()
        })
        .catch(() => {})
    },
    async deleteAllNotifications() {
      this.$confirm('确定要删除全部消息吗？', '提示', { type: 'warning' })
        .then(async () => {
          const res = await deleteAllNotificationsApi()
          if (res.data.code === 200) {
            this.$message.success('全部删除成功')
            this.loadNotifications()
          } else {
            this.$message.error('全部删除失败')
          }
        })
        .catch(() => {})
    }
  }
}
</script>

<style scoped>
.notification-page {
  width: 100%;
  margin: 0;
  background: #fff0f6;
  min-height: 100vh;
  padding: 40px 0 40px 0;
}
.page-header {
  margin-bottom: 24px;
}
.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.title-section h2 {
  color: #ff5c8a;
  font-weight: bold;
  margin-bottom: 4px;
  font-size: 2.2rem;
  letter-spacing: 2px;
}
.title-section p {
  color: #b48bb3;
  font-size: 16px;
  margin-top: 2px;
}
.back-btn {
  margin-left: 16px;
}
.pink-btn {
  background: linear-gradient(90deg, #ffb6d5 0%, #ff5c8a 100%) !important;
  border: none !important;
  color: #fff !important;
  font-weight: bold;
  border-radius: 20px !important;
  padding: 10px 24px !important;
  box-shadow: 0 2px 8px #f0c1d6cc;
  transition: box-shadow 0.2s, background 0.2s;
}
.pink-btn:hover, .pink-btn:focus {
  background: linear-gradient(90deg, #ff5c8a 0%, #ffb6d5 100%) !important;
  box-shadow: 0 4px 16px #ffb6d5cc;
}
.pink-btn-outline {
  background: #fff !important;
  color: #ff5c8a !important;
  border: 1.5px solid #ffb6d5 !important;
  border-radius: 20px !important;
  font-weight: bold;
  transition: background 0.2s, color 0.2s;
}
.pink-btn-outline:hover, .pink-btn-outline:focus {
  background: #fff0f6 !important;
  color: #ff5c8a !important;
}
.pink-link {
  color: #ff5c8a !important;
  font-weight: bold;
}
.pink-link:disabled {
  color: #ffd6e6 !important;
}
.pink-card {
  border-radius: 24px !important;
  box-shadow: 0 4px 24px #ffd6e6cc !important;
  padding: 32px 32px 28px 32px !important;
  background: #fff !important;
  width: 100%;
}
.permission-error, .empty-state {
  text-align: center;
  color: #bbb;
  padding: 60px 0 40px 0;
  border-radius: 24px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px #ffd6e6cc;
}
.pink-gradient-bg {
  background: linear-gradient(135deg, #fff0f6 60%, #ffb6d5 100%);
}
.permission-icon, .empty-state i {
  font-size: 56px;
  color: #ffb6d5;
  margin-bottom: 18px;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 2px 12px #ffd6e6cc;
  padding: 12px;
}
.pink-icon {
  color: #ff5c8a !important;
  background: linear-gradient(135deg, #fff0f6 60%, #ffb6d5 100%);
}
.notification-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  margin-bottom: 16px;
  gap: 20px;
}
.unread-count {
  color: #ff5c8a;
  font-weight: bold;
  font-size: 16px;
  background: #fff0f6;
  border-radius: 12px;
  padding: 2px 12px;
}
.notification-table {
  border-radius: 18px !important;
  overflow: hidden;
  background: #fff;
  font-size: 16px;
}
.pink-table ::v-deep .el-table__header th {
  background: #fff0f6 !important;
  color: #ff5c8a !important;
  font-weight: bold;
  font-size: 16px;
}
.pink-table ::v-deep .el-table__row:hover td {
  background: #fff0f6 !important;
}
.pink-unread {
  color: #ff5c8a !important;
  font-weight: bold;
  background: linear-gradient(90deg, #fff0f6 60%, #ffd6e6 100%);
  border-radius: 8px;
  padding: 2px 8px;
}
.unread-msg {
  cursor: pointer;
}
.pagination-container {
  margin-top: 32px;
  text-align: right;
}
.pink-pagination ::v-deep .el-pager li.active {
  background: linear-gradient(90deg, #ffb6d5 0%, #ff5c8a 100%) !important;
  color: #fff !important;
  border-radius: 50%;
}
.pink-pagination ::v-deep .el-pagination__total,
.pink-pagination ::v-deep .el-pagination__sizes,
.pink-pagination ::v-deep .el-pagination__jump {
  color: #ff5c8a !important;
}
.pink-pagination ::v-deep .el-pagination__sizes .el-input__inner {
  border-radius: 12px;
  border: 1.5px solid #ffb6d5;
}
.detail-content {
  font-size: 18px;
  margin-bottom: 18px;
  color: #333;
  background: #fff0f6;
  border-radius: 12px;
  padding: 16px;
}
.detail-meta {
  display: flex;
  align-items: center;
  gap: 18px;
  color: #888;
}
.detail-time {
  font-size: 14px;
}
.pink-tag {
  background: linear-gradient(90deg, #ffb6d5 0%, #fff0f6 100%) !important;
  color: #ff5c8a !important;
  border-radius: 12px !important;
  border: none !important;
  font-weight: bold;
  font-size: 14px;
  padding: 2px 14px;
}
.action-btns {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: flex-start;
  align-items: center;
  row-gap: 10px;
  column-gap: 12px;
}
.action-btns .el-button {
  font-size: 13px !important;
  padding: 5px 10px !important;
  min-width: 60px;
  border-radius: 16px !important;
}
.pink-dialog ::v-deep .el-dialog {
  border-radius: 24px !important;
  background: #fff0f6 !important;
  box-shadow: 0 4px 24px #ffd6e6cc !important;
}
.pink-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(90deg, #ffb6d5 0%, #fff0f6 100%);
  border-radius: 24px 24px 0 0;
  color: #ff5c8a;
  font-weight: bold;
  font-size: 20px;
}
.pink-dialog ::v-deep .el-dialog__footer {
  background: #fff0f6;
  border-radius: 0 0 24px 24px;
}
.dialog-footer .pink-btn {
  min-width: 100px;
}
@media (max-width: 600px) {
  .notification-page {
    padding: 0 4vw 24px 4vw;
    max-width: 100vw;
  }
  .pink-card {
    padding: 10px 2vw 14px 2vw !important;
    border-radius: 14px !important;
  }
  .el-card__body {
    padding: 0 !important;
  }
  .action-btns {
    flex-direction: column;
    gap: 6px;
    align-items: stretch;
  }
  .detail-content {
    font-size: 15px;
    padding: 10px;
    border-radius: 8px;
  }
  .detail-meta {
    gap: 8px;
    flex-wrap: wrap;
  }
  .el-table {
    font-size: 14px !important;
  }
  .pink-dialog ::v-deep .el-dialog {
    width: 96vw !important;
    min-width: unset !important;
    max-width: 100vw !important;
    padding: 0 !important;
  }
  .pink-dialog ::v-deep .el-dialog__header {
    font-size: 16px;
    border-radius: 14px 14px 0 0;
  }
  .pink-dialog ::v-deep .el-dialog__footer {
    border-radius: 0 0 14px 14px;
  }
}
</style>
