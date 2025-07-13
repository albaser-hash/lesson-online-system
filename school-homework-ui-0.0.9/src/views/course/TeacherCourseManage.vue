<template>
  <div class="teacher-course-manage-page">
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h2>我的课程管理</h2>
          <p>管理您创建的所有课程</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" size="small" @click="createNewCourse" class="create-btn">
            <i class="el-icon-plus"></i>
            新建课程
          </el-button>
          <el-button @click="goHome" class="back-btn">
            <i class="el-icon-back"></i>
            返回主页
          </el-button>
        </div>
      </div>
    </div>

    <el-card class="course-manage-card">
      <!-- 课程列表 -->
      <el-table :data="myCourses" border style="width:100%;margin-bottom:20px;" v-loading="loading" class="course-table">
        <el-table-column prop="courseId" label="序号" type="index" width="80" />
        <el-table-column prop="courseName" label="课程名称" />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="price" label="价格" width="100">
          <template slot-scope="scope">
            ￥{{ scope.row.price }}
            <span v-if="scope.row.originalPrice" class="original-price">
              ￥{{ scope.row.originalPrice }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="studentCount" label="学生数" width="100" />
        <el-table-column prop="score" label="评分" width="100" />
        <el-table-column prop="auditStatus" label="审核状态" width="140">
          <template slot-scope="scope">
            <div class="status-container">
              <el-tag :type="getAuditStatusType(scope.row.auditStatus)" class="status-tag">
                {{ getAuditStatusText(scope.row.auditStatus) }}
              </el-tag>
              <el-button
                v-if="scope.row.auditStatus === 'REJECTED'"
                size="mini"
                type="warning"
                @click="viewRejectReason(scope.row)"
                class="reject-reason-btn"
              >
                拒绝理由
              </el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320">
          <template slot-scope="scope">
            <div class="action-buttons">
              <el-button size="mini" @click="viewCourse(scope.row)" class="view-btn">查看</el-button>
              <el-button size="mini" type="primary" @click="editCourse(scope.row)" class="edit-btn">编辑</el-button>
              <el-button size="mini" type="success" @click="manageChapters(scope.row)" class="chapter-btn">章节</el-button>
              <el-button size="mini" type="danger" @click="deleteCourse(scope.row)" class="delete-btn">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页栏 -->
      <div class="pagination-container">
        <div class="pagination-wrapper">
          <div class="pagination-info">
            <span class="total-info">共 {{ total }} 条</span>
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
                v-for="p in visiblePages"
                :key="p"
                @click="handlePageChange(p)"
                :class="['page-btn', { active: p === page }]"
                size="small"
                :disabled="p === '...'"
              >
                {{ p }}
              </el-button>
            </div>
            <div class="page-numbers mobile" v-else>
              <el-button
                v-for="p in mobileVisiblePages"
                :key="p"
                @click="handlePageChange(p)"
                :class="['page-btn', { active: p === page }]"
                size="small"
                :disabled="p === '...'"
              >
                {{ p }}
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
          </div>
        </div>
      </div>

      <!-- 空数据提示 -->
      <div v-if="!loading && myCourses.length === 0" class="empty-state">
        <i class="el-icon-folder"></i>
        <p>暂无课程，点击右上角"新建课程"开始创建您的第一个课程</p>
      </div>
    </el-card>

    <!-- 新建/编辑课程弹窗 -->
    <el-dialog :visible.sync="courseDialogVisible" :title="courseForm.courseId ? '编辑课程' : '新建课程'" :width="courseDialogWidth" class="course-dialog">
      <el-form :model="courseForm" :rules="courseRules" ref="courseForm" label-width="100px">
        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model="courseForm.courseName" maxlength="100" />
        </el-form-item>
        <el-form-item label="课程简介" prop="courseDesc">
          <el-input type="textarea" v-model="courseForm.courseDesc" maxlength="200" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="courseForm.categoryId" placeholder="请选择分类">
            <el-option v-for="cat in categories" :key="cat.categoryId" :label="cat.categoryName" :value="cat.categoryId" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="courseForm.price" :min="0" :step="1" />
        </el-form-item>
        <el-form-item label="原价" prop="originalPrice">
          <el-input-number v-model="courseForm.originalPrice" :min="0" :step="1" />
        </el-form-item>
        <el-form-item label="封面图片">
          <el-upload
            class="cover-uploader"
            action="#"
            :show-file-list="false"
            :before-upload="beforeCoverUpload"
            :on-change="handleCoverChange"
            :auto-upload="false"
            :http-request="() => {}"
          >
            <img v-if="courseForm.coverImage" :src="courseForm.coverImage" class="cover-img" />
            <i v-else class="el-icon-plus cover-uploader-icon"></i>
          </el-upload>
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="tagsInput" placeholder="用逗号分隔，如：数学,基础" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="courseDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCourse" class="save-btn">保存</el-button>
      </div>
    </el-dialog>

    <!-- 拒绝理由查看对话框 -->
    <el-dialog
      :visible.sync="rejectReasonDialogVisible"
      :width="isMobile ? '90%' : '500px'"
      title="拒绝理由详情"
      class="reject-reason-dialog"
      :close-on-click-modal="false"
      :close-on-press-escape="true"
    >
      <div class="reject-reason-content">
        <div class="course-info">
          <div class="course-title">{{ rejectReasonData.courseName }}</div>
          <div class="reject-time" v-if="rejectReasonData.rejectTime">
            拒绝时间：{{ formatDateTime(rejectReasonData.rejectTime) }}
          </div>
        </div>

        <div class="reason-section">
          <div class="reason-label">拒绝理由：</div>
          <div class="reason-content">
            {{ rejectReasonData.auditMessage || '暂无拒绝理由' }}
          </div>
        </div>

        <div class="suggestion-section">
          <div class="suggestion-title">
            <i class="el-icon-lightbulb"></i>
            修改建议
          </div>
          <div class="suggestion-content">
            <ul>
              <li>请根据拒绝理由修改课程内容</li>
              <li>确保课程信息完整且符合平台规范</li>
              <li>修改完成后可重新提交审核</li>
            </ul>
          </div>
        </div>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="rejectReasonDialogVisible = false" class="close-btn">关闭</el-button>
        <el-button
          type="primary"
          @click="editRejectedCourse"
          class="edit-course-btn"
        >
          立即修改
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import { listCourseCategories, getTeacherCourses, createCourse, uploadImage, updateCourse } from '@/api/createCourse'
import { deleteCourse } from '@/api/course'

export default {
  name: 'TeacherCourseManage',
  data() {
    return {
      loading: false,
      myCourses: [],
      categories: [],
      courseDialogVisible: false,
      courseForm: {
        courseId: null,
        courseName: '',
        courseDesc: '',
        categoryId: '',
        price: 0,
        originalPrice: 0,
        coverImage: '',
        courseTags: []
      },
      courseRules: {
        courseName: [
          { required: true, message: '请输入课程名称', trigger: 'blur' }
        ],
        courseDesc: [
          { required: true, message: '请输入课程简介', trigger: 'blur' }
        ],
        categoryId: [
          { required: true, message: '请选择分类', trigger: 'change' }
        ],
        price: [
          { required: true, message: '请输入价格', trigger: 'blur' }
        ]
      },
      tagsInput: '',
      rejectReasonDialogVisible: false,
      rejectReasonData: {
        courseName: '',
        auditMessage: '',
        rejectTime: ''
      },
      page: 1,
      pageSize: 10,
      total: 0
    }
  },
  computed: {
    ...mapState({
      userType: state => state.user.userType,
      userId: state => state.user.id
    }),
    courseDialogWidth() {
      return window.innerWidth < 700 ? '98vw' : '600px'
    },
    isMobile() {
      return window.innerWidth <= 768
    },
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
      if (totalPages <= 5) {
        for (let i = 1; i <= totalPages; i++) {
          pages.push(i)
        }
      } else {
        if (currentPage <= 3) {
          for (let i = 1; i <= 4; i++) {
            pages.push(i)
          }
          pages.push('...')
          pages.push(totalPages)
        } else if (currentPage >= totalPages - 2) {
          pages.push(1)
          pages.push('...')
          for (let i = totalPages - 3; i <= totalPages; i++) {
            pages.push(i)
          }
        } else {
          pages.push(1)
          pages.push('...')
          for (let i = currentPage - 1; i <= currentPage + 1; i++) {
            pages.push(i)
          }
          pages.push('...')
          pages.push(totalPages)
        }
      }
      return pages
    }
  },
  created() {
    this.checkTeacherPermission()
    this.loadCategories()
    this.loadMyCourses()
  },
  mounted() {
    this.$nextTick(() => {
      if (this.userType && this.userType !== 'TEACHER') {
        this.$message.error('只有教师可以访问此页面')
        this.$router.push('/')
      }
    })

    // 添加窗口大小变化监听
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    // 移除监听器
    window.removeEventListener('resize', this.handleResize)
  },
  methods: {
    checkTeacherPermission() {
      if (!this.userType) {
        setTimeout(() => {
          this.checkTeacherPermission()
        }, 100)
        return
      }
      if (this.userType !== 'TEACHER') {
        this.$message.error('只有教师可以访问课程管理页面')
        this.$router.push('/')
      }
    },
    loadCategories() {
      listCourseCategories({ page: 1, pageSize: 100 }).then(res => {
        if (res.data && res.data.code === 200) {
          this.categories = res.data.data.records || []
        } else {
          this.categories = []
        }
      })
    },
    loadMyCourses() {
      this.loading = true
      getTeacherCourses({ teacherId: this.userId, page: this.page, pageSize: this.pageSize }).then(res => {
        if (res.data && res.data.code === 200) {
          this.myCourses = res.data.data.records || []
          this.total = res.data.data.total || 0
        } else {
          this.myCourses = []
          this.total = 0
        }
        this.loading = false
      }).catch(() => {
        this.myCourses = []
        this.total = 0
        this.loading = false
      })
    },
    createNewCourse() {
      this.courseForm = {
        courseId: null,
        courseName: '',
        courseDesc: '',
        categoryId: '',
        price: 0,
        originalPrice: 0,
        coverImage: '',
        courseTags: []
      }
      this.tagsInput = ''
      this.courseDialogVisible = true
    },
    editCourse(row) {
      this.courseForm = {
        courseId: row.courseId,
        courseName: row.courseName,
        courseDesc: row.courseDesc,
        categoryId: row.categoryId,
        price: row.price,
        originalPrice: row.originalPrice,
        coverImage: row.coverImage || row.coverUrl || row.cover || row.coverImageUrl || '',
        courseTags: row.courseTags || []
      }
      this.tagsInput = Array.isArray(row.courseTags) ? row.courseTags.join(',') : (row.courseTags || '')
      this.courseDialogVisible = true
    },
    viewCourse(row) {
      this.$router.push(`/course/${row.courseId}`)
    },
    manageChapters(row) {
      this.$router.push({
        path: '/chapter-manage',
        query: {
          courseId: row.courseId,
          courseName: row.courseName
        }
      })
    },
    deleteCourse(row) {
      this.$confirm('确定要删除这个课程吗？删除后无法恢复。', '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteCourse(row.courseId).then(res => {
          if(res && res.data.code === 200) {
            this.loadMyCourses()
            this.$message.success(res.data.data || '删除成功')
          }else {
            this.$message.error(res.data.msg || '删除失败')
          }
        }).catch(error => {
          console.error('删除课程失败:', error)
          this.$message.error('删除失败，请重试')
        })
      }).catch(() => {
        this.$message.info('已取消删除')
      })
    },
    beforeCoverUpload(file) {
      const isJPG = file.type === 'image/jpeg' || file.type === 'image/png';
      const isLt2M = file.size / 1024 / 1024 < 2;
      if (!isJPG) {
        this.$message.error('只能上传 JPG/PNG 格式图片!');
        return false;
      }
      if (!isLt2M) {
        this.$message.error('图片大小不能超过 2MB!');
        return false;
      }
      return true;
    },
    async handleCoverChange(file) {
      try {
        const res = await uploadImage(file.raw);
        if (res.code === 200 && res.data) {
          // 根据后端返回的数据结构，图片URL在 imageUrl 字段中
          const imageUrl = res.data.imageUrl || res.data.url || res.data;
          this.courseForm.coverImage = imageUrl;
          this.$message.success('图片上传成功');
        } else {
          this.$message.error(res.msg || '图片上传失败');
        }
      } catch (e) {
        console.error('图片上传失败:', e);
        this.$message.error('图片上传失败');
      }
    },
    saveCourse() {
      this.$refs.courseForm.validate(valid => {
        if (!valid) {
          return
        }
        const formData = { ...this.courseForm }
        if (this.tagsInput) {
          formData.courseTags = this.tagsInput.split(',').map(tag => tag.trim()).filter(tag => tag)
        }
        if (formData.courseId) {
          updateCourse(formData).then(res => {
            if (res.data && res.data.code === 200) {
              this.$message.success(res.data.data.updateMessage || '课程更新成功！')
              this.courseDialogVisible = false
              this.loadMyCourses()
            } else {
              this.$message.error(res.data?.msg || res.data?.data?.updateMessage || '更新失败，请重试')
            }
          }).catch(() => {
            this.$message.error('更新失败，请重试')
          })
        } else {
          createCourse(formData).then(res => {
            if (
              res && res.data.code === 200
            ) {
              this.$message.success( '课程创建成功！')
              this.courseDialogVisible = false
              this.loadMyCourses()
            } else {
              this.$message.error(res?.msg || '创建失败，请重试')
            }
          }).catch(() => {
            this.$message.error('创建失败，请重试')
          })
        }
      })
    },
    getAuditStatusType(status) {
      switch (status) {
        case 'APPROVED': return 'success'
        case 'PENDING': return 'info'
        case 'REJECTED': return 'danger'
        default: return 'info'
      }
    },
    getAuditStatusText(status) {
      switch (status) {
        case 'APPROVED': return '已通过'
        case 'PENDING': return '待审核'
        case 'REJECTED': return '已拒绝'
        default: return status
      }
    },
    formatDateTime(dateTime) {
      if (!dateTime) return ''
      return new Date(dateTime).toLocaleString()
    },
    goHome() {
      this.$router.push('/')
    },
    viewRejectReason(row) {
      this.rejectReasonData = {
        courseName: row.courseName,
        auditMessage: row.auditMessage,
        rejectTime: row.createTime // 这里可以根据实际情况调整，如果有专门的拒绝时间字段
      }
      this.rejectReasonDialogVisible = true
    },
    editRejectedCourse() {
      // 关闭拒绝理由对话框
      this.rejectReasonDialogVisible = false
      // 找到对应的课程数据并打开编辑对话框
      const course = this.myCourses.find(c => c.courseName === this.rejectReasonData.courseName)
      if (course) {
        this.editCourse(course)
      }
    },
    handleResize() {
      // 强制更新计算属性
      this.$forceUpdate()
    },
    handlePageChange(newPage) {
      this.page = newPage
      this.loadMyCourses()
    },
    handleSizeChange(newSize) {
      this.pageSize = newSize
      this.page = 1
      this.loadMyCourses()
    }
  }
}
</script>

<style scoped>
.teacher-course-manage-page {
  padding: 20px;
  min-height: 100vh;
  background: linear-gradient(135deg, #ffe4ec 0%, #ffd6e6 100%);
  max-width: 1600px;
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
  gap: 12px;
}

.create-btn {
  background: #ffb6d5 !important;
  border-color: #ffb6d5 !important;
  color: #fff !important;
  font-weight: bold;
  border-radius: 12px;
  padding: 10px 20px;
  display: flex;
  align-items: center;
}

.create-btn i {
  margin-right: 5px;
}

.create-btn:hover {
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

.course-manage-card {
  margin-bottom: 20px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 16px #f0c1d6cc;
  border: none;
  overflow: hidden;
}

.course-table {
  border-radius: 12px;
  overflow: hidden;
}

.course-table ::v-deep .el-table__header-wrapper {
  background: linear-gradient(90deg, #ffe4ec 0%, #ffd6e6 100%);
}

.course-table ::v-deep .el-table__header th {
  background: transparent;
  color: #ff5c8a;
  font-weight: bold;
  border-bottom: 2px solid #ffb6d5;
}

.course-table ::v-deep .el-table__body tr:hover > td {
  background: #fff5f8;
}

.course-table ::v-deep .el-table__body td {
  border-bottom: 1px solid #ffe4ec;
}

.original-price {
  text-decoration: line-through;
  color: #bbb;
  font-size: 12px;
  margin-left: 5px;
}

.status-tag {
  border-radius: 8px;
  font-weight: bold;
}

.status-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.reject-reason-btn {
  border-radius: 6px;
  font-weight: bold;
  font-size: 11px;
  padding: 2px 6px;
  height: 24px;
  line-height: 20px;
}

.action-buttons {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.view-btn {
  background: #ffb6d5 !important;
  border-color: #ffb6d5 !important;
  color: #fff !important;
  font-weight: bold;
  border-radius: 8px;
}

.view-btn:hover {
  background: #ff5c8a !important;
  border-color: #ff5c8a !important;
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

.chapter-btn {
  border-radius: 8px;
  font-weight: bold;
}

.delete-btn {
  border-radius: 8px;
  font-weight: bold;
}

.empty-state {
  text-align: center;
  padding: 40px 0;
  color: #999;
}

.empty-state i {
  font-size: 48px;
  margin-bottom: 16px;
  color: #ffb6d5;
}

.empty-state p {
  margin: 0;
  font-size: 16px;
}

.cover-uploader {
  display: inline-block;
}

.cover-uploader-icon {
  font-size: 32px;
  color: #ffb6d5;
  border: 1px dashed #ffd6e6;
  border-radius: 8px;
  width: 80px;
  height: 80px;
  line-height: 80px;
  text-align: center;
  background: #fff0f6;
}

.cover-img {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  border: 2px solid #ffb6d5;
  object-fit: cover;
  background: #fff;
}

.course-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(90deg, #ffe4ec 0%, #ffd6e6 100%);
  color: #ff5c8a;
  font-weight: bold;
}

.save-btn {
  background: #ffb6d5 !important;
  border-color: #ffb6d5 !important;
  color: #fff !important;
  font-weight: bold;
  border-radius: 12px;
  padding: 10px 20px;
}

.save-btn:hover {
  background: #ff5c8a !important;
  border-color: #ff5c8a !important;
}

/* 拒绝理由对话框样式 */
.reject-reason-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(90deg, #ffe4ec 0%, #ffd6e6 100%);
  color: #ff5c8a;
  font-weight: bold;
  border-radius: 12px 12px 0 0;
}

.reject-reason-dialog ::v-deep .el-dialog__body {
  padding: 20px;
  max-height: 70vh;
  overflow-y: auto;
}

.reject-reason-dialog ::v-deep .el-dialog__footer {
  background: #fafafa;
  border-radius: 0 0 12px 12px;
  padding: 15px 20px;
}

.reject-reason-content {
  padding: 0;
}

.course-info {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #ffe4ec;
}

.course-title {
  font-size: 18px;
  font-weight: bold;
  color: #ff5c8a;
  margin-bottom: 8px;
  text-align: center;
}

.reject-time {
  font-size: 12px;
  color: #999;
  text-align: center;
}

.reason-section {
  margin-bottom: 20px;
}

.reason-label {
  font-weight: bold;
  color: #666;
  margin-bottom: 8px;
  font-size: 14px;
}

.reason-content {
  background: #fdf2f2;
  border: 1px solid #e74c3c;
  border-radius: 8px;
  padding: 12px;
  color: #e74c3c;
  font-size: 14px;
  line-height: 1.5;
  min-height: 60px;
}

.suggestion-section {
  background: #fff8e1;
  border: 1px solid #ffb74d;
  border-radius: 8px;
  padding: 15px;
}

.suggestion-title {
  font-weight: bold;
  color: #f57c00;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.suggestion-title i {
  font-size: 16px;
}

.suggestion-content ul {
  margin: 0;
  padding-left: 20px;
  color: #666;
  font-size: 13px;
  line-height: 1.6;
}

.suggestion-content li {
  margin-bottom: 6px;
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

.edit-course-btn {
  background: #ffb6d5 !important;
  border-color: #ffb6d5 !important;
  color: #fff !important;
  font-weight: bold;
  border-radius: 8px;
  padding: 10px 20px;
}

.edit-course-btn:hover {
  background: #ff5c8a !important;
  border-color: #ff5c8a !important;
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
@media (max-width: 768px) {
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
