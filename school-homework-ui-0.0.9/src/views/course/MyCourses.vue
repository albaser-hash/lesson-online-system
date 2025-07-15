<template>
  <div class="my-courses-page">
    <div class="page-header">
      <h1 class="page-title">我的课程</h1>
      <p class="page-subtitle">继续您的学习之旅</p>
    </div>

    <!-- 学习统计 -->
    <div class="stats-section">
      <el-row :gutter="20">
        <el-col :xs="12" :sm="6">
          <div class="stat-card">
            <div class="stat-icon">
              <i class="el-icon-reading"></i>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ stats.totalCourses || 0 }}</div>
              <div class="stat-label">总课程数</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card">
            <div class="stat-icon">
              <i class="el-icon-video-play"></i>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ stats.totalChapters || 0 }}</div>
              <div class="stat-label">总章节数</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card">
            <div class="stat-icon">
              <i class="el-icon-time"></i>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ formatMinutes(stats.totalStudyTime) }}</div>
              <div class="stat-label">学习时长</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card">
            <div class="stat-icon">
              <i class="el-icon-trophy"></i>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ stats.completedCourses || 0 }}</div>
              <div class="stat-label">已完成</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 课程列表 -->
    <div class="courses-section">
      <div class="section-header">
        <h2>我的课程</h2>
        <div class="header-actions">
          <el-button type="primary" size="small" @click="goHome">返回主页</el-button>
          <el-radio-group v-model="filterType" size="small">
            <el-radio-button label="all">全部</el-radio-button>
            <el-radio-button label="learning">学习中</el-radio-button>
            <el-radio-button label="completed">已完成</el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <el-row :gutter="20" class="courses-grid">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="course in filteredCourses" :key="course.courseId" class="course-col">
          <div class="course-card" @click="viewCourse(course)">
            <div class="course-cover">
              <img :src="getCourseCover(course.coverImage)" :alt="course.courseName" />
              <div class="course-overlay">
                <el-button type="primary" size="small" round>继续学习</el-button>
              </div>
              <div class="course-progress-badge" v-if="course.progress > 0">
                {{ course.progress }}%
              </div>
            </div>
            <div class="course-info">
              <h3 class="course-title">{{ course.courseName }}</h3>
              <p class="course-desc">{{ course.courseDesc }}</p>
              <div class="course-meta">
                <span class="teacher-name">
                  <i class="el-icon-user"></i>
                  {{ course.teacherName }}
                </span>
                <span class="category-name">
                  <i class="el-icon-folder"></i>
                  {{ course.categoryName }}
                </span>
              </div>
              <div class="progress-section">
                <div class="progress-info">
                  <span>学习进度</span>
                  <span>{{ course.completedChapters }}/{{ course.totalChapters }} 章节</span>
                </div>
                <el-progress
                  :percentage="course.progress"
                  :stroke-width="8"
                  :show-text="false"
                  color="#ffb6d5"
                />
              </div>
              <div class="course-stats">
                <span class="study-time">
                  <i class="el-icon-time"></i>
                  {{ formatMinutes(course.studyTime) }}
                </span>
                <span class="last-study" v-if="course.lastStudyTime">
                  上次学习: {{ formatTime(course.lastStudyTime) }}
                </span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 空状态 -->
      <div v-if="filteredCourses.length === 0" class="empty-state">
        <div class="empty-icon">
          <i class="el-icon-reading"></i>
        </div>
        <h3>暂无课程</h3>
        <p>您还没有购买任何课程，快去探索精彩课程吧！</p>
        <el-button type="primary" @click="goToCourseList">浏览课程</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import { getStudyStats, getAllMyCourses } from '@/api/myCourses'

export default {
  name: 'MyCourses',
  data() {
    return {
      filterType: 'all',
      stats: {},
      myCourses: []
    }
  },
  computed: {
    ...mapState({
      userType: state => state.user.userType,
      userId: state => state.user.id
    }),
    filteredCourses() {
      if (this.filterType === 'all') {
        return this.myCourses
      } else if (this.filterType === 'learning') {
        return this.myCourses.filter(course => course.status === 'learning')
      } else if (this.filterType === 'completed') {
        return this.myCourses.filter(course => course.status === 'completed')
      }
      return this.myCourses
    }
  },
  mounted() {
    this.checkStudentPermission()
    this.loadData()
  },
  methods: {
    checkStudentPermission() {
      if (this.userType && this.userType !== 'STUDENT') {
        this.$message.error('只有学生可以访问此页面')
        this.$router.push('/')
      }
    },
    async loadData() {
      try {
        // 并行加载统计信息和课程列表
        const [statsRes, coursesRes] = await Promise.all([
          getStudyStats(),
          getAllMyCourses()
        ])

        if (statsRes.data.code === 200) {
          this.stats = statsRes.data.data
        } else {
          this.stats = {}
        }

        if (coursesRes.data.code === 200) {
          this.myCourses = coursesRes.data.data
        } else {
          this.myCourses = []
        }
      } catch (error) {
        console.error('加载数据失败:', error)
        this.$message.error('加载数据失败，请稍后重试')
        this.stats = {}
        this.myCourses = []
      }
    },
    getCourseCover(coverImage) {
      if (!coverImage || !/^https?:\/\//.test(coverImage)) {
        return require('@/assets/images/default-course.jpg')
      }
      return coverImage
    },
    formatTime(timeStr) {
      if (!timeStr) return ''

      const date = new Date(timeStr)
      const now = new Date()
      const diff = now - date
      const days = Math.floor(diff / (1000 * 60 * 60 * 24))

      if (days === 0) {
        return '今天'
      } else if (days === 1) {
        return '昨天'
      } else if (days < 7) {
        return `${days}天前`
      } else {
        return date.toLocaleDateString()
      }
    },
    formatMinutes(minutes) {
      if (!minutes || isNaN(minutes)) return '0分钟';
      const h = Math.floor(minutes / 60);
      const m = minutes % 60;
      if (h > 0 && m > 0) return `${h}小时${m}分钟`;
      if (h > 0) return `${h}小时`;
      return `${m}分钟`;
    },
    viewCourse(course) {
      this.$router.push(`/course/${course.courseId}`)
    },
    goToCourseList() {
      this.$router.push('/')
    },
    goHome() {
      this.$router.push('/')
    }
  }
}
</script>

<style scoped>
.my-courses-page {
  padding: 30px;
  max-width: 1200px;
  margin: 0 auto;
  background: #f8f9fa;
  min-height: 100vh;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
}

.page-title {
  font-size: 2.5rem;
  color: #ff5c8a;
  font-weight: bold;
  margin: 0 0 10px 0;
}

.page-subtitle {
  font-size: 1.1rem;
  color: #666;
  margin: 0;
}

.stats-section {
  margin-bottom: 40px;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  text-align: center;
  box-shadow: 0 4px 12px rgba(255, 182, 213, 0.1);
  border: 1px solid #ffe4ec;
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ffb6d5, #ff5c8a);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
}

.stat-content {
  flex: 1;
  text-align: left;
}

.stat-number {
  font-size: 2rem;
  font-weight: bold;
  color: #ff5c8a;
  line-height: 1;
}

.stat-label {
  font-size: 0.9rem;
  color: #666;
  margin-top: 4px;
}

.courses-section {
  background: white;
  border-radius: 20px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  flex-wrap: wrap;
  gap: 16px;
}

.section-header h2 {
  font-size: 1.5rem;
  color: #333;
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.courses-grid {
  margin-bottom: 20px;
}

.course-col {
  margin-bottom: 24px;
}

.course-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  cursor: pointer;
  border: 1px solid #f0f0f0;
}

.course-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(255, 182, 213, 0.2);
  border-color: #ffb6d5;
}

.course-cover {
  position: relative;
  height: 160px;
  overflow: hidden;
}

.course-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.course-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.course-card:hover .course-overlay {
  opacity: 1;
}

.course-progress-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: #ff5c8a;
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: bold;
}

.course-info {
  padding: 20px;
}

.course-title {
  font-size: 1.1rem;
  font-weight: bold;
  color: #333;
  margin: 0 0 8px 0;
  line-height: 1.3;
}

.course-desc {
  font-size: 0.9rem;
  color: #666;
  margin: 0 0 12px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.course-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  font-size: 0.85rem;
  color: #888;
}

.course-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.progress-section {
  margin-bottom: 16px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  font-size: 0.85rem;
  color: #666;
  margin-bottom: 8px;
}

.course-stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.8rem;
  color: #999;
}

.study-time {
  display: flex;
  align-items: center;
  gap: 4px;
}

.last-study {
  text-align: right;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.empty-icon {
  font-size: 4rem;
  color: #ddd;
  margin-bottom: 20px;
}

.empty-state h3 {
  font-size: 1.5rem;
  color: #666;
  margin: 0 0 10px 0;
}

.empty-state p {
  font-size: 1rem;
  margin: 0 0 20px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .my-courses-page {
    padding: 20px;
  }

  .page-title {
    font-size: 2rem;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .header-actions {
    width: 100%;
    justify-content: space-between;
  }

  .stat-card {
    padding: 16px;
  }

  .stat-icon {
    width: 50px;
    height: 50px;
    font-size: 20px;
  }

  .stat-number {
    font-size: 1.5rem;
  }
}

/* 让primary按钮变粉色，与首页一致 */
::v-deep .el-button--primary {
  background: #ffb6d5 !important;
  border-color: #ffb6d5 !important;
}

::v-deep .el-button--primary:hover,
::v-deep .el-button--primary:focus {
  background: #ff5c8a !important;
  border-color: #ff5c8a !important;
}

::v-deep .el-radio-button__inner {
  border-color: #ffb6d5 !important;
  color: #ffb6d5 !important;
}

::v-deep .el-radio-button__orig-radio:checked + .el-radio-button__inner {
  background: #ffb6d5 !important;
  border-color: #ffb6d5 !important;
  color: white !important;
}
</style>
