<template>
  <div class="forum-topic-detail-page">
    <div class="page-header">
      <div class="header-content">
        <el-button @click="goHome" class="back-btn">
          <i class="el-icon-back"></i>
          返回论坛
        </el-button>
      </div>
    </div>
    <el-card class="topic-card">
      <div slot="header" class="clearfix topic-header">
        <div>
          <span class="topic-title-main">{{ topic.topicTitle }}</span>
          <el-tag class="topic-tag">{{ topic.topicCategory }}</el-tag>
        </div>
        <div v-if="topic.userId === currentUserId">
          <el-button 
            type="primary" 
            size="mini" 
            @click="handleEditTopic"
            class="edit-btn"
          >
            编辑
          </el-button>
        </div>
      </div>
      <div class="topic-meta">
        <div class="topic-meta-row">
          <div class="topic-avatar">
            <img
              :src="getAvatarUrl(topic.avatar)"
              alt="头像"
              class="topic-avatar-img"
              @error="onAvatarError"
            />
          </div>
          <span class="topic-username"><b>{{ topic.userName }}</b></span>
          <span class="topic-time">发布时间：{{ formatDateTime(topic.createTime) }}</span>
        </div>
      </div>
      <div class="topic-content">{{ topic.topicContent }}</div>
    </el-card>
    <el-card class="reply-card">
      <div slot="header" class="reply-header"><span>回复列表</span></div>
      <el-timeline>
        <el-timeline-item v-for="reply in replies" :key="reply.replyId" :timestamp="formatDateTime(reply.createTime)" placement="top">
          <div class="reply-row">
            <div class="topic-avatar">
              <img
                :src="getAvatarUrl(reply.avatar)"
                alt="头像"
                class="topic-avatar-img"
                @error="onAvatarError"
              />
            </div>
            <div class="reply-main">
              <b class="topic-username">{{ reply.userName }}</b>
              <div v-if="reply.userId === currentUserId || userType === 'ADMIN'" class="reply-actions">
                <el-button type="text" size="mini" @click="handleEditReply(reply)"><i class="el-icon-edit"></i> 编辑</el-button>
                <el-button type="text" size="mini" class="delete-text-btn" @click="handleDeleteReply(reply.replyId)"><i class="el-icon-delete"></i> 删除</el-button>
              </div>
              <div v-if="reply.isEditing" class="reply-edit-box">
                <el-input type="textarea" v-model="reply.editContent" rows="3" />
                <div class="reply-edit-btns">
                  <el-button size="mini" type="primary" @click="submitEditReply(reply)">保存</el-button>
                  <el-button size="mini" @click="cancelEditReply(reply)">取消</el-button>
                </div>
              </div>
              <div v-else class="reply-content">
                <template v-if="reply.replyToUserName">
                  <span class="reply-to">回复 @{{ reply.replyToUserName }}</span>
                </template>
                {{ reply.replyContent }}
              </div>
              <div class="reply-reply-btn">
                <el-button type="text" size="mini" @click="setReplyTarget(reply)">回复</el-button>
              </div>
            </div>
          </div>
          <div v-if="(reply.children && reply.children.length)" class="child-reply-list">
            <div v-for="child in (reply.children || [])" :key="child.replyId" class="child-reply-card">
              <div class="reply-row">
                <div class="topic-avatar">
                  <img
                    :src="getAvatarUrl(child.avatar)"
                    alt="头像"
                    class="topic-avatar-img"
                    @error="onAvatarError"
                  />
                </div>
                <div class="reply-main">
                  <div><b class="topic-username">{{ child.userName }}</b> <span class="reply-to">回复</span> <b>{{ child.replyToUserName || '楼主' }}</b></div>
                  <el-button
                    v-if="child.userId === currentUserId || userType === 'ADMIN'"
                    type="text"
                    size="mini"
                    class="delete-text-btn"
                    @click="handleDeleteReply(child.replyId)"
                  >
                    <i class="el-icon-delete"></i> 删除
                  </el-button>
                  <el-button
                    v-if="child.userId === currentUserId || userType === 'ADMIN'"
                    type="text"
                    size="mini"
                    @click="handleEditReply(child)"
                  >
                    <i class="el-icon-edit"></i> 编辑
                  </el-button>
                  <div v-if="child.isEditing" class="reply-edit-box">
                    <el-input type="textarea" v-model="child.editContent" rows="3" />
                    <div class="reply-edit-btns">
                      <el-button size="mini" type="primary" @click="submitEditReply(child)">保存</el-button>
                      <el-button size="mini" @click="cancelEditReply(child)">取消</el-button>
                    </div>
                  </div>
                  <div v-else class="reply-content">
                    {{ child.replyContent }}
                  </div>
                  <div class="child-reply-time">{{ formatDateTime(child.createTime) }}</div>
                </div>
              </div>
            </div>
          </div>
        </el-timeline-item>
      </el-timeline>
      <div class="reply-input-box">
        <el-input type="textarea" v-model="newReply" :placeholder="replyPlaceholder" rows="3" />
        <el-button type="primary" class="submit-btn" @click="submitReply">{{ replyTarget ? '回复' : '发表评论' }}</el-button>
        <el-button v-if="replyTarget" class="cancel-reply-btn" @click="cancelReply">取消回复</el-button>
      </div>
    </el-card>
    <el-dialog :visible.sync="editDialogVisible" title="编辑主题" width="90%" class="forum-dialog">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="标题" prop="topicTitle" required>
          <el-input v-model="editForm.topicTitle" placeholder="请输入帖子标题"></el-input>
        </el-form-item>
        <el-form-item label="分类" prop="topicCategory" required>
          <el-select v-model="editForm.topicCategory" placeholder="请选择分类" style="width: 100%;">
            <el-option 
              v-for="category in categories" 
              :key="category.categoryId" 
              :label="category.categoryName" 
              :value="category.categoryName"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="topicContent" required>
          <el-input type="textarea" :rows="8" v-model="editForm.topicContent" placeholder="请输入帖子内容"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEditTopic" class="submit-btn">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>
import { mapState } from 'vuex'
import defaultAvatar from '@/assets/images/profile.jpg'
export default {
  name: 'ForumTopicDetail',
  data() {
    return {
      defaultAvatar: defaultAvatar,
      topic: {},
      replies: [],
      categories: [
        { categoryId: 1, categoryName: '编程语言' },
        { categoryId: 2, categoryName: '前端开发' },
        { categoryId: 3, categoryName: '后端开发' },
        { categoryId: 4, categoryName: '数据库' },
        { categoryId: 5, categoryName: 'DevOps' },
        { categoryId: 6, categoryName: '学习资源' }
      ],
      newReply: '',
      replyTarget: null,
      editDialogVisible: false,
      editForm: {
        topicTitle: '',
        topicCategory: '',
        topicContent: ''
      }
    }
  },
  computed: {
    ...mapState({
      currentUserName: state => state.user.userName,
      name: state => state.user.name,
      currentUserAvatar: state => state.user.avatar,
      currentUserId: state => state.user.id,
      userType: state => state.user.userType
    }),
    replyPlaceholder() {
      if (!this.replyTarget) return '请输入评论内容';
      const targetName = this.replyTarget.replyToUserName;
      return `回复 @${targetName}：`;
    }
  },
  created() {
    const topicId = this.$route.params.id;
    if (topicId) {
      this.loadTopicDetail(topicId);
    }
  },
  methods: {
    formatDateTime(ts) {
      if (!ts) return ''
      const date = new Date(ts)
      const Y = date.getFullYear()
      const M = (date.getMonth() + 1).toString().padStart(2, '0')
      const D = date.getDate().toString().padStart(2, '0')
      const h = date.getHours().toString().padStart(2, '0')
      const m = date.getMinutes().toString().padStart(2, '0')
      return `${Y}-${M}-${D} ${h}:${m}`
    },
    getAvatarUrl(avatar) {
      if (!avatar || (!/^https?:\/\//.test(avatar))) {
        return require('@/assets/images/profile.jpg')
      }
      return avatar
    },
    onAvatarError(e) {
      e.target.src = require('@/assets/images/profile.jpg')
    },
    goHome() {
      this.$router.push('/forum')
    },
    setReplyTarget(reply, child) {
      if (child) {
        this.replyTarget = {
          parentReplyId: child.replyId,
          replyToUserId: child.userId,
          replyToUserName: child.userName
        };
      } else {
        this.replyTarget = {
          parentReplyId: reply.replyId,
          replyToUserId: reply.userId,
          replyToUserName: reply.userName
        };
      }
      this.newReply = '';
    },
    cancelReply() {
      this.replyTarget = null;
      this.newReply = '';
    },
    submitReply() {
      if (!this.newReply.trim()) {
        this.$message.error('评论内容不能为空')
        return
      }
      const newReplyData = {
        replyId: Date.now(),
        userId: this.currentUserId || 1,
        userName: this.name || this.currentUserName || '当前用户',
        avatar: this.currentUserAvatar || '',
        replyContent: this.newReply,
        createTime: new Date().toISOString().replace('T', ' ').substring(0, 19),
        parentReplyId: this.replyTarget ? this.replyTarget.parentReplyId : null,
        replyToUserId: this.replyTarget ? this.replyTarget.replyToUserId : null,
        replyToUserName: this.replyTarget ? this.replyTarget.replyToUserName : null,
        children: []
      };
      if (this.replyTarget) {
        const targetReply = this.findReplyById(this.replies, this.replyTarget.parentReplyId);
        if (targetReply) {
          targetReply.children.push(newReplyData);
        }
      } else {
        this.replies.push(newReplyData);
      }
      this.$message.success('评论成功');
      this.newReply = '';
      this.replyTarget = null;
    },
    findReplyById(replies, replyId) {
      for (let reply of replies) {
        if (reply.replyId === replyId) {
          return reply;
        }
        if (reply.children && reply.children.length > 0) {
          const found = this.findReplyById(reply.children, replyId);
          if (found) return found;
        }
      }
      return null;
    },
    handleDeleteReply(replyId) {
      return this.$confirm('确定要删除这条回复吗？', '提示', { type: 'warning' })
        .then(() => {
          this.removeReplyById(this.replies, replyId);
          this.$message.success('删除成功');
        })
        .catch((err) => {
          console.log('取消或出错', err)
        })
    },
    removeReplyById(replies, replyId) {
      for (let i = 0; i < replies.length; i++) {
        if (replies[i].replyId === replyId) {
          replies.splice(i, 1);
          return true;
        }
        if (replies[i].children && replies[i].children.length > 0) {
          if (this.removeReplyById(replies[i].children, replyId)) {
            return true;
          }
        }
      }
      return false;
    },
    handleEditTopic() {
      this.editForm.topicTitle = this.topic.topicTitle;
      this.editForm.topicCategory = this.topic.topicCategory;
      this.editForm.topicContent = this.topic.topicContent;
      this.editDialogVisible = true;
    },
    submitEditTopic() {
      if (!this.editForm.topicTitle.trim() || !this.editForm.topicContent.trim()) {
        this.$message.error('请填写标题和内容')
        return
      }
      this.topic.topicTitle = this.editForm.topicTitle;
      this.topic.topicCategory = this.editForm.topicCategory;
      this.topic.topicContent = this.editForm.topicContent;
      this.$message.success('编辑成功')
      this.editDialogVisible = false
    },
    handleEditReply(reply) {
      this.$set(reply, 'isEditing', true)
      this.$set(reply, 'editContent', reply.replyContent)
    },
    submitEditReply(reply) {
      if (!reply.editContent.trim()) {
        this.$message.error('回复内容不能为空')
        return
      }
      reply.replyContent = reply.editContent
      this.$set(reply, 'isEditing', false)
      this.$message.success('编辑成功')
    },
    cancelEditReply(reply) {
      this.$set(reply, 'isEditing', false)
      this.$set(reply, 'editContent', '')
    },
    loadTopicDetail(topicId) {
      console.log('开始加载主题详情，topicId:', topicId);
      const mockTopic = {
        topicId: topicId,
        userId: 1,
        topicTitle: 'Java多线程编程最佳实践',
        topicContent: '最近在学习Java多线程，想请教一下在实际项目中如何避免死锁？有什么好的设计模式推荐吗？\n\n我目前遇到的问题是：\n1. 如何正确使用synchronized关键字\n2. 什么时候使用ReentrantLock\n3. 线程池的最佳实践\n4. 如何避免死锁情况\n\n希望有经验的朋友能够分享一下实际项目中的经验。',
        topicCategory: '编程语言',
        replyCount: 3,
        createTime: '2025-07-01 10:30:00',
        userName: '张三',
        avatar: ''
      };
      const mockReplies = [
        {
          replyId: 1,
          userId: 2,
          userName: '李四',
          avatar: '',
          replyContent: '对于死锁问题，建议使用以下策略：\n1. 按固定顺序获取锁\n2. 使用tryLock()方法\n3. 设置锁超时时间。',
          createTime: '2025-07-01 11:00:00',
          parentReplyId: null,
          replyToUserId: null,
          replyToUserName: null,
          children: [
            {
              replyId: 2,
              userId: 3,
              userName: '王五',
              avatar: '',
              replyContent: '@李四 能详细说说tryLock()的使用方法吗？',
              createTime: '2025-07-01 11:30:00',
              parentReplyId: 1,
              replyToUserId: 2,
              replyToUserName: '李四',
              children: []
            }
          ]
        },
        {
          replyId: 3,
          userId: 4,
          userName: '赵六',
          avatar: '',
          replyContent: '推荐使用ThreadPoolExecutor，可以更好地控制线程池参数，避免资源耗尽。',
          createTime: '2025-07-01 12:00:00',
          parentReplyId: null,
          replyToUserId: null,
          replyToUserName: null,
          children: []
        }
      ];
      this.topic = mockTopic;
      this.replies = mockReplies;
      console.log('设置的主题数据', this.topic);
      console.log('设置的回复数据', this.replies);
    }
  }
}
</script>
<style scoped>
.forum-topic-detail-page {
  padding: 20px;
  min-height: 100vh;
  background: linear-gradient(135deg, #ffe4ec 0%, #ffd6e6 100%);
  max-width: 900px;
  margin: 0 auto;
}
.page-header {
  margin-bottom: 20px;
}
.header-content {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  background: #fff;
  padding: 20px;
  border-radius: 16px;
  box-shadow: 0 4px 16px #f0c1d6cc;
}
.back-btn {
  background: #ffb6d5 !important;
  border-color: #ffb6d5 !important;
  color: #fff !important;
  font-weight: bold;
  border-radius: 12px;
  padding: 10px 20px;
  display: flex;
  align-items: center;
}
.back-btn i {
  margin-right: 5px;
}
.back-btn:hover {
  background: #ff5c8a !important;
  border-color: #ff5c8a !important;
}
.topic-card {
  margin-bottom: 20px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 16px #f0c1d6cc;
  border: none;
  overflow: hidden;
}
.topic-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.topic-title-main {
  font-size: 22px;
  font-weight: bold;
  color: #ff5c8a;
}
.topic-tag {
  margin-left: 16px;
  background: #ffb6d5 !important;
  color: #fff !important;
  border-radius: 8px;
  font-weight: bold;
  border: none;
}
.topic-meta-row {
  display: flex;
  align-items: center;
  gap: 15px;
}
.topic-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 10px;
  border: 2px solid #ffb6d5;
  background: #fff;
}
.topic-avatar-img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  background: #fff;
}
.topic-username {
  color: #ff5c8a;
  font-weight: bold;
  font-size: 15px;
}
.topic-time {
  color: #888;
  font-size: 14px;
}
.topic-content {
  margin-top: 18px;
  font-size: 16px;
  line-height: 1.7;
}
.reply-card {
  margin-top: 30px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 16px #f0c1d6cc;
  border: none;
  overflow: hidden;
}
.reply-header {
  font-size: 18px;
  font-weight: bold;
  color: #ff5c8a;
}
.reply-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 10px;
}
.reply-main {
  flex: 1;
}
.reply-actions {
  display: inline-block;
  margin-left: 8px;
}
.delete-text-btn {
  color: #f56c6c !important;
  margin-left: 8px;
}
.reply-edit-box {
  margin-top: 8px;
}
.reply-edit-btns {
  margin-top: 8px;
}
.reply-content {
  margin-top: 4px;
  font-size: 15px;
}
.reply-reply-btn {
  margin-top: 8px;
}
.child-reply-list {
  margin-left: 50px;
  margin-top: 8px;
}
.child-reply-card {
  background: #fff5f8;
  border: 1px solid #ffe4ec;
  font-size: 14px;
  padding: 10px;
  margin-bottom: 8px;
  border-radius: 10px;
}
.child-reply-time {
  font-size: 12px;
  color: #888;
  margin-top: 4px;
}
.reply-input-box {
  margin-top: 24px;
  text-align: right;
}
.submit-btn {
  background: #ffb6d5 !important;
  border-color: #ffb6d5 !important;
  color: #fff !important;
  font-weight: bold;
  border-radius: 12px;
  padding: 10px 20px;
  margin-left: 10px;
}
.submit-btn:hover {
  background: #ff5c8a !important;
  border-color: #ff5c8a !important;
}
.cancel-reply-btn {
  margin-left: 10px;
}
.forum-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(90deg, #ffe4ec 0%, #ffd6e6 100%);
  color: #ff5c8a;
  font-weight: bold;
}
.edit-btn {
  background: #ffb6d5 !important;
  border-color: #ffb6d5 !important;
  color: #fff !important;
  font-weight: bold;
  border-radius: 8px;
}
.edit-btn:hover {
  background: #ff5c8a !important;
  border-color: #ff5c8a !important;
}
@media (max-width: 768px) {
  .forum-topic-detail-page {
    padding: 8px;
  }
  .header-content {
    flex-direction: column;
    text-align: center;
  }
  .back-btn {
    width: 100%;
    margin-bottom: 10px;
  }
  .topic-card, .reply-card {
    border-radius: 10px;
    padding: 8px;
  }
  .topic-title-main {
    font-size: 18px;
  }
  .reply-header {
    font-size: 16px;
  }
  .topic-avatar, .topic-avatar-img {
    width: 32px;
    height: 32px;
  }
  .forum-dialog ::v-deep .el-dialog {
    margin: 5vh auto !important;
    width: 95% !important;
  }
  .forum-dialog ::v-deep .el-dialog__body {
    padding: 15px;
  }
  .forum-dialog ::v-deep .el-form-item__label {
    font-size: 14px;
  }
  .forum-dialog ::v-deep .el-input__inner {
    font-size: 14px;
  }
  .forum-dialog ::v-deep .el-textarea__inner {
    font-size: 14px;
  }
  .forum-dialog ::v-deep .el-select {
    width: 100%;
  }
}
</style>
