<template>
  <div class="exam-list">
    <div class="header">
      <div class="header-content">
        <div class="title-section">
          <h2>我的考试</h2>
          <p>查看和管理您的考试安排</p>
        </div>
        <div class="header-actions">
          <el-button @click="goHome" class="back-btn">
            <i class="el-icon-back"></i>
            回到主页
          </el-button>
        </div>
      </div>
    </div>

    <!-- 考试列表 -->
    <div class="exam-cards">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <i class="el-icon-loading" style="font-size: 32px; color: #ff5c8a;"></i>
        <p>正在加载考试列表...</p>
      </div>

      <!-- 空状态 -->
      <div v-else-if="examList.length === 0" class="empty-container">
        <i class="el-icon-document"></i>
        <p>暂无考试</p>
        <p class="empty-desc">您还没有可参加的考试</p>
      </div>

      <!-- 考试列表 -->
      <el-row v-else :gutter="20">
        <el-col :xs="24" :sm="12" :md="8" v-for="exam in examList" :key="exam.examId">
          <el-card class="exam-card" :class="{ 'exam-finished': exam.status === 'FINISHED' }">
            <div class="exam-header">
              <h3>{{ exam.examName }}</h3>
              <el-tag :type="getStatusType(exam.status)">
                {{ getStatusText(exam.status) }}
              </el-tag>
            </div>

            <div class="exam-info">
              <p><i class="el-icon-reading"></i> {{ exam.courseName }}</p>
              <p><i class="el-icon-time"></i> {{ formatDateTime(exam.startTime) }} - {{ formatDateTime(exam.endTime) }}</p>
              <p><i class="el-icon-document"></i> {{ exam.examCount }} 道题目</p>
              <p v-if="exam.score !== null"><i class="el-icon-trophy"></i> 得分：{{ exam.score }}</p>
            </div>

            <div class="exam-actions">
              <el-button
                v-if="exam.status === 'ONGOING' && !exam.hasSubmitted"
                type="primary"
                @click="startExam(exam)"
              >
                开始考试
              </el-button>
              <el-button
                v-else-if="exam.status === 'FINISHED' || exam.hasSubmitted"
                type="success"
                @click="viewResult(exam)"
              >
                查看结果
              </el-button>
              <el-button
                v-else-if="exam.status === 'UPCOMING'"
                type="info"
                disabled
              >
                等待开始
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 考试页面 -->
    <el-dialog
      :visible.sync="showExamDialog"
      title="在线考试"
      width="90%"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
    >
      <div v-if="currentExam" class="exam-container">
        <div class="exam-header-bar">
          <div class="exam-title">{{ currentExam.examName }}</div>
          <div class="exam-timer">
            <i class="el-icon-time"></i>
            剩余时间：{{ formatTime(remainingTime) }}
          </div>
        </div>

        <div class="exam-content">
          <div class="question-navigation">
            <div class="nav-title">题目导航</div>
            <div class="nav-buttons">
              <el-button
                v-for="(question, index) in currentExam.questions"
                :key="index"
                :type="getQuestionButtonType(index)"
                size="mini"
                @click="goToQuestion(index)"
              >
                {{ index + 1 }}
              </el-button>
            </div>
          </div>

          <div class="question-content">
            <div v-if="currentQuestion" class="question">
              <div class="question-header">
                <span class="question-number">第 {{ currentQuestionIndex + 1 }} 题</span>
                <span class="question-type">[{{ getQuestionTypeText(currentQuestion.questionType) }}]</span>
                <span class="question-score">({{ currentQuestion.score }}分)</span>
              </div>

              <div class="question-text">
                {{ currentQuestion.questionContent }}
              </div>

              <!-- 简答题 -->
              <div v-if="currentQuestion.questionType === 'TEXT'" class="text-answer">
                <el-input
                  v-model="answers[currentQuestionIndex]"
                  type="textarea"
                  :rows="6"
                  placeholder="请输入您的答案"
                />
              </div>

              <!-- 单选题 -->
              <div v-else-if="currentQuestion.questionType === 'SINGLE_CHOICE' && parseOptions(currentQuestion.options).length > 0" class="options">
                <el-radio-group v-model="answers[currentQuestionIndex]">
                  <el-radio
                    v-for="option in parseOptions(currentQuestion.options)"
                    :key="option.key"
                    :label="option.key"
                    class="option-item"
                  >
                    {{ option.key }}. {{ option.text }}
                  </el-radio>
                </el-radio-group>
              </div>

              <!-- 多选题 -->
              <div v-else-if="currentQuestion.questionType === 'MULTIPLE_CHOICE' && parseOptions(currentQuestion.options).length > 0" class="options">
                <el-checkbox-group v-model="answers[currentQuestionIndex]">
                  <el-checkbox
                    v-for="option in parseOptions(currentQuestion.options)"
                    :key="option.key"
                    :label="option.key"
                    class="option-item"
                  >
                    {{ option.key }}. {{ option.text }}
                  </el-checkbox>
                </el-checkbox-group>
              </div>

              <!-- 判断题 -->
              <div v-else-if="currentQuestion.questionType === 'JUDGE'" class="options">
                <el-radio-group v-model="answers[currentQuestionIndex]">
                  <el-radio label="true" class="option-item">正确</el-radio>
                  <el-radio label="false" class="option-item">错误</el-radio>
                </el-radio-group>
              </div>

              <!-- 没有选项的单选题或多选题 -->
              <div v-else-if="(currentQuestion.questionType === 'SINGLE_CHOICE' || currentQuestion.questionType === 'MULTIPLE_CHOICE') && parseOptions(currentQuestion.options).length === 0" class="no-options">
                <p class="no-options-text">该题目暂无选项，请直接输入答案：</p>
                <el-input
                  v-model="answers[currentQuestionIndex]"
                  type="textarea"
                  :rows="4"
                  placeholder="请输入您的答案"
                />
              </div>
            </div>
          </div>
        </div>

        <div class="exam-footer">
          <el-button @click="previousQuestion" :disabled="currentQuestionIndex === 0">
            上一题
          </el-button>
          <el-button
            type="primary"
            @click="nextQuestion"
            :disabled="currentQuestionIndex === currentExam.questions.length - 1"
          >
            下一题
          </el-button>
          <el-button type="success" @click="submitExam">
            提交考试
          </el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 考试结果对话框 -->
    <el-dialog
      :visible.sync="showResultDialog"
      title="考试结果"
      :width="resultDialogWidth"
    >
      <div v-if="examResult" class="exam-result">
        <div class="result-summary">
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="result-item">
                <div class="result-number">
                  <template v-if="examResult.isReviewed && examResult.finalScore != null">
                    {{ examResult.finalScore }}
                  </template>
                  <template v-else>
                    <span style="color:#f56c6c">成绩待批改</span>
                  </template>
                </div>
                <div class="result-label">总分</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="result-item">
                <div class="result-number">{{ examResult.correctCount }}</div>
                <div class="result-label">正确题数</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="result-item">
                <div class="result-number">{{ examResult.accuracy }}%</div>
                <div class="result-label">正确率</div>
              </div>
            </el-col>
          </el-row>
        </div>

        <div class="result-details">
          <h3>详细结果</h3>
          <div v-for="(question, index) in examResult.questions" :key="index" class="question-result">
            <div class="question-header">
              <span class="question-number">{{ index + 1 }}.</span>
              <span class="question-type">[{{ getQuestionTypeText(question.questionType) }}]</span>
              <span class="question-content">{{ question.questionContent }}</span>
              <span class="question-score">
                {{ question.obtainedScore }}/{{ question.score }}分
              </span>
            </div>

            <div v-if="question.options" class="question-options">
              <div v-for="option in parseOptions(question.options)" :key="option.key" class="option">
                {{ option.key }}. {{ option.text }}
              </div>
            </div>

            <div class="answer-info">
              <div class="your-answer">
                <strong>您的答案：</strong>
                <span :class="{ 'correct': question.isCorrect, 'incorrect': !question.isCorrect }">
                  {{ question.userAnswer || '未作答' }}
                </span>
              </div>
              <div class="correct-answer">
                <strong>正确答案：</strong>{{ question.correctAnswer }}
              </div>
              <div v-if="question.explanation" class="explanation">
                <strong>解析：</strong>{{ question.explanation }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import {
  getStudentExams,
  getStudentExamDetail,
  submitExamAnswers,
  getExamResult
} from '@/api/exam'
import webSocketService from '@/utils/websocket'

export default {
  name: 'StudentExamList',
  data() {
    return {
      examList: [],
      showExamDialog: false,
      showResultDialog: false,
      currentExam: null,
      currentQuestionIndex: 0,
      answers: {},
      remainingTime: 0,
      timer: null,
      examResult: null,
      loading: true
    }
  },

  computed: {
    ...mapState({
      userType: state => state.user.userType,
      userId: state => state.user.id
    }),
    currentQuestion() {
      if (!this.currentExam || !this.currentExam.questions) return null
      return this.currentExam.questions[this.currentQuestionIndex]
    },
    resultDialogWidth() {
      return window.innerWidth < 900 ? '98vw' : '800px'
    }
  },

  mounted() {
    this.checkStudentPermission()
    this.loadExamList()
  },

  beforeDestroy() {
    if (this.timer) {
      clearInterval(this.timer)
    }
    // 组件销毁时断开WebSocket连接
    webSocketService.disconnect()
  },

  watch: {
    userId(newVal) {
      if (newVal) {
        this.initWebSocket()
      }
    },
    // 监听题目切换，确保移除焦点
    currentQuestionIndex() {
      this.$nextTick(() => {
        this.blurActiveElement()
      })
    }
  },

  methods: {
    // 切换题目时移除焦点，避免 aria-hidden 警告
    blurActiveElement() {
      if (document.activeElement && typeof document.activeElement.blur === 'function') {
        document.activeElement.blur()
      }
    },
    checkStudentPermission() {
      if (!this.userType) {
        setTimeout(() => {
          this.checkStudentPermission()
        }, 100)
        return
      }
      if (this.userType !== 'STUDENT') {
        this.$message.error('只有学生可以访问考试页面')
        this.$router.push('/')
      }
    },

    async loadExamList() {
      try {
        const res = await getStudentExams()
        const data = res.data // 统一解包
        if (data.code === 200) {
          this.examList = data.data || []
        } else {
          this.examList = []
          this.$message.error(data.msg || '获取考试列表失败')
        }
      } catch (error) {
        this.$message.error('获取考试列表失败')
      } finally {
        this.loading = false
      }
    },

    getStatusType(status) {
      switch (status) {
        case 'UPCOMING':
          return 'info'
        case 'ONGOING':
          return 'warning'
        case 'FINISHED':
          return 'success'
        default:
          return 'info'
      }
    },

    getStatusText(status) {
      switch (status) {
        case 'UPCOMING':
          return '未开始'
        case 'ONGOING':
          return '进行中'
        case 'FINISHED':
          return '已结束'
        default:
          return '未知'
      }
    },

    formatDateTime(time) {
      if (!time) return ''
      return new Date(time).toLocaleString()
    },

    async startExam(exam) {
      try {
        const res = await getStudentExamDetail(exam.examId)
        const data = res.data
        if (data.code === 200) {
          this.currentExam = data.data
        } else {
          this.$message.error(data.msg || '获取考试详情失败')
          return
        }
        this.currentQuestionIndex = 0
        // 开始考试时也移除焦点
        this.$nextTick(() => {
          this.blurActiveElement()
        })
        // 初始化 answers，保证每道题的答案格式正确
        this.answers = this.currentExam.questions.map(q => {
          if (q.questionType === 'MULTIPLE_CHOICE') return []
          return ''
        })
        // 计算剩余时间
        const now = new Date().getTime()
        const endTime = new Date(this.currentExam.endTime).getTime()
        this.remainingTime = Math.max(0, Math.floor((endTime - now) / 1000))
        this.showExamDialog = true
        this.startTimer()
      } catch (error) {
        this.$message.error('获取考试详情失败')
      }
    },

    startTimer() {
      this.timer = setInterval(() => {
        this.remainingTime--
        if (this.remainingTime <= 0) {
          this.submitExam()
        }
      }, 1000)
    },

    stopTimer() {
      if (this.timer) {
        clearInterval(this.timer)
        this.timer = null
      }
    },

    formatTime(seconds) {
      const hours = Math.floor(seconds / 3600)
      const minutes = Math.floor((seconds % 3600) / 60)
      const secs = seconds % 60
      return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
    },

    getQuestionTypeText(type) {
      switch (type) {
        case 'SINGLE_CHOICE':
          return '单选题'
        case 'MULTIPLE_CHOICE':
          return '多选题'
        case 'JUDGE':
          return '判断题'
        case 'TEXT':
          return '简答题'
        default:
          return '未知'
      }
    },

    parseOptions(options) {
      if (!options || typeof options !== 'string' || options.trim() === '') return []
      
      return options.split('\n')
        .map(option => option.trim())
        .filter(option => option.length > 0)
        .map(option => {
          // 匹配 "A:list" 格式
          const match = option.match(/^([A-Z]):\s*(.+)$/)
          if (match) {
            return { key: match[1], text: match[2].trim() }
          }
          // 匹配 "A. list" 格式
          const match2 = option.match(/^([A-Z])\.\s*(.+)$/)
          if (match2) {
            return { key: match2[1], text: match2[2].trim() }
          }
          // 如果没有匹配到标准格式，返回原选项
          return { key: option, text: option }
        })
        .filter(option => option.text && option.text.trim() !== '')
    },

    getQuestionButtonType(index) {
      const question = this.currentExam.questions[index]
      const answer = this.answers[index]
      
      // 检查是否已答题
      let hasAnswer = false
      
      if (question.questionType === 'MULTIPLE_CHOICE') {
        // 多选题：检查数组是否有内容
        hasAnswer = Array.isArray(answer) && answer.length > 0
      } else if (question.questionType === 'TEXT') {
        // 简答题：检查字符串是否有内容
        hasAnswer = typeof answer === 'string' && answer.trim() !== ''
      } else {
        // 单选题、判断题：检查是否有值
        hasAnswer = answer !== undefined && answer !== null && answer !== ''
      }
      
      if (hasAnswer) {
        return 'success'
      }
      return index === this.currentQuestionIndex ? 'primary' : 'default'
    },

    goToQuestion(index) {
      this.blurActiveElement()
      this.currentQuestionIndex = index
      // 确保DOM更新后再移除焦点
      this.$nextTick(() => {
        this.blurActiveElement()
      })
    },

    previousQuestion() {
      this.blurActiveElement()
      if (this.currentQuestionIndex > 0) {
        this.currentQuestionIndex--
      }
      // 确保DOM更新后再移除焦点
      this.$nextTick(() => {
        this.blurActiveElement()
      })
    },

    nextQuestion() {
      this.blurActiveElement()
      if (this.currentQuestionIndex < this.currentExam.questions.length - 1) {
        this.currentQuestionIndex++
      }
      // 确保DOM更新后再移除焦点
      this.$nextTick(() => {
        this.blurActiveElement()
      })
    },

    async submitExam() {
      try {
        await this.$confirm('确定要提交考试吗？提交后将无法修改答案。', '确认提交', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        // 转换 answers 为 Map<Integer, String>，多选题用英文逗号拼接
        const answersMap = {}
        this.currentExam.questions.forEach((q, idx) => {
          if (q.questionType === 'MULTIPLE_CHOICE') {
            answersMap[q.questionId] = (this.answers[idx] || []).join(',')
          } else {
            answersMap[q.questionId] = this.answers[idx] || ''
          }
        })
        const data = {
          examId: this.currentExam.examId,
          answers: answersMap
        }
        const res = await submitExamAnswers(data)
        const resp = res.data
        if (resp.code === 200) {
          this.$message.success('考试提交成功')
          this.stopTimer()
          this.showExamDialog = false
          this.loadExamList()
        } else {
          this.$message.error(resp.msg || '提交考试失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('提交考试失败')
        }
      }
    },

    async viewResult(exam) {
      try {
        const res = await getExamResult(exam.examId)
        const data = res.data
        if (data.code === 200) {
          this.examResult = data.data
          this.showResultDialog = true
        } else if (data.code === 0 && data.msg && data.msg.includes('未找到考试结果')) {
          this.$message.info('您未参加本场考试，无成绩可查')
        } else {
          this.$message.error(data.msg || '获取考试结果失败')
        }
      } catch (error) {
        this.$message.error('获取考试结果失败')
      }
    },

    goHome() {
      this.$router.push('/')
    },

    initWebSocket() {
      webSocketService.connect(this.userId)
    }
  }
}
</script>

<style scoped>
.exam-list {
  padding: 20px;
  min-height: 100vh;
  background: linear-gradient(135deg, #ffe4ec 0%, #ffd6e6 100%);
  max-width: 1200px;
  margin: 0 auto;
}

.header {
  margin-bottom: 20px;
  background: #fff;
  padding: 20px;
  border-radius: 16px;
  box-shadow: 0 4px 16px #f0c1d6cc;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-section {
  flex: 1;
}

.header h2 {
  margin: 0 0 8px 0;
  color: #ff5c8a;
  font-size: 28px;
  font-weight: bold;
}

.header p {
  margin: 0;
  color: #606266;
}

.header-actions {
  display: flex;
  align-items: center;
}

.back-btn {
  background: #ffb6d5;
  border-color: #ffb6d5;
  color: #fff;
  font-weight: bold;
  border-radius: 12px;
  padding: 10px 20px;
}

.back-btn:hover {
  background: #ff5c8a;
  border-color: #ff5c8a;
}

.exam-cards {
  margin-bottom: 20px;
}

.exam-card {
  margin-bottom: 20px;
  transition: all 0.3s;
  border-radius: 16px;
  box-shadow: 0 4px 16px #f0c1d6cc;
  border: none;
  overflow: hidden;
}

.exam-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 24px #ffb6d5cc;
}

.exam-finished {
  opacity: 0.8;
}

.exam-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.exam-header h3 {
  margin: 0;
  color: #ff5c8a;
  font-weight: bold;
}

.exam-info p {
  margin: 8px 0;
  color: #606266;
}

.exam-info i {
  margin-right: 8px;
  color: #ff5c8a;
}

.exam-actions {
  margin-top: 15px;
  text-align: center;
}

.exam-actions ::v-deep .el-button--primary {
  background: #ffb6d5 !important;
  border-color: #ffb6d5 !important;
  color: #fff !important;
  font-weight: bold;
  border-radius: 12px;
}

.exam-actions ::v-deep .el-button--primary:hover {
  background: #ff5c8a !important;
  border-color: #ff5c8a !important;
}

.exam-actions ::v-deep .el-button--success {
  background: #67c23a !important;
  border-color: #67c23a !important;
  color: #fff !important;
  font-weight: bold;
  border-radius: 12px;
}

.exam-actions ::v-deep .el-button--success:hover {
  background: #85ce61 !important;
  border-color: #85ce61 !important;
}

.exam-actions ::v-deep .el-button--info {
  border-radius: 12px;
  font-weight: bold;
}

.exam-container {
  height: 70vh;
  display: flex;
  flex-direction: column;
}

.exam-header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background: linear-gradient(90deg, #ffe4ec 0%, #ffd6e6 100%);
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px #f0c1d6cc;
}

.exam-title {
  font-size: 18px;
  font-weight: bold;
  color: #ff5c8a;
}

.exam-timer {
  font-size: 16px;
  color: #e6a23c;
  font-weight: bold;
}

.exam-content {
  flex: 1;
  display: flex;
  gap: 20px;
}

.question-navigation {
  width: 200px;
  background: #fff;
  padding: 15px;
  border-radius: 12px;
  box-shadow: 0 2px 8px #f0c1d6cc;
}

.nav-title {
  font-weight: bold;
  margin-bottom: 15px;
  color: #ff5c8a;
}

.nav-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.nav-buttons ::v-deep .el-button--mini {
  border-radius: 8px;
  font-weight: bold;
}

.nav-buttons ::v-deep .el-button--primary {
  background: #ffb6d5 !important;
  border-color: #ffb6d5 !important;
  color: #fff !important;
}

.nav-buttons ::v-deep .el-button--primary:hover {
  background: #ff5c8a !important;
  border-color: #ff5c8a !important;
}

.nav-buttons ::v-deep .el-button--success {
  background: #67c23a !important;
  border-color: #67c23a !important;
  color: #fff !important;
}

.nav-buttons ::v-deep .el-button--success:hover {
  background: #85ce61 !important;
  border-color: #85ce61 !important;
}

.question-content {
  flex: 1;
  background: white;
  padding: 20px;
  border-radius: 12px;
  border: 1px solid #ffe4ec;
  box-shadow: 0 2px 8px #f0c1d6cc;
}

.question-header {
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ffe4ec;
}

.question-number {
  font-size: 18px;
  font-weight: bold;
  margin-right: 10px;
  color: #ff5c8a;
}

.question-type {
  color: #ff5c8a;
  margin-right: 10px;
}

.question-score {
  color: #f56c6c;
  margin-left: 10px;
}

.question-text {
  font-size: 16px;
  line-height: 1.6;
  margin-bottom: 20px;
  color: #303133;
}

.options {
  margin-bottom: 20px;
}

.option-item {
  display: block;
  margin: 10px 0;
  padding: 10px;
  border: 1px solid #ffe4ec;
  border-radius: 8px;
  transition: all 0.3s;
  background: #fff;
}

.option-item:hover {
  background: #fff5f8;
  border-color: #ffb6d5;
}

.text-answer {
  margin-bottom: 20px;
}

.text-answer ::v-deep .el-textarea__inner {
  border-color: #ffe4ec;
  border-radius: 8px;
}

.text-answer ::v-deep .el-textarea__inner:focus {
  border-color: #ffb6d5;
}

.no-options {
  margin-bottom: 20px;
}

.no-options-text {
  color: #f56c6c;
  font-weight: bold;
  margin-bottom: 10px;
}

.no-options ::v-deep .el-textarea__inner {
  border-color: #ffe4ec;
  border-radius: 8px;
}

.no-options ::v-deep .el-textarea__inner:focus {
  border-color: #ffb6d5;
}

.exam-footer {
  display: flex;
  justify-content: space-between;
  padding: 15px 0;
  border-top: 1px solid #ffe4ec;
  margin-top: 20px;
}

.exam-footer ::v-deep .el-button--primary {
  background: #ffb6d5 !important;
  border-color: #ffb6d5 !important;
  color: #fff !important;
  font-weight: bold;
  border-radius: 12px;
}

.exam-footer ::v-deep .el-button--primary:hover {
  background: #ff5c8a !important;
  border-color: #ff5c8a !important;
}

.exam-footer ::v-deep .el-button--success {
  background: #67c23a !important;
  border-color: #67c23a !important;
  color: #fff !important;
  font-weight: bold;
  border-radius: 12px;
}

.exam-footer ::v-deep .el-button--success:hover {
  background: #85ce61 !important;
  border-color: #85ce61 !important;
}

.exam-result {
  text-align: center;
}

.result-summary {
  margin-bottom: 30px;
}

.result-item {
  text-align: center;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 12px;
  box-shadow: 0 2px 8px #f0c1d6cc;
}

.result-number {
  font-size: 24px;
  font-weight: bold;
  color: #ff5c8a;
  margin-bottom: 5px;
}

.result-label {
  color: #606266;
}

.result-details {
  text-align: left;
}

.result-details h3 {
  color: #ff5c8a;
  font-weight: bold;
  margin-bottom: 20px;
}

.question-result {
  border: 1px solid #ffe4ec;
  border-radius: 12px;
  padding: 15px;
  margin-bottom: 15px;
  background: #fff;
  box-shadow: 0 2px 8px #f0c1d6cc;
}

.question-result .question-header {
  border-bottom: 1px solid #ffe4ec;
  margin-bottom: 15px;
  padding-bottom: 10px;
}

.question-result .question-number {
  color: #ff5c8a;
  font-weight: bold;
}

.question-result .question-type {
  color: #ff5c8a;
}

.question-result .question-content {
  color: #303133;
  font-weight: bold;
}

.question-result .question-score {
  color: #f56c6c;
  font-weight: bold;
}

.question-options {
  margin: 10px 0;
  padding-left: 20px;
}

.question-options .option {
  margin: 5px 0;
  color: #666;
}

.answer-info {
  margin-top: 15px;
  padding: 15px;
  background: #fff5f8;
  border-radius: 8px;
  border: 1px solid #ffe4ec;
}

.your-answer, .correct-answer, .explanation {
  margin: 8px 0;
}

.correct {
  color: #67c23a;
  font-weight: bold;
}

.incorrect {
  color: #f56c6c;
  font-weight: bold;
}

.loading-container {
  text-align: center;
  padding: 50px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 16px #f0c1d6cc;
}

.loading-container p {
  margin-top: 20px;
  color: #606266;
}

.empty-container {
  text-align: center;
  padding: 50px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 16px #f0c1d6cc;
}

.empty-container i {
  font-size: 48px;
  color: #ffb6d5;
  margin-bottom: 20px;
}

.empty-container p {
  margin: 10px 0;
  color: #606266;
}

.empty-desc {
  color: #909399;
  font-size: 14px;
}

/* 对话框样式 */
::v-deep .el-dialog__header {
  background: linear-gradient(90deg, #ffe4ec 0%, #ffd6e6 100%);
  color: #ff5c8a;
  font-weight: bold;
}

::v-deep .el-dialog__title {
  color: #ff5c8a;
  font-weight: bold;
}

::v-deep .el-dialog__footer {
  border-top: 1px solid #ffe4ec;
}

::v-deep .el-dialog__footer .el-button--primary {
  background: #ffb6d5 !important;
  border-color: #ffb6d5 !important;
  color: #fff !important;
  font-weight: bold;
  border-radius: 12px;
  padding: 10px 20px;
}

::v-deep .el-dialog__footer .el-button--primary:hover {
  background: #ff5c8a !important;
  border-color: #ff5c8a !important;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .exam-list {
    padding: 10px;
  }

  .header {
    padding: 15px;
  }

  .header-content {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }

  .header h2 {
    font-size: 24px;
  }

  .back-btn {
    width: 100%;
  }

  .exam-content {
    flex-direction: column;
    gap: 15px;
  }

  .question-navigation {
    width: 100%;
  }

  .exam-header-bar {
    flex-direction: column;
    gap: 10px;
    text-align: center;
  }

  .exam-footer {
    flex-direction: column;
    gap: 10px;
  }

  .exam-footer ::v-deep .el-button {
    width: 100%;
  }
}

@media (max-width: 900px) {
  .exam-result {
    padding: 0 4px;
  }
  .result-summary {
    flex-direction: column;
    gap: 10px;
  }
  .result-item {
    padding: 10px;
    font-size: 16px;
  }
  .result-details h3 {
    font-size: 18px;
  }
  .question-result {
    padding: 8px;
    font-size: 15px;
  }
  .question-result .question-header {
    flex-direction: column;
    gap: 4px;
    font-size: 15px;
  }
  .answer-info {
    padding: 8px;
    font-size: 14px;
  }
}

.result-summary .el-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

@media (max-width: 900px) {
  .result-summary .el-row {
    flex-direction: column;
    gap: 10px;
  }
}

/* 响应式调整 el-message-box 弹窗 */
@media (max-width: 700px) {
  ::v-deep .el-message-box {
    width: 90vw !important;
    min-width: unset !important;
    max-width: 98vw !important;
    left: 5vw !important;
    padding: 12px 8px !important;
  }
  ::v-deep .el-message-box__btns {
    flex-direction: column;
    gap: 8px;
  }
  ::v-deep .el-message-box__btns .el-button {
    width: 100%;
    font-size: 16px;
  }
  ::v-deep .el-message-box__content {
    font-size: 16px;
    word-break: break-word;
  }
}
</style>
