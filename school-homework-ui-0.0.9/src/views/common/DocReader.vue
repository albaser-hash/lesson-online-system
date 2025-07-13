<template>
  <div class="doc-reader-page">
    <div class="doc-reader-header">
      <span class="doc-reader-title">{{ title }}</span>
      <el-button class="doc-back-btn" @click="goBackToCourse" icon="el-icon-arrow-left" type="text">返回课程</el-button>
    </div>
    <div v-if="loading" class="doc-loading">
      <el-spinner></el-spinner>
      <div class="doc-loading-text">加载中...</div>
    </div>
    <div v-else class="doc-content-area">
      <div class="doc-content-flex">
        <!-- 有解析内容时显示分页阅读 -->
        <div v-if="hasContent && pages.length > 0" class="doc-page-content">
          <div v-if="currentPage <= pages.length && currentPage > 0" class="doc-page-text">
            <div v-if="isMarkdown" v-html="renderedPages[currentPage-1]" class="markdown-body"></div>
            <div v-else>{{ pages[currentPage-1].content }}</div>
          </div>
          <div v-else class="doc-empty">暂无内容</div>
        </div>
        <!-- 没有解析内容时显示下载选项 -->
        <div v-else class="doc-download-area">
          <div class="doc-download-content">
            <i class="el-icon-document doc-download-icon"></i>
            <div class="doc-download-message">{{ downloadMessage }}</div>
            <div v-if="downloadUrl" class="doc-download-info">
              <p><strong>文件名：</strong>{{ fileName }}</p>
              <el-button type="primary" @click="downloadFile" class="doc-download-btn">
                <i class="el-icon-download"></i>
                下载文档
              </el-button>
            </div>
            <div v-else class="doc-download-error">
              <p>无法获取原始文件，请联系管理员</p>
            </div>
          </div>
        </div>
        <!-- 阅读完成提示（仅在有内容时显示） -->
        <div v-if="hasContent && pages.length && finished" class="doc-finished">
          <i class="el-icon-success doc-finished-icon"></i> 已全部阅读完成
        </div>
      </div>
      <!-- 分页器始终在底部 -->
      <el-pagination
        v-if="hasContent && pages.length > 1"
        class="doc-pagination"
        background
        layout="prev, pager, next"
        :page-size="1"
        :total="pages.length"
        :current-page.sync="currentPage"
        @current-change="onPageChange"
      />
    </div>
  </div>
</template>
<script>
import { mapState } from 'vuex'
import marked from 'marked'
export default {
  name: 'DocReader',
  props: {
    chapterId: { type: [String, Number], default: '' },
    visible: { type: Boolean, default: true },
    title: { type: String, default: '文档阅读' },
    docJson: { type: String, default: '' }
  },
  data() {
    return {
      loading: false,
      pages: [
        { content: '# 第一章：Vue.js简介\nVue.js 是一套用于构建用户界面的渐进式框架。' },
        { content: '## 第二章：组件化开发\n组件系统是Vue 的核心特性之一。' },
        { content: '### 第三章：响应式原理\nVue 的响应式系统使得数据变化自动驱动视图更新。' }
      ],
      currentPage: 1,
      finished: false,
      hasContent: true,
      downloadMessage: '',
      downloadUrl: '',
      fileName: 'vue-course.md',
      isMarkdown: true,
      renderedPages: []
    }
  },
  computed: {
    ...mapState({
      userId: state => state.user.id
    }),
    realChapterId() {
      return this.$route.params.chapterId || this.chapterId || ''
    }
  },
  mounted() {
    this.renderedPages = this.pages.map(p => marked(p.content || ''))
  },
  methods: {
    onPageChange(page) {
      this.currentPage = page
      if (this.currentPage === this.pages.length && this.pages.length > 0 && !this.finished) {
        const userType = this.$store?.state?.user?.userType
        if (userType === 'STUDENT') {
          this.finished = true
          this.$emit('finished')
        }
      }
    },
    async downloadFile() {
      this.$message.success('开始下载文档（假数据）')
      setTimeout(() => {
        this.$message.success('文档下载完成（假数据）')
        const userType = this.$store?.state?.user?.userType
        if (userType === 'STUDENT') {
          this.finished = true
          this.$emit('finished')
        }
      }, 1500)
    },
    handleClose() {
      this.$emit('update:visible', false)
    },
    goBackToCourse() {
      const courseId = this.$route.params.courseId
      if (courseId) {
        this.$router.push({ name: 'CourseDetail', params: { id: courseId } })
      } else {
        this.$router.push({ name: 'CourseList' })
      }
    }
  }
}
</script>
<style scoped>
.doc-reader-page {
  width: 100vw;
  min-height: 100vh;
  background: linear-gradient(135deg, #fff0f6 0%, #ffe4ec 100%);
  display: flex;
  flex-direction: column;
  align-items: stretch;
  padding-bottom: 32px;
}
.doc-reader-header {
  width: 100vw;
  margin: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(90deg, #ffe4ec 0%, #ffb6d5 100%);
  border-radius: 0 0 24px 24px;
  color: #ff5c8a;
  font-weight: bold;
  font-size: 22px;
  padding: 22px 32px 12px 32px;
  border-bottom: 1.5px solid #ffb6d5;
  letter-spacing: 1px;
  box-sizing: border-box;
}
.doc-reader-title {
  font-size: 22px;
  font-weight: bold;
  color: #ff5c8a;
}
.doc-back-btn {
  color: #ff5c8a !important;
  font-size: 16px;
  font-weight: bold;
  border-radius: 12px;
  padding: 6px 18px;
  background: #fff0f6 !important;
  border: 1px solid #ffb6d5 !important;
  margin-left: 12px;
  transition: background 0.2s, color 0.2s;
}
.doc-back-btn:hover {
  background: #ffb6d5 !important;
  color: #fff !important;
}
.doc-content-area {
  min-height: 320px;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  width: 100%;
  height: 100%;
  position: relative;
  background: transparent;
  box-sizing: border-box;
  padding: 0 0 0 0;
}
.doc-content-flex {
  flex: 1 1 auto;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  min-height: 0;
}
.doc-page-content {
  width: 100%;
  min-height: 220px;
  background: linear-gradient(120deg, #fff5f8 60%, #ffe4ec 100%);
  border-radius: 18px;
  box-shadow: 0 2px 8px #f0c1d6cc, 0 1px 4px #ffb6d540;
  padding: 36px 28px;
  font-size: 18px;
  color: #333;
  margin-bottom: 22px;
  white-space: pre-wrap;
  word-break: break-all;
  line-height: 2.1;
  transition: box-shadow 0.2s;
}
.doc-page-content:hover {
  box-shadow: 0 4px 16px #ffb6d5cc, 0 2px 8px #f0c1d6cc;
}
.doc-page-text {
  min-height: 180px;
}
.doc-empty {
  color: #bbb;
  text-align: center;
  font-size: 17px;
  padding: 60px 0;
}
.doc-pagination {
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 22px 0 0 0;
  border-top: 1px solid #ffe4ec;
  background: transparent;
  padding-top: 12px;
  box-sizing: border-box;
}
.doc-pagination >>> .el-pager li {
  border-radius: 10px;
  font-weight: bold;
  color: #ff5c8a;
  font-size: 16px;
  margin: 0 2px;
  transition: background 0.2s, color 0.2s;
}
.doc-pagination >>> .el-pager li.active {
  background: linear-gradient(90deg, #ffb6d5 0%, #ff5c8a 100%) !important;
  color: #fff !important;
  box-shadow: 0 2px 8px #ffb6d580;
}
.doc-pagination >>> .el-pager li:hover {
  background: #ffe4ec !important;
  color: #ff5c8a !important;
}
.doc-finished {
  text-align: center;
  color: #67C23A;
  font-weight: bold;
  font-size: 19px;
  margin-top: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.doc-finished-icon {
  font-size: 24px;
  margin-right: 10px;
}
.doc-footer {
  display: flex;
  justify-content: center;
  padding: 16px 0 10px 0;
  background: transparent;
}
.doc-close-btn {
  background: linear-gradient(90deg, #ffb6d5 0%, #ff5c8a 100%) !important;
  border: none !important;
  color: #fff !important;
  font-weight: bold;
  border-radius: 14px;
  padding: 12px 40px;
  font-size: 17px;
  box-shadow: 0 2px 8px #ffb6d580;
  transition: background 0.2s, box-shadow 0.2s;
}
.doc-close-btn:hover {
  background: linear-gradient(90deg, #ff5c8a 0%, #ffb6d5 100%) !important;
  box-shadow: 0 4px 16px #ffb6d5cc;
}
.doc-download-area {
  width: 100%;
  padding: 36px 28px;
  background: linear-gradient(120deg, #fff5f8 60%, #ffe4ec 100%);
  border-radius: 18px;
  box-shadow: 0 2px 8px #f0c1d6cc, 0 1px 4px #ffb6d540;
  margin-bottom: 22px;
}
.doc-download-content {
  text-align: center;
}
.doc-download-icon {
  font-size: 54px;
  color: #ff5c8a;
  margin-bottom: 18px;
}
.doc-download-message {
  font-size: 19px;
  color: #ff5c8a;
  margin-bottom: 18px;
  font-weight: bold;
}
.doc-download-info {
  margin-top: 18px;
}
.doc-download-info p {
  margin: 10px 0;
}
.doc-download-btn {
  background: linear-gradient(90deg, #ffb6d5 0%, #ff5c8a 100%) !important;
  border: none !important;
  color: #fff !important;
  font-weight: bold;
  border-radius: 14px;
  padding: 12px 40px;
  font-size: 17px;
  margin-top: 10px;
  box-shadow: 0 2px 8px #ffb6d580;
  transition: background 0.2s, box-shadow 0.2s;
}
.doc-download-btn:hover {
  background: linear-gradient(90deg, #ff5c8a 0%, #ffb6d5 100%) !important;
  box-shadow: 0 4px 16px #ffb6d5cc;
}
.doc-download-error {
  color: #f56c6c;
  font-size: 17px;
  margin-top: 18px;
}
@media (max-width: 900px) {
  .doc-reader-page {
    width: 100vw;
    min-height: 100vh;
    padding-bottom: 16px;
  }
  .doc-reader-header {
    font-size: 18px;
    padding: 14px 10px 8px 10px;
    border-radius: 0 0 18px 18px;
    width: 100vw;
    margin: 0;
  }
  .doc-content-area {
    height: 100%;
    padding: 0 0 0 0;
  }
  .doc-content-flex {
    flex: 1 1 auto;
    display: flex;
    flex-direction: column;
    justify-content: flex-start;
    min-height: 0;
  }
  .doc-page-content, .doc-download-area {
    padding: 10px 4px;
    font-size: 15px;
    border-radius: 0;
    margin-bottom: 12px;
  }
  .doc-footer {
    padding: 8px 0 4px 0;
  }
  .doc-close-btn, .doc-download-btn {
    padding: 10px 0;
    width: 100%;
    font-size: 15px;
    border-radius: 0;
  }
  .doc-pagination {
    width: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    margin: 12px 0 0 0;
    border-top: 1px solid #ffe4ec;
    background: #fff0f6;
    padding-top: 8px;
    box-sizing: border-box;
  }
}
@media (max-width: 600px) {
  .doc-reader-page {
    width: 100vw;
    min-height: 100vh;
    padding-bottom: 8px;
  }
  .doc-reader-header {
    font-size: 16px;
    padding: 10px 4px 6px 4px;
    border-radius: 0 0 12px 12px;
    width: 100vw;
    margin: 0;
  }
  .doc-content-area {
    height: 100%;
    padding: 0 0 0 0;
  }
  .doc-content-flex {
    flex: 1 1 auto;
    display: flex;
    flex-direction: column;
    justify-content: flex-start;
    min-height: 0;
  }
  .doc-page-content, .doc-download-area {
    padding: 4px 2px;
    font-size: 14px;
    border-radius: 0;
    margin-bottom: 8px;
  }
  .doc-footer {
    padding: 4px 0 2px 0;
  }
  .doc-close-btn, .doc-download-btn {
    padding: 8px 0;
    width: 100%;
    font-size: 14px;
    border-radius: 0;
  }
  .doc-pagination {
    width: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    margin: 8px 0 0 0;
    border-top: 1px solid #ffe4ec;
    background: #fff0f6;
    padding-top: 4px;
    box-sizing: border-box;
  }
}
</style>
<style src="github-markdown-css/github-markdown.css"></style>
