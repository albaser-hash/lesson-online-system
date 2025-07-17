<template>
  <div class="qa-page">
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h2>问答社区</h2>
          <p>在这里提问和回答问题</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" @click="askDialogVisible=true" class="ask-btn">
            <i class="el-icon-edit"></i>
            我要提问
          </el-button>
          <el-button @click="goHome" class="back-btn">
            <i class="el-icon-back"></i>
            返回主页
          </el-button>
        </div>
      </div>
    </div>

    <el-card class="qa-card">
      <div v-if="questions.length === 0" class="empty-state">
        <i class="el-icon-chat-dot-round empty-icon"></i>
        <p>暂无问题，快来提问吧！</p>
        <el-button type="primary" @click="askDialogVisible=true">我要提问</el-button>
      </div>

      <el-table v-else :data="questions" border style="width: 100%;" class="qa-table" @row-click="viewDetail">
        <el-table-column prop="questionTitle" label="标题" min-width="400">
          <template slot-scope="scope">
            <div class="forum-list-row">
              <img
                :src="getAvatarUrl(scope.row.avatar)"
                alt="头像"
                class="forum-list-avatar"
                @error="onAvatarError"
              />
              <span class="forum-list-username">{{ scope.row.userName }}</span>
              <span class="topic-title">{{ scope.row.questionTitle }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="tags" label="标签" width="150">
          <template slot-scope="scope">
            <el-tag v-for="tag in getTags(scope.row.tags)" :key="tag" size="small" type="info" style="margin-right: 4px;">
              {{ tag }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="answerCount" label="回答数" width="100">
          <template slot-scope="scope">
            <span class="answer-count">{{ scope.row.answerCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="提问时间" width="160">
          <template slot-scope="scope">
            <span class="time-text">{{ formatTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template slot-scope="scope">
            <el-button
              v-if="scope.row.userId === currentUserId || userType === 'ADMIN'"
              type="primary"
              size="mini"
              @click.stop="handleEditQuestion(scope.row)"
              class="edit-btn"
            >
              编辑
            </el-button>
            <el-button
              v-if="scope.row.userId === currentUserId || userType === 'ADMIN'"
              type="danger"
              size="mini"
              @click.stop="handleDeleteQuestion(scope.row)"
              class="delete-btn"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container" v-if="questions.length > 0">
        <div class="pagination-wrapper">
          <div class="pagination-info">
            <span class="total-info">共 {{ total }} 条</span>
            <span class="page-info">第 {{ page }} / {{ Math.ceil(total / pageSize) }} 页</span>
          </div>
          <div class="pagination-controls">
            <el-button 
              :disabled="page <= 1" 
              @click="handleCurrentChange(page - 1)"
              class="page-btn prev-btn"
              size="small"
            >
              <i class="el-icon-arrow-left"></i>
              上一页
            </el-button>
            
            <div class="page-numbers" v-if="!isMobile">
              <el-button 
                v-for="pageNum in visiblePages" 
                :key="pageNum"
                @click="handleCurrentChange(pageNum)"
                :class="['page-btn', { active: pageNum === page }]"
                size="small"
              >
                {{ pageNum }}
              </el-button>
            </div>
            
            <div class="page-numbers mobile" v-else>
              <el-button 
                v-for="pageNum in mobileVisiblePages" 
                :key="pageNum"
                @click="handleCurrentChange(pageNum)"
                :class="['page-btn', { active: pageNum === page }]"
                size="small"
              >
                {{ pageNum }}
              </el-button>
            </div>
            
            <el-button 
              :disabled="page >= Math.ceil(total / pageSize)" 
              @click="handleCurrentChange(page + 1)"
              class="page-btn next-btn"
              size="small"
            >
              下一页
              <i class="el-icon-arrow-right"></i>
            </el-button>
            
            <!-- PC端跳转功能 -->
            <div class="jump-to-page" v-if="!isMobile">
              <span class="jump-text">跳转到</span>
              <el-input
                v-model="jumpPage"
                class="jump-input"
                size="small"
                @keyup.enter="handleJumpPage"
                placeholder="页码"
              />
              <span class="jump-text">页</span>
              <el-button 
                @click="handleJumpPage"
                class="jump-btn"
                size="small"
              >
                确定
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 提问对话框 -->
    <el-dialog :visible.sync="askDialogVisible" title="我要提问" width="500px" class="ask-dialog">
      <el-form :model="askForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="askForm.question_title" maxlength="100" placeholder="请输入问题标题" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input type="textarea" v-model="askForm.question_content" maxlength="500" rows="4" placeholder="请详细描述您的问题" />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="askForm.tags" placeholder="用逗号分隔，如：高数,极限" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="askDialogVisible=false">取消</el-button>
        <el-button type="primary" @click="submitAsk" class="submit-btn">提交</el-button>
      </div>
    </el-dialog>

    <!-- 编辑对话框 -->
    <el-dialog :visible.sync="editDialogVisible" title="编辑问题" width="500px" class="edit-dialog">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="editForm.question_title" maxlength="100" placeholder="请输入问题标题" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input type="textarea" v-model="editForm.question_content" maxlength="500" rows="4" placeholder="请详细描述您的问题" />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="editForm.tags" placeholder="用逗号分隔，如：高数,极限" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="editDialogVisible=false">取消</el-button>
        <el-button type="primary" @click="submitEdit" class="submit-btn">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { getQuestionList, addQuestion, deleteQuestion, updateQuestion } from '@/api/questions'
import { mapState } from 'vuex'

export default {
  name: 'QA',
  data() {
    return {
      questions: [],
      total: 0,
      page: 1,
      pageSize: 10,
      askDialogVisible: false,
      askForm: {
        question_title: '',
        question_content: '',
        tags: ''
      },
      editDialogVisible: false,
      editForm: {
        question_title: '',
        question_content: '',
        tags: ''
      },
      currentEditId: null,
      isMobile: false,
      jumpPage: ''
    }
  },
  computed: {
    ...mapState({
      currentUserId: state => state.user.id,
      userType: state => state.user.userType
    }),
    visiblePages() {
      const totalPages = Math.ceil(this.total / this.pageSize)
      const currentPage = this.page
      const pages = []
      
      if (totalPages <= 10) {
        for (let i = 1; i <= totalPages; i++) {
          pages.push(i)
        }
      } else {
        if (currentPage <= 5) {
          for (let i = 1; i <= 7; i++) {
            pages.push(i)
          }
          pages.push('...')
          pages.push(totalPages)
        } else if (currentPage >= totalPages - 4) {
          pages.push(1)
          pages.push('...')
          for (let i = totalPages - 6; i <= totalPages; i++) {
            pages.push(i)
          }
        } else {
          pages.push(1)
          pages.push('...')
          for (let i = currentPage - 2; i <= currentPage + 2; i++) {
            pages.push(i)
          }
          pages.push('...')
          pages.push(totalPages)
        }
      }
      
      return pages
    },
    mobileVisiblePages() {
      const totalPages = Math.ceil(this.total / this.pageSize)
      const currentPage = this.page
      const pages = []
      
      if (totalPages <= 3) {
        for (let i = 1; i <= totalPages; i++) {
          pages.push(i)
        }
      } else {
        if (currentPage <= 2) {
          for (let i = 1; i <= 3; i++) {
            pages.push(i)
          }
        } else if (currentPage >= totalPages - 1) {
          for (let i = totalPages - 2; i <= totalPages; i++) {
            pages.push(i)
          }
        } else {
          pages.push(currentPage - 1)
          pages.push(currentPage)
          pages.push(currentPage + 1)
        }
      }
      
      return pages
    }
  },
  methods: {
    goHome() {
      this.$router.push('/')
    },
    fetchQuestions() {
      getQuestionList({
        page: this.page,
        pageSize: this.pageSize
      }).then(res => {
        if (res && res.data) {
          const records = res.data.data.records || res.data.data.list || []
          this.questions = records // 直接使用后端字段
          this.total = Number(res.data.data.total)
        }
      }).catch(error => {
        this.$message.error('加载问题失败，请重试')
      })
    },
    formatTime(ts) {
      if (!ts) return ''
      const date = new Date(ts)
      const y = date.getFullYear()
      const m = String(date.getMonth() + 1).padStart(2, '0')
      const d = String(date.getDate()).padStart(2, '0')
      const h = String(date.getHours()).padStart(2, '0')
      const min = String(date.getMinutes()).padStart(2, '0')
      return `${y}-${m}-${d} ${h}:${min}`
    },
    handleCurrentChange(val) {
      this.page = val
      this.fetchQuestions()
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.page = 1
      this.fetchQuestions()
    },
    viewDetail(row) {
      this.$router.push({ name: 'QAQuestionDetail', params: { id: row.questionId } })
    },
    handleDeleteQuestion(row) {
      this.$confirm('确定要删除这个问题吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteQuestion(row.questionId).then(() => {
          this.$message.success('删除成功')
          this.fetchQuestions()
        }).catch(() => {
          this.$message.error('删除失败，请重试')
        })
      }).catch(() => {
        // 用户取消删除
      })
    },
    submitAsk() {
      if (!this.askForm.question_title.trim() || !this.askForm.question_content.trim()) {
        this.$message.error('请填写标题和内容')
        return
      }
      addQuestion({
        userId: this.$store.state.user.id,
        questionTitle: this.askForm.question_title,
        questionContent: this.askForm.question_content,
        tags: this.askForm.tags
      }).then(res => {
        this.$message.success('提问成功')
        this.askDialogVisible = false
        this.askForm = { question_title: '', question_content: '', tags: '' }
        this.page = 1
        this.fetchQuestions()
        // 如需跳转到详情页可解开下面注释
        // if (res && res.data && res.data.id) {
        //   this.$router.push(`/qa/${res.data.id}`)
        // }
      })
    },
    handleEditQuestion(row) {
      this.editForm = {
        question_title: row.questionTitle,
        question_content: row.questionContent,
        tags: row.tags
      }
      this.currentEditId = row.questionId
      this.editDialogVisible = true
    },
    submitEdit() {
      if (!this.editForm.question_title.trim() || !this.editForm.question_content.trim()) {
        this.$message.error('请填写标题和内容')
        return
      }
      updateQuestion({
        questionId: this.currentEditId,
        questionTitle: this.editForm.question_title,
        questionContent: this.editForm.question_content,
        tags: this.editForm.tags,
        userId: this.currentUserId
      }).then(() => {
        this.$message.success('编辑成功')
        this.editDialogVisible = false
        this.currentEditId = null
        this.fetchQuestions()
      }).catch(() => {
        this.$message.error('编辑失败，请重试')
      })
    },
    getTags(tags) {
      if (!tags) return []
      return tags.split(',').map(tag => tag.trim()).filter(tag => tag.length > 0)
    },
    getAvatarUrl(avatar) {
      if (!avatar) return require('@/assets/images/profile.jpg')
      if (/^https?:\/\//.test(avatar) || avatar.startsWith('/')) {
        return avatar
      }
      return '/api/upload/' + avatar
    },
    onAvatarError(e) {
      e.target.src = require('@/assets/images/profile.jpg')
    },
    checkMobile() {
      this.isMobile = window.innerWidth < 768
    },
    handleJumpPage() {
      const page = parseInt(this.jumpPage)
      if (page >= 1 && page <= Math.ceil(this.total / this.pageSize)) {
        this.page = page
        this.fetchQuestions()
      } else {
        this.$message.error('请输入有效的页码')
      }
    }
  },
  mounted() {
    this.fetchQuestions()
    this.checkMobile()
    window.addEventListener('resize', this.checkMobile)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.checkMobile)
  }
}
</script>
<style scoped>
.qa-page {
  padding: 20px;
  min-height: 100vh;
  background: linear-gradient(135deg, #ffe4ec 0%, #ffd6e6 100%);
}

.qa-page ::v-deep .el-table__row {
  cursor: pointer;
}

.qa-page ::v-deep .el-table__row:hover {
  background-color: #fff5f8 !important;
}

.qa-title-cell {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;
  padding: 8px 0;
  min-width: 220px;
}
.qa-user-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.qa-avatar-img {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #ffb6d5;
  background: #fff;
  box-shadow: 0 2px 8px #ffd6e6;
}
.user-name {
  color: #ff5c8a;
  font-weight: bold;
  font-size: 15px;
  letter-spacing: 0.5px;
}
.qa-title-link {
  font-weight: bold;
  color: #ff5c8a;
  font-size: 18px;
  word-break: break-all;
  line-height: 1.5;
  transition: color 0.2s, text-decoration 0.2s;
  cursor: pointer;
}
.qa-title-link:hover {
  color: #ff3380;
  text-decoration: underline wavy #ffb6d5 2px;
}
.qa-table ::v-deep .el-table__row,
.qa-table >>> .el-table__row,
.qa-table /deep/ .el-table__row {
  background: #fff !important;
}
.qa-table ::v-deep .el-table__row:hover,
.qa-table >>> .el-table__row:hover,
.qa-table /deep/ .el-table__row:hover {
  background: #f5f7fa !important;
}

.page-header {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  padding: 20px;
  border-radius: 16px;
  box-shadow: 0 4px 16px #f0c1d6cc;
}

.header-content > * {
  margin-bottom: 15px;
}

.title-section h2 {
  margin: 0 0 8px 0;
  color: #ff5c8a;
  font-size: 28px;
  font-weight: bold;
}

.title-section p {
  margin: 0;
  color: #888;
  font-size: 14px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}
.ask-btn, .back-btn {
  background: #ffb6d5 !important;
  border-color: #ffb6d5 !important;
  color: #fff !important;
  font-weight: bold;
  border-radius: 12px;
  padding: 10px 20px;
  display: flex;
  align-items: center;
}
.ask-btn:hover, .back-btn:hover {
  background: #ff5c8a !important;
  border-color: #ff5c8a !important;
}
.ask-btn i, .back-btn i {
  margin-right: 5px;
}

.qa-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 16px #f0c1d6cc;
  border: none;
  overflow: hidden;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #888;
}

.empty-icon {
  font-size: 60px;
  color: #ffb6d5;
  margin-bottom: 20px;
  display: block;
}

.empty-state p {
  font-size: 16px;
  margin-bottom: 20px;
  color: #666;
}

.empty-state .el-button {
  background: #ffb6d5;
  border-color: #ffb6d5;
  color: #fff;
  font-weight: bold;
  border-radius: 12px;
  padding: 12px 24px;
}

.empty-state .el-button:hover {
  background: #ff5c8a;
  border-color: #ff5c8a;
}

.qa-table {
  border-radius: 12px;
  overflow: hidden;
}

.qa-table ::v-deep .el-table__header-wrapper {
  background: linear-gradient(90deg, #ffe4ec 0%, #ffd6e6 100%);
}

.qa-table ::v-deep .el-table__header th {
  background: transparent;
  color: #ff5c8a;
  font-weight: bold;
  border-bottom: 2px solid #ffb6d5;
}

.qa-table ::v-deep .el-table__body td {
  border-bottom: 1px solid #ffe4ec;
}

.answer-count {
  color: #ff5c8a;
  font-weight: bold;
  font-size: 14px;
}

.time-text {
  color: #888;
  font-size: 12px;
}

.edit-btn {
  background: #ffb6d5 !important;
  border-color: #ffb6d5 !important;
  color: #fff !important;
  font-weight: bold;
  border-radius: 8px;
  margin-right: 8px;
}

.edit-btn:hover {
  background: #ff5c8a !important;
  border-color: #ff5c8a !important;
}

.delete-btn {
  border-radius: 8px;
  font-weight: bold;
}

.pagination-container {
  margin-top: 20px;
  padding: 20px 0;
}

.pagination-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-radius: 12px;
  padding: 16px 24px;
  box-shadow: 0 2px 8px rgba(255, 182, 213, 0.1);
  border: 1px solid #ffe4ec;
}

.pagination-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.total-info {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.page-info {
  font-size: 12px;
  color: #999;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-numbers {
  display: flex;
  align-items: center;
  gap: 4px;
}

.page-numbers.mobile {
  gap: 2px;
}

.page-btn {
  min-width: 36px;
  height: 36px;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  background: #fff;
  color: #606266;
  font-weight: 500;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.page-btn:hover {
  border-color: #ffb6d5;
  color: #ff5c8a;
  background: #fff5f8;
}

.page-btn.active {
  background: linear-gradient(135deg, #ffb6d5, #ff5c8a);
  border-color: #ff5c8a;
  color: #fff;
  box-shadow: 0 2px 8px rgba(255, 182, 213, 0.3);
}

.page-btn:disabled {
  background: #f5f7fa;
  border-color: #e4e7ed;
  color: #c0c4cc;
  cursor: not-allowed;
}

.page-btn:disabled:hover {
  background: #f5f7fa;
  border-color: #e4e7ed;
  color: #c0c4cc;
}

.prev-btn, .next-btn {
  padding: 0 12px;
  min-width: 80px;
  font-size: 13px;
}

.prev-btn i, .next-btn i {
  margin: 0 4px;
}

.ask-dialog ::v-deep .el-dialog__header,
.edit-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(90deg, #ffe4ec 0%, #ffd6e6 100%);
  color: #ff5c8a;
  font-weight: bold;
}

.submit-btn {
  background: #ffb6d5 !important;
  border-color: #ffb6d5 !important;
  color: #fff !important;
  font-weight: bold;
  border-radius: 12px;
  padding: 10px 20px;
}

.submit-btn:hover {
  background: #ff5c8a !important;
  border-color: #ff5c8a !important;
}

.forum-list-row {
  display: flex;
  align-items: center;
  gap: 10px;
}
.forum-list-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 2px solid #ffb6d5;
  object-fit: cover;
  background: #fff;
}
.forum-list-username {
  color: #ff5c8a;
  font-weight: bold;
  font-size: 14px;
}
.topic-title {
  font-weight: bold;
  margin-left: 8px;
}

.jump-to-page {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: 16px;
}

.jump-text {
  font-size: 12px;
  color: #666;
  white-space: nowrap;
}

.jump-input {
  width: 60px;
}

.jump-input ::v-deep .el-input__inner {
  text-align: center;
  padding: 0 8px;
  font-size: 12px;
  height: 36px;
  line-height: 36px;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.jump-btn {
  background: #ffb6d5 !important;
  border-color: #ffb6d5 !important;
  color: #fff !important;
  font-weight: 500;
  border-radius: 8px;
  padding: 0 12px;
  font-size: 12px;
  height: 36px;
  line-height: 36px;
  min-width: 50px;
}

.jump-btn:hover {
  background: #ff5c8a !important;
  border-color: #ff5c8a !important;
}

@media (max-width: 768px) {
  .qa-page {
    padding: 10px;
  }

  .header-content {
    flex-direction: column;
    text-align: center;
    padding: 16px;
  }

  .header-content > * {
    margin-bottom: 15px;
  }

  .title-section h2 {
    font-size: 24px;
  }

  .header-actions {
    flex-direction: column;
    width: 100%;
    gap: 8px;
  }

  .ask-btn, .back-btn {
    width: 100%;
    justify-content: center;
  }

  .qa-table ::v-deep .el-table {
    font-size: 12px;
  }

  .qa-table ::v-deep .el-table th,
  .qa-table ::v-deep .el-table td {
    padding: 8px 4px;
  }

  .qa-title-cell {
    gap: 2px;
    padding: 4px 0;
    min-width: 180px;
  }
  
  .qa-avatar-img {
    width: 22px;
    height: 22px;
  }
  
  .user-name {
    font-size: 12px;
  }
  
  .qa-title-link {
    font-size: 15px;
    color: #ff5c8a;
  }
  
  .forum-list-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }
  
  .forum-list-avatar {
    width: 24px;
    height: 24px;
  }
  
  .forum-list-username {
    font-size: 12px;
  }
  
  .topic-title {
    font-size: 14px;
    margin-left: 0;
    margin-top: 5px;
  }
  
  /* 对话框响应式 */
  .ask-dialog ::v-deep .el-dialog,
  .edit-dialog ::v-deep .el-dialog {
    width: 95% !important;
    margin: 5vh auto !important;
  }
  
  .ask-dialog ::v-deep .el-dialog__body,
  .edit-dialog ::v-deep .el-dialog__body {
    padding: 16px;
  }
  
  .ask-dialog ::v-deep .el-form-item,
  .edit-dialog ::v-deep .el-form-item {
    margin-bottom: 16px;
  }
  
  .ask-dialog ::v-deep .el-form-item__label,
  .edit-dialog ::v-deep .el-form-item__label {
    line-height: 1.5;
    padding-bottom: 8px;
  }
  
  .ask-dialog ::v-deep .el-input,
  .edit-dialog ::v-deep .el-input {
    font-size: 14px;
  }
  
  .ask-dialog ::v-deep .el-textarea__inner,
  .edit-dialog ::v-deep .el-textarea__inner {
    font-size: 14px;
    min-height: 80px;
  }
  
  /* 分页响应式 */
  .pagination-wrapper {
    flex-direction: column;
    gap: 16px;
    padding: 12px 16px;
  }
  
  .pagination-info {
    text-align: center;
  }
  
  .pagination-controls {
    width: 100%;
    justify-content: center;
  }
  
  .page-btn {
    min-width: 32px;
    height: 32px;
    font-size: 12px;
  }
  
  .prev-btn, .next-btn {
    min-width: 70px;
    font-size: 12px;
    padding: 0 8px;
  }
}

@media (max-width: 480px) {
  .qa-page {
    padding: 8px;
  }
  
  .header-content {
    padding: 12px;
  }
  
  .title-section h2 {
    font-size: 20px;
  }
  
  .title-section p {
    font-size: 12px;
  }
  
  .qa-table ::v-deep .el-table {
    font-size: 11px;
  }
  
  .qa-table ::v-deep .el-table th,
  .qa-table ::v-deep .el-table td {
    padding: 6px 2px;
  }
  
  .qa-title-cell {
    min-width: 150px;
  }
  
  .qa-title-link {
    font-size: 13px;
  }
  
  .user-name {
    font-size: 11px;
  }
  
  .qa-avatar-img {
    width: 20px;
    height: 20px;
  }
  
  .answer-count {
    font-size: 12px;
  }
  
  .time-text {
    font-size: 11px;
  }
  
  .edit-btn, .delete-btn {
    padding: 4px 8px;
    font-size: 11px;
  }
  
  /* 对话框进一步优化 */
  .ask-dialog ::v-deep .el-dialog,
  .edit-dialog ::v-deep .el-dialog {
    width: 98% !important;
    margin: 2vh auto !important;
  }
  
  .ask-dialog ::v-deep .el-dialog__header,
  .edit-dialog ::v-deep .el-dialog__header {
    padding: 12px 16px;
  }
  
  .ask-dialog ::v-deep .el-dialog__title,
  .edit-dialog ::v-deep .el-dialog__title {
    font-size: 16px;
  }
  
  .ask-dialog ::v-deep .el-dialog__body,
  .edit-dialog ::v-deep .el-dialog__body {
    padding: 12px;
  }
  
  .ask-dialog ::v-deep .el-form-item__label,
  .edit-dialog ::v-deep .el-form-item__label {
    font-size: 13px;
    padding-bottom: 6px;
  }
  
  .ask-dialog ::v-deep .el-input__inner,
  .edit-dialog ::v-deep .el-input__inner {
    font-size: 13px;
    padding: 8px 12px;
  }
  
  .ask-dialog ::v-deep .el-textarea__inner,
  .edit-dialog ::v-deep .el-textarea__inner {
    font-size: 13px;
    padding: 8px 12px;
    min-height: 60px;
  }
  
  .ask-dialog ::v-deep .el-dialog__footer,
  .edit-dialog ::v-deep .el-dialog__footer {
    padding: 12px 16px;
  }
  
  .submit-btn {
    padding: 8px 16px;
    font-size: 13px;
  }
  
  /* 空状态响应式 */
  .empty-state {
    padding: 40px 16px;
  }
  
  .empty-icon {
    font-size: 40px;
    margin-bottom: 16px;
  }
  
  .empty-state p {
    font-size: 14px;
    margin-bottom: 16px;
  }
  
  .empty-state .el-button {
    padding: 10px 20px;
    font-size: 13px;
  }
}
</style>
