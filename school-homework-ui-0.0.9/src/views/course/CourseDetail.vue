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
    <el-card class="chapters-card">
      <div slot="header" class="chapters-header">
        <span class="chapters-title">课程章节</span>
        <span class="chapters-count">共 {{ chapters.length }} 章</span>
      </div>
      <div class="chapters-list">
        <div
          v-for="chapter in chapters"
          :key="chapter.chapterId"
          class="chapter-item"
          :class="{ 'free': chapter.isFree, 'locked': !chapter.isFree && !hasPurchased }"
          @click="handleChapterClick(chapter)"
        >
          <div class="chapter-info">
            <div class="chapter-title">{{ chapter.chapterName }}</div>
            <div class="chapter-meta">
              <span class="chapter-duration">
                <i class="el-icon-time"></i>
                {{ chapter.videoDuration }}
              </span>
              <span class="chapter-size">
                <i class="el-icon-document"></i>
                {{ formatFileSize(chapter.videoSize) }}
              </span>
            </div>
          </div>
          <div class="chapter-status">
            <el-tag v-if="chapter.isFree" type="success" size="small">免费</el-tag>
            <el-tag v-else-if="!hasPurchased" type="danger" size="small">付费</el-tag>
            <el-tag v-else type="primary" size="small">可观看</el-tag>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>
<script>
import { mapState } from 'vuex'
export default {
  name: 'CourseDetail',
  data() {
    return {
      course: {
        courseId: 1,
        courseName: 'Vue.js入门到精通',
        courseDesc: '从零开始学习Vue.js框架，掌握前端开发技能。本课程涵盖Vue.js基础语法、组件开发、路由管理、状态管理等核心知识点，通过实战项目帮助学员快速掌握Vue.js开发技能',
        categoryName: '前端开发',
        teacherName: '张老师',
        studentCount: 1250,
        score: 4.8,
        price: 99.00,
        originalPrice: 199.00,
        courseTags: 'Vue,前端,JavaScript,组件',
        auditStatus: 'APPROVED',
        coverImage: 'https://picsum.photos/300/200?random=1'
      },
      chapters: [
        {
          chapterId: 1,
          chapterName: '第一章：Vue.js基础入门',
          orderNum: 1,
          isFree: true,
          videoDuration: '45:30',
          videoSize: 15728640,
          videoUrl: 'https://picsum.photos/300/200?random=1',
          chapterType: 'VIDEO'
        },
        {
          chapterId: 2,
          chapterName: '第二章：Vue组件开发',
          orderNum: 2,
          isFree: false,
          videoDuration: '52:15',
          videoSize: 20971520,
          videoUrl: 'https://picsum.photos/300/200?random=2',
          chapterType: 'VIDEO'
        },
        {
          chapterId: 3,
          chapterName: '第三章：Vue路由管理',
          orderNum: 3,
          isFree: false,
          videoDuration: '38:45',
          videoSize: 15728640,
          videoUrl: 'https://picsum.photos/300/200?random=3',
          chapterType: 'VIDEO'
        },
        {
          chapterId: 4,
          chapterName: '第四章：Vuex状态管理',
          orderNum: 4,
          isFree: false,
          videoDuration: '41:20',
          videoSize: 18350080,
          videoUrl: 'https://picsum.photos/300/200?random=4',
          chapterType: 'VIDEO'
        }
      ],
      hasPurchased: false,
      isFavorited: false
    }
  },
  computed: {
    ...mapState({
      userType: state => state.user.userType,
      userId: state => state.user.id
    }),
    canBuy() {
      return this.userType === 'STUDENT' && this.course.auditStatus === 'APPROVED'
    },
    buyButtonText() {
      if (this.hasPurchased) return '已购买'
      return `购买课程 ￥${this.course.price}`
    }
  },
  mounted() {
    console.log('课程详情页面使用假数据')
    this.loadCourseDetail()
  },
  methods: {
    goHome() {
      this.$router.push('/')
    },
    auditStatusText(status) {
      switch (status) {
        case 'APPROVED': return '已通过'
        case 'PENDING': return '待审核'
        case 'REJECTED': return '已拒绝'
        default: return '未知'
      }
    },
    formatFileSize(bytes) {
      if (!bytes) return '0 B'
      const k = 1024
      const sizes = ['B', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    },
    loadCourseDetail() {
      setTimeout(() => {
        this.$message.success('课程详情加载成功（假数据）')
      }, 300)
    },
    buyCourse() {
      if (this.hasPurchased) {
        this.$message.info('您已购买此课程')
        return
      }
      this.$confirm(`确定要购买课程"${this.course.courseName}"吗？价格：￥${this.course.price}`, '确认购买', {
        confirmButtonText: '确定购买',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.hasPurchased = true
        this.$message.success('购买成功（假数据）')
      }).catch(() => {
        this.$message.info('已取消购买')
      })
    },
    toggleFavorite() {
      this.isFavorited = !this.isFavorited
      this.$message.success(this.isFavorited ? '收藏成功（假数据）' : '取消收藏成功（假数据）')
    },
    handleChapterClick(chapter) {
      if (!chapter.isFree && !this.hasPurchased) {
        this.$message.warning('请先购买课程才能观看此章节')
        return
      }
      if (chapter.chapterType === 'VIDEO') {
        this.$router.push(`/video/${chapter.chapterId}`)
      } else {
        this.$router.push(`/doc/${chapter.chapterId}`)
      }
    }
  }
}
</script>
<style scoped>
.course-detail-page {
  padding: 30px;
  max-width: 1200px;
  margin: 0 auto;
  background: #f8f9fa;
  min-height: 100vh;
}
.page-header {
  margin-bottom: 20px;
}
.header-content {
  display: flex;
  justify-content: flex-start;
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
.course-info-card {
  margin-bottom: 20px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}
.course-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}
.course-title-section {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}
.course-title {
  font-size: 1.8rem;
  color: #ff5c8a;
  font-weight: bold;
}
.audit-tag {
  border-radius: 8px;
  font-weight: bold;
}
.course-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
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
.buy-btn:disabled {
  background: #c0c4cc !important;
  border-color: #c0c4cc !important;
  color: #fff !important;
}
.favorite-btn {
  border-radius: 12px;
  padding: 10px 20px;
  font-weight: bold;
}
.course-meta-beauty {
  padding: 20px 0;
}
.meta-group {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}
.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #ffb6d5;
}
.meta-icon {
  color: #ffb6d5;
  font-size: 1.2rem;
}
.price {
  color: #e74c3c;
  font-weight: bold;
  font-size: 1.1rem;
}
.original-price {
  color: #999;
  text-decoration: line-through;
  font-size: 0.9rem;
}
.meta-tags {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #ffb6d5;
}
.tag-pink {
  background: #ffb6d5;
  color: #fff;
  border: none;
  border-radius: 8px;
  margin-right: 8px;
}
.meta-desc {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #ffb6d5;
  line-height: 1.6;
}
.chapters-card {
  background: white;
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}
.chapters-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.chapters-title {
  font-size: 1.3rem;
  color: #333;
  font-weight: bold;
}
.chapters-count {
  color: #666;
  font-size: 0.9rem;
}
.chapters-list {
  padding: 20px 0;
}
.chapter-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  margin-bottom: 12px;
  background: #f8f9fa;
  border-radius: 12px;
  border-left: 4px solid #ffb6d5;
  cursor: pointer;
  transition: all 0.3s ease;
}
.chapter-item:hover {
  background: #fff5f8;
  transform: translateX(4px);
}
.chapter-item.free {
  border-left-color: #67c23a;
}
.chapter-item.locked {
  border-left-color: #f56c6c;
  opacity: 0.7;
}
.chapter-info {
  flex: 1;
}
.chapter-title {
  font-size: 1.1rem;
  color: #333;
  font-weight: 500;
  margin-bottom: 8px;
}
.chapter-meta {
  display: flex;
  gap: 16px;
  font-size: 0.9rem;
  color: #666;
}
.chapter-duration, .chapter-size {
  display: flex;
  align-items: center;
  gap: 4px;
}
.chapter-status {
  flex-shrink: 0;
}
@media (max-width: 768px) {
  .course-detail-page {
    padding: 20px;
  }
  .course-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  .course-title {
    font-size: 1.5rem;
  }
  .course-actions {
    width: 100%;
    justify-content: center;
  }
  .meta-group {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  .meta-item {
    padding: 10px;
  }
  .chapter-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  .chapter-meta {
    flex-direction: column;
    gap: 8px;
  }
  .chapter-status {
    align-self: flex-end;
  }
}
</style>
