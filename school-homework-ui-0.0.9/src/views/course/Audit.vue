<template>
  <div class="audit-page">
    <div class="page-header">
      <h1 class="page-title">课程审核</h1>
      <p class="page-subtitle">管理课程审核状态</p>
    </div>
    <el-card class="audit-card">
      <div slot="header" class="card-header">
        <span class="header-title">待审核课程</span>
        <el-button type="primary" size="small" @click="goHome">返回主页</el-button>
      </div>
      <el-table :data="courses" border style="width:100%;margin-bottom:20px;" class="audit-table" v-loading="loading">
        <el-table-column prop="courseName" label="课程名称" />
        <el-table-column prop="teacherName" label="教师" />
        <el-table-column prop="price" label="价格" width="100">
          <template slot-scope="scope">
            ￥{{ scope.row.price ? scope.row.price.toFixed(2) : '0.00' }}
          </template>
        </el-table-column>
        <el-table-column label="分类" width="120">
          <template slot-scope="scope">
            <template v-if="scope.row.auditStatus === 'APPROVED'">
              <el-select v-model="scope.row.categoryId" placeholder="请选择" size="mini" @change="changeCategory(scope.row)" class="category-select">
                <el-option v-for="cat in categories" :key="cat.categoryId" :label="cat.categoryName" :value="cat.categoryId" />
              </el-select>
            </template>
            <template v-else>
              <span>{{ scope.row.categoryName || getCategoryName(scope.row.categoryId) || ('ID:' + scope.row.categoryId) }}</span>
            </template>
          </template>
        </el-table-column>
        <el-table-column label="推荐" width="180">
          <template slot-scope="scope">
            <el-switch
              v-model="scope.row.isRecommend"
              @change="toggleRecommend(scope.row)"
              active-color="#ffb6d5"
              inactive-color="#dcdfe6"
            />
          </template>
        </el-table-column>
        <el-table-column label="审核状态" width="120">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.auditStatus)" size="small">
              {{ getStatusText(scope.row.auditStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center">
          <template slot-scope="scope">
            <el-button
              v-if="scope.row.auditStatus === 'PENDING'"
              type="success"
              size="mini"
              @click="approveCourse(scope.row)"
              class="approve-btn"
            >
              通过
            </el-button>
            <el-button
              v-if="scope.row.auditStatus === 'PENDING'"
              type="danger"
              size="mini"
              @click="rejectCourse(scope.row)"
              class="reject-btn"
            >
              拒绝
            </el-button>
            <el-button
              type="primary"
              size="mini"
              @click="viewDetail(scope.row)"
              class="detail-btn"
            >
              详情
            </el-button>
            <el-button
              v-if="scope.row.auditStatus === 'REJECTED'"
              type="warning"
              size="mini"
              @click="viewRejectReason(scope.row)"
              class="reason-btn"
            >
              拒绝理由
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <!-- 详情对话框 -->
    <el-dialog :visible.sync="detailVisible" title="课程详情" width="60%" class="detail-dialog">
      <div v-if="detailCourse.courseId" class="course-detail">
        <div class="course-title">{{ detailCourse.courseName }}</div>
        <div class="detail-grid">
          <div class="detail-item">
            <span class="detail-label">课程ID：</span>
            <span class="detail-value">{{ detailCourse.courseId }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">教师：</span>
            <span class="detail-value">{{ detailCourse.teacherName }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">价格：</span>
            <span class="detail-value">￥{{ detailCourse.price ? detailCourse.price.toFixed(2) : '0.00' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">状态：</span>
            <span class="detail-value">
              <el-tag :type="getStatusType(detailCourse.auditStatus)" size="small">
                {{ getStatusText(detailCourse.auditStatus) }}
              </el-tag>
            </span>
          </div>
          <div class="detail-item full-width">
            <span class="detail-label">简介：</span>
            <span class="detail-value">{{ detailCourse.courseDesc || '暂无' }}</span>
          </div>
          <div class="detail-item full-width">
            <span class="detail-label">标签：</span>
            <span class="detail-value">{{ detailCourse.courseTags || '暂无' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">分类：</span>
            <span class="detail-value">{{ detailCourse.categoryName || '暂无' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">学生数：</span>
            <span class="detail-value">{{ detailCourse.studentCount || 0 }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">评分：</span>
            <span class="detail-value">{{ detailCourse.score || 0 }}</span>
          </div>
          <div class="detail-item full-width">
            <span class="detail-label">创建时间：</span>
            <span class="detail-value">{{ formatTime(detailCourse.createTime) }}</span>
          </div>
          <div v-if="detailCourse.auditMessage" class="detail-item full-width">
            <span class="detail-label">拒绝理由：</span>
            <span class="detail-value reject-message">{{ detailCourse.auditMessage }}</span>
          </div>
        </div>
        <div v-if="detailCourse.coverImage" class="cover-container">
          <img :src="detailCourse.coverImage" class="course-cover" />
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="detailVisible=false" class="close-btn">关闭</el-button>
        <el-button
          type="primary"
          @click="goCourse(detailCourse.courseId)"
          v-if="detailCourse.courseId"
          class="go-course-btn"
        >
          前往课程
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>
export default {
  name: 'Audit',
  data() {
    return {
      loading: false,
      courses: [
        {
          courseId: 1,
          courseName: 'Vue.js入门到精通',
          teacherName: '张老师',
          price: 99.00,
          categoryId: 1,
          categoryName: '前端开发',
          score: 95,
          studentCount: 1250,
          courseDesc: '从零开始学习Vue.js框架，掌握前端开发技能',
          courseTags: 'Vue,前端,JavaScript',
          coverImage: 'https://picsum.photos/300/200?random=1',
          auditStatus: 'PENDING',
          isRecommend: false,
          createTime: '2024-01-15 10:30:00'
        },
        {
          courseId: 2,
          courseName: 'React全栈开发',
          teacherName: '李老师',
          price: 129.00,
          categoryId: 1,
          categoryName: '前端开发',
          score: 92,
          studentCount: 890,
          courseDesc: '学习React框架，掌握现代前端开发技能',
          courseTags: 'React,前端,JavaScript',
          coverImage: 'https://picsum.photos/300/200?random=2',
          auditStatus: 'APPROVED',
          isRecommend: true,
          createTime: '2024-01-14 14:20:00'
        },
        {
          courseId: 3,
          courseName: 'Java核心技术',
          teacherName: '王老师',
          price: 159.00,
          categoryId: 2,
          categoryName: '后端开发',
          score: 88,
          studentCount: 756,
          courseDesc: '深入理解Java面向对象编程和核心概念',
          courseTags: 'Java,后端,编程',
          coverImage: 'https://picsum.photos/300/200?random=3',
          auditStatus: 'REJECTED',
          auditMessage: '课程内容需要进一步完善，请补充更多实践案例',
          isRecommend: false,
          createTime: '2024-01-13 09:15:00'
        }
      ],
      categories: [
        { categoryId: 1, categoryName: '前端开发' },
        { categoryId: 2, categoryName: '后端开发' },
        { categoryId: 3, categoryName: '数据库' },
        { categoryId: 4, categoryName: '编程语言' }
      ],
      detailVisible: false,
      detailCourse: {}
    }
  },
  mounted() {
    console.log('课程审核页面使用假数据')
  },
  methods: {
    goHome() {
      this.$router.push('/')
    },
    getStatusType(status) {
      switch (status) {
        case 'APPROVED': return 'success'
        case 'PENDING': return 'warning'
        case 'REJECTED': return 'danger'
        default: return 'info'
      }
    },
    getStatusText(status) {
      switch (status) {
        case 'APPROVED': return '已通过'
        case 'PENDING': return '待审核'
        case 'REJECTED': return '已拒绝'
        default: return '未知'
      }
    },
    getCategoryName(categoryId) {
      const category = this.categories.find(cat => cat.categoryId === categoryId)
      return category ? category.categoryName : ''
    },
    formatTime(timeStr) {
      if (!timeStr) return ''
      const date = new Date(timeStr)
      return date.toLocaleString()
    },
    async approveCourse(course) {
      try {
        await this.$confirm('确定要通过这个课程吗？', '提示', { type: 'warning' })
        course.auditStatus = 'APPROVED'
        this.$message.success('课程审核通过（假数据）')
      } catch (e) {
        if (e !== 'cancel') {
          this.$message.error('操作失败')
        }
      }
    },
    async rejectCourse(course) {
      try {
        const { value: reason } = await this.$prompt('请输入拒绝理由', '拒绝课程', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          inputType: 'textarea',
          inputPlaceholder: '请输入拒绝理由...'
        })
        if (reason) {
          course.auditStatus = 'REJECTED'
          course.auditMessage = reason
          this.$message.success('课程已拒绝（假数据）')
        }
      } catch (e) {
        if (e !== 'cancel') {
          this.$message.error('操作失败')
        }
      }
    },
    toggleRecommend(course) {
      this.$message.success(`推荐状态已${course.isRecommend ? '开启' : '关闭'}（假数据）`)
    },
    changeCategory(course) {
      const category = this.categories.find(cat => cat.categoryId === course.categoryId)
      if (category) {
        course.categoryName = category.categoryName
        this.$message.success('分类已更新（假数据）')
      }
    },
    viewDetail(course) {
      this.detailCourse = { ...course }
      this.detailVisible = true
    },
    viewRejectReason(course) {
      this.$alert(course.auditMessage || '暂无拒绝理由', '拒绝理由', {
        confirmButtonText: '确定'
      })
    },
    goCourse(courseId) {
      this.$router.push(`/course/${courseId}`)
    }
  }
}
</script>
<style scoped>
.audit-page {
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
.audit-card {
  background: white;
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-title {
  font-size: 1.5rem;
  color: #333;
  font-weight: bold;
}
.audit-table {
  border-radius: 12px;
  overflow: hidden;
}
.audit-table ::v-deep .el-table__header-wrapper {
  background: linear-gradient(90deg, #ffe4ec 0%, #ffd6e6 100%);
}
.audit-table ::v-deep .el-table__header th {
  background: transparent;
  color: #ff5c8a;
  font-weight: bold;
  border-bottom: 2px solid #ffb6d5;
}
.audit-table ::v-deep .el-table__body td {
  border-bottom: 1px solid #ffe4ec;
}
.approve-btn {
  background: #67c23a !important;
  border-color: #67c23a !important;
  color: #fff !important;
  margin-right: 8px;
}
.reject-btn {
  background: #f56c6c !important;
  border-color: #f56c6c !important;
  color: #fff !important;
  margin-right: 8px;
}
.detail-btn {
  background: #ffb6d5 !important;
  border-color: #ffb6d5 !important;
  color: #fff !important;
  margin-right: 8px;
}
.reason-btn {
  background: #e6a23c !important;
  border-color: #e6a23c !important;
  color: #fff !important;
}
.detail-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(90deg, #ffe4ec 0%, #ffd6e6 100%);
  color: #ff5c8a;
  font-weight: bold;
}
.course-detail {
  padding: 20px 0;
}
.course-title {
  font-size: 1.5rem;
  color: #ff5c8a;
  font-weight: bold;
  margin-bottom: 20px;
  text-align: center;
}
.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 20px;
}
.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #ffb6d5;
}
.detail-item.full-width {
  grid-column: 1 / -1;
}
.detail-label {
  font-weight: bold;
  color: #666;
  min-width: 80px;
}
.detail-value {
  color: #333;
  flex: 1;
}
.reject-message {
  color: #f56c6c;
  font-weight: bold;
}
.cover-container {
  text-align: center;
  margin-top: 20px;
}
.course-cover {
  max-width: 300px;
  max-height: 200px;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}
.close-btn {
  background: #909399 !important;
  border-color: #909399 !important;
  color: #fff !important;
}
.go-course-btn {
  background: #ffb6d5 !important;
  border-color: #ffb6d5 !important;
  color: #fff !important;
}
.go-course-btn:hover {
  background: #ff5c8a !important;
  border-color: #ff5c8a !important;
}
@media (max-width: 768px) {
  .audit-page {
    padding: 20px;
  }
  .page-title {
    font-size: 2rem;
  }
  .detail-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  .detail-item {
    padding: 10px;
  }
  .detail-label {
    min-width: 60px;
    font-size: 0.9rem;
  }
  .course-cover {
    max-width: 100%;
    max-height: 150px;
  }
}
</style>
