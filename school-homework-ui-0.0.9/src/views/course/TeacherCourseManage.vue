<template>
  <div class="teacher-course-manage">
    <div class="page-header">
      <h1 class="page-title">我的课程管理</h1>
      <p class="page-subtitle">管理您创建的课程</p>
    </div>
    <el-card class="course-manage-card">
      <div slot="header" class="card-header">
        <span class="header-title">课程列表</span>
        <el-button type="primary" @click="createCourse" class="create-btn">
          <i class="el-icon-plus"></i>
          新建课程
        </el-button>
      </div>
      <el-table :data="courses" border style="width:100%;" class="course-table" v-loading="loading">
        <el-table-column prop="courseName" label="课程名称" />
        <el-table-column prop="categoryName" label="分类" width="120">
          <template slot-scope="scope">
            <el-tag class="category-tag">{{ scope.row.categoryName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="100">
          <template slot-scope="scope">
            ￥{{ scope.row.price ? scope.row.price.toFixed(2) : '0.00' }}
          </template>
        </el-table-column>
        <el-table-column prop="studentCount" label="学生数" width="100" />
        <el-table-column prop="score" label="评分" width="120">
          <template slot-scope="scope">
            <el-rate v-model="scope.row.score" disabled show-score text-color="#ff9900" />
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
            <el-button type="primary" size="mini" @click="editCourse(scope.row)" class="edit-btn">
              编辑
            </el-button>
            <el-button type="danger" size="mini" @click="deleteCourse(scope.row)" class="delete-btn">
              删除
            </el-button>
            <el-button type="success" size="mini" @click="manageChapters(scope.row)" class="chapter-btn">
              章节
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <!-- 空状态 -->
      <div v-if="courses.length === 0" class="empty-state">
        <i class="el-icon-folder"></i>
        <p>暂无课程，点击右上角"新建课程"开始创建您的第一个课程！</p>
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
              <li>检查课程内容是否完整，确保包含必要的章节和知识点</li>
              <li>完善课程简介，让学员更好地了解课程内容</li>
              <li>确保课程价格合理，符合市场水平</li>
              <li>添加更多实践案例和练习题</li>
            </ul>
          </div>
        </div>
      </div>
      <div slot="footer">
        <el-button @click="rejectReasonDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="editRejectedCourse">编辑课程</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
export default {
  name: 'TeacherCourseManage',
  data() {
    return {
      loading: false,
      courses: [
        {
          courseId: 1,
          courseName: '高等数学基础',
          courseDesc: '系统学习高等数学的基本概念和方法，为后续学习打下坚实基础',
          categoryId: 1,
          categoryName: '数学',
          price: 199,
          originalPrice: 299,
          coverImage: 'https://picsum.photos/300/200?random=1',
          courseTags: ['数学', '基础', '高等数学'],
          studentCount: 156,
          score: 4.8,
          auditStatus: 'APPROVED',
          createTime: '2024-01-10 09:30:00'
        },
        {
          courseId: 2,
          courseName: '线性代数入门',
          courseDesc: '系统学习线性代数的基本概念和方法，为后续学习打下坚实基础',
          categoryId: 1,
          categoryName: '数学',
          price: 159,
          originalPrice: 0,
          coverImage: 'https://picsum.photos/300/200?random=2',
          courseTags: ['数学', '线性代数'],
          studentCount: 89,
          score: 4.6,
          auditStatus: 'PENDING',
          createTime: '2024-01-12 14:20:00'
        },
        {
          courseId: 3,
          courseName: '概率论与数理统计',
          courseDesc: '深入浅出地讲解概率论与数理统计的基本原理和应用',
          categoryId: 1,
          categoryName: '数学',
          price: 179,
          originalPrice: 249,
          coverImage: 'https://picsum.photos/300/200?random=3',
          courseTags: ['数学', '概率论', '统计'],
          studentCount: 203,
          score: 4.9,
          auditStatus: 'REJECTED',
          auditMessage: '课程内容需要进一步完善，请补充更多实践案例',
          createTime: '2024-01-08 16:45:00'
        }
      ],
      categories: [
        { categoryId: 1, categoryName: '数学' },
        { categoryId: 2, categoryName: '物理' },
        { categoryId: 3, categoryName: '化学' },
        { categoryId: 4, categoryName: '计算机' }
      ],
      courseDialogVisible: false,
      courseDialogWidth: '600px',
      courseForm: {
        courseId: null,
        courseName: '',
        courseDesc: '',
        categoryId: null,
        price: 0,
        originalPrice: 0,
        coverImage: '',
        courseTags: []
      },
      courseRules: {
        courseName: [
          { required: true, message: '请输入课程名称', trigger: 'blur' },
          { min: 2, max: 50, message: '课程名称长度在2到50个字符', trigger: 'blur' }
        ],
        courseDesc: [
          { required: true, message: '请输入课程简介', trigger: 'blur' },
          { min: 10, max: 200, message: '课程简介长度在10到200个字符', trigger: 'blur' }
        ],
        categoryId: [
          { required: true, message: '请选择课程分类', trigger: 'change' }
        ],
        price: [
          { required: true, message: '请输入课程价格', trigger: 'blur' }
        ]
      },
      tagsInput: '',
      rejectReasonDialogVisible: false,
      rejectReasonData: {},
      isMobile: false
    }
  },
  mounted() {
    console.log('教师课程管理页面使用假数据')
    this.checkMobile()
    window.addEventListener('resize', this.checkMobile)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.checkMobile)
  },
  methods: {
    checkMobile() {
      this.isMobile = window.innerWidth <= 768
      this.courseDialogWidth = this.isMobile ? '95%' : '600px'
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
    formatDateTime(timeStr) {
      if (!timeStr) return ''
      const date = new Date(timeStr)
      return date.toLocaleString()
    },
    createCourse() {
      this.courseForm = {
        courseId: null,
        courseName: '',
        courseDesc: '',
        categoryId: null,
        price: 0,
        originalPrice: 0,
        coverImage: '',
        courseTags: []
      }
      this.tagsInput = ''
      this.courseDialogVisible = true
    },
    editCourse(course) {
      this.courseForm = { ...course }
      this.tagsInput = course.courseTags ? course.courseTags.join(',') : ''
      this.courseDialogVisible = true
    },
    async saveCourse() {
      this.$refs.courseForm.validate(async valid => {
        if (!valid) return
        try {
          const courseData = {
            ...this.courseForm,
            courseTags: this.tagsInput.split(',').map(tag => tag.trim()).filter(tag => tag)
          }
          if (this.courseForm.courseId) {
            // 编辑课程
            const index = this.courses.findIndex(c => c.courseId === this.courseForm.courseId)
            if (index > -1) {
              this.courses[index] = { ...this.courses[index], ...courseData }
            }
            this.$message.success('课程更新成功（假数据）')
          } else {
            // 新建课程
            const newCourse = {
              ...courseData,
              courseId: Date.now(),
              studentCount: 0,
              score: 0,
              auditStatus: 'PENDING',
              createTime: new Date().toLocaleString()
            }
            this.courses.unshift(newCourse)
            this.$message.success('课程创建成功（假数据）')
          }
          this.courseDialogVisible = false
        } catch (error) {
          this.$message.error('操作失败，请重试')
        }
      })
    },
    async deleteCourse(course) {
      try {
        await this.$confirm('确定要删除这个课程吗？删除后无法恢复', '提示', { type: 'warning' })
        const index = this.courses.findIndex(c => c.courseId === course.courseId)
        if (index > -1) {
          this.courses.splice(index, 1)
          this.$message.success('课程删除成功（假数据）')
        }
      } catch (e) {
        if (e !== 'cancel') {
          this.$message.error('删除失败')
        }
      }
    },
    manageChapters(course) {
      this.$router.push(`/course/${course.courseId}/chapters`)
    },
    beforeCoverUpload(file) {
      const isImage = file.type.startsWith('image/')
      const isLt2M = file.size / 1024 / 1024 < 2
      if (!isImage) {
        this.$message.error('只能上传图片文件!')
        return false
      }
      if (!isLt2M) {
        this.$message.error('图片大小不能超过 2MB!')
        return false
      }
      return true
    },
    handleCoverChange(file) {
      this.uploadCover(file.raw)
    },
    async uploadCover(file) {
      try {
        this.$message.info('正在上传封面...')
        setTimeout(() => {
          const imageUrl = `https://picsum.photos/300/200?random=${Date.now()}`
          this.courseForm.coverImage = imageUrl
          this.$message.success('封面上传成功（假数据）')
        }, 1000)
      } catch (error) {
        this.$message.error('封面上传失败，请重试')
      }
    },
    viewRejectReason(course) {
      this.rejectReasonData = {
        courseName: course.courseName,
        auditMessage: course.auditMessage,
        rejectTime: course.createTime
      }
      this.rejectReasonDialogVisible = true
    },
    editRejectedCourse() {
      const course = this.courses.find(c => c.courseId === this.rejectReasonData.courseId)
      if (course) {
        this.editCourse(course)
      }
      this.rejectReasonDialogVisible = false
    }
  }
}
</script>
<style scoped>
.teacher-course-manage {
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
.course-manage-card {
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
.create-btn {
  background: #ffb6d5 !important;
  border-color: #ffb6d5 !important;
  color: #fff !important;
  font-weight: bold;
  border-radius: 12px;
  padding: 10px 20px;
}
.create-btn:hover {
  background: #ff5c8a !important;
  border-color: #ff5c8a !important;
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
.course-table ::v-deep .el-table__body td {
  border-bottom: 1px solid #ffe4ec;
}
.category-tag {
  background: #ffb6d5;
  color: #fff;
  border: none;
  border-radius: 8px;
}
.edit-btn {
  background: #ffb6d5 !important;
  border-color: #ffb6d5 !important;
  color: #fff !important;
  margin-right: 8px;
}
.delete-btn {
  background: #f56c6c !important;
  border-color: #f56c6c !important;
  color: #fff !important;
  margin-right: 8px;
}
.chapter-btn {
  background: #67c23a !important;
  border-color: #67c23a !important;
  color: #fff !important;
}
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}
.empty-state i {
  font-size: 4rem;
  color: #ddd;
  margin-bottom: 20px;
  display: block;
}
.empty-state p {
  font-size: 1rem;
  margin: 0;
  color: #666;
}
.course-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(90deg, #ffe4ec 0%, #ffd6e6 100%);
  color: #ff5c8a;
  font-weight: bold;
}
.cover-uploader {
  text-align: center;
}
.cover-img {
  width: 200px;
  height: 120px;
  border-radius: 8px;
  border: 2px solid #ffb6d5;
  object-fit: cover;
}
.cover-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 200px;
  height: 120px;
  line-height: 120px;
  text-align: center;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
}
.save-btn {
  background: #ffb6d5 !important;
  border-color: #ffb6d5 !important;
  color: #fff !important;
}
.save-btn:hover {
  background: #ff5c8a !important;
  border-color: #ff5c8a !important;
}
.reject-reason-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(90deg, #ffe4ec 0%, #ffd6e6 100%);
  color: #ff5c8a;
  font-weight: bold;
}
.reject-reason-content {
  padding: 20px 0;
}
.course-info {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}
.course-title {
  font-size: 1.2rem;
  color: #ff5c8a;
  font-weight: bold;
  margin-bottom: 8px;
}
.reject-time {
  font-size: 0.9rem;
  color: #999;
}
.reason-section {
  margin-bottom: 20px;
}
.reason-label {
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}
.reason-content {
  background: #fdf2f2;
  padding: 12px;
  border-radius: 8px;
  border-left: 4px solid #f56c6c;
  color: #f56c6c;
  font-weight: 500;
}
.suggestion-section {
  background: #f0f9ff;
  padding: 16px;
  border-radius: 8px;
  border-left: 4px solid #409eff;
}
.suggestion-title {
  font-weight: bold;
  color: #409eff;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.suggestion-content ul {
  margin: 0;
  padding-left: 20px;
  color: #666;
}
.suggestion-content li {
  margin-bottom: 8px;
  line-height: 1.5;
}
@media (max-width: 768px) {
  .teacher-course-manage {
    padding: 20px;
  }
  .page-title {
    font-size: 2rem;
  }
  .card-header {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
  .create-btn {
    width: 100%;
  }
  .course-table ::v-deep .el-table {
    font-size: 12px;
  }
  .course-table ::v-deep .el-table th,
  .course-table ::v-deep .el-table td {
    padding: 8px 4px;
  }
  .edit-btn, .delete-btn, .chapter-btn {
    margin-bottom: 5px;
    width: 100%;
  }
  .course-dialog ::v-deep .el-dialog {
    width: 95% !important;
    margin: 5vh auto !important;
  }
  .cover-img, .cover-uploader-icon {
    width: 150px;
    height: 90px;
  }
  .cover-uploader-icon {
    line-height: 90px;
  }
}
</style>
