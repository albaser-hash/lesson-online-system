<template>
  <div class="video-fullscreen">
    <div class="video-header">
      <el-button icon="el-icon-back" @click="goBackToCourse" class="back-btn" type="primary" style="margin-right: 16px;">
        返回课程详情
      </el-button>
      <span class="video-title"><i class="el-icon-video-play"></i> {{ title }}</span>
    </div>
    <div class="video-container">
      <video
        ref="video"
        :src="videoUrl"
        controls
        controlsList="nodownload"
        @contextmenu.prevent
        autoplay
        class="video-player"
        poster="/src/assets/images/default-course.jpg"
        @loadedmetadata="onLoadedMetadata"
        @timeupdate="onTimeUpdate"
        @seeking="onSeeking"
        @ended="onEnded"
        @play="onPlay"
        @pause="onPause"
      ></video>
      <div class="video-tips">
        <p><i class="el-icon-info"></i> 请完整观看视频，系统会记录您的学习进度</p>
      </div>
    </div>
  </div>
</template>

<script>
import { reportChapterProgress } from '@/api/chapters'
export default {
  name: 'VideoPlayerPage',
  data() {
    return {
      watched: [],
      interval: 5,
      duration: 0,
      isPlaying: false,
      videoUrl: '',
      title: '',
      chapterId: '',
      courseId: ''
    }
  },
  created() {
    // 支持 params 和 query 方式
    this.videoUrl = this.$route.params.videoUrl || this.$route.query.videoUrl
    this.title = this.$route.params.chapterName || this.$route.query.chapterName || '视频播放'
    this.chapterId = this.$route.params.chapterId || this.$route.query.chapterId
    this.courseId = this.$route.params.courseId || this.$route.query.courseId
  },
  methods: {
    goBackToCourse() {
      this.$router.push({
        name: 'CourseDetail',
        params: { id: this.courseId }
      })
    },
    onLoadedMetadata(e) {
      this.duration = e.target.duration
    },
    onPlay() {
      this.isPlaying = true
    },
    onPause() {
      this.isPlaying = false
    },
    onTimeUpdate(e) {
      const userType = this.$store?.state?.user?.userType
      if (userType !== 'STUDENT') return
      const idx = Math.floor(e.target.currentTime / this.interval)
      const maxIdx = this.watched.lastIndexOf(true)
      if (idx <= maxIdx + 1) {
        this.$set(this.watched, idx, true)
      }
    },
    onSeeking(e) {
      const userType = this.$store?.state?.user?.userType
      if (userType !== 'STUDENT') return
      const idx = Math.floor(e.target.currentTime / this.interval)
      const maxIdx = this.watched.lastIndexOf(true)
      if (idx > maxIdx + 1) {
        this.$confirm('请按顺序观看，不能跳过！', '提示', {
          confirmButtonText: '知道了',
          showCancelButton: false,
          type: 'warning'
        })
      }
    },
    async onEnded(e) {
      const userType = this.$store?.state?.user?.userType
      if (userType !== 'STUDENT') return
      
      const totalBlocks = Math.ceil(this.duration / this.interval)
      const allWatched = this.watched.length >= totalBlocks && this.watched.slice(0, totalBlocks).every(Boolean)
      if (allWatched) {
        try {
          await reportChapterProgress({
            courseId: this.courseId,
            chapterId: this.chapterId
          })
          this.$alert('视频学习进度已记录', '提示', {
            confirmButtonText: '确定',
            type: 'success'
          })
        } catch {
          this.$alert('视频进度上报失败', '提示', {
            confirmButtonText: '确定',
            type: 'warning'
          })
        }
        this.$confirm('已完成本节学习，是否返回课程详情？', '提示', {
          confirmButtonText: '立即返回',
          cancelButtonText: '留在本页',
          type: 'success'
        }).then(() => {
          this.goBackToCourse()
        })
      } else {
        this.$confirm('请完整观看视频，不能跳过！', '提示', {
          confirmButtonText: '知道了',
          showCancelButton: false,
          type: 'warning'
        })
      }
    }
  }
}
</script>

<style scoped>
.video-fullscreen {
  width: 100vw;
  min-height: 100vh;
  background: #000;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
}
.video-header {
  width: 100vw;
  color: #fff;
  font-size: 20px;
  padding: 18px 24px 10px 24px;
  display: flex;
  justify-content: flex-start;
  align-items: center;
  background: #111;
  box-sizing: border-box;
}
.video-title {
  display: flex;
  align-items: center;
  font-weight: bold;
  color: #ff5c8a;
  font-size: 20px;
}
.back-btn {
  background: #ffb6d5 !important;
  border-color: #ffb6d5 !important;
  color: #fff !important;
  font-weight: bold;
  border-radius: 12px;
  padding: 10px 20px;
}
.video-container {
  width: 100vw;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 0 24px 0;
  box-sizing: border-box;
}
.video-player {
  width: 80vw;
  max-width: 900px;
  height: 45vw;
  max-height: 60vh;
  background: #000;
  border-radius: 8px;
  margin-top: 16px;
}
.video-tips {
  margin-top: 20px;
  text-align: left;
  color: #fff;
  font-size: 14px;
  max-width: 900px;
  width: 80vw;
}
.video-tips p {
  margin: 8px 0;
  display: flex;
  align-items: center;
}
.video-tips i {
  margin-right: 8px;
  color: #ffb6d5;
}
@media (max-width: 900px) {
  .video-player, .video-tips {
    width: 98vw;
    max-width: 98vw;
  }
  .video-player {
    height: 56vw;
    max-height: 50vh;
  }
}
@media (max-width: 600px) {
  .video-header {
    font-size: 15px;
    padding: 10px 8px 6px 8px;
  }
  .video-title {
    font-size: 15px;
  }
  .video-player {
    width: 100vw;
    max-width: 100vw;
    height: 56vw;
    max-height: 40vh;
  }
  .video-tips {
    font-size: 12px;
    width: 100vw;
    max-width: 100vw;
  }
}
</style> 