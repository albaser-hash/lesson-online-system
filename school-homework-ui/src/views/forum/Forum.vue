<template>
  <div class="forum-page">
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h2>论坛社区</h2>
          <p>交流与分享你的想法</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" icon="el-icon-edit" @click="handleNewTopic" class="new-topic-btn">
            发表新主题
          </el-button>
          <el-button @click="goHome" class="back-btn">
            <i class="el-icon-back"></i>
            返回主页
          </el-button>
        </div>
      </div>
    </div>

    <el-card class="forum-card">
      <el-table :data="topics" style="width: 100%; margin-top: 0;" class="forum-table" @row-click="viewDetail">
        <el-table-column label="主题" min-width="400">
          <template slot-scope="scope">
            <div class="forum-list-row">
              <img
                :src="getAvatarUrl(scope.row.userAvatar || scope.row.avatar)"
                alt="头像"
                class="forum-list-avatar"
                @error="onAvatarError"
              />
              <span class="forum-list-username">{{ scope.row.userName }}</span>
              <span class="topic-title">{{ scope.row.topicTitle }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="topicCategory" label="分类" width="150" align="center"/>
        <el-table-column prop="replyCount" label="回复" width="100" align="center"/>
        <el-table-column prop="createTime" label="发布时间" width="200" align="center" :formatter="formatDateTime"/>
        <el-table-column label="操作" width="180" align="center">
          <template slot-scope="scope">
            <el-button 
              v-if="scope.row.userId === currentUserId || userType === 'ADMIN'" 
              type="primary" 
              size="mini" 
              @click.stop="handleEditTopic(scope.row)"
              class="edit-btn"
            >
              编辑
            </el-button>
            <el-button 
              v-if="scope.row.userId === currentUserId || userType === 'ADMIN'" 
              type="danger" 
              size="mini" 
              @click.stop="handleDeleteTopic(scope.row)"
              class="delete-btn"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <div class="pagination-wrapper">
          <div class="pagination-info">
            <span class="total-info">共 {{ total }} 条</span>
            <span class="page-info">第 {{ listQuery.page }} / {{ Math.ceil(total / listQuery.pageSize) }} 页</span>
          </div>
          <div class="pagination-controls">
            <el-button 
              :disabled="listQuery.page <= 1" 
              @click="handlePageChange(listQuery.page - 1)"
              class="page-btn prev-btn"
              size="small"
            >
              <i class="el-icon-arrow-left"></i>
              上一页
            </el-button>
            
            <div class="page-numbers" v-if="!isMobile">
              <el-button 
                v-for="page in visiblePages" 
                :key="page"
                @click="handlePageChange(page)"
                :class="['page-btn', { active: page === listQuery.page }]"
                size="small"
              >
                {{ page }}
              </el-button>
            </div>
            
            <div class="page-numbers mobile" v-else>
              <el-button 
                v-for="page in mobileVisiblePages" 
                :key="page"
                @click="handlePageChange(page)"
                :class="['page-btn', { active: page === listQuery.page }]"
                size="small"
              >
                {{ page }}
              </el-button>
            </div>
            
            <el-button 
              :disabled="listQuery.page >= Math.ceil(total / listQuery.pageSize)" 
              @click="handlePageChange(listQuery.page + 1)"
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

    <el-dialog title="发表新主题" :visible.sync="dialogVisible" width="90%" class="forum-dialog">
      <el-form :model="newTopicForm" ref="topicForm" label-width="80px">
        <el-form-item label="标题" prop="topicTitle" required>
          <el-input v-model="newTopicForm.topicTitle" placeholder="请输入帖子标题"></el-input>
        </el-form-item>
        <el-form-item label="分类" prop="topicCategory" required>
          <el-select v-model="newTopicForm.topicCategory" placeholder="请选择分类" style="width: 100%;">
            <el-option 
              v-for="category in categories" 
              :key="category.categoryId" 
              :label="category.categoryName" 
              :value="category.categoryName"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="topicContent" required>
          <el-input type="textarea" :rows="8" v-model="newTopicForm.topicContent" placeholder="请输入帖子内容"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitNewTopic" class="submit-btn">发 布</el-button>
      </span>
    </el-dialog>

    <el-dialog title="编辑主题" :visible.sync="editDialogVisible" width="90%" class="forum-dialog">
      <el-form :model="editTopicForm" ref="editTopicForm" label-width="80px">
        <el-form-item label="标题" prop="topicTitle" required>
          <el-input v-model="editTopicForm.topicTitle" placeholder="请输入帖子标题"></el-input>
        </el-form-item>
        <el-form-item label="分类" prop="topicCategory" required>
          <el-select v-model="editTopicForm.topicCategory" placeholder="请选择分类" style="width: 100%;">
            <el-option 
              v-for="category in categories" 
              :key="category.categoryId" 
              :label="category.categoryName" 
              :value="category.categoryName"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="topicContent" required>
          <el-input type="textarea" :rows="8" v-model="editTopicForm.topicContent" placeholder="请输入帖子内容"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitEditTopic" class="submit-btn">保 存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { listForumTopics, publishTopic, deleteTopic, updateTopic } from '@/api/forum'
import { listCourseCategories } from '@/api/courseCategories'
import { mapState } from 'vuex'

export default {
  name: 'Forum',
  data() {
    return {
      topics: [],
      total: 0,
      categories: [],
      listQuery: {
        page: 1,
        pageSize: 10
      },
      dialogVisible: false,
      editDialogVisible: false,
      newTopicForm: {
        topicTitle: '',
        topicCategory: '',
        topicContent: ''
      },
      editTopicForm: {
        topicTitle: '',
        topicCategory: '',
        topicContent: ''
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
      const totalPages = Math.ceil(this.total / this.listQuery.pageSize)
      const currentPage = this.listQuery.page
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
      const totalPages = Math.ceil(this.total / this.listQuery.pageSize)
      const currentPage = this.listQuery.page
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
  created() {
    this.loadTopics()
    this.loadCategories()
    this.checkMobile()
  },
  mounted() {
    window.addEventListener('resize', this.checkMobile)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.checkMobile)
  },
  methods: {
    goHome() {
      this.$router.push('/')
    },
    loadTopics() {
      listForumTopics(this.listQuery).then(response => {
        this.topics = response.data.data.records
        this.total = Number(response.data.data.total)
      }).catch(error => {
        console.error('加载论坛帖子失败:', error)
        this.$message.error('加载帖子失败，请重试')
      })
    },
    loadCategories() {
      listCourseCategories({ page: 1, pageSize: 100 }).then(response => {
        this.categories = response.data.data.records || []
      }).catch(error => {
        console.error('加载分类失败:', error)
        this.$message.error('加载分类失败')
      })
    },
    formatDateTime(row, column, cellValue) {
      if (!cellValue) return ''
      const date = new Date(cellValue)
      const Y = date.getFullYear()
      const M = (date.getMonth() + 1).toString().padStart(2, '0')
      const D = date.getDate().toString().padStart(2, '0')
      const h = date.getHours().toString().padStart(2, '0')
      const m = date.getMinutes().toString().padStart(2, '0')
      return `${Y}-${M}-${D} ${h}:${m}`
    },
    handlePageChange(newPage) {
      this.listQuery.page = newPage
      this.loadTopics()
    },
    viewDetail(row) {
      this.$router.push({ name: 'ForumTopicDetail', params: { id: row.topicId } })
    },
    handleDeleteTopic(row) {
      this.$confirm('确定要删除这个帖子吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteTopic(row.topicId).then(() => {
          this.$message.success('删除成功')
          this.loadTopics()
        }).catch(() => {
          this.$message.error('删除失败，请重试')
        })
      }).catch(() => {
        // 用户取消删除
      })
    },
    handleEditTopic(row) {
      this.currentEditId = row.topicId
      this.editTopicForm = {
        topicTitle: row.topicTitle,
        topicCategory: row.topicCategory,
        topicContent: row.topicContent
      }
      this.editDialogVisible = true
    },
    submitEditTopic() {
      this.$refs.editTopicForm.validate(valid => {
        if (valid) {
          const topicData = {
            topicId: this.currentEditId,
            topicTitle: this.editTopicForm.topicTitle,
            topicCategory: this.editTopicForm.topicCategory,
            topicContent: this.editTopicForm.topicContent,
            userId: this.currentUserId
          }
          updateTopic(topicData).then(() => {
            this.$message.success('编辑成功')
            this.editDialogVisible = false
            this.currentEditId = null
            this.loadTopics()
          }).catch(() => {
            this.$message.error('编辑失败，请重试')
          })
        }
      })
    },
    handleNewTopic() {
      this.dialogVisible = true
      this.$nextTick(() => {
        if(this.$refs.topicForm) {
          this.$refs.topicForm.resetFields()
        }
      })
    },
    submitNewTopic() {
      this.$refs.topicForm.validate(valid => {
        if (valid) {
          const topicData = {
            ...this.newTopicForm,
            userId: this.currentUserId,
            userName: this.$store.state.user.name || this.$store.state.user.userName || '用户',
            userAvatar: this.$store.state.user.avatar || ''
          }
          publishTopic(topicData).then(() => {
            this.$message.success('发布成功')
            this.dialogVisible = false
            this.loadTopics()
          }).catch(() => {
            this.$message.error('发布失败，请重试')
          })
        }
      })
    },
    getAvatarUrl(avatar) {
      if (!avatar || (!/^https?:\/\//.test(avatar) && !avatar.startsWith('/'))) {
        return require('@/assets/images/profile.jpg')
      }
      return avatar
    },
    onAvatarError(e) {
      e.target.src = require('@/assets/images/profile.jpg')
    },
    checkMobile() {
      this.isMobile = window.innerWidth < 768
    },
    handleJumpPage() {
      const newPage = parseInt(this.jumpPage)
      if (newPage >= 1 && newPage <= Math.ceil(this.total / this.listQuery.pageSize)) {
        this.handlePageChange(newPage)
      } else {
        this.$message.error('请输入有效的页码')
      }
    }
  }
}
</script>

<style scoped>
.forum-page {
  padding: 20px;
  min-height: 100vh;
  background: linear-gradient(135deg, #ffe4ec 0%, #ffd6e6 100%);
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
}

.new-topic-btn {
  background: #ffb6d5 !important;
  border-color: #ffb6d5 !important;
  color: #fff !important;
  font-weight: bold;
  border-radius: 12px;
  padding: 10px 20px;
  display: flex;
  align-items: center;
  margin-right: 10px;
}

.new-topic-btn:hover {
  background: #ff5c8a !important;
  border-color: #ff5c8a !important;
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

.forum-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 16px #f0c1d6cc;
  border: none;
  overflow: hidden;
}

.forum-table {
  border-radius: 12px;
  overflow: hidden;
}

.forum-table ::v-deep .el-table__header-wrapper {
  background: linear-gradient(90deg, #ffe4ec 0%, #ffd6e6 100%);
}

.forum-table ::v-deep .el-table__header th {
  background: transparent;
  color: #ff5c8a;
  font-weight: bold;
  border-bottom: 2px solid #ffb6d5;
}

.forum-table ::v-deep .el-table__body tr:hover > td {
  background: #fff5f8;
}

.forum-table ::v-deep .el-table__body td {
  border-bottom: 1px solid #ffe4ec;
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

.forum-dialog ::v-deep .el-dialog__header {
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

.forum-dialog ::v-deep .el-select {
  width: 100%;
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
  .forum-page {
    padding: 10px;
  }
  .header-content {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }
  .title-section h2 {
    font-size: 24px;
  }
  .new-topic-btn, .back-btn {
    width: 100%;
    margin-bottom: 10px;
  }
  .forum-table ::v-deep .el-table {
    font-size: 12px;
  }
  .forum-table ::v-deep .el-table th,
  .forum-table ::v-deep .el-table td {
    padding: 8px 4px;
  }
  .forum-table ::v-deep .el-table .cell {
    padding: 8px 4px;
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
</style>
