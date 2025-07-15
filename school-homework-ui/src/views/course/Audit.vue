<template>
  <div class="audit-page">
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h2>课程审核</h2>
          <p>审核待处理的课程申请</p>
        </div>
        <div class="header-actions">
          <el-button @click="goHome" class="back-btn">
            <i class="el-icon-back"></i>
            返回主页
          </el-button>
        </div>
      </div>
    </div>

    <!-- 新增：状态导航栏 -->
    <div class="status-nav">
      <div class="nav-container">
        <div
          class="nav-item"
          :class="{ active: activeStatus === 'PENDING' }"
          @click="handleStatusChange({ name: 'PENDING' })"
        >
          <i class="el-icon-time"></i>
          <span>待审核</span>
          <div v-if="statusCounts.PENDING > 0" class="badge">{{ statusCounts.PENDING }}</div>
        </div>
        <div
          class="nav-item"
          :class="{ active: activeStatus === 'APPROVED' }"
          @click="handleStatusChange({ name: 'APPROVED' })"
        >
          <i class="el-icon-check"></i>
          <span>已通过</span>
          <div v-if="statusCounts.APPROVED > 0" class="badge">{{ statusCounts.APPROVED }}</div>
        </div>
        <div
          class="nav-item"
          :class="{ active: activeStatus === 'REJECTED' }"
          @click="handleStatusChange({ name: 'REJECTED' })"
        >
          <i class="el-icon-close"></i>
          <span>已拒绝</span>
          <div v-if="statusCounts.REJECTED > 0" class="badge">{{ statusCounts.REJECTED }}</div>
        </div>
        <div
          class="nav-item"
          :class="{ active: activeStatus === 'ALL' }"
          @click="handleStatusChange({ name: 'ALL' })"
        >
          <i class="el-icon-menu"></i>
          <span>全部课程</span>
        </div>
      </div>
    </div>

    <el-card class="audit-card">
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
            <div style="display:flex;align-items:center;flex-wrap:wrap;">
              <el-rate
                v-model="scope.row.scoreStar"
                :max="5"
                :allow-half="false"
                :disabled="scope.row.auditStatus !== 'APPROVED'"
                @change="val => handleScoreChange(scope.row, val)"
                show-score
                score-template="{value}星"
                style="vertical-align: middle;"
              />
              <span v-if="scope.row.score" style="margin-left:8px;color:#ffb600;font-weight:bold;white-space:nowrap;">
                {{ Math.round(scope.row.score) }}分
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="auditStatus" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.auditStatus)" class="status-tag">
              {{ getStatusText(scope.row.auditStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240">
          <template slot-scope="scope">
            <el-button size="mini" @click="showDetail(scope.row)" class="detail-btn">详情</el-button>
            <el-button v-if="scope.row.auditStatus==='PENDING'" type="success" size="mini" @click="approve(scope.row)" class="approve-btn">通过</el-button>
            <el-button v-if="scope.row.auditStatus==='PENDING'" type="danger" size="mini" @click="showRejectDialog(scope.row)" class="reject-btn">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空数据提示 -->
      <div v-if="!loading && total === 0" class="empty-tip">
        <i class="el-icon-document"></i>
        <p>暂无{{ getStatusText(activeStatus) }}的课程</p>
      </div>

      <!-- 分页组件 -->
      <div class="pagination-wrapper" v-if="total > 0 && !loading">
        <div class="pagination-info">
          <span class="total-info">共 {{ total }} 门课程</span>
          <span class="page-info">第 {{ page }} / {{ Math.ceil(total / pageSize) }} 页</span>
        </div>

        <div class="pagination-controls">
          <el-button
            :disabled="page <= 1"
            @click="handlePageChange(page - 1)"
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
              @click="handlePageChange(pageNum)"
              :class="['page-btn', { active: pageNum === page }]"
              size="small"
            >
              {{ pageNum }}
            </el-button>
          </div>

          <el-button
            :disabled="page >= Math.ceil(total / pageSize)"
            @click="handlePageChange(page + 1)"
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
    </el-card>

    <!-- 拒绝理由输入对话框 -->
    <el-dialog :visible.sync="rejectDialogVisible" width="500px" title="拒绝课程" class="reject-dialog">
      <div class="reject-form">
        <el-form :model="rejectForm" ref="rejectForm" :rules="rejectRules">
          <el-form-item label="拒绝理由" prop="auditMessage" label-width="80px">
            <el-input
              type="textarea"
              v-model="rejectForm.auditMessage"
              :rows="4"
              placeholder="请输入拒绝理由..."
              maxlength="500"
              show-word-limit
            ></el-input>
          </el-form-item>
        </el-form>
      </div>
      <span slot="footer">
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject" :loading="rejectLoading">确认拒绝</el-button>
      </span>
    </el-dialog>

    <el-dialog
      :visible.sync="detailVisible"
      :width="isMobile ? '90%' : '600px'"
      :title="detailCourse.courseName || '课程详情'"
      v-if="detailVisible"
      class="audit-dialog"
      :close-on-click-modal="false"
      :close-on-press-escape="true"
    >
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
            <span class="detail-value">{{ detailCourse.courseDesc || '无' }}</span>
          </div>
          <div class="detail-item full-width">
            <span class="detail-label">标签：</span>
            <span class="detail-value">{{ detailCourse.courseTags || '无' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">分类：</span>
            <span class="detail-value">{{ detailCourse.categoryName || '无' }}</span>
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
          前往课程页
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getPendingCourses, getCoursesByStatus, auditCourse, updateCourseCategory, toggleRecommend } from '@/api/audit'
import {listCourseCategories} from "@/api/courseCategories";

export default {
  name: 'Audit',
  data() {
    return {
      loading: false,
      courses: [],
      activeStatus: 'PENDING', // 默认显示待审核
      statusCounts: {
        PENDING: 0,
        APPROVED: 0,
        REJECTED: 0
      },
      categories: [], // 改为后端返回的分类列表
      detailVisible: false,
      detailCourse: {},
      rejectDialogVisible: false,
      rejectLoading: false,
      rejectForm: {
        courseId: null,
        auditMessage: ''
      },
      rejectRules: {
        auditMessage: [
          { required: true, message: '请输入拒绝理由', trigger: 'blur' },
          { min: 5, message: '拒绝理由至少5个字符', trigger: 'blur' }
        ]
      },
      // 分页相关
      page: 1,
      pageSize: 10,
      total: 0,
      visiblePages: [],
      jumpPage: '',
      isMobile: false
    }
  },
  mounted() {
    this.checkMobile()
    this.loadCategories() // 先加载分类列表
    this.loadCourses() // 再加载课程数据
  },
  methods: {
    // 检测是否为移动端
    checkMobile() {
      this.isMobile = window.innerWidth <= 768
      window.addEventListener('resize', () => {
        this.isMobile = window.innerWidth <= 768
      })
    },

    // 格式化时间戳
    formatTime(timestamp) {
      if (!timestamp) return '无'
      const date = new Date(timestamp)
      return date.toLocaleString('zh-CN')
    },

    // 获取状态类型
    getStatusType(status) {
      switch (status) {
        case 'PENDING': return 'info'
        case 'APPROVED': return 'success'
        case 'REJECTED': return 'danger'
        default: return 'info'
      }
    },

    // 获取状态文本
    getStatusText(status) {
      switch (status) {
        case 'PENDING': return '待审核'
        case 'APPROVED': return '已通过'
        case 'REJECTED': return '已拒绝'
        case 'ALL': return '全部'
        default: return '未知'
      }
    },

    // 处理状态切换
    handleStatusChange(tab) {
      this.activeStatus = tab.name
      this.page = 1 // 重置到第一页
      this.jumpPage = '' // 清空跳转输入框
      this.courses = [] // 清空当前数据
      this.total = 0 // 重置总数
      this.visiblePages = [] // 清空可见页码
      this.loadCourses() // 重新加载数据
    },

    async loadCourses() {
      this.loading = true
      try {
        // 使用新的API获取分页课程数据
        const response = await getCoursesByStatus(this.activeStatus, this.page, this.pageSize)
        const res = response.data // 正确解包axios响应
        if (res.code === 200) {
          const pageData = res.data
          this.courses = (pageData.records || []).map(course => {
            course.scoreStar = course.score ? course.score / 20 : 0; // 1-5星
            return course;
          });
          this.total = pageData.total || 0
          this.statusCounts = pageData.statusCounts || { PENDING: 0, APPROVED: 0, REJECTED: 0 }
          this.updateVisiblePages()
        } else {
          this.$message.error(res.msg || '获取课程失败')
        }
      } catch (error) {
        console.error('获取课程失败:', error)
        this.courses = []
        this.total = 0
        this.visiblePages = []
        if (error.response) {
          this.$message.error(`请求失败: ${error.response.status} - ${error.response.data?.msg || '未知错误'}`)
        } else if (error.request) {
          this.$message.error('网络连接失败，请检查网络设置')
        } else {
          this.$message.error('获取课程失败')
        }
      } finally {
        this.loading = false
      }
    },

    async loadCategories() {
      // 新增：从后端获取分类列表
      try {
        const res = await listCourseCategories({ page: 1, pageSize: 100 })
        if (res.data && Array.isArray(res.data.data.records)) {
          this.categories = res.data.data.records
        } else {
          this.categories = []
        }
      } catch (e) {
        console.error('加载分类失败:', e)
        this.categories = []
      }
    },

    goHome() {
      this.$router.push('/')
    },

    async approve(row) {
      try {
        const res = await auditCourse({
          courseId: row.courseId,
          auditStatus: 'APPROVED',
          auditMessage: '审核通过'
        })

        if (res.data && (res.data.code === 1 || res.data.code === 200)) {
          this.$message.success(res.data.data || res.data.msg || '审核通过成功')
          // 重新加载当前页数据
          this.loadCourses()
        } else {
          this.$message.error(res.data?.msg || '审核失败')
        }
      } catch (error) {
        console.error('审核失败:', error)
        if (error.response) {
          this.$message.error(`审核失败: ${error.response.status} - ${error.response.data?.msg || '未知错误'}`)
        } else if (error.request) {
          this.$message.error('网络连接失败，请检查网络设置')
        } else {
          this.$message.error('审核失败')
        }
      }
    },

    showRejectDialog(row) {
      this.rejectForm.courseId = row.courseId
      this.rejectForm.auditMessage = ''
      this.rejectDialogVisible = true
    },

    async confirmReject() {
      this.$refs.rejectForm.validate(async (valid) => {
        if (!valid) return

        this.rejectLoading = true
        try {
          const res = await auditCourse({
            courseId: this.rejectForm.courseId,
            auditStatus: 'REJECTED',
            auditMessage: this.rejectForm.auditMessage
          })

          if (res.data && (res.data.code === 1 || res.data.code === 200)) {
            this.$message.success(res.data.data || res.data.msg || '拒绝成功')
            // 重新加载当前页数据
            this.loadCourses()
            this.rejectDialogVisible = false
          } else {
            this.$message.error(res.data?.msg || '拒绝失败')
          }
        } catch (error) {
          console.error('拒绝失败:', error)
          if (error.response) {
            this.$message.error(`拒绝失败: ${error.response.status} - ${error.response.data?.msg || '未知错误'}`)
          } else if (error.request) {
            this.$message.error('网络连接失败，请检查网络设置')
          } else {
            this.$message.error('拒绝失败')
          }
        } finally {
          this.rejectLoading = false
        }
      })
    },

    showDetail(row) {
      this.detailCourse = row
      this.detailVisible = true
    },

    goCourse(id) {
      this.$router.push(`/course/${id}`)
    },

    async changeCategory(row) {
      try {
        const response = await updateCourseCategory({
          courseId: row.courseId,
          categoryId: row.categoryId
        })
        const res = response.data
        if (res.code === 200) {
          this.$message.success('分类修改成功')
          // 重新加载当前页数据
          this.loadCourses()
        } else {
          this.$message.error(res.msg || '分类修改失败')
        }
      } catch (error) {
        console.error('分类修改失败:', error)
        this.$message.error('分类修改失败')
      }
    },

    async handleScoreChange(row, val) {
      // val为1-5星，后端存储为0-100分
      const score = Math.round(val * 20)
      try {
        const response = await toggleRecommend({
          courseId: row.courseId,
          score: score
        })
        const res = response.data // 正确解包
        if (res.code === 200) {
          this.$message.success('评分已更新为' + score + '分')
          // 重新加载当前页数据
          this.loadCourses()
        } else {
          this.$message.error(res.msg || '评分更新失败')
        }
      } catch (error) {
        this.$message.error('评分更新失败')
      }
    },

    getCategoryName(id) {
      const cat = this.categories.find(c => String(c.categoryId) === String(id))
      return cat ? cat.categoryName : ''
    },

    updateVisiblePages() {
      const totalPages = Math.ceil(this.total / this.pageSize)
      this.visiblePages = []

      if (totalPages <= 0) return

      const maxVisible = this.isMobile ? 3 : 10
      let start = Math.max(1, this.page - Math.floor(maxVisible / 2))
      let end = Math.min(totalPages, start + maxVisible - 1)

      if (end - start + 1 < maxVisible) {
        start = Math.max(1, end - maxVisible + 1)
      }

      for (let i = start; i <= end; i++) {
        this.visiblePages.push(i)
      }
    },

    handlePageChange(newPage) {
      this.page = newPage
      this.jumpPage = '' // 清空跳转输入框
      this.loadCourses()
    },

    handleJumpPage() {
      const newPage = parseInt(this.jumpPage)
      if (isNaN(newPage)) {
        this.$message.error('请输入有效的页码')
        return
      }
      if (newPage >= 1 && newPage <= Math.ceil(this.total / this.pageSize)) {
        this.page = newPage
        this.loadCourses()
        this.jumpPage = '' // 清空输入框
      } else {
        this.$message.error('请输入有效的页码')
      }
    }
  }
}
</script>

<style scoped>
.audit-page {
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

/* 新增：状态导航栏样式 */
.status-nav {
  margin-bottom: 20px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 16px #f0c1d6cc;
  overflow: hidden;
}

.nav-container {
  display: flex;
  justify-content: space-around;
  padding: 0;
  background: linear-gradient(135deg, #ffe4ec 0%, #ffd6e6 100%);
}

.nav-item {
  flex: 1;
  cursor: pointer;
  padding: 16px 12px;
  text-align: center;
  transition: all 0.3s ease;
  position: relative;
  color: #ff5c8a;
  font-weight: 500;
  border-bottom: 3px solid transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
}

.nav-item.active {
  background: #fff;
  color: #ff5c8a;
  border-bottom: 3px solid #ff5c8a;
  box-shadow: 0 4px 12px rgba(255, 92, 138, 0.2);
  font-weight: bold;
}

.nav-item i {
  font-size: 18px;
  margin-right: 4px;
}

.nav-item span {
  font-size: 14px;
  font-weight: inherit;
}

.badge {
  background: #ff5c8a;
  color: white;
  border-radius: 12px;
  padding: 2px 8px;
  font-size: 12px;
  font-weight: bold;
  min-width: 20px;
  text-align: center;
  box-shadow: 0 2px 4px rgba(255, 92, 138, 0.3);
}

.audit-card {
  margin-bottom: 20px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 16px #f0c1d6cc;
  border: none;
  overflow: hidden;
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
.audit-table ::v-deep .el-table__body tr:hover > td {
  background: #fff5f8;
}
.audit-table ::v-deep .el-table__body td {
  border-bottom: 1px solid #ffe4ec;
}
.category-select {
  border-radius: 8px;
}
.recommend-btn {
  color: #ffb600 !important;
}
.status-tag {
  border-radius: 8px;
  font-weight: bold;
}
.detail-btn {
  background: #ffb6d5 !important;
  border-color: #ffb6d5 !important;
  color: #fff !important;
  font-weight: bold;
  border-radius: 8px;
  margin-right: 8px;
}
.detail-btn:hover {
  background: #ff5c8a !important;
  border-color: #ff5c8a !important;
}
.approve-btn {
  border-radius: 8px;
  font-weight: bold;
  margin-right: 8px;
}
.reject-btn {
  border-radius: 8px;
  font-weight: bold;
}
.course-detail {
  padding: 20px 0;
}
.course-title {
  font-size: 18px;
  font-weight: bold;
  color: #ff5c8a;
  margin-bottom: 15px;
  text-align: center;
  padding-bottom: 10px;
  border-bottom: 2px solid #ffe4ec;
}
.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 20px;
}
.detail-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 8px 12px;
  background: #fafafa;
  border-radius: 8px;
  border-left: 3px solid #ffb6d5;
}
.detail-item.full-width {
  grid-column: 1 / -1;
}
.detail-label {
  font-weight: bold;
  color: #666;
  min-width: 80px;
  flex-shrink: 0;
  font-size: 13px;
}
.detail-value {
  color: #333;
  flex: 1;
  word-break: break-word;
  line-height: 1.4;
}
.reject-message {
  color: #e74c3c;
  font-style: italic;
  background: #fdf2f2;
  padding: 8px;
  border-radius: 4px;
  border-left: 3px solid #e74c3c;
}
.cover-container {
  text-align: center;
  margin-top: 20px;
  padding: 15px;
  background: #fafafa;
  border-radius: 12px;
  border: 2px dashed #ffb6d5;
}
.course-cover {
  width: 100%;
  max-width: 320px;
  border-radius: 8px;
  border: 2px solid #ffb6d5;
  box-shadow: 0 4px 12px rgba(255, 182, 213, 0.3);
}
.audit-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(90deg, #ffe4ec 0%, #ffd6e6 100%);
  color: #ff5c8a;
  font-weight: bold;
  border-radius: 12px 12px 0 0;
}
.audit-dialog ::v-deep .el-dialog__body {
  padding: 20px;
  max-height: 70vh;
  overflow-y: auto;
}
.audit-dialog ::v-deep .el-dialog__footer {
  background: #fafafa;
  border-radius: 0 0 12px 12px;
  padding: 15px 20px;
}
.dialog-footer {
  display: flex;
  justify-content: center;
  gap: 12px;
  width: 100%;
}
.close-btn {
  background: #f5f5f5 !important;
  border-color: #e4e7ed !important;
  color: #666 !important;
  font-weight: 500;
  border-radius: 8px;
  padding: 10px 20px;
}
.close-btn:hover {
  background: #e4e7ed !important;
  border-color: #c0c4cc !important;
  color: #333 !important;
}
.go-course-btn {
  background: #ffb6d5 !important;
  border-color: #ffb6d5 !important;
  color: #fff !important;
  font-weight: bold;
  border-radius: 8px;
  padding: 10px 20px;
}
.go-course-btn:hover {
  background: #ff5c8a !important;
  border-color: #ff5c8a !important;
}
.empty-tip {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}
.empty-tip i {
  font-size: 48px;
  margin-bottom: 10px;
  display: block;
}
.empty-tip p {
  margin: 0;
  font-size: 16px;
}
.pagination-wrapper {
  margin-top: 20px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 16px #f0c1d6cc;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}
.pagination-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-direction: row;
}
.total-info {
  font-size: 14px;
  color: #666;
  font-weight: 500;
  white-space: nowrap;
}
.page-info {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
}
.pagination-controls {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.page-numbers {
  display: flex;
  gap: 4px;
}
.jump-to-page {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: 16px;
}
.page-btn {
  background: #ffb6d5 !important;
  border-color: #ffb6d5 !important;
  color: #fff !important;
  font-weight: bold;
  border-radius: 8px;
  padding: 8px 12px;
  min-width: 36px;
  height: 36px;
  line-height: 20px;
}
.page-btn:hover {
  background: #ff5c8a !important;
  border-color: #ff5c8a !important;
}
.page-btn.active {
  background: #ff5c8a !important;
  border-color: #ff5c8a !important;
}
.page-btn:disabled {
  background: #f5f5f5 !important;
  border-color: #e4e7ed !important;
  color: #c0c4cc !important;
}
.prev-btn i, .next-btn i {
  margin: 0 4px;
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
.reject-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(90deg, #ffe4ec 0%, #ffd6e6 100%);
  color: #ff5c8a;
  font-weight: bold;
}
.reject-form {
  padding: 20px 0;
}

@media (max-width: 768px) {
  .audit-page {
    padding: 8px;
  }
  .header-content {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }
  .title-section h2 {
    font-size: 24px;
  }
  .back-btn {
    width: 100%;
    margin-bottom: 10px;
  }

  /* 移动端导航栏样式 */
  .nav-container {
    flex-direction: column;
    gap: 2px;
  }

  .nav-item {
    padding: 12px 8px;
    border-bottom: none;
    border-radius: 8px;
    margin: 2px 8px;
  }

  .nav-item.active {
    border-bottom: none;
    box-shadow: 0 2px 8px rgba(255, 92, 138, 0.3);
  }

  .nav-item i {
    font-size: 16px;
  }

  .nav-item span {
    font-size: 13px;
  }

  .badge {
    font-size: 11px;
    padding: 1px 6px;
  }

  .audit-card {
    border-radius: 10px;
    padding: 8px;
  }
  .audit-table ::v-deep .el-table {
    font-size: 12px;
  }
  .audit-table ::v-deep .el-table th,
  .audit-table ::v-deep .el-table td {
    padding: 8px 4px;
  }
  .detail-btn, .approve-btn, .reject-btn {
    margin-bottom: 5px;
    width: 100%;
  }

  /* 移动端分页样式 */
  .pagination-wrapper {
    flex-direction: column;
    gap: 12px;
  }
  .pagination-info {
    margin-bottom: 0;
    text-align: center;
    width: 100%;
  }
  .pagination-controls {
    width: 100%;
    justify-content: center;
  }
  .page-numbers {
    display: none;
  }
  .jump-to-page {
    display: none;
  }

  /* 移动端详情对话框样式 */
  .audit-dialog ::v-deep .el-dialog {
    margin: 5vh auto !important;
    max-width: 95vw;
  }

  .audit-dialog ::v-deep .el-dialog__body {
    padding: 15px;
    max-height: 60vh;
  }

  .audit-dialog ::v-deep .el-dialog__footer {
    padding: 10px 15px;
  }

  .course-title {
    font-size: 16px;
    margin-bottom: 12px;
    padding-bottom: 8px;
  }

  .detail-grid {
    grid-template-columns: 1fr;
    gap: 8px;
    margin-bottom: 15px;
  }

  .detail-item {
    padding: 6px 10px;
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }

  .detail-label {
    min-width: auto;
    font-size: 12px;
    color: #999;
  }

  .detail-value {
    font-size: 14px;
    width: 100%;
  }

  .reject-message {
    padding: 6px;
    font-size: 13px;
  }

  .cover-container {
    margin-top: 15px;
    padding: 10px;
  }

  .course-cover {
    max-width: 280px;
  }

  .dialog-footer {
    flex-direction: column;
    gap: 8px;
  }

  .close-btn, .go-course-btn {
    width: 100%;
    padding: 12px 20px;
    font-size: 14px;
  }
}
</style>
