<template>
  <div class="qa-detail-page">
    <div class="page-header">
      <div class="header-content">
        <el-button @click="goHome" class="back-btn">
          <i class="el-icon-back"></i>
          返回列表
        </el-button>
      </div>
    </div>
    <el-card class="question-card">
      <div slot="header" class="question-header">
        <span class="question-title">{{ question.questionTitle }}</span>
        <div v-if="question.userId === userId" class="question-actions">
          <el-button 
            type="primary" 
            size="mini" 
            @click="handleEditQuestion"
            class="edit-btn"
          >
            编辑
          </el-button>
          <el-button 
            type="danger" 
            size="mini" 
            @click="handleDeleteQuestion"
            class="delete-btn"
          >
            删除问题
          </el-button>
        </div>
      </div>
      <div class="question-meta">
        <div class="question-meta-row">
          <img :src="getAvatarUrl(question.avatar)" class="qa-avatar-img" @error="onAvatarError" />
          <span class="question-user">提问者：{{ question.userName }}</span>
          <span class="question-time">{{ formatTime(question.createTime) }}</span>
        </div>
      </div>
      <div class="question-tags">标签：{{ question.tags }}</div>
      <div class="question-content">{{ question.questionContent }}</div>
    </el-card>
    <el-card class="answer-card">
      <div slot="header" class="answer-header">全部回答</div>
      <el-timeline>
        <el-timeline-item v-for="answer in answers" :key="answer.answerId" :timestamp="formatTime(answer.createTime)" placement="top">
          <div class="answer-row" :class="{'best-answer-row': answer.isBest === 1}">
            <img :src="getAvatarUrl(answer.avatar)" class="qa-avatar-img" @error="onAvatarError" />
            <b class="answer-username">{{ answer.userName }}</b>：
            <el-tag v-if="answer.isBest === 1" type="success" size="mini">最佳</el-tag>
            <div v-if="answer.userId === userId || userType === 'ADMIN'" class="answer-actions">
              <el-button type="text" size="mini" @click="handleEditAnswer(answer)"><i class="el-icon-edit"></i> 编辑</el-button>
              <el-button type="text" size="mini" class="delete-text-btn" @click="deleteMyAnswer(answer.answerId)"><i class="el-icon-delete"></i> 删除</el-button>
            </div>
            <el-button v-if="question.userId === userId && answer.isBest !== 1" type="text" size="mini" @click="setBest(answer.answerId)">设为最佳</el-button>
          </div>
          <div v-if="answer.isEditing" class="answer-edit-box">
            <el-input type="textarea" v-model="answer.editContent" rows="3" />
            <div class="answer-edit-btns">
              <el-button size="mini" type="primary" @click="submitEditAnswer(answer)">保存</el-button>
              <el-button size="mini" @click="cancelEditAnswer(answer)">取消</el-button>
            </div>
          </div>
          <div v-else class="answer-content">{{ answer.answerContent }}</div>
        </el-timeline-item>
      </el-timeline>
      <div class="answer-input-box">
        <el-input type="textarea" v-model="newAnswer" placeholder="写下你的回答..." rows="3" />
        <el-button type="primary" class="submit-btn" @click="submitAnswer">提交回答</el-button>
      </div>
    </el-card>

    <el-dialog :visible.sync="editDialogVisible" title="编辑问题" width="500px" class="qa-dialog">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="editForm.questionTitle" maxlength="100" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input type="textarea" v-model="editForm.questionContent" maxlength="500" rows="4" />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="editForm.tags" placeholder="用逗号分隔，如：高数,极限" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="editDialogVisible=false">取消</el-button>
        <el-button type="primary" @click="submitEditQuestion" class="submit-btn">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getQuestionById, deleteQuestion, updateQuestion, setBestAnswer } from '@/api/questions'
import { addAnswer, deleteAnswer, updateAnswer } from '@/api/answer'
import { mapState } from 'vuex'
export default {
  name: 'QAQuestionDetail',
  data() {
    return {
      question: {},
      answers: [],
      newAnswer: '',
      editDialogVisible: false,
      editForm: {
        questionTitle: '',
        questionContent: '',
        tags: ''
      }
    }
  },
  computed: {
    ...mapState({
      userId: state => state.user.id,
      userName: state => state.user.userName,
      name: state => state.user.name,
      avatar: state => state.user.avatar,
      userType: state => state.user.userType
    })
  },
  mounted() {
    this.fetchQuestion()
  },
  methods: {
    fetchQuestion() {
      const id = this.$route.params.id
      getQuestionById(id).then(res => {
        if (res && res.data) {
          this.question = res.data.data
          this.answers = Array.isArray(res.data.data.answers) ? res.data.data.answers : []
        }
      }).catch(error => {
        this.$message.error('加载问题详情失败，请重试')
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
    async submitAnswer() {
      if (!this.newAnswer.trim()) {
        this.$message.error('回答内容不能为空')
        return
      }
      try {
        const payload = {
          questionId: this.question.questionId,
          answerContent: this.newAnswer,
          userId: this.userId,
          userName: this.name || this.userName,
          avatar: this.avatar
        }
        await addAnswer(payload)
        this.$message.success('回答成功')
        this.newAnswer = ''
        this.fetchQuestion() // 刷新回答列表
      } catch (e) {
        this.$message.error('提交失败')
      }
    },
    async deleteMyAnswer(answerId) {
      this.$confirm('确定要删除这条回答吗？', '提示', { type: 'warning' })
        .then(async () => {
          try {
            await deleteAnswer(answerId)
            this.$message.success('删除成功')
            this.fetchQuestion()
          } catch (e) {
            this.$message.error('删除失败')
          }
        })
        .catch(() => {})
    },
    handleEditQuestion() {
      this.editForm.questionTitle = this.question.questionTitle
      this.editForm.questionContent = this.question.questionContent
      this.editForm.tags = this.question.tags
      this.editDialogVisible = true
    },
    submitEditQuestion() {
      if (!this.editForm.questionTitle.trim() || !this.editForm.questionContent.trim()) {
        this.$message.error('请填写标题和内容')
        return
      }
      updateQuestion({
        questionId: this.question.questionId,
        questionTitle: this.editForm.questionTitle,
        questionContent: this.editForm.questionContent,
        tags: this.editForm.tags,
        userId: this.userId
      }).then(() => {
        this.$message.success('编辑成功')
        this.editDialogVisible = false
        this.fetchQuestion()
      }).catch(() => {
        this.$message.error('编辑失败，请重试')
      })
    },
    handleEditAnswer(answer) {
      this.$set(answer, 'isEditing', true)
      this.$set(answer, 'editContent', answer.answerContent)
    },
    submitEditAnswer(answer) {
      if (!answer.editContent.trim()) {
        this.$message.error('回答内容不能为空')
        return
      }
      updateAnswer({
        answerId: answer.answerId,
        answerContent: answer.editContent,
        userId: this.userId
      }).then(() => {
        this.$message.success('编辑成功')
        this.$set(answer, 'answerContent', answer.editContent)
        this.$set(answer, 'isEditing', false)
      }).catch(() => {
        this.$message.error('编辑失败，请重试')
      })
    },
    cancelEditAnswer(answer) {
      this.$set(answer, 'isEditing', false)
      this.$set(answer, 'editContent', '')
    },
    handleDeleteQuestion() {
      this.$confirm('确定要删除这个问题吗？删除后所有回答也会被删除。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteQuestion(this.question.questionId).then(() => {
          this.$message.success('删除成功')
          this.$router.push('/qa')
        }).catch(() => {
          this.$message.error('删除失败，请重试')
        })
      }).catch(() => {
        // 用户取消删除
      })
    },
    goHome() {
      this.$router.push('/qa')
    },
    async setBest(answerId) {
      try {
        await this.$confirm('确定要将这条回答设为最佳回答吗？', '提示', { type: 'warning' })
        await setBestAnswer(this.question.questionId, answerId)
        this.$message.success('设置成功')
        this.fetchQuestion()
      } catch (e) {
        if (e !== 'cancel') {
          this.$message.error('设置失败')
        }
      }
    }
  }
}
</script>

<style scoped>
.qa-detail-page {
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
.question-card {
  margin-bottom: 20px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 16px #f0c1d6cc;
  border: none;
  overflow: hidden;
}
.question-header {
  font-size: 20px;
  font-weight: bold;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.question-title {
  color: #ff5c8a;
  font-size: 22px;
  font-weight: bold;
}
.question-actions {
  display: flex;
  gap: 8px;
}
.question-meta-row {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #888;
  margin-bottom: 8px;
}
.question-user {
  color: #ff5c8a;
  font-weight: bold;
}
.question-time {
  color: #888;
  font-size: 14px;
}
.question-tags {
  margin-bottom: 8px;
  color: #666;
  font-size: 14px;
}
.question-content {
  font-size: 16px;
  line-height: 1.7;
  color: #333;
}
.answer-card {
  margin-top: 30px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 16px #f0c1d6cc;
  border: none;
  overflow: hidden;
}
.answer-header {
  font-size: 18px;
  font-weight: bold;
  color: #ff5c8a;
}
.answer-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}
.answer-username {
  color: #ff5c8a;
  font-weight: bold;
}
.answer-actions {
  margin-left: 8px;
}
.delete-text-btn {
  color: #f56c6c !important;
  margin-left: 8px;
}
.answer-edit-box {
  margin-left: 52px;
  margin-top: 8px;
}
.answer-edit-btns {
  margin-top: 8px;
}
.answer-content {
  margin-left: 52px;
  font-size: 15px;
  line-height: 1.6;
  color: #333;
}
.answer-input-box {
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
.qa-dialog ::v-deep .el-dialog__header {
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
.delete-btn {
  border-radius: 8px;
  font-weight: bold;
}
.qa-avatar-img {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  background: #fff;
  border: 2px solid #ffb6d5;
}
.best-answer-row {
  background: #f6ffed;
  border-left: 4px solid #52c41a;
}
@media (max-width: 768px) {
  .qa-detail-page {
    padding: 8px;
  }
  
  .header-content {
    flex-direction: column;
    text-align: center;
    padding: 16px;
  }
  
  .back-btn {
    width: 100%;
    margin-bottom: 10px;
    justify-content: center;
  }
  
  .question-card, .answer-card {
    border-radius: 10px;
    padding: 8px;
  }
  
  .question-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .question-title {
    font-size: 18px;
    line-height: 1.4;
  }
  
  .question-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .answer-header {
    font-size: 16px;
  }
  
  .question-meta-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .answer-row {
    display: flex;
    flex-direction: row;
    align-items: center;
    gap: 6px;
    background: #fff7fa;
    border-radius: 10px;
    padding: 10px 8px;
    margin-bottom: 12px;
    box-shadow: 0 2px 8px #ffd6e633;
    width: 100%;
    flex-wrap: wrap;
  }
  
  .answer-username {
    font-size: 15px;
    margin-top: 0;
    white-space: nowrap;
  }
  
  .answer-content {
    font-size: 15px;
    line-height: 1.7;
    color: #333;
    word-break: break-all;
    margin: 0;
    width: auto;
    flex: 1 1 100%;
  }
  
  .answer-actions {
    margin: 0;
    width: auto;
    display: flex;
    gap: 8px;
    justify-content: flex-end;
  }
  
  .qa-avatar-img {
    width: 28px;
    height: 28px;
    margin-bottom: 2px;
  }
  
  .el-timeline-item__timestamp {
    font-size: 12px !important;
    color: #bbb !important;
    margin-bottom: 2px;
  }
  
  /* 对话框响应式 */
  .qa-dialog ::v-deep .el-dialog {
    width: 95% !important;
    margin: 5vh auto !important;
  }
  
  .qa-dialog ::v-deep .el-dialog__body {
    padding: 16px;
  }
  
  .qa-dialog ::v-deep .el-form-item {
    margin-bottom: 16px;
  }
  
  .qa-dialog ::v-deep .el-form-item__label {
    line-height: 1.5;
    padding-bottom: 8px;
  }
  
  .qa-dialog ::v-deep .el-input,
  .qa-dialog ::v-deep .el-textarea {
    font-size: 14px;
  }
  
  .qa-dialog ::v-deep .el-textarea__inner {
    min-height: 80px;
  }
  
  /* 回答输入框响应式 */
  .answer-input-box {
    margin-top: 16px;
  }
  
  .answer-input-box ::v-deep .el-textarea__inner {
    font-size: 14px;
    min-height: 80px;
  }
  
  .submit-btn {
    padding: 8px 16px;
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  .qa-detail-page {
    padding: 6px;
  }
  
  .header-content {
    padding: 12px;
  }
  
  .question-card, .answer-card {
    padding: 6px;
  }
  
  .question-title {
    font-size: 16px;
  }
  
  .question-content {
    font-size: 14px;
    line-height: 1.6;
  }
  
  .question-tags {
    font-size: 12px;
  }
  
  .question-time {
    font-size: 12px;
  }
  
  .answer-header {
    font-size: 14px;
  }
  
  .answer-content {
    font-size: 13px;
    line-height: 1.6;
  }
  
  .answer-username {
    font-size: 13px;
  }
  
  .qa-avatar-img {
    width: 24px;
    height: 24px;
  }
  
  .answer-row {
    padding: 8px 6px;
    gap: 4px;
  }
  
  .answer-actions {
    gap: 4px;
  }
  
  .edit-btn, .delete-btn {
    padding: 4px 8px;
    font-size: 11px;
  }
  
  /* 对话框进一步优化 */
  .qa-dialog ::v-deep .el-dialog {
    width: 98% !important;
    margin: 2vh auto !important;
  }
  
  .qa-dialog ::v-deep .el-dialog__header {
    padding: 12px 16px;
  }
  
  .qa-dialog ::v-deep .el-dialog__title {
    font-size: 16px;
  }
  
  .qa-dialog ::v-deep .el-dialog__body {
    padding: 12px;
  }
  
  .qa-dialog ::v-deep .el-form-item__label {
    font-size: 13px;
    padding-bottom: 6px;
  }
  
  .qa-dialog ::v-deep .el-input__inner,
  .qa-dialog ::v-deep .el-textarea__inner {
    font-size: 13px;
    padding: 8px 12px;
  }
  
  .qa-dialog ::v-deep .el-textarea__inner {
    min-height: 60px;
  }
  
  .qa-dialog ::v-deep .el-dialog__footer {
    padding: 12px 16px;
  }
  
  /* 回答输入框进一步优化 */
  .answer-input-box ::v-deep .el-textarea__inner {
    font-size: 13px;
    padding: 8px 12px;
    min-height: 60px;
  }
  
  .submit-btn {
    padding: 6px 12px;
    font-size: 12px;
    margin-left: 8px;
  }
  
  .back-btn {
    padding: 8px 16px;
    font-size: 13px;
  }
}
</style> 