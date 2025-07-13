<template>
  <div class="chapter-manage-page">
    <!-- 隐藏的video元素用于获取视频时长 -->
    <video
      ref="hiddenVideo"
      preload="metadata"
      style="display: none;"
    ></video>
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h2>章节管理 (假数据)</h2>
          <p>{{ courseName }}</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" size="small" @click="addChapter" class="add-btn">
            <i class="el-icon-plus"></i>
            添加章节
          </el-button>
          <el-button @click="goBack" class="back-btn">
            <i class="el-icon-back"></i>
            返回课程管理
          </el-button>
        </div>
      </div>
    </div>
    <el-card class="chapter-manage-card">
      <!-- 章节列表 -->
      <el-table :data="chapters" border style="width:100%;margin-bottom:20px;" v-loading="loading" class="chapter-table">
        <el-table-column type="index" label="序号" width="80" />
        <el-table-column prop="chapterName" label="章节名称" />
        <el-table-column prop="orderNum" label="顺序" width="80" />
        <el-table-column label="内容类型" width="100">
          <template slot-scope="scope">
            <el-tag type="primary" class="content-type-tag">
              {{ scope.row.contentType === 'DOCUMENT' ? '文档' : '视频' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isFree" label="是否免费" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.isFree == 1 ? 'success' : 'info'" class="free-tag">
              {{ scope.row.isFree == 1 ? '免费' : '付费' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="videoDuration" label="时长" width="120">
          <template slot-scope="scope">
            {{ scope.row.videoDuration || '00:00:00' }}
          </template>
        </el-table-column>
        <el-table-column prop="videoSize" label="大小(MB)" width="100">
          <template slot-scope="scope">
            {{ scope.row.videoSize ? (scope.row.videoSize / 1024 / 1024).toFixed(2) : '0.00' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template slot-scope="scope">
            <el-button size="mini" @click="editChapter(scope.row)" class="edit-btn">编辑</el-button>
            <el-button size="mini" type="danger" @click="deleteChapter(scope.row)" class="delete-btn">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <!-- 空数据提示 -->
      <div v-if="!loading && chapters.length === 0" class="empty-state">
        <i class="el-icon-folder"></i>
        <p>暂无章节，点击右上角"添加章节"开始创建课程内容</p>
      </div>
    </el-card>
    <!-- 添加/编辑章节弹窗 -->
    <el-dialog :visible.sync="chapterDialogVisible" :title="chapterForm.chapterId ? '编辑章节' : '添加章节'" :width="chapterDialogWidth" class="chapter-dialog">
      <el-form :model="chapterForm" :rules="chapterRules" ref="chapterForm" label-width="100px">
        <el-form-item label="章节名称" prop="chapterName">
          <el-input v-model="chapterForm.chapterName" maxlength="100" />
        </el-form-item>
        <el-form-item label="顺序" prop="orderNum">
          <el-input-number v-model="chapterForm.orderNum" :min="1" :step="1" />
        </el-form-item>
        <el-form-item label="内容类型" prop="contentType">
          <el-radio-group v-model="chapterForm.contentType">
            <el-radio label="VIDEO">视频</el-radio>
            <el-radio label="DOCUMENT">文档</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="是否免费" prop="isFree">
          <el-switch v-model="chapterForm.isFree" />
        </el-form-item>
        <el-form-item label="文件上传">
          <!-- 上传进度 -->
          <div v-if="isUploading" style="margin-bottom: 10px;">
            <el-progress :percentage="uploadProgress" :stroke-width="8" color="#ffb6d5"></el-progress>
            <div style="text-align: center; color: #666; font-size: 12px; margin-top: 5px;">
              正在上传文件... {{ uploadProgress }}%
            </div>
          </div>
          <!-- 视频上传 -->
          <el-upload
            v-if="chapterForm.contentType === 'VIDEO'"
            class="video-uploader"
            action="#"
            :http-request="customVideoUpload"
            :show-file-list="false"
            :before-upload="beforeVideoUpload"
            :disabled="isUploading"
          >
            <i v-if="!chapterForm.fileUrl" class="el-icon-plus video-uploader-icon"></i>
            <div v-else class="video-preview">
              <i class="el-icon-video-play"></i>
              <span>已选择视频文件</span>
            </div>
          </el-upload>
          <!-- 文档上传 -->
          <el-upload
            v-else
            class="document-uploader"
            action="#"
            :http-request="customDocumentUpload"
            :show-file-list="false"
            :before-upload="beforeDocumentUpload"
            :disabled="isUploading"
            accept=".pdf,.doc,.docx,.txt,.md"
          >
            <i v-if="!chapterForm.fileUrl" class="el-icon-plus document-uploader-icon"></i>
            <div v-else class="document-preview">
              <i class="el-icon-document"></i>
              <span>已选择文档文件</span>
            </div>
          </el-upload>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="chapterDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="saveChapter"
          :disabled="isUploading || (chapterForm.contentType === 'VIDEO' && !chapterForm.fileUrl)"
          :loading="isUploading"
          class="save-btn"
        >
          {{ isUploading ? '上传中...' : '保存' }}
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
export default {
  name: 'ChapterManage',
  data() {
    return {
      courseId: null,
      courseName: '',
      loading: false,
      chapters: [
        {
          chapterId: 1,
          chapterName: '第一章：课程介绍',
          orderNum: 1,
          contentType: 'VIDEO',
          isFree: 1,
          videoUrl: 'https://example.com/chapter1.mp4',
          videoDuration: '00:15:30',
          videoSize: 15728640
        },
        {
          chapterId: 2,
          chapterName: '第二章：基础知识',
          orderNum: 2,
          contentType: 'DOCUMENT',
          isFree: 0,
          videoUrl: '',
          videoDuration: '',
          videoSize: 0
        },
        {
          chapterId: 3,
          chapterName: '第三章：实践练习',
          orderNum: 3,
          contentType: 'VIDEO',
          isFree: 0,
          videoUrl: 'https://example.com/chapter3.mp4',
          videoDuration: '00:25:45',
          videoSize: 31457280
        }
      ],
      chapterDialogVisible: false,
      uploadProgress: 0,
      isUploading: false,
      chapterForm: {
        chapterId: null,
        chapterName: '',
        orderNum: 1,
        contentType: 'VIDEO',
        isFree: false,
        fileUrl: '',
        duration: '',
        pageCount: 0,
        fileSize: 0
      },
      chapterRules: {
        chapterName: [
          { required: true, message: '请输入章节名称', trigger: 'blur' }
        ],
        orderNum: [
          { required: true, message: '请输入顺序', trigger: 'blur' }
        ]
      },
      pendingDocFile: null
    }
  },
  created() {
    this.courseId = this.$route.query.courseId
    this.courseName = this.$route.query.courseName || '未知课程'
    this.loadChapters()
  },
  methods: {
    loadChapters() {
      if (!this.courseId) {
        this.$message.error('课程ID不能为空')
        return
      }
      this.loading = true
      setTimeout(() => {
        this.loading = false
        this.$message.success('章节列表加载成功 (假数据)')
      }, 500)
    },
    addChapter() {
      this.chapterForm = {
        chapterId: null,
        chapterName: '',
        orderNum: this.chapters.length + 1,
        contentType: 'VIDEO',
        isFree: false,
        fileUrl: '',
        duration: '',
        pageCount: 0,
        fileSize: 0
      }
      this.chapterDialogVisible = true
    },
    editChapter(row) {
      this.chapterForm = {
        chapterId: row.chapterId,
        chapterName: row.chapterName,
        orderNum: row.orderNum,
        contentType: row.contentType || 'VIDEO',
        isFree: row.isFree === 1,
        fileUrl: row.contentType === 'VIDEO' ? row.videoUrl : '',
        duration: row.videoDuration,
        pageCount: 0,
        fileSize: row.videoSize
      }
      this.chapterDialogVisible = true
    },
    deleteChapter(row) {
      this.$confirm('确定要删除这个章节吗？删除后无法恢复', '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.chapters = this.chapters.filter(chapter => chapter.chapterId !== row.chapterId)
        this.$message.success('删除成功 (假数据)')
      }).catch(() => {
        this.$message.info('已取消删除')
      })
    },
    beforeVideoUpload(file) {
      const isVideo = file.type.startsWith('video/')
      const isLt1000M = file.size / 1024 / 1024 < 1000
      if (!isVideo) {
        this.$message.error('只能上传视频文件!')
        return false
      }
      if (!isLt1000M) {
        this.$message.error('视频大小不能超过 1000MB!')
        return false
      }
      return true
    },
    async customVideoUpload(options) {
      this.isUploading = true
      this.uploadProgress = 0
      const interval = setInterval(() => {
        this.uploadProgress += Math.random() * 20
        if (this.uploadProgress >= 100) {
          this.uploadProgress = 100
          clearInterval(interval)
          setTimeout(() => {
            this.chapterForm.fileUrl = 'https://example.com/uploaded-video.mp4'
            this.chapterForm.duration = '00:10:30'
            this.chapterForm.fileSize = options.file.size
            this.$message.success('视频上传成功 (假数据)')
            this.isUploading = false
            this.uploadProgress = 0
          }, 500)
        }
      }, 200)
    },
    getVideoDurationFromFile(file) {
      return new Promise((resolve, reject) => {
        const video = this.$refs.hiddenVideo;
        if (!video) {
          console.error('Video element not found')
          reject(new Error('Video element not found'));
          return;
        }
        const timeout = setTimeout(() => {
          console.warn('获取视频时长超时')
          video.src = '';
          reject(new Error('获取视频时长超时'));
        }, 10000);
        const handleMetadata = () => {
          clearTimeout(timeout);
          if (video.duration && !isNaN(video.duration)) {
            const hours = Math.floor(video.duration / 3600);
            const minutes = Math.floor((video.duration % 3600) / 60);
            const seconds = Math.floor(video.duration % 60);
            const formattedDuration = `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
            video.removeEventListener('loadedmetadata', handleMetadata);
            video.src = '';
            resolve(formattedDuration);
          } else {
            console.error('无法获取有效的视频时长，duration:', video.duration)
            video.removeEventListener('loadedmetadata', handleMetadata);
            video.src = '';
            reject(new Error('无法获取有效的视频时长'));
          }
        };
        video.addEventListener('loadedmetadata', handleMetadata);
        const handleError = () => {
          console.error('视频文件加载失败')
          clearTimeout(timeout);
          video.removeEventListener('loadedmetadata', handleMetadata);
          video.removeEventListener('error', handleError);
          video.src = '';
          reject(new Error('视频文件加载失败'));
        };
        video.addEventListener('error', handleError);
        const url = URL.createObjectURL(file);
        video.src = url;
      });
    },
    beforeDocumentUpload(file) {
      const isDocument = file.type === 'application/pdf' ||
                        file.type === 'application/msword' ||
                        file.type === 'application/vnd.openxmlformats-officedocument.wordprocessingml.document' ||
                        file.type === 'text/plain' ||
                        file.name.toLowerCase().endsWith('.md')
      const isLt50M = file.size / 1024 / 1024 < 50
      if (!isDocument) {
        this.$message.error('只能上传 PDF、Word、TXT、Markdown 格式文档!')
        return false
      }
      if (!isLt50M) {
        this.$message.error('文档大小不能超过 50MB!')
        return false
      }
      return true
    },
    async customDocumentUpload(options) {
      if (!this.chapterForm.chapterId) {
        this.pendingDocFile = options.file
        this.$message.info('请先保存章节，系统将自动上传文档')
        return
      }
      this.isUploading = true
      this.uploadProgress = 0
      const interval = setInterval(() => {
        this.uploadProgress += Math.random() * 15
        if (this.uploadProgress >= 100) {
          this.uploadProgress = 100
          clearInterval(interval)
          setTimeout(() => {
            this.$message.success('文档上传并解析成功 (假数据)')
            this.isUploading = false
            this.uploadProgress = 0
          }, 500)
        }
      }, 300)
    },
    saveChapter() {
      this.$refs.chapterForm.validate(async valid => {
        if (!valid) return
        if (this.chapterForm.contentType === 'VIDEO' && !this.chapterForm.fileUrl) {
          this.$message.error('请先上传视频文件')
          return
        }
        setTimeout(() => {
          if (this.chapterForm.chapterId) {
            const index = this.chapters.findIndex(chapter => chapter.chapterId === this.chapterForm.chapterId)
            if (index !== -1) {
              this.chapters[index] = {
                ...this.chapters[index],
                chapterName: this.chapterForm.chapterName,
                orderNum: this.chapterForm.orderNum,
                isFree: this.chapterForm.isFree ? 1 : 0,
                videoUrl: this.chapterForm.fileUrl,
                videoDuration: this.chapterForm.duration,
                videoSize: this.chapterForm.fileSize,
                contentType: this.chapterForm.contentType
              }
            }
            this.$message.success('章节更新成功 (假数据)')
          } else {
            const newChapter = {
              chapterId: Date.now(),
              chapterName: this.chapterForm.chapterName,
              orderNum: this.chapterForm.orderNum,
              isFree: this.chapterForm.isFree ? 1 : 0,
              videoUrl: this.chapterForm.fileUrl,
              videoDuration: this.chapterForm.duration,
              videoSize: this.chapterForm.fileSize || 0,
              contentType: this.chapterForm.contentType
            }
            this.chapters.push(newChapter)
            this.$message.success('章节添加成功 (假数据)')
          }
          this.chapterDialogVisible = false
          if (this.pendingDocFile) {
            this.pendingDocFile = null
          }
        }, 500)
      })
    },
    goBack() {
      this.$router.push('/course-create')
    }
  },
  computed: {
    chapterDialogWidth() {
      return window.innerWidth < 700 ? '98vw' : '600px'
    }
  }
}
</script>
<style scoped>
.chapter-manage-page {
  padding: 20px;
  min-height: 100vh;
  background: linear-gradient(135deg, #ffe4ec 0%, #ffd6e6 100%);
  max-width: 1200px;
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
.add-btn {
  background: #ffb6d5 !important;
  border-color: #ffb6d5 !important;
  color: #fff !important;
  font-weight: bold;
  border-radius: 12px;
  padding: 10px 20px;
  display: flex;
  align-items: center;
}
.add-btn i {
  margin-right: 5px;
}
.add-btn:hover {
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
.chapter-manage-card {
  margin-bottom: 20px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 16px #f0c1d6cc;
  border: none;
  overflow: hidden;
}
.chapter-table {
  border-radius: 12px;
  overflow: hidden;
}
.chapter-table ::v-deep .el-table__header-wrapper {
  background: linear-gradient(90deg, #ffe4ec 0%, #ffd6e6 100%);
}
.chapter-table ::v-deep .el-table__header th {
  background: transparent;
  color: #ff5c8a;
  font-weight: bold;
  border-bottom: 2px solid #ffb6d5;
}
.chapter-table ::v-deep .el-table__body tr:hover > td {
  background: #fff5f8;
}
.chapter-table ::v-deep .el-table__body td {
  border-bottom: 1px solid #ffe4ec;
}
.content-type-tag {
  background: #ffb6d5 !important;
  color: #fff !important;
  border-radius: 8px;
  font-weight: bold;
  border: none;
}
.free-tag {
  border-radius: 8px;
  font-weight: bold;
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
.video-uploader {
  display: inline-block;
}
.video-uploader-icon {
  font-size: 32px;
  color: #ffb6d5;
  border: 1px dashed #ffd6e6;
  border-radius: 8px;
  width: 120px;
  height: 80px;
  line-height: 80px;
  text-align: center;
  background: #fff0f6;
}
.video-preview {
  width: 120px;
  height: 80px;
  border: 1px solid #ffb6d5;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #fff0f6;
  color: #ffb6d5;
}
.video-preview i {
  font-size: 24px;
  margin-bottom: 4px;
}
.video-preview span {
  font-size: 12px;
}
.document-uploader {
  display: inline-block;
}
.document-uploader-icon {
  font-size: 32px;
  color: #ffb6d5;
  border: 1px dashed #ffd6e6;
  border-radius: 8px;
  width: 120px;
  height: 80px;
  line-height: 80px;
  text-align: center;
  background: #fff0f6;
}
.document-preview {
  width: 120px;
  height: 80px;
  border: 1px solid #ffb6d5;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #fff0f6;
  color: #ffb6d5;
}
.document-preview i {
  font-size: 24px;
  margin-bottom: 4px;
}
.document-preview span {
  font-size: 12px;
}
.chapter-dialog ::v-deep .el-dialog__header {
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
@media (max-width: 768px) {
  .chapter-manage-page {
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
  .header-actions {
    flex-direction: column;
    width: 100%;
  }
  .add-btn, .back-btn {
    width: 100%;
    margin-bottom: 10px;
  }
  .chapter-manage-card {
    border-radius: 10px;
    padding: 8px;
  }
  .chapter-table ::v-deep .el-table {
    font-size: 12px;
  }
  .chapter-table ::v-deep .el-table th,
  .chapter-table ::v-deep .el-table td {
    padding: 8px 4px;
  }
  .edit-btn, .delete-btn {
    margin-bottom: 5px;
    width: 100%;
  }
}
@media (max-width: 700px) {
  .chapter-dialog ::v-deep .el-dialog__body {
    padding: 8px 2vw !important;
  }
  .chapter-dialog ::v-deep .el-form-item {
    display: flex;
    flex-direction: column;
    align-items: stretch;
    margin-bottom: 12px;
  }
  .chapter-dialog ::v-deep .el-form-item__label {
    width: 100% !important;
    text-align: left !important;
    font-size: 15px;
    margin-bottom: 4px;
  }
  .chapter-dialog ::v-deep .el-input,
  .chapter-dialog ::v-deep .el-input-number,
  .chapter-dialog ::v-deep .el-select,
  .chapter-dialog ::v-deep .el-textarea {
    width: 100% !important;
    font-size: 15px;
  }
  .video-uploader, .video-uploader-icon,
  .video-preview, .document-uploader, .document-uploader-icon, .document-preview {
    width: 100px !important;
    height: 100px !important;
  }
  .save-btn, .chapter-dialog ::v-deep .el-button {
    width: 100%;
    margin-top: 8px;
    font-size: 16px;
  }
}
</style>
