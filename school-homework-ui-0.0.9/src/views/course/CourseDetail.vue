<template>
  <div class="course-detail-page">
    <div class="page-header">
      <div class="header-content">
        <el-button @click="goHome" class="back-btn">
          <i class="el-icon-back"></i>
          返回主页
        </el-button>
      </div>
    </div>
    <el-card class="course-info-card">
      <div slot="header" class="course-header">
        <div class="course-title-section">
          <span class="course-title">{{ course.courseName }}</span>
          <el-tag v-if="course.auditStatus !== 'APPROVED'" :type="course.auditStatus==='PENDING' ? 'info' : 'danger'" class="audit-tag">{{ auditStatusText(course.auditStatus) }}</el-tag>
        </div>
        <div class="course-actions">
          <el-button v-if="canBuy" :disabled="hasPurchased" type="primary" class="buy-btn" @click="buyCourse">{{ buyButtonText }}</el-button>
          <el-button :type="isFavorited ? 'danger' : 'primary'" v-if="userType === 'STUDENT'" @click="toggleFavorite" class="favorite-btn">
            {{ isFavorited ? '取消收藏' : '收藏课程' }}
          </el-button>
        </div>
      </div>
      <div class="course-meta-beauty">
        <div class="meta-group">
          <div class="meta-item">
            <i class="el-icon-s-order meta-icon"></i>
            <span>课程ID：</span>{{ course.courseId }}
          </div>
          <div class="meta-item">
            <i class="el-icon-collection-tag meta-icon"></i>
            <span>分类：</span>{{ course.categoryName }}
          </div>
          <div class="meta-item">
            <i class="el-icon-user meta-icon"></i>
            <span>教师：</span>{{ course.teacherName }}
          </div>
          <div class="meta-item">
            <i class="el-icon-s-custom meta-icon"></i>
            <span>学生数：</span>{{ course.studentCount }}
          </div>
          <div class="meta-item">
            <i class="el-icon-star-on meta-icon"></i>
            <span>评分：</span>{{ course.score }}
          </div>
          <div class="meta-item">
            <i class="el-icon-money meta-icon"></i>
            <span>价格：</span>
            <span class="price">￥{{ course.price }}</span>
            <span v-if="course.originalPrice" class="original-price">（原价￥{{ course.originalPrice }}）</span>
          </div>
        </div>
        <div class="meta-tags" v-if="course.courseTags">
          <i class="el-icon-collection-tag meta-icon"></i>
          <span>标签：</span>
          <el-tag
            v-for="(tag, idx) in course.courseTags.split(',')"
            :key="idx"
            class="tag-pink"
            effect="plain"
          >{{ tag }}</el-tag>
        </div>
        <div class="meta-desc">
          <i class="el-icon-document meta-icon"></i>
          <span>简介：</span>
          <span>{{ course.courseDesc }}</span>
        </div>
      </div>
    </el-card>
    <el-card class="chapter-list-card">
      <div slot="header" class="chapter-header"><span>章节列表</span></div>
      <el-table :data="chapters" border style="width:100%;" class="chapter-table">
        <el-table-column prop="chapterId" type="index" label="序号" width="80"/>
        <el-table-column prop="chapterName" label="章节名称"/>
        <el-table-column prop="orderNum" label="顺序" width="80"/>
        <el-table-column prop="isFree" label="是否免费" width="100">
          <template slot-scope="scope">
            <el-tag :type="isChapterFree(scope.row) ? 'success' : 'info'">{{ isChapterFree(scope.row) ? '免费' : '付费' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="videoDuration" label="时长" width="120"/>
        <el-table-column prop="videoSize" label="大小(MB)" width="100">
          <template slot-scope="scope">{{ (scope.row.videoSize/1024/1024).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template slot-scope="scope">
            <!-- 视频章节 -->
            <el-button
              v-if="isVideoChapter(scope.row) && scope.row.videoUrl && scope.row.videoUrl !== 'false' && scope.row.videoUrl !== ''"
              size="mini"
              type="primary"
              @click="playVideo(scope.row)"
              class="action-btn"
            >
              观看视频
            </el-button>
            <!-- 文档章节：显示"阅读文档"和"下载文档" -->
            <template v-else-if="isDocumentChapter(scope.row)">
              <el-button
                size="mini"
                type="success"
                @click="goToDocReader(scope.row)"
                class="action-btn"
              >
                阅读文档
              </el-button>
              <el-button
                size="mini"
                type="primary"
                @click="downloadDocument(scope.row)"
                class="action-btn"
                style="margin-left: 6px;"
              >
                下载文档
              </el-button>
            </template>
            <!-- 无内容 -->
            <el-button
              v-else
              size="mini"
              type="info"
              disabled
              class="action-btn"
            >
              无内容
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>
<script>
import { mapState } from 'vuex'
import { getCourseDetail } from '@/api/course'
import {favoriteCourse, favoriteCourseStatus} from '@/api/favorite'
import { addToCart, checkPurchased } from '@/api/cart'
import { reportChapterProgress, getChaptersList, getPurchasedCourseDetail, getDocDownloadUrl } from '@/api/chapters'
import { getVideoPlayUrl } from '@/api/video'
import getters from "@/store/getters";
import 'github-markdown-css/github-markdown.css'
export default {
  name: 'CourseDetail',
  data() {
    return {
      course: {},
      chapters: [],
      favoriteStatus: false,
      hasPurchased: false,
    }
  },
  computed: {
    ...mapState({
      userType: state => state.user.userType,
      userId: state => state.user.id
    }),
    isFavorited() {
      return this.favoriteStatus;
    },
    canBuy() {
      if (this.userType !== 'STUDENT') return false
      if (this.userType === 'ADMIN') return false
      return !this.hasPurchased
    },
    buyButtonText() {
      if (this.hasPurchased) return '已购买';
      return '立即购买';
    },
  },
  created() {
    this.loadCourse()
  },
  methods: {
    goHome() {
      this.$router.push('/')
    },
    async loadCourse() {
      const id = this.$route.params.id

      // 先检查用户是否已购买该课程
      this.hasPurchased = false // 默认未购买
      if (this.userType === 'STUDENT' && this.userId) {
        try {
          const purchaseRes = await checkPurchased(id)
          const res = purchaseRes.data // 统一解包
          // 新逻辑：data: true 表示已购买，data: false 表示未购买
          this.hasPurchased = res && res.code === 200 && res.data === true
        } catch (error) {
          console.warn('检查购买状态失败:', error)
          this.hasPurchased = false
        }
      }

      // 根据购买状态选择API
      if (this.hasPurchased) {
        // 已购买，获取完整详情（包含章节内容）
        try {
          const res = await getPurchasedCourseDetail(id)
          if (res.code === 200) {
            this.course = res.data
            this.chapters = Array.isArray(res.data.chapteList) ? res.data.chapteList : []
          } else {
            await this.loadCourseFromScan()
          }
        } catch (error) {
          await this.loadCourseFromScan()
        }
      } else {
        await this.loadCourseFromScan()
      }

      this.checkFavorite()
    },

    async loadCourseFromScan() {
      const id = this.$route.params.id
      const res = await getCourseDetail(id)
      // 兼容多层data结构
      const data = res && res.data && res.data.data ? res.data.data : (res.data || res)
      this.course = data
      const chaptersRes = await getChaptersList(id)
      if (chaptersRes.data && chaptersRes.data.code === 200 && Array.isArray(chaptersRes.data.data)) {
        this.chapters = chaptersRes.data.data
      } else {
        this.chapters = []
      }
      // 只有在未购买时才赋值 false，避免覆盖已购买状态
      if (!this.hasPurchased) {
        this.hasPurchased = false
      }
    },
    async checkFavorite() {
      const courseId = this.course.courseId;
      const userId = this.userId;

      // 参数校验
      if (!courseId || !userId) {
        return;
      }

      try {
        const response = await favoriteCourseStatus(courseId, userId);
        const res = response.data // 统一解包
        // 根据后端返回的数据结构判断收藏状态
        if (res && res.code === 200) {
          // 后端返回: {"code":200,"msg":null,"data":"已收藏"}
          const isFavorited = res.data === "已收藏";
          this.favoriteStatus = isFavorited;
        } else {
          this.favoriteStatus = false;
        }
      } catch (error) {
        this.favoriteStatus = false;
      }
    },
    async toggleFavorite() {
      try {
        const courseId = this.course.courseId

        const response = await favoriteCourse(courseId)
        const res = response.data // 统一解包
        if (res && res.code === 200) {
          // 操作成功后重新检查收藏状态
          await this.checkFavorite()

          // 根据当前状态显示消息
          const message = this.favoriteStatus ? '收藏成功' : '取消收藏成功';
          this.$message.success(message)
        } else {
          this.$message.error(res?.msg || '操作失败')
        }
      } catch (error) {
        this.$message.error('操作失败，请稍后重试')
      }
    },
    isVideoChapter(chapter) {
      // 根据文件URL或内容类型判断是否为视频章节
      if (chapter.contentType) {
        return chapter.contentType === 'VIDEO'
      }
      // 如果没有contentType字段，根据文件URL判断
      if (chapter.videoUrl) {
        const videoExtensions = ['.mp4', '.avi', '.mov', '.wmv', '.flv', '.mkv', '.webm']
        return videoExtensions.some(ext => chapter.videoUrl.toLowerCase().includes(ext))
      }
      return false
    },
    isDocumentChapter(chapter) {
      // 根据文件URL或内容类型判断是否为文档章节
      if (chapter.contentType) {
        return chapter.contentType === 'DOCUMENT'
      }
      // 如果没有contentType字段，根据文件URL判断
      if (chapter.videoUrl) {
        const documentExtensions = ['.pdf', '.doc', '.docx', '.txt', '.ppt', '.pptx']
        return documentExtensions.some(ext => chapter.videoUrl.toLowerCase().includes(ext))
      }
      return false
    },
    isChapterFree(chapter) {
      // 检查章节是否免费，处理字符串和数字类型
      if (chapter.isFree === true || chapter.isFree === 1 || chapter.isFree === '1') {
        return true
      }
      return false
    },
    buyCourse: async function() {
      if (this.userType === 'TEACHER' && !this.isCourseOwner()) {
        this.$message.warning('暂不支持老师购买课程')
        return
      }
      if (this.hasPurchased) {
        this.$message.info('您已购买过该课程')
        return
      }
      try {
        const response = await addToCart({ courseId: this.course.courseId })
        const res = response.data // 统一解包
        if (res.code === 200) {
          this.$message.success('已加入购物车')
          this.$router.push('/cart')
        } else {
          this.$message.error(res.msg || '加入购物车失败')
        }
      } catch (e) {
        this.$message.error('加入购物车失败')
      }
    },
    async playVideo(chapter) {
      if (!chapter.chapterId) {
        this.$message.error('章节ID无效');
        return;
      }
      const userId = this.userId;
      try {
        const res = await getVideoPlayUrl({ chapterId: chapter.chapterId, userId });
        const result = res.data && res.data.code !== undefined ? res.data : res;
        if (result.code === 200 && result.data) {
          this.$router.push({
            name: 'VideoPlayerPage',
            params: {
              courseId: this.course.courseId,
              chapterId: chapter.chapterId,
              videoUrl: result.data,
              chapterName: chapter.chapterName
            }
          })
        } else if (result.code === 403) {
          if (this.userType === 'TEACHER' && !this.isCourseOwner()) {
            this.$message.warning('暂不支持老师购买课程')
            return
          }
          this.$confirm('您未购买该课程，是否立即购买？', '无权限观看', {
            confirmButtonText: '去购买',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(async () => {
            const response = await addToCart({ courseId: this.course.courseId });
            const res = response.data // 统一解包
            if (res.code === 200) {
              this.$message.success('已加入购物车');
              this.$router.push('/cart');
            } else {
              this.$message.error(res.msg || '加入购物车失败');
            }
          });
        } else {
          this.$message.error(result.msg || '获取播放地址失败');
        }
      } catch (e) {
        this.$message.error('获取播放地址失败');
      }
    },
    async downloadDocument(chapter) {
      if (!this.userId) {
        this.$confirm('请先登录后下载文档', '提示', { type: 'warning' }).then(() => {
          this.$router.push('/login')
        })
        return
      }
      if (chapter.chapterId) {
        try {
          const response = await getDocDownloadUrl(chapter.chapterId, this.userId)
          const res = response.data // 统一解包
          if (res.code === 200 && res.data) {
            window.open(res.data, '_blank');
            this.$message.success('开始下载文档（5分钟内有效）');
            // 只有学生才上报进度
            if (this.userType === 'STUDENT') {
              try {
                await reportChapterProgress({
                  courseId: this.course.courseId,
                  chapterId: chapter.chapterId
                })
                this.$message.success('已记录下载进度')
              } catch {
                this.$message.warning('下载进度上报失败')
              }
            }
          } else if (res.code === 403) {
            if (this.userType === 'TEACHER' && !this.isCourseOwner()) {
              this.$message.warning('暂不支持老师购买课程')
              return
            }
            this.$confirm('您未购买该课程，是否立即购买？', '无权限下载', {
              confirmButtonText: '去购买',
              cancelButtonText: '取消',
              type: 'warning'
            }).then(() => {
              this.buyCourse()
            })
          } else {
            this.$message.error(res.msg || '获取下载链接失败')
          }
        } catch (e) {
          this.$message.error('获取下载链接失败')
        }
      } else {
        this.$message.error('文档地址无效')
      }
    },
    goToDocReader(chapter) {
      // 跳转到 DocReader 全屏页面，传递 courseId 和 chapterId
      this.$router.push({
        name: 'DocReader',
        params: {
          courseId: this.course.courseId || this.$route.params.id,
          chapterId: chapter.chapterId
        }
      })
    },
    isCourseOwner() {
      // 只判断teacherId
      return this.userType === 'TEACHER' && this.course && this.course.teacherId == this.userId
    },
    auditStatusText(status) {
      switch (status) {
        case 'PENDING': return '待审核';
        case 'APPROVED': return '已通过';
        case 'REJECTED': return '已拒绝';
        default: return '未知';
      }
    },
    async handleAddToCartFromDocReader() {
      const response = await addToCart({ courseId: this.course.courseId });
      const res = response.data // 统一解包
      if (res.code === 200) {
        this.$message.success('已加入购物车');
        this.$router.push('/cart');
      } else {
        this.$message.error(res.msg || '加入购物车失败');
      }
    }
  },
  mounted() {
    window.addEventListener('doc-finished-debug', () => {
    })
    window.addEventListener('message', async (event) => {
      if (event.data && event.data.type === 'videoEnded') {
        try {
          await this.$axios.post('/student/learn/complete', {
            courseId: event.data.courseId,
            chapterId: event.data.chapterId
          })
          this.$message.success('视频学习进度已记录')
        } catch {
          this.$message.warning('视频进度上报失败')
        }
      }
    });
  }
}
</script>
<style scoped>
.course-detail-page {
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
.course-info-card {
  margin-bottom: 20px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 16px #f0c1d6cc;
  border: none;
  overflow: hidden;
}
.course-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.course-title-section {
  display: flex;
  align-items: center;
}
.course-title {
  font-size: 22px;
  font-weight: bold;
  color: #ff5c8a;
}
.audit-tag {
  margin-left: 16px;
  border-radius: 8px;
  font-weight: bold;
}
.course-actions {
  display: flex;
  gap: 12px;
}
.course-meta-beauty {
  background: linear-gradient(135deg, #fff0f6 60%, #ffd6e6 100%);
  border-radius: 18px;
  box-shadow: 0 2px 12px #ffd6e6cc;
  padding: 28px 32px 18px 32px;
  margin: 24px 0 0 0;
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.meta-group {
  display: flex;
  flex-wrap: wrap;
  gap: 24px 40px;
}
.meta-item {
  display: flex;
  align-items: center;
  font-size: 16px;
  color: #ff5c8a;
  font-weight: bold;
  min-width: 180px;
  margin-bottom: 6px;
}
.meta-icon {
  color: #ffb6d5;
  font-size: 20px;
  margin-right: 8px;
}
.price {
  color: #ff5c8a;
  font-size: 18px;
  font-weight: bold;
  margin-left: 2px;
}
.original-price {
  color: #bbb;
  text-decoration: line-through;
  font-size: 14px;
  margin-left: 6px;
}
.meta-tags {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.tag-pink {
  background: #fff0f6 !important;
  color: #ff5c8a !important;
  border: 1.5px solid #ffb6d5 !important;
  border-radius: 10px !important;
  font-weight: bold;
}
.meta-desc {
  display: flex;
  align-items: flex-start;
  color: #888;
  font-size: 15px;
  margin-top: 8px;
  gap: 8px;
}
.meta-desc .meta-icon {
  margin-top: 2px;
}
.chapter-list-card {
  margin-top: 30px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 16px #f0c1d6cc;
  border: none;
  overflow: hidden;
}
.chapter-header {
  font-size: 18px;
  font-weight: bold;
  color: #ff5c8a;
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
.buy-btn {
  background: #ffb6d5 !important;
  border-color: #ffb6d5 !important;
  color: #fff !important;
  font-weight: bold;
  border-radius: 12px;
  padding: 10px 20px;
}
.buy-btn:hover {
  background: #ff5c8a !important;
  border-color: #ff5c8a !important;
}
.favorite-btn {
  background: #ffb6d5 !important;
  border-color: #ffb6d5 !important;
  color: #fff !important;
  font-weight: bold;
  border-radius: 12px;
  padding: 10px 20px;
}
.favorite-btn:hover {
  background: #ff5c8a !important;
  border-color: #ff5c8a !important;
}
.action-btn {
  border-radius: 8px;
  font-weight: bold;
}
/* 操作按钮样式 */
::v-deep .el-button--mini {
  padding: 5px 10px;
  font-size: 12px;
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
/* 成功按钮样式 */
::v-deep .el-button--success {
  background: #67c23a !important;
  border-color: #67c23a !important;
}
::v-deep .el-button--success:hover,
::v-deep .el-button--success:focus {
  background: #85ce61 !important;
  border-color: #85ce61 !important;
}
@media (max-width: 768px) {
  .course-detail-page {
    padding: 8px;
  }
  .header-content {
    flex-direction: column;
    text-align: center;
  }
  .back-btn {
    width: 100%;
    margin-bottom: 10px;
  }
  .course-info-card, .chapter-list-card {
    border-radius: 10px;
    padding: 8px;
  }
  .course-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  .course-title {
    font-size: 18px;
  }
  .chapter-header {
    font-size: 16px;
  }
  .course-actions {
    flex-direction: column;
    width: 100%;
  }
  .buy-btn, .favorite-btn {
    width: 100%;
  }
  .chapter-table ::v-deep .el-table {
    font-size: 12px;
  }
  .chapter-table ::v-deep .el-table th,
  .chapter-table ::v-deep .el-table td {
    padding: 8px 4px;
  }
  .course-meta-beauty {
    padding: 12px 8px 10px 8px;
    border-radius: 10px;
    gap: 10px;
  }
  .meta-group {
    flex-direction: column;
    gap: 8px;
  }
  .meta-item {
    min-width: 120px;
    font-size: 14px;
  }
}
</style>
