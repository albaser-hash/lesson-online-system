<template>
  <div class="review-papers">
    <div class="header">
      <div class="header-content">
        <div class="title-section">
          <h2>批改试卷</h2>
          <p>教师可在此批量批改学生答卷</p>
        </div>
        <div class="header-actions">
          <el-button @click="$router.push('/teacher/exam')" class="back-btn">
            <i class="el-icon-back"></i>
            返回考试管理
          </el-button>
        </div>
      </div>
    </div>
    <el-divider></el-divider>
    <div class="main-content">
      <el-select v-model="selectedExamId" placeholder="选择考试" @change="loadPapers" class="exam-select">
        <el-option v-for="exam in examList" :key="exam.examId" :label="exam.examName" :value="exam.examId" />
      </el-select>
      <!-- PC端表�?-->
      <el-table v-if="!isMobile" :data="paperList" v-loading="loading" class="paper-table">
        <el-table-column prop="paperId" label="试卷ID" width="100" />
        <el-table-column prop="userId" label="学生" width="200">
          <template slot-scope="scope">
            <div class="student-info">
              <div class="student-name">{{ userNameMap[scope.row.userId] || scope.row.userId }}</div>
              <div class="student-id">ID: {{ scope.row.userId }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="180">
          <template slot-scope="scope">
            {{ formatTime(scope.row.submitTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" icon="el-icon-edit" @click="viewPaper(scope.row)">批改</el-button>
          </template>
        </el-table-column>
      </el-table>
      <!-- 移动端卡�?-->
      <div v-else class="paper-cards">
        <div v-for="paper in paperList" :key="paper.paperId" class="paper-card">
          <div class="paper-card-header">
            <div class="paper-id">试卷 #{{ paper.paperId }}</div>
            <el-button size="mini" type="primary" icon="el-icon-edit" @click="viewPaper(paper)" class="review-btn">
              批改
            </el-button>
          </div>
          <div class="paper-card-content">
            <div class="paper-info-row">
              <span class="info-label">学生</span>
              <span class="info-value">{{ userNameMap[paper.userId] || paper.userId }}</span>
            </div>
            <div class="paper-info-row">
              <span class="info-label">ID</span>
              <span class="info-value student-id">{{ paper.userId }}</span>
            </div>
            <div class="paper-info-row">
              <span class="info-label">提交</span>
              <span class="info-value submit-time">{{ formatTime(paper.submitTime) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    <el-dialog :visible.sync="showDialog" width="700px" title="批改试卷" class="review-dialog">
      <div v-if="currentPaper && currentPaper.questions">
        <div class="student-name">
          <b>学生：</b>{{ userNameMap[currentPaper.userId] || currentPaper.userId }}
        </div>
        <div v-for="(q, idx) in currentPaper.questions" :key="q.questionId" class="question-card">
          <div class="q-title"><b>题目{{ idx+1 }} [{{ getTypeText(q.questionType) }}]：</b>{{ q.questionContent }}</div>
          <div v-if="q.questionType==='TEXT'">
            <div class="q-answer"><b>学生答案：</b>{{ q.userAnswer }}</div>
            <el-input
              v-model.number="subjectiveScores[q.questionId]"
              type="number"
              :max="q.score"
              :min="0"
              :placeholder="`得分（最�?{q.score}）`"
              @input="onScoreInput(q)"
              class="score-input pink-input"
            />
            <span class="score-max">/ {{ q.score }}分</span>
          </div>
          <div v-else>
            <div class="q-answer"><b>学生答案：</b>{{ q.userAnswer }}</div>
            <div class="q-correct"><b>正确答案：</b>{{ q.correctAnswer }}</div>
          </div>
        </div>
        <div class="dialog-actions">
          <el-button type="primary" icon="el-icon-check" @click="submitReview">提交批改</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { getExamList, getReviewPapers, getPaperDetail, submitReview } from '@/api/exam'
import { getUserName } from '@/api/user'
export default {
  name: 'ReviewPapers',
  data() {
    return {
      examList: [],
      selectedExamId: '',
      paperList: [],
      loading: false,
      showDialog: false,
      currentPaper: null,
      subjectiveScores: {},
      userNameMap: {},
      isMobile: false
    }
  },
  mounted() {
    this.loadExams()
    this.checkIsMobile()
    window.addEventListener('resize', this.checkIsMobile)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.checkIsMobile)
  },
  methods: {
    async loadExams() {
      const response = await getExamList()
      const res = response.data
      if (res && res.code === 200 && Array.isArray(res.data)) {
        this.examList = res.data
      } else {
        this.examList = []
        this.$message.error(res?.msg || '获取考试列表失败')
      }
    },
    async fetchUserName(userId) {
      if (this.userNameMap[userId]) return this.userNameMap[userId];
      const res = await getUserName(userId);
      if (res.data && res.data.code === 200) {
        this.$set(this.userNameMap, userId, res.data.data);
        return res.data.data;
      }
      return userId;
    },
    async loadPapers() {
      if (!this.selectedExamId) return
      this.loading = true
      const response = await getReviewPapers(this.selectedExamId, 0)
      const res = response.data
      if (res && res.code === 200 && Array.isArray(res.data)) {
        this.paperList = res.data
      } else {
        this.paperList = []
        this.$message.error(res?.msg || '获取试卷列表失败')
      }
      this.loading = false
      const userIds = [...new Set(this.paperList.map(p => p.userId))];
      await Promise.all(userIds.map(id => this.fetchUserName(id)));
    },
    async viewPaper(row) {
      const response = await getPaperDetail(row.paperId)
      const res = response.data
      if (res && res.code === 200 && res.data) {
        if (res.data.questions) {
          res.data.questions.forEach(q => {
            q.score = Number(q.score) || 0
          })
        }
        this.currentPaper = res.data
        await this.fetchUserName(this.currentPaper.userId)
        this.subjectiveScores = {}
        if (this.currentPaper && this.currentPaper.questions) {
          this.currentPaper.questions.forEach(q => {
            if (q.questionType === 'TEXT') {
              this.$set(this.subjectiveScores, q.questionId, q.score)
            }
          })
        }
        this.showDialog = true
      } else {
        this.currentPaper = null
        this.$message.error(res?.msg || '获取试卷详情失败')
      }
    },
    async submitReview() {
      if (!this.currentPaper) return
      for (const q of this.currentPaper.questions) {
        if (q.questionType === 'TEXT' && (this.subjectiveScores[q.questionId] === undefined || this.subjectiveScores[q.questionId] === null)) {
          this.$message.warning('请为所有简答题打分')
          return
        }
      }
      const response = await submitReview({
        paperId: this.currentPaper.paperId,
        subjectiveScores: this.subjectiveScores
      })
      const res = response.data
      if (res && res.code === 200) {
        this.$message.success(res.data || res.msg || '批改成功')
        this.showDialog = false
        this.loadPapers()
      } else {
        this.$message.error(res?.msg || '批改失败')
      }
    },
    getTypeText(type) {
      switch(type) {
        case 'SINGLE_CHOICE': return '单选题'
        case 'MULTIPLE_CHOICE': return '多选题'
        case 'JUDGE': return '判断题'
        case 'TEXT': return '简答题'
        default: return type
      }
    },
    formatTime(ts) {
      if (!ts) return '';
      const date = new Date(ts);
      return date.getFullYear() + '-' +
        String(date.getMonth() + 1).padStart(2, '0') + '-' +
        String(date.getDate()).padStart(2, '0') + ' ' +
        String(date.getHours()).padStart(2, '0') + ':' +
        String(date.getMinutes()).padStart(2, '0') + ':' +
        String(date.getSeconds()).padStart(2, '0');
    },
    onScoreInput(q) {
      let val = this.subjectiveScores[q.questionId]
      if (val > q.score) this.subjectiveScores[q.questionId] = q.score
      if (val < 0) this.subjectiveScores[q.questionId] = 0
    },
    checkIsMobile() {
      this.isMobile = window.innerWidth < 768;
    }
  },
  computed: {
    reviewDialogWidth() {
      return window.innerWidth < 800 ? '98vw' : '700px'
    }
  }
}
</script>
<style scoped>
.review-papers {
  padding: 0;
  min-height: 100vh;
  background: linear-gradient(135deg, #fff0f6 0%, #ffd6e6 100%);
  width: 100%;
  margin: 0;
}
.header {
  margin-bottom: 24px;
  background: #fff;
  padding: 32px 32px 24px 32px;
  border-radius: 24px;
  box-shadow: 0 4px 24px #ffd6e6cc;
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
  font-size: 2.2rem;
  font-weight: bold;
  letter-spacing: 2px;
}
.header p {
  margin: 0;
  color: #b48bb3;
  font-size: 16px;
}
.header-actions {
  display: flex;
  align-items: center;
}
.back-btn {
  background: linear-gradient(90deg, #ffb6d5 0%, #ff5c8a 100%) !important;
  border: none !important;
  color: #fff !important;
  font-weight: bold;
  border-radius: 20px !important;
  padding: 10px 24px !important;
  box-shadow: 0 2px 8px #f0c1d6cc;
  transition: box-shadow 0.2s, background 0.2s;
}
.back-btn:hover {
  background: linear-gradient(90deg, #ff5c8a 0%, #ffb6d5 100%) !important;
  box-shadow: 0 4px 16px #ffb6d5cc;
}
.main-content {
  margin-bottom: 24px;
  padding: 0 32px;
}
.exam-select {
  min-width: 200px;
  margin-bottom: 20px;
  background: #fff0f6 !important;
  border-radius: 16px !important;
  border: 1.5px solid #ffb6d5 !important;
  color: #ff5c8a !important;
  font-weight: bold;
  font-size: 16px;
  padding: 6px 16px !important;
}
.paper-table {
  margin-top: 20px;
  border-radius: 18px !important;
  box-shadow: 0 4px 24px #ffd6e6cc !important;
  background: #fff;
  font-size: 16px;
  width: 100%;
}
.paper-table ::v-deep .el-table__header th {
  background: #fff0f6 !important;
  color: #ff5c8a !important;
  font-weight: bold;
  font-size: 16px;
}
.paper-table ::v-deep .el-table__row:hover td {
  background: #fff0f6 !important;
}
.paper-table ::v-deep .el-table__column--selection .cell {
  color: #ff5c8a !important;
}
.paper-table ::v-deep .el-button--primary {
  background: linear-gradient(90deg, #ffb6d5 0%, #ff5c8a 100%) !important;
  border: none !important;
  color: #fff !important;
  border-radius: 16px !important;
  font-weight: bold;
  transition: box-shadow 0.2s, background 0.2s;
}
.paper-table ::v-deep .el-button--primary:hover {
  background: linear-gradient(90deg, #ff5c8a 0%, #ffb6d5 100%) !important;
  box-shadow: 0 4px 16px #ffb6d5cc;
}
.review-dialog >>> .el-dialog__body {
  padding: 30px 24px 10px 24px;
  background: #fff0f6;
  border-radius: 24px;
}
.review-dialog >>> .el-dialog {
  border-radius: 24px !important;
  background: #fff0f6 !important;
  box-shadow: 0 4px 24px #ffd6e6cc !important;
}
.review-dialog >>> .el-dialog__header {
  background: linear-gradient(90deg, #ffb6d5 0%, #fff0f6 100%);
  border-radius: 24px 24px 0 0;
  color: #ff5c8a;
  font-weight: bold;
  font-size: 20px;
}
.review-dialog >>> .el-dialog__footer {
  background: #fff0f6;
  border-radius: 0 0 24px 24px;
}
.question-card {
  background: linear-gradient(135deg, #fff0f6 60%, #ffd6e6 100%);
  border-radius: 16px;
  box-shadow: 0 2px 12px #ffd6e6cc;
  padding: 18px 24px;
  margin-bottom: 18px;
}
.q-title {
  font-size: 1.1rem;
  font-weight: 500;
  margin-bottom: 8px;
  color: #ff5c8a;
}
.q-answer, .q-correct {
  margin: 6px 0;
}
.score-input {
  margin-right: 8px;
  width: 140px;
}
.pink-input ::v-deep .el-input__inner {
  background: #fff0f6 !important;
  color: #ff5c8a !important;
  border-radius: 8px !important;
  border: 1.5px solid #ffb6d5 !important;
  font-weight: bold;
}
.score-max {
  color: #f56c6c;
  font-weight: 500;
}
.dialog-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
}
.dialog-actions .el-button {
  background: linear-gradient(90deg, #ffb6d5 0%, #ff5c8a 100%) !important;
  border: none !important;
  color: #fff !important;
  border-radius: 20px !important;
  font-weight: bold;
  padding: 10px 24px !important;
  box-shadow: 0 2px 8px #f0c1d6cc;
  transition: box-shadow 0.2s, background 0.2s;
}
.dialog-actions .el-button:hover {
  background: linear-gradient(90deg, #ff5c8a 0%, #ffb6d5 100%) !important;
  box-shadow: 0 4px 16px #ffb6d5cc;
}
.student-name {
  font-size: 18px;
  color: #ff5c8a;
  font-weight: bold;
  margin-bottom: 18px;
}
.paper-cards {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.paper-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 12px #ffd6e6cc;
  padding: 16px;
  transition: all 0.3s ease;
}
.paper-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px #ffd6e6cc;
}
.paper-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.paper-id {
  font-size: 18px;
  font-weight: bold;
  color: #ff5c8a;
}
.review-btn {
  background: linear-gradient(90deg, #ffb6d5 0%, #ff5c8a 100%) !important;
  border: none !important;
  color: #fff !important;
  border-radius: 12px !important;
  font-weight: bold;
  padding: 8px 16px !important;
  box-shadow: 0 2px 8px #f0c1d6cc;
  transition: box-shadow 0.2s, background 0.2s;
}
.review-btn:hover {
  background: linear-gradient(90deg, #ff5c8a 0%, #ffb6d5 100%) !important;
  box-shadow: 0 4px 16px #ffb6d5cc;
}
.paper-card-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.paper-info-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.info-label {
  color: #666;
  font-size: 14px;
  min-width: 50px;
  flex-shrink: 0;
}
.info-value {
  color: #333;
  font-size: 14px;
  font-weight: 500;
  flex: 1;
}
.submit-time {
  color: #b48bb3;
  font-size: 12px;
}
.student-id {
  color: #b48bb3;
  font-size: 12px;
}
.student-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.student-info .student-name {
  font-weight: 500;
  color: #333;
}
.student-info .student-id {
  font-size: 12px;
  color: #999;
}
@media (max-width: 768px) {
  .review-papers {
    padding: 0 4vw 24px 4vw;
    max-width: 100vw;
  }
  .header {
    padding: 15px 2vw 10px 2vw;
    border-radius: 14px;
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
    padding: 10px 0 !important;
    border-radius: 14px !important;
  }
  .main-content {
    margin-bottom: 10px;
    padding: 0 2vw;
  }
  .exam-select {
    width: 100%;
    border-radius: 12px !important;
    font-size: 15px;
  }
  .paper-cards {
    gap: 12px;
  }
  .paper-card {
    padding: 12px;
    border-radius: 12px;
  }
  .paper-card-header {
    margin-bottom: 10px;
  }
  .paper-id {
    font-size: 16px;
  }
  .review-btn {
    padding: 6px 12px !important;
    font-size: 12px;
    border-radius: 10px !important;
  }
  .paper-info-row {
    gap: 6px;
  }
  .info-label {
    font-size: 13px;
    min-width: 45px;
  }
  .info-value {
    font-size: 13px;
  }
  .submit-time {
    font-size: 12px;
  }
  .student-id {
    font-size: 12px;
  }
  .paper-table {
    font-size: 14px !important;
    border-radius: 10px !important;
  }
  .question-card {
    padding: 10px 8px;
    border-radius: 10px;
  }
  .review-dialog >>> .el-dialog {
    width: 96vw !important;
    min-width: unset !important;
    max-width: 100vw !important;
    padding: 0 !important;
    border-radius: 14px !important;
  }
  .review-dialog >>> .el-dialog__header {
    font-size: 16px;
    border-radius: 14px 14px 0 0;
  }
  .review-dialog >>> .el-dialog__footer {
    border-radius: 0 0 14px 14px;
  }
  .score-input {
    width: 100px;
  }
}
@media (max-width: 700px) {
  .main-content {
    padding: 0 2vw;
  }
  .exam-select {
    width: 100%;
    min-width: unset;
    font-size: 15px;
    margin-bottom: 10px;
  }
  .paper-cards {
    gap: 8px;
  }
  .paper-card {
    padding: 10px;
    border-radius: 10px;
  }
  .paper-card-header {
    margin-bottom: 8px;
  }
  .paper-id {
    font-size: 15px;
  }
  .review-btn {
    padding: 4px 10px !important;
    font-size: 11px;
    border-radius: 8px !important;
  }
  .paper-info-row {
    gap: 4px;
  }
  .info-label {
    font-size: 12px;
    min-width: 40px;
  }
  .info-value {
    font-size: 12px;
  }
  .submit-time {
    font-size: 11px;
  }
  .student-id {
    font-size: 11px;
  }
  .paper-table {
    width: 100% !important;
    font-size: 13px !important;
    border-radius: 8px !important;
    overflow-x: auto;
    display: block;
  }
  .paper-table ::v-deep .el-table__header th,
  .paper-table ::v-deep .el-table__cell {
    font-size: 13px !important;
    padding: 6px 4px !important;
  }
  .paper-table ::v-deep .el-button--primary {
    font-size: 13px !important;
    padding: 6px 10px !important;
    border-radius: 10px !important;
  }
  .el-table__empty-block {
    font-size: 13px !important;
  }
  .review-dialog >>> .el-dialog {
    width: 98vw !important;
    min-width: unset !important;
    max-width: 100vw !important;
    border-radius: 14px !important;
    padding: 0 !important;
  }
  .review-dialog >>> .el-dialog__body {
    padding: 12px 2vw 8px 2vw !important;
    border-radius: 0 0 14px 14px;
  }
  .question-card {
    padding: 8px 4px;
    border-radius: 10px;
    font-size: 15px;
  }
  .q-title {
    font-size: 15px;
    margin-bottom: 4px;
  }
  .score-input {
    width: 80px;
    margin-right: 4px;
  }
  .dialog-actions {
    flex-direction: column;
    gap: 10px;
    margin-top: 10px;
  }
  .dialog-actions .el-button {
    width: 100%;
    font-size: 16px;
    padding: 10px 0 !important;
    border-radius: 14px !important;
  }
  .student-name {
    font-size: 16px;
    margin-bottom: 10px;
  }
}
</style>
