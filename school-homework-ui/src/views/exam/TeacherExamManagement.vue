<template>
  <div class="exam-management">
    <div class="header">
      <div class="header-content">
        <div class="title-section">
          <h2>考试管理</h2>
          <p>创建和管理您的考试</p>
        </div>
        <div class="header-actions">
          <el-button @click="goHome" class="back-btn">
            <i class="el-icon-back"></i>
            回到主页
          </el-button>
          <el-button type="primary" @click="openCreateDialog" class="create-btn">
            <i class="el-icon-plus"></i>
            创建考试
          </el-button>
          <el-button type="warning" @click="$router.push('/teacher/exam/review')" class="create-btn">
            <i class="el-icon-edit"></i>
            批改试卷
          </el-button>
        </div>
      </div>
    </div>
    <!-- 考试列表 -->
    <div class="exam-list">
      <!-- PC端表�?-->
      <el-table v-if="!isMobile" :data="examList" v-loading="loading" style="width: 100%">
        <el-table-column prop="examName" label="考试名称" min-width="150" />
        <el-table-column prop="courseName" label="所属课程" min-width="120" />
        <el-table-column prop="examCount" label="题目数量" min-width="80" />
        <el-table-column label="考试时间" min-width="200">
          <template slot-scope="scope">
            <div class="exam-time">
              <div>{{ formatDateTime(scope.row.startTime) }}</div>
              <div>{{ formatDateTime(scope.row.endTime) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" min-width="80">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="mini">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="350" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" @click="viewExam(scope.row)">查看</el-button>
            <el-button
              size="mini"
              type="primary"
              @click="publishExam(scope.row)"
              :disabled="scope.row.status !== 'UPCOMING'"
              :title="scope.row.status !== 'UPCOMING' ? getPublishDisabledReason(scope.row.status) : '推送考试给学生'"
            >
              推送
            </el-button>
            <el-button
              size="mini"
              type="success"
              @click="viewStatistics(scope.row)"
              :disabled="scope.row.status === 'UPCOMING'"
              :title="scope.row.status === 'UPCOMING' ? '考试未开始，暂无统计数据' : '查看考试统计'"
            >
              统计
            </el-button>
            <el-button
              size="mini"
              type="warning"
              @click="editExam(scope.row)"
              :disabled="scope.row.status !== 'UPCOMING'"
              :title="scope.row.status !== 'UPCOMING' ? getEditDisabledReason(scope.row.status) : '编辑考试'"
            >
              编辑
            </el-button>
            <el-button
              size="mini"
              type="danger"
              @click="deleteExam(scope.row)"
              :disabled="scope.row.status !== 'UPCOMING'"
              :title="scope.row.status !== 'UPCOMING' ? getDeleteDisabledReason(scope.row.status) : '删除考试'"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <!-- 移动端卡片 -->
      <div v-else class="exam-cards">
        <div v-for="exam in examList" :key="exam.examId" class="exam-card">
          <div class="exam-card-header">
            <div class="exam-title">{{ exam.examName }}</div>
            <el-tag :type="getStatusType(exam.status)" size="mini" class="exam-status">
              {{ getStatusText(exam.status) }}
            </el-tag>
          </div>
          <div class="exam-card-content">
            <div class="exam-info-row">
              <span class="info-label">课程：</span>
              <span class="info-value">{{ exam.courseName }}</span>
            </div>
            <div class="exam-info-row">
              <span class="info-label">题目：</span>
              <span class="info-value">{{ exam.examCount }}道</span>
            </div>
            <div class="exam-info-row">
              <span class="info-label">开始：</span>
              <span class="info-value start-time">{{ formatDateTime(exam.startTime) }}</span>
            </div>
            <div class="exam-info-row">
              <span class="info-label">结束：</span>
              <span class="info-value end-time">{{ formatDateTime(exam.endTime) }}</span>
            </div>
          </div>
          <div class="exam-card-actions">
            <el-button size="mini" @click="viewExam(exam)" class="action-btn">查看</el-button>
            <el-button
              size="mini"
              type="primary"
              @click="publishExam(exam)"
              :disabled="exam.status !== 'UPCOMING'"
              :title="exam.status !== 'UPCOMING' ? getPublishDisabledReason(exam.status) : '推送考试给学生'"
              class="action-btn"
            >
              推送
            </el-button>
            <el-button
              size="mini"
              type="success"
              @click="viewStatistics(exam)"
              :disabled="exam.status === 'UPCOMING'"
              :title="exam.status === 'UPCOMING' ? '考试未开始，暂无统计数据' : '查看考试统计'"
              class="action-btn"
            >
              统计
            </el-button>
            <el-button
              size="mini"
              type="warning"
              @click="editExam(exam)"
              :disabled="exam.status !== 'UPCOMING'"
              :title="exam.status !== 'UPCOMING' ? getEditDisabledReason(exam.status) : '编辑考试'"
              class="action-btn"
            >
              编辑
            </el-button>
            <el-button
              size="mini"
              type="danger"
              @click="deleteExam(exam)"
              :disabled="exam.status !== 'UPCOMING'"
              :title="exam.status !== 'UPCOMING' ? getDeleteDisabledReason(exam.status) : '删除考试'"
              class="action-btn"
            >
              删除
            </el-button>
          </div>
        </div>
      </div>
    </div>
    <!-- 创建考试对话框 -->
    <el-dialog
      :visible.sync="showCreateDialog"
      title="创建考试"
      :width="createDialogWidth"
      :close-on-click-modal="false"
      @open="onDialogOpen"
    >
      <el-form :model="examForm" :rules="examRules" ref="examFormRef" label-width="100px">
        <el-form-item label="考试名称" prop="examName">
          <el-input v-model="examForm.examName" placeholder="请输入考试名称" />
        </el-form-item>
        <el-form-item label="所属课程" prop="courseId">
          <el-select v-model="examForm.courseId" placeholder="请选择课程" style="width: 100%">
            <el-option
              v-for="course in courseList"
              :key="course.courseId"
              :label="course.courseName"
              :value="course.courseId"
            />
            <el-option v-if="courseList.length === 0" disabled label="暂无课程" value="" />
          </el-select>
          <div v-if="courseList.length === 0" style="color: #909399; font-size: 12px; margin-top: 5px;">
            暂无可用课程，请先创建课程
          </div>
          <div v-else style="color: #67c23a; font-size: 12px; margin-top: 5px;">
            已加载{{ courseList.length }} 门课程
          </div>
        </el-form-item>
        <el-form-item label="考试时间" prop="examTime">
          <template v-if="!isMobile">
            <el-date-picker
              v-model="examForm.examTime"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              style="width: 100%"
            />
          </template>
          <template v-else>
            <el-date-picker
              v-model="examForm.examTime[0]"
              type="datetime"
              placeholder="选择开始时间"
              style="width: 100%; margin-bottom: 8px"
            />
            <el-date-picker
              v-model="examForm.examTime[1]"
              type="datetime"
              placeholder="选择结束时间"
              style="width: 100%"
            />
          </template>
        </el-form-item>
      </el-form>
      <!-- 题目列表 -->
      <div class="questions-section">
        <div class="questions-header">
          <h3>考试题目</h3>
          <div class="questions-info">
            <span class="total-score">总分：{{ totalScore }}分</span>
            <span class="question-count">题目数：{{ examForm.questions.length }}道</span>
          </div>
          <div class="questions-actions">
            <el-button type="primary" @click="addQuestion">添加题目</el-button>
            <el-button type="success" @click="importCourseQuestions" :disabled="!examForm.courseId">导入题库</el-button>
          </div>
        </div>
        <div v-for="(question, index) in examForm.questions" :key="index" class="question-item">
          <div class="question-header">
            <span>题目 {{ index + 1 }}</span>
            <div class="question-score-display">
              <span class="score-text">{{ question.score }}分</span>
              <el-button type="danger" size="mini" @click="removeQuestion(index)">删除</el-button>
            </div>
          </div>
          <el-form :model="question" label-width="100px">
            <el-form-item label="题目类型">
              <el-select v-model="question.questionType" style="width: 200px" @change="onQuestionTypeChange(question)">
                <el-option label="单选题" value="SINGLE_CHOICE" />
                <el-option label="多选题" value="MULTIPLE_CHOICE" />
                <el-option label="判断题" value="JUDGE" />
                <el-option label="简答题" value="TEXT" />
              </el-select>
            </el-form-item>
            <el-form-item label="题目内容">
              <el-input
                v-model="question.questionContent"
                type="textarea"
                :rows="3"
                placeholder="请输入题目内容"
              />
            </el-form-item>
            <el-form-item label="选项" v-if="['SINGLE_CHOICE', 'MULTIPLE_CHOICE'].includes(question.questionType)">
              <el-input
                v-model="question.options"
                type="textarea"
                :rows="4"
                placeholder="请输入选项，每行一个选项，如：A. 选项1&#10;B. 选项2&#10;C. 选项3&#10;D. 选项4"
              />
            </el-form-item>
            <el-form-item label="正确答案" prop="correctAnswer">
              <!-- 单选题 -->
              <el-radio-group v-if="question.questionType === 'SINGLE_CHOICE' && parseOptions(question.options).length > 0" v-model="question.correctAnswer">
                <el-radio
                  v-for="option in parseOptions(question.options)"
                  :key="option.key"
                  :label="option.key"
                >
                  {{ option.key }}. {{ option.text }}
                </el-radio>
              </el-radio-group>
              <!-- 多选题 -->
              <el-checkbox-group v-else-if="question.questionType === 'MULTIPLE_CHOICE' && parseOptions(question.options).length > 0" v-model="question.correctAnswer">
                <el-checkbox
                  v-for="option in parseOptions(question.options)"
                  :key="option.key"
                  :label="option.key"
                >
                  {{ option.key }}. {{ option.text }}
                </el-checkbox>
              </el-checkbox-group>
              <!-- 判断题 -->
              <el-radio-group v-else-if="question.questionType === 'JUDGE'" v-model="question.correctAnswer">
                <el-radio label="true">正确</el-radio>
                <el-radio label="false">错误</el-radio>
              </el-radio-group>
              <!-- 简答题 -->
              <el-input
                v-else-if="question.questionType === 'TEXT'"
                v-model="question.correctAnswer"
                type="textarea"
                :rows="3"
                placeholder="请输入参考答案"
              />
              <!-- 默认输入框（用于没有选项的单选题、多选题等） -->
              <el-input
                v-else
                v-model="question.correctAnswer"
                :placeholder="getAnswerPlaceholder(question.questionType)"
              />
            </el-form-item>
            <el-form-item label="题目解析">
              <el-input
                v-model="question.explanation"
                type="textarea"
                :rows="3"
                placeholder="请输入题目解析（可选）"
              />
            </el-form-item>
            <el-form-item label="分数">
              <el-input-number v-model="question.score" :min="1" :max="100" />
            </el-form-item>
            <el-form-item label="难度">
              <el-select v-model="question.difficulty" style="width: 200px">
                <el-option label="简单" value="EASY" />
                <el-option label="中等" value="MEDIUM" />
                <el-option label="困难" value="HARD" />
              </el-select>
            </el-form-item>
          </el-form>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="addQuestion">添加题目</el-button>
        <el-button type="danger" @click="clearQuestions">清空题目</el-button>
        <el-button type="primary" @click="createExam" :loading="creating">创建考试</el-button>
      </div>
    </el-dialog>
    <!-- 编辑考试对话框 -->
    <el-dialog
      :visible.sync="showEditDialog"
      title="编辑考试"
      :width="createDialogWidth"
      :close-on-click-modal="false"
      @open="onEditDialogOpen"
    >
      <el-form :model="editForm" :rules="examRules" ref="editFormRef" label-width="100px">
        <el-form-item label="考试名称" prop="examName">
          <el-input v-model="editForm.examName" placeholder="请输入考试名称" />
        </el-form-item>
        <el-form-item label="所属课程" prop="courseId">
          <el-select v-model="editForm.courseId" placeholder="请选择课程" style="width: 100%" disabled>
            <el-option
              v-for="course in courseList"
              :key="course.courseId"
              :label="course.courseName"
              :value="course.courseId"
            />
          </el-select>
          <div style="color: #909399; font-size: 12px; margin-top: 5px;">
            编辑时不能修改所属课程
          </div>
        </el-form-item>
        <el-form-item label="考试时间" prop="examTime">
          <template v-if="!isMobile">
            <el-date-picker
              v-model="editForm.examTime"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              style="width: 100%"
            />
          </template>
          <template v-else>
            <el-date-picker
              v-model="editForm.examTime[0]"
              type="datetime"
              placeholder="选择开始时间"
              style="width: 100%; margin-bottom: 8px"
            />
            <el-date-picker
              v-model="editForm.examTime[1]"
              type="datetime"
              placeholder="选择结束时间"
              style="width: 100%"
            />
          </template>
        </el-form-item>
      </el-form>
      <!-- 题目列表 -->
      <div class="questions-section">
        <div class="questions-header">
          <h3>考试题目</h3>
          <div class="questions-info">
            <span class="total-score">总分：{{ editTotalScore }}分</span>
            <span class="question-count">题目数：{{ editForm.questions.length }}道</span>
          </div>
          <div class="questions-actions">
            <el-button type="primary" @click="addEditQuestion">添加题目</el-button>
            <el-button type="success" @click="importEditCourseQuestions" :disabled="!editForm.courseId">导入题库</el-button>
          </div>
        </div>
        <div v-for="(question, index) in editForm.questions" :key="index" class="question-item">
          <div class="question-header">
            <span>题目 {{ index + 1 }}</span>
            <div class="question-score-display">
              <span class="score-text">{{ question.score }}分</span>
              <el-button type="danger" size="mini" @click="removeEditQuestion(index)">删除</el-button>
            </div>
          </div>
          <el-form :model="question" label-width="100px">
            <el-form-item label="题目类型">
              <el-select v-model="question.questionType" style="width: 200px" @change="onEditQuestionTypeChange(question)">
                <el-option label="单选题" value="SINGLE_CHOICE" />
                <el-option label="多选题" value="MULTIPLE_CHOICE" />
                <el-option label="判断题" value="JUDGE" />
                <el-option label="简答题" value="TEXT" />
              </el-select>
            </el-form-item>
            <el-form-item label="题目内容">
              <el-input
                v-model="question.questionContent"
                type="textarea"
                :rows="3"
                placeholder="请输入题目内容"
              />
            </el-form-item>
            <el-form-item label="选项" v-if="['SINGLE_CHOICE', 'MULTIPLE_CHOICE'].includes(question.questionType)">
              <el-input
                v-model="question.options"
                type="textarea"
                :rows="4"
                placeholder="请输入选项，每行一个选项，如：A. 选项1&#10;B. 选项2&#10;C. 选项3&#10;D. 选项4"
              />
            </el-form-item>
            <el-form-item label="正确答案" prop="correctAnswer">
              <!-- 单选题 -->
              <el-radio-group v-if="question.questionType === 'SINGLE_CHOICE' && parseOptions(question.options).length > 0" v-model="question.correctAnswer">
                <el-radio
                  v-for="option in parseOptions(question.options)"
                  :key="option.key"
                  :label="option.key"
                >
                  {{ option.key }}. {{ option.text }}
                </el-radio>
              </el-radio-group>
              <!-- 多选题 -->
              <el-checkbox-group v-else-if="question.questionType === 'MULTIPLE_CHOICE' && parseOptions(question.options).length > 0" v-model="question.correctAnswer">
                <el-checkbox
                  v-for="option in parseOptions(question.options)"
                  :key="option.key"
                  :label="option.key"
                >
                  {{ option.key }}. {{ option.text }}
                </el-checkbox>
              </el-checkbox-group>
              <!-- 判断题 -->
              <el-radio-group v-else-if="question.questionType === 'JUDGE'" v-model="question.correctAnswer">
                <el-radio label="true">正确</el-radio>
                <el-radio label="false">错误</el-radio>
              </el-radio-group>
              <!-- 简答题 -->
              <el-input
                v-else-if="question.questionType === 'TEXT'"
                v-model="question.correctAnswer"
                type="textarea"
                :rows="3"
                placeholder="请输入参考答案"
              />
              <!-- 默认输入框（用于没有选项的单选题、多选题等） -->
              <el-input
                v-else
                v-model="question.correctAnswer"
                :placeholder="getAnswerPlaceholder(question.questionType)"
              />
            </el-form-item>
            <el-form-item label="题目解析">
              <el-input
                v-model="question.explanation"
                type="textarea"
                :rows="3"
                placeholder="请输入题目解析（可选）"
              />
            </el-form-item>
            <el-form-item label="分数">
              <el-input-number v-model="question.score" :min="1" :max="100" />
            </el-form-item>
            <el-form-item label="难度">
              <el-select v-model="question.difficulty" style="width: 200px">
                <el-option label="简单" value="EASY" />
                <el-option label="中等" value="MEDIUM" />
                <el-option label="困难" value="HARD" />
              </el-select>
            </el-form-item>
          </el-form>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="addEditQuestion">添加题目</el-button>
        <el-button type="danger" @click="clearEditQuestions">清空题目</el-button>
        <el-button type="primary" @click="updateExam" :loading="updating">保存修改</el-button>
      </div>
    </el-dialog>
    <!-- 考试详情对话框 -->
    <el-dialog
      :visible.sync="showDetailDialog"
      title="考试详情"
      :width="detailDialogWidth"
    >
      <div v-if="examDetail">
        <div class="exam-info">
          <p><strong>考试名称：</strong>{{ examDetail.examName }}</p>
          <p><strong>所属课程：</strong>{{ examDetail.courseName }}</p>
          <p><strong>题目数量：</strong>{{ examDetail.examCount }}</p>
          <p><strong>考试时间：</strong>{{ formatDateTime(examDetail.startTime) }} - {{ formatDateTime(examDetail.endTime) }}</p>
        </div>
        <div class="questions-list">
          <h3>题目列表</h3>
          <div v-for="(question, index) in examDetail.questions" :key="index" class="question-detail">
            <div class="question-title">
              <span class="question-number">{{ index + 1 }}.</span>
              <span class="question-type">[{{ getQuestionTypeText(question.questionType) }}]</span>
              <span class="question-content">{{ question.questionContent }}</span>
              <span class="question-score">({{ question.score }}分)</span>
            </div>
            <div v-if="question.options" class="question-options">
              <div v-for="option in question.options.split('\n')" :key="option" class="option">
                {{ option }}
              </div>
            </div>
            <div class="question-answer">
              <strong>正确答案：</strong>{{ question.correctAnswer }}
            </div>
            <div v-if="question.explanation" class="question-explanation">
              <strong>题目解析：</strong>{{ question.explanation }}
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
    <!-- 统计信息对话框 -->
    <el-dialog
      :visible.sync="showStatisticsDialog"
      title="考试统计"
      :width="statisticsDialogWidth"
    >
      <div v-if="examStatistics">
        <div class="statistics-info">
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="stat-item">
                <div class="stat-number">{{ examStatistics.totalStudents }}</div>
                <div class="stat-label">参加人数</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-item">
                <div class="stat-number">{{ examStatistics.averageScore }}</div>
                <div class="stat-label">平均分</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-item">
                <div class="stat-number">{{ examStatistics.highestScore }}</div>
                <div class="stat-label">最高分</div>
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="20" style="margin-top: 20px;">
            <el-col :span="8">
              <div class="stat-item">
                <div class="stat-number">{{ examStatistics.lowestScore }}</div>
                <div class="stat-label">最低分</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-item">
                <div class="stat-number">{{ examStatistics.passRate }}%</div>
                <div class="stat-label">及格率</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-item">
                <div class="stat-number">{{ examStatistics.correctRate }}%</div>
                <div class="stat-label">正确率</div>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { mapState } from 'vuex'
import { getExamList, createExam, getExamDetail, publishExam, getExamStatistics, updateExam, deleteExam } from '@/api/exam'
import { getTeacherTestCourses } from '@/api/createCourse'
import { getExamQuestions, saveExamQuestions, clearExamQuestions, getCourseQuestions } from '@/api/exam'
export default {
  name: 'TeacherExamManagement',
  data() {
    return {
      loading: false,
      creating: false,
      examList: [
        {
          examId: 1,
          examName: 'Vue.js基础测试',
          courseName: 'Vue.js入门到精通',
          examCount: 10,
          startTime: '2024-01-20 10:00:00',
          endTime: '2024-01-20 12:00:00',
          status: 'ONGOING'
        },
        {
          examId: 2,
          examName: 'React框架测试',
          courseName: 'React全栈开发',
          examCount: 15,
          startTime: '2024-01-18 14:00:00',
          endTime: '2024-01-18 16:00:00',
          status: 'FINISHED'
        },
        {
          examId: 3,
          examName: 'Java编程测试',
          courseName: 'Java核心技术',
          examCount: 20,
          startTime: '2024-01-25 09:00:00',
          endTime: '2024-01-25 11:00:00',
          status: 'UPCOMING'
        }
      ],
      courseList: [
        {
          courseId: 1,
          courseName: 'Vue.js入门到精通',
          categoryName: '前端开发',
          teacherName: '张老师'
        },
        {
          courseId: 2,
          courseName: 'React全栈开发',
          categoryName: '前端开发',
          teacherName: '李老师'
        },
        {
          courseId: 3,
          courseName: 'Java核心技术',
          categoryName: '后端开发',
          teacherName: '王老师'
        }
      ],
      showCreateDialog: false,
      showDetailDialog: false,
      showStatisticsDialog: false,
      examDetail: null,
      examStatistics: {
        totalStudents: 45,
        averageScore: 78.5,
        highestScore: 95,
        lowestScore: 45,
        passRate: 82.2,
        correctRate: 76.8
      },
      examForm: {
        examName: '',
        courseId: '',
        examTime: [],
        questions: []
      },
      examRules: {
        examName: [
          { required: true, message: '请输入考试名称', trigger: 'blur' }
        ],
        courseId: [
          { required: true, message: '请选择所属课程', trigger: 'change' }
        ],
        examTime: [
          { required: true, message: '请选择考试时间', trigger: 'change' }
        ]
      },
      userNameMap: {},
      showEditDialog: false,
      updating: false,
      editForm: {
        examId: null,
        examName: '',
        courseId: '',
        examTime: [],
        questions: []
      },
      isMobile: false
    }
  },
  computed: {
    ...mapState({
      userType: state => state.user.userType,
      userId: state => state.user.id
    }),
    createDialogWidth() {
      return window.innerWidth < 900 ? '98vw' : '800px'
    },
    detailDialogWidth() {
      return window.innerWidth < 900 ? '98vw' : '800px'
    },
    statisticsDialogWidth() {
      return window.innerWidth < 700 ? '98vw' : '600px'
    },
    totalScore() {
      return this.examForm.questions.reduce((total, question) => {
        return total + (question.score || 0)
      }, 0)
    },
    editTotalScore() {
      return this.editForm.questions.reduce((total, question) => {
        return total + (question.score || 0)
      }, 0)
    }
  },
  mounted() {
    console.log('教师考试管理页面使用假数据')
    this.checkTeacherPermission()
    this.checkMobile()
    window.addEventListener('resize', this.checkMobile)
  },
  watch: {
    userId(newVal) {
      if (newVal) {
        this.initWebSocket()
      }
    }
  },
  beforeDestroy() {
    webSocketService.disconnect()
    window.removeEventListener('resize', this.checkMobile)
  },
  methods: {
    checkTeacherPermission() {
      console.log('权限检查（假数据）')
      if (!this.userType) {
        setTimeout(() => {
          this.checkTeacherPermission()
        }, 100)
        return
      }
      if (this.userType !== 'TEACHER') {
        this.$message.error('只有教师可以访问考试管理页面')
        this.$router.push('/')
      } else {
        this.loadExamList()
      }
    },
    async loadExamList() {
      console.log('加载考试列表（假数据）')
      this.loading = true
      setTimeout(() => {
        this.loading = false
        this.$message.success('考试列表加载成功（假数据）')
      }, 500)
    },
    addQuestion() {
      this.examForm.questions.push({
        questionType: 'SINGLE_CHOICE',
        questionContent: '',
        options: '',
        correctAnswer: '',
        explanation: '',
        score: 10,
        difficulty: 'MEDIUM'
      })
    },
    removeQuestion(index) {
      this.examForm.questions.splice(index, 1)
    },
    onQuestionTypeChange(question) {
      if (question.questionType === 'MULTIPLE_CHOICE') {
        question.correctAnswer = []
      } else {
        question.correctAnswer = ''
      }
    },
    getAnswerPlaceholder(questionType) {
      const placeholders = {
        'SINGLE_CHOICE': '请输入正确答案，如：A',
        'MULTIPLE_CHOICE': '请输入正确答案，如：A,B,C',
        'JUDGE': '请输入正确答案，如：true 或 false',
        'TEXT': '请输入参考答案'
      }
      return placeholders[questionType] || '请输入正确答案'
    },
    async openCreateDialog(exam) {
      console.log('打开创建考试对话框（假数据）')
      if (this.courseList.length === 0) {
        console.log('课程列表为空，使用假数据')
      }
      if (this.courseList.length > 0) {
        if (exam && exam.examId) {
          console.log('编辑考试（假数据）')
          this.examForm = {
            ...exam,
            examTime: [exam.startTime, exam.endTime],
            questions: [
              {
                questionType: 'SINGLE_CHOICE',
                questionContent: 'Vue.js是什么？',
                options: 'A. 前端框架\nB. 后端框架\nC. 数据库\nD. 操作系统',
                correctAnswer: 'A',
                explanation: 'Vue.js是一个流行的前端JavaScript框架',
                score: 10,
                difficulty: 'MEDIUM'
              }
            ]
          }
        } else {
          const now = new Date()
          const start = new Date(now.getTime() + 60 * 60 * 1000)
          const end = new Date(now.getTime() + 2 * 60 * 60 * 1000)
          this.examForm = {
            examName: '',
            courseId: '',
            examTime: [start, end],
            questions: []
          }
        }
        this.showCreateDialog = true
      } else {
        this.$message.warning('暂无可用课程，请先创建课程')
      }
    },
    async saveQuestions() {
      console.log('保存题目（假数据）')
      this.$message.success('题目已保存（假数据）')
    },
    async clearQuestions() {
      console.log('清空题目（假数据）')
      await this.$confirm('确定要清空所有题目吗？', '提示')
      this.examForm.questions = []
      this.$message.success('题目已清空（假数据）')
    },
    async createExam() {
      this.$refs.examFormRef.validate(async (valid) => {
        if (!valid) return
        if (this.examForm.questions.length === 0) {
          this.$message.warning('请至少添加一道题目')
          return
        }
        for (let i = 0; i < this.examForm.questions.length; i++) {
          const question = this.examForm.questions[i]
          if (question.questionType === 'MULTIPLE_CHOICE') {
            if (!Array.isArray(question.correctAnswer) || question.correctAnswer.length === 0) {
              this.$message.error(`第${i + 1}题（多选题）请选择正确答案`)
              return
            }
            question.correctAnswer = question.correctAnswer.join(',')
          } else {
            if (!question.correctAnswer || question.correctAnswer.toString().trim() === '') {
              this.$message.error(`第${i + 1}题请填写正确答案`)
              return
            }
          }
        }
        this.creating = true
        try {
          console.log('创建考试（假数据）')
          setTimeout(() => {
            this.$message.success('考试创建成功（假数据）')
            this.showCreateDialog = false
            this.resetForm()
            this.loadExamList()
            this.creating = false
          }, 1000)
        } catch (error) {
          this.$message.error('创建考试失败')
          console.error('创建考试错误:', error)
          this.creating = false
        }
      })
    },
    resetForm() {
      this.examForm = {
        examName: '',
        courseId: '',
        examTime: [],
        questions: []
      }
      this.$refs.examFormRef.resetFields()
    },
    async viewExam(exam) {
      console.log('查看考试详情（假数据）')
      this.examDetail = {
        examId: exam.examId,
        examName: exam.examName,
        courseName: exam.courseName,
        examCount: exam.examCount,
        startTime: exam.startTime,
        endTime: exam.endTime,
        questions: [
          {
            questionType: 'SINGLE_CHOICE',
            questionContent: 'Vue.js是什么？',
            options: 'A. 前端框架\nB. 后端框架\nC. 数据库\nD. 操作系统',
            correctAnswer: 'A',
            explanation: 'Vue.js是一个流行的前端JavaScript框架',
            score: 10
          }
        ]
      }
      this.showDetailDialog = true
    },
    async publishExam(exam) {
      try {
        await this.$confirm('确定要推送这个考试吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        console.log('推送考试（假数据）')
        setTimeout(() => {
          this.$message.success('考试推送成功（假数据）')
          this.loadExamList()
        }, 500)
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('推送考试失败')
          console.error(error)
        }
      }
    },
    async viewStatistics(exam) {
      console.log('查看考试统计（假数据）')
      this.examStatistics = {
        totalStudents: 45,
        averageScore: 78.5,
        highestScore: 95,
        lowestScore: 45,
        passRate: 82.2,
        correctRate: 76.8
      }
      this.showStatisticsDialog = true
    },
    formatDateTime(dateTime) {
      if (!dateTime) return ''
      const date = new Date(dateTime)
      return date.toLocaleString('zh-CN', {
        timeZone: 'Asia/Shanghai',
        year: 'numeric',
        month: 'numeric',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
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
    goHome() {
      this.$router.push('/')
    },
    onDialogOpen() {
      this.getCourseList()
    },
    initWebSocket() {
      console.log('初始化WebSocket（假数据）')
    },
    getCourseList() {
      if (this.courseList.length > 0) {
        return
      }
      getTeacherTestCourses({ teacherId: this.userId, page: 1, pageSize: 100 }).then(response => {
        const res = response.data
        if (res && res.code === 200 && res.data && Array.isArray(res.data.records)) {
          this.courseList = res.data.records
        } else {
          this.courseList = []
          this.$message.error(res?.msg || '获取课程列表失败');
        }
      });
    },
    async importCourseQuestions() {
      if (!this.examForm.courseId) {
        this.$message.warning('请先选择课程');
        return;
      }
      console.log('导入课程题目（假数据）')
      const processedQuestions = [
        {
          questionType: 'SINGLE_CHOICE',
          questionContent: 'Vue.js是什么？',
          options: 'A. 前端框架\nB. 后端框架\nC. 数据库\nD. 操作系统',
          correctAnswer: 'A',
          explanation: 'Vue.js是一个流行的前端JavaScript框架',
          score: 10,
          difficulty: 'MEDIUM'
        }
      ];
      this.examForm.questions = this.examForm.questions.concat(processedQuestions);
      this.$message.success(`已导入${processedQuestions.length}道题目（假数据）`);
    },
    async fetchUserName(userId) {
      if (!userId) return '';
      if (this.userNameMap[userId]) return this.userNameMap[userId];
      console.log('获取用户名（假数据）')
      return `用户${userId}`;
    },
    async fetchUserNames(userIdList) {
      console.log('批量获取用户名（假数据）')
    },
    parseOptions(options) {
      if (!options || typeof options !== 'string' || options.trim() === '') return []
      return options.split('\n')
        .map(option => option.trim())
        .filter(option => option.length > 0)
        .map(option => {
          const match = option.match(/^([A-Z]):\s*(.+)$/)
          if (match) {
            return { key: match[1], text: match[2].trim() }
          }
          const match2 = option.match(/^([A-Z])\.\s*(.+)$/)
          if (match2) {
            return { key: match2[1], text: match2[2].trim() }
          }
          return { key: option, text: option }
        })
        .filter(option => option.text && option.text.trim() !== '')
    },
    async editExam(exam) {
      console.log('编辑考试（假数据）')
      this.editForm = {
        examId: exam.examId,
        examName: exam.examName,
        courseId: exam.courseId,
        examTime: [exam.startTime, exam.endTime],
        questions: [
          {
            questionType: 'SINGLE_CHOICE',
            questionContent: 'Vue.js是什么？',
            options: 'A. 前端框架\nB. 后端框架\nC. 数据库\nD. 操作系统',
            correctAnswer: 'A',
            explanation: 'Vue.js是一个流行的前端JavaScript框架',
            score: 10,
            difficulty: 'MEDIUM'
          }
        ]
      }
      this.showEditDialog = true
    },
    onEditDialogOpen() {
      console.log('编辑对话框打开（假数据）')
    },
    addEditQuestion() {
      this.editForm.questions.push({
        questionType: 'SINGLE_CHOICE',
        questionContent: '',
        options: '',
        correctAnswer: '',
        explanation: '',
        score: 10,
        difficulty: 'MEDIUM'
      })
    },
    removeEditQuestion(index) {
      this.editForm.questions.splice(index, 1)
    },
    onEditQuestionTypeChange(question) {
      if (question.questionType === 'MULTIPLE_CHOICE') {
        question.correctAnswer = []
      } else {
        question.correctAnswer = ''
      }
    },
    async importEditCourseQuestions() {
      if (!this.editForm.courseId) {
        this.$message.warning('请先选择课程');
        return;
      }
      console.log('导入编辑课程题目（假数据）')
      const processedQuestions = [
        {
          questionType: 'SINGLE_CHOICE',
          questionContent: 'Vue.js是什么？',
          options: 'A. 前端框架\nB. 后端框架\nC. 数据库\nD. 操作系统',
          correctAnswer: 'A',
          explanation: 'Vue.js是一个流行的前端JavaScript框架',
          score: 10,
          difficulty: 'MEDIUM'
        }
      ];
      this.editForm.questions = this.editForm.questions.concat(processedQuestions);
      this.$message.success(`已导入${processedQuestions.length}道题目（假数据）`);
    },
    clearEditQuestions() {
      this.editForm.questions = []
    },
    async updateExam() {
      this.$refs.editFormRef.validate(async (valid) => {
        if (!valid) return
        if (this.editForm.questions.length === 0) {
          this.$message.warning('请至少添加一道题目')
          return
        }
        for (let i = 0; i < this.editForm.questions.length; i++) {
          const question = this.editForm.questions[i]
          if (question.questionType === 'MULTIPLE_CHOICE') {
            if (!Array.isArray(question.correctAnswer) || question.correctAnswer.length === 0) {
              this.$message.error(`第${i + 1}题（多选题）请选择正确答案`)
              return
            }
            question.correctAnswer = question.correctAnswer.join(',')
          } else {
            if (!question.correctAnswer || question.correctAnswer.toString().trim() === '') {
              this.$message.error(`第${i + 1}题请填写正确答案`)
              return
            }
          }
        }
        this.updating = true
        try {
          console.log('更新考试（假数据）')
          setTimeout(() => {
            this.$message.success('考试更新成功（假数据）')
            this.showEditDialog = false
            this.resetEditForm()
            this.loadExamList()
            this.updating = false
          }, 1000)
        } catch (error) {
          this.$message.error('更新考试失败')
          console.error('更新考试错误:', error)
          this.updating = false
        }
      })
    },
    resetEditForm() {
      this.editForm = {
        examId: null,
        examName: '',
        courseId: '',
        examTime: [],
        questions: []
      }
      this.$refs.editFormRef.resetFields()
    },
    async deleteExam(exam) {
      try {
        await this.$confirm('确定要删除这个考试吗？删除后无法恢复', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        console.log('删除考试（假数据）')
        setTimeout(() => {
          this.examList = this.examList.filter(item => item.examId !== exam.examId)
          this.$message.success('考试删除成功（假数据）')
        }, 500)
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('删除考试失败')
          console.error(error)
        }
      }
    },
    getPublishDisabledReason(status) {
      switch (status) {
        case 'ONGOING':
          return '考试进行中，无法推送'
        case 'FINISHED':
          return '考试已结束，无法推送'
        default:
          return '无法推送'
      }
    },
    getEditDisabledReason(status) {
      switch (status) {
        case 'ONGOING':
          return '考试进行中，无法编辑'
        case 'FINISHED':
          return '考试已结束，无法编辑'
        default:
          return '无法编辑'
      }
    },
    getDeleteDisabledReason(status) {
      switch (status) {
        case 'ONGOING':
          return '考试进行中，无法删除'
        case 'FINISHED':
          return '考试已结束，无法删除'
        default:
          return '无法删除'
      }
    },
    checkMobile() {
      this.isMobile = window.innerWidth < 768
    }
  }
}
</script>
<style scoped>
.exam-management {
  padding: 20px;
  min-height: 100vh;
  background: linear-gradient(135deg, #ffe4ec 0%, #ffd6e6 100%);
  max-width: 1600px;
  margin: 0 auto;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  margin-right: 20px;
}
.title-section h2 {
  margin: 0 0 8px 0;
  color: #ff5c8a;
  font-size: 28px;
  font-weight: bold;
}
.title-section p {
  color: #666;
  font-size: 16px;
}
.header-actions {
  display: flex;
  align-items: center;
}
.back-btn,
.create-btn {
  background: #ffb6d5 !important;
  border-color: #ffb6d5 !important;
  color: #fff !important;
  font-weight: bold;
  border-radius: 12px;
  padding: 10px 20px;
  margin-left: 10px;
}
.back-btn:hover,
.create-btn:hover {
  background: #ff5c8a !important;
  border-color: #ff5c8a !important;
}
.back-btn i,
.create-btn i {
  margin-right: 5px;
}
.exam-list {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 16px #f0c1d6cc;
  border: none;
  overflow: hidden;
  width: 100%;
}
.exam-list ::v-deep .el-table {
  border-radius: 12px;
  overflow: hidden;
  width: 100% !important;
}
.exam-list ::v-deep .el-table__body-wrapper {
  overflow-x: auto;
}
.exam-list ::v-deep .el-table__header-wrapper {
  background: linear-gradient(90deg, #ffe4ec 0%, #ffd6e6 100%);
}
.exam-list ::v-deep .el-table__header th {
  background: transparent;
  color: #ff5c8a;
  font-weight: bold;
  border-bottom: 2px solid #ffb6d5;
}
.exam-list ::v-deep .el-table__body tr:hover > td {
  background: #fff5f8;
}
.exam-list ::v-deep .el-table__body td {
  border-bottom: 1px solid #ffe4ec;
}
.exam-list ::v-deep .el-button--mini {
  border-radius: 8px;
  font-weight: bold;
}
.exam-list ::v-deep .el-button--primary {
  background: #ffb6d5 !important;
  border-color: #ffb6d5 !important;
  color: #fff !important;
}
.exam-list ::v-deep .el-button--primary:hover {
  background: #ff5c8a !important;
  border-color: #ff5c8a !important;
}
.exam-list ::v-deep .el-button--success {
  background: #67c23a !important;
  border-color: #67c23a !important;
  color: #fff !important;
}
.exam-list ::v-deep .el-button--success:hover {
  background: #85ce61 !important;
  border-color: #85ce61 !important;
}
.exam-list ::v-deep .el-button--warning {
  background: #e6a23c !important;
  border-color: #e6a23c !important;
  color: #fff !important;
}
.exam-list ::v-deep .el-button--warning:hover {
  background: #ebb563 !important;
  border-color: #ebb563 !important;
}
.exam-list ::v-deep .el-button--danger {
  background: #f56c6c !important;
  border-color: #f56c6c !important;
  color: #fff !important;
}
.exam-list ::v-deep .el-button--danger:hover {
  background: #f78989 !important;
  border-color: #f78989 !important;
}
.exam-list ::v-deep .el-button.is-disabled {
  cursor: help !important;
  opacity: 0.6;
}
.exam-list ::v-deep .el-button.is-disabled:hover {
  opacity: 0.8;
}
.exam-list ::v-deep .el-table__body .el-button + .el-button {
  margin-left: 8px;
}
@media (min-width: 1200px) {
  .exam-list ::v-deep .el-table__body .el-button + .el-button {
    margin-left: 4px;
  }
  .exam-list ::v-deep .el-button--mini {
    padding: 5px 8px;
    font-size: 12px;
  }
}
@media (max-width: 1199px) and (min-width: 768px) {
  .exam-list ::v-deep .el-table__body .el-button + .el-button {
    margin-left: 6px;
  }
}
.questions-section {
  margin-top: 20px;
}
.questions-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  flex-wrap: wrap;
  gap: 10px;
}
.questions-header h3 {
  margin: 0;
  color: #ff5c8a;
  font-size: 20px;
  font-weight: bold;
}
.questions-info {
  display: flex;
  gap: 20px;
  align-items: center;
}
.total-score {
  color: #67c23a;
  font-weight: bold;
  font-size: 16px;
}
.question-count {
  color: #409eff;
  font-weight: bold;
  font-size: 16px;
}
.questions-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}
.questions-header .el-button {
  background: #ffb6d5 !important;
  border-color: #ffb6d5 !important;
  color: #fff !important;
  font-weight: bold;
  border-radius: 12px;
  padding: 8px 16px;
}
.questions-header .el-button:hover {
  background: #ff5c8a !important;
  border-color: #ff5c8a !important;
}
.question-item {
  border: 1px solid #ffe4ec;
  border-radius: 12px;
  padding: 15px;
  margin-bottom: 15px;
  background: #fff;
  box-shadow: 0 2px 8px #f0c1d6cc;
}
.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  font-weight: bold;
  color: #ff5c8a;
}
.question-score-display {
  display: flex;
  align-items: center;
  gap: 10px;
}
.score-text {
  color: #f56c6c;
  font-weight: bold;
  font-size: 14px;
  background: #fef0f0;
  padding: 4px 8px;
  border-radius: 6px;
  border: 1px solid #fbc4c4;
}
.question-header .el-button {
  border-radius: 8px;
  font-weight: bold;
}
.exam-info p {
  margin: 10px 0;
  color: #666;
}
.questions-list h3 {
  margin: 20px 0 15px 0;
  color: #ff5c8a;
  font-weight: bold;
}
.question-detail {
  border: 1px solid #ffe4ec;
  border-radius: 12px;
  padding: 15px;
  margin-bottom: 15px;
  background: #fff;
  box-shadow: 0 2px 8px #f0c1d6cc;
}
.question-title {
  margin-bottom: 10px;
  font-weight: bold;
}
.question-number {
  color: #ff5c8a;
  margin-right: 5px;
}
.question-type {
  color: #909399;
  margin-right: 5px;
}
.question-score {
  color: #f56c6c;
  margin-left: 5px;
}
.question-options {
  margin: 10px 0;
  padding-left: 20px;
}
.option {
  margin: 5px 0;
  color: #666;
}
.question-answer {
  margin-top: 10px;
  color: #67c23a;
  font-weight: bold;
}
.question-explanation {
  margin-top: 10px;
  color: #909399;
  font-size: 14px;
}
.statistics-info {
  text-align: center;
}
.stat-item {
  padding: 20px;
  background: #f5f7fa;
  border-radius: 12px;
  box-shadow: 0 2px 8px #f0c1d6cc;
}
.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #ff5c8a;
  margin-bottom: 5px;
}
.stat-label {
  color: #666;
  font-size: 14px;
}
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
::v-deep .el-form-item__label {
  color: #ff5c8a;
  font-weight: bold;
}
::v-deep .el-input__inner,
::v-deep .el-textarea__inner {
  border-color: #ffe4ec;
  border-radius: 8px;
}
::v-deep .el-input__inner:focus,
::v-deep .el-textarea__inner:focus {
  border-color: #ffb6d5;
}
::v-deep .el-select .el-input__inner {
  border-color: #ffe4ec;
  border-radius: 8px;
}
::v-deep .el-select .el-input__inner:focus {
  border-color: #ffb6d5;
}
::v-deep .el-date-editor {
  border-color: #ffe4ec;
  border-radius: 8px;
}
::v-deep .el-date-editor:focus {
  border-color: #ffb6d5;
}
@media (max-width: 768px) {
  .exam-management {
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
  .title-section {
    margin-right: 0;
  }
  .title-section h2 {
    font-size: 24px;
  }
  .header-actions {
    flex-direction: column;
    gap: 10px;
  }
  .back-btn,
  .create-btn {
    width: 100%;
    margin-left: 0;
  }
  .exam-list {
    border-radius: 12px;
    padding: 10px;
    overflow-x: auto;
  }
  .exam-cards {
    gap: 12px;
  }
  .exam-card {
    padding: 12px;
    border-radius: 12px;
  }
  .exam-card-header {
    margin-bottom: 10px;
    gap: 8px;
  }
  .exam-title {
    font-size: 16px;
  }
  .exam-info-row {
    margin-bottom: 6px;
  }
  .info-label {
    font-size: 13px;
    min-width: 45px;
  }
  .info-value {
    font-size: 13px;
  }
  .exam-card-actions {
    gap: 6px;
  }
  .action-btn {
    min-width: 50px;
    max-width: 70px;
    font-size: 11px;
    padding: 4px 6px;
  }
  .exam-list ::v-deep .el-table {
    font-size: 12px;
    min-width: 600px;
  }
  .exam-list ::v-deep .el-table th,
  .exam-list ::v-deep .el-table td {
    padding: 6px 4px;
    white-space: nowrap;
  }
  .exam-list ::v-deep .el-table__body .el-button + .el-button {
    margin-left: 2px;
    margin-top: 2px;
  }
  .exam-list ::v-deep .el-button--mini {
    padding: 3px 5px;
    font-size: 10px;
    min-width: auto;
  }
  .exam-list ::v-deep .el-table__body-wrapper {
    overflow-x: auto;
  }
  .exam-list ::v-deep .el-table__fixed-right {
    box-shadow: -2px 0 8px rgba(0,0,0,0.1);
  }
}
@media (max-width: 900px) {
  .el-dialog {
    min-width: unset !important;
  }
  .questions-section, .questions-header, .question-item, .question-header {
    flex-direction: column !important;
    gap: 8px;
  }
  .questions-header {
    align-items: flex-start !important;
  }
  .questions-info {
    flex-direction: column !important;
    gap: 5px !important;
  }
  .questions-actions {
    flex-direction: column !important;
    width: 100%;
  }
  .questions-actions .el-button {
    width: 100%;
  }
  .questions-actions .el-button:hover {
    background: #ffb6d5 !important;
    border-color: #ffb6d5 !important;
  }
  .question-score-display {
    flex-direction: column !important;
    gap: 5px !important;
  }
  .questions-header h3 {
    font-size: 18px;
  }
  .question-item {
    padding: 8px;
    font-size: 15px;
  }
  .question-header {
    font-size: 15px;
  }
  .el-form-item__label {
    font-size: 15px !important;
  }
  .el-input, .el-textarea, .el-select, .el-input-number {
    font-size: 15px !important;
    width: 100% !important;
  }
  .dialog-footer .el-button {
    width: 100%;
    margin-top: 8px;
    font-size: 16px;
  }
  .exam-info, .questions-list, .question-detail, .question-title {
    padding: 0 2vw !important;
    font-size: 15px;
  }
  .question-detail {
    padding: 8px !important;
    font-size: 15px;
  }
  .question-title {
    flex-direction: column !important;
    gap: 4px;
    font-size: 15px;
  }
  .questions-list h3 {
    font-size: 18px;
  }
}
@media (max-width: 700px) {
  .statistics-info {
    padding: 0 2vw !important;
  }
  .stat-item {
    padding: 10px !important;
    font-size: 15px;
  }
  .stat-number {
    font-size: 18px !important;
  }
  .stat-label {
    font-size: 13px !important;
  }
  .el-dialog {
    min-width: unset !important;
  }
  .exam-list {
    padding: 5px;
  }
  .exam-list ::v-deep .el-table {
    font-size: 11px;
    min-width: 500px;
  }
  .exam-list ::v-deep .el-table th,
  .exam-list ::v-deep .el-table td {
    padding: 4px 2px;
  }
  .exam-list ::v-deep .el-button--mini {
    padding: 2px 4px;
    font-size: 9px;
  }
  .exam-list ::v-deep .el-table__body .el-button + .el-button {
    margin-left: 1px;
    margin-top: 1px;
  }
  .exam-time {
    font-size: 10px;
    line-height: 1.2;
  }
  .exam-time div {
    margin-bottom: 2px;
  }
  .exam-cards {
    gap: 8px;
  }
  .exam-card {
    padding: 10px;
    border-radius: 10px;
  }
  .exam-card-header {
    margin-bottom: 8px;
    gap: 6px;
  }
  .exam-title {
    font-size: 15px;
  }
  .exam-info-row {
    margin-bottom: 4px;
  }
  .info-label {
    font-size: 12px;
    min-width: 40px;
  }
  .info-value {
    font-size: 12px;
  }
  .exam-card-actions {
    gap: 4px;
  }
  .action-btn {
    min-width: 45px;
    max-width: 65px;
    font-size: 10px;
    padding: 3px 5px;
  }
}
.exam-time {
  font-size: 12px;
  line-height: 1.3;
}
.exam-time div:first-child {
  color: #409eff;
  font-weight: bold;
}
.exam-time div:last-child {
  color: #f56c6c;
  font-weight: bold;
}
.exam-cards {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.exam-card {
  background: #fff;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 4px 16px #f0c1d6cc;
  border: 1px solid #ffe4ec;
  transition: all 0.3s ease;
}
.exam-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px #f0c1d6cc;
}
.exam-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
  gap: 12px;
}
.exam-title {
  font-size: 18px;
  font-weight: bold;
  color: #ff5c8a;
  line-height: 1.3;
  flex: 1;
}
.exam-status {
  flex-shrink: 0;
}
.exam-card-content {
  margin-bottom: 16px;
}
.exam-info-row {
  display: flex;
  margin-bottom: 8px;
  align-items: center;
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
}
.start-time {
  color: #409eff;
}
.end-time {
  color: #f56c6c;
}
.exam-card-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: flex-start;
}
.action-btn {
  flex: 1;
  min-width: 60px;
  max-width: 80px;
  font-size: 12px;
  padding: 6px 8px;
  border-radius: 8px;
  font-weight: bold;
}
</style>
