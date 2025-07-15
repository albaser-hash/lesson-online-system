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
import axios from 'axios'
import { getChapterDocPages, getDocDownloadUrl, reportChapterProgress } from '@/api/chapters'
import { mapState } from 'vuex'
import marked from 'marked'
import { addToCart } from '@/api/cart'
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
      pages: [],
      currentPage: 1,
      finished: false,
      hasContent: false,
      downloadMessage: '',
      downloadUrl: '',
      fileName: '',
      isMarkdown: false,
      renderedPages: []
    }
  },
  created() {
  },
  mounted() {
    // 页面模式：只要有 chapterId 就加载
    if (this.realChapterId) {
      this.loadPages()
    }
  },
  watch: {
    visible(val) {
      if (val) {
        if (this.docJson) {
          this.parseDocJson(this.docJson)
        } else if (this.realChapterId) {
          this.loadPages()
        }
        // 打开时如果已在最后一页且未上报，自动触发
        if (this.hasContent && this.currentPage === this.pages.length && this.pages.length > 0 && !this.finished) {
          const userType = this.$store?.state?.user?.userType
          if (userType === 'STUDENT') {
            this.finished = true
            this.$emit('finished')
            this.reportDocFinished && this.reportDocFinished()
            window.dispatchEvent(new Event('doc-finished-debug'))
          }
        }
      }
    },
    currentPage(val) {
      if (val === this.pages.length && this.pages.length > 0) {
        const userType = this.$store?.state?.user?.userType
        if (userType === 'STUDENT') {
          this.finished = true
          this.$emit('finished')
          this.reportDocFinished && this.reportDocFinished()
        }
      }
    },
    docJson: {
      handler(val) {
        if (val) {
          this.parseDocJson(val)
        }
      },
      immediate: true
    },
    chapterId(val) {
      if (this.visible && !this.docJson && this.realChapterId) {
        this.loadPages()
      }
    }
  },
  computed: {
    ...mapState({
      userId: state => state.user.id
    }),
    realChapterId() {
      // 路由优先，props 兜底
      return this.$route.params.chapterId || this.chapterId || ''
    }
  },
  methods: {
    async loadPages() {
      this.loading = true
      this.pages = []
      this.currentPage = 1
      this.finished = false
      this.hasContent = false
      this.downloadMessage = ''
      this.downloadUrl = ''
      this.fileName = ''

      try {
        const res = await getChapterDocPages(this.realChapterId, this.userId)

        if (!res.data || res.data.code !== 200) {
          // 处理无权限、未登录等情况
          if (res.data && res.data.code === 403) {
            const userType = this.$store?.state?.user?.userType
            if (userType === 'TEACHER') {
              this.$message.error('没有权限阅读该文档')
              this.loading = false
              return
            }
            // 学生或其它，弹窗引导购买
            this.$confirm('您未购买该课程，是否立即购买？', '无权限阅读', {
              confirmButtonText: '去购买',
              cancelButtonText: '取消',
              type: 'warning'
            }).then(async () => {
              // 先加购再跳转购物车
              const courseId = this.$route.params.courseId
              if (courseId) {
                try {
                  const response = await addToCart({ courseId })
                  const res = response.data
                  if (res.code === 200) {
                    this.$message.success('已加入购物车')
                    this.handleClose()
                    this.$router.push('/cart')
                  } else {
                    this.$message.error(res.msg || '加入购物车失败')
                  }
                } catch (e) {
                  this.$message.error('加入购物车失败')
                }
              } else {
                this.$message.error('课程ID无效，无法加入购物车')
              }
            }).catch(() => {
              this.handleClose()
            })
            this.loading = false
            return
          }
          // 其它错误
          this.hasContent = false
          this.downloadMessage = res.data?.msg || '加载文档失败，请下载原始文件查看'
          this.loading = false
          return
        }
        const docData = res.data.data
        if (docData && docData.hasContent && docData.pages) {
          // 有解析的内容
          this.pages = docData.pages
          this.hasContent = true
          // 自动判断是否为 Markdown
          if (this.pages.length > 0) {
            const firstContent = this.pages[0].content || ''
            this.isMarkdown = firstContent.trim().startsWith('#') || firstContent.includes('**') || firstContent.includes('|')
            if (this.isMarkdown) {
              this.renderedPages = this.pages.map(p => marked(p.content || ''))
            } else {
              this.renderedPages = []
            }
          }
        } else {
          // 没有解析的内容，显示下载选项
          this.hasContent = false
          this.downloadMessage = docData?.message || '文档尚未解析，请下载原始文件查看'
          this.downloadUrl = docData?.downloadUrl || ''
          this.fileName = docData?.fileName || ''
          this.isMarkdown = this.fileName && this.fileName.toLowerCase().endsWith('.md')
        }
      } catch (e) {
        this.hasContent = false
        this.downloadMessage = '加载文档失败，请下载原始文件查看'
      }
      this.loading = false
      // 单页文档自动上报
      if (this.hasContent && this.pages.length === 1 && !this.finished) {
        const userType = this.$store?.state?.user?.userType
        if (userType === 'STUDENT') {
          this.finished = true
          this.$emit('finished')
          this.reportDocFinished && this.reportDocFinished()
          window.dispatchEvent(new Event('doc-finished-debug'))
        }
      }
    },
    parseDocJson(val) {
      this.loading = true
      this.pages = []
      this.currentPage = 1
      this.finished = false
      this.hasContent = false
      this.downloadMessage = ''
      this.downloadUrl = ''
      this.fileName = ''

      try {
        const json = JSON.parse(val)
        if (json.pages && json.pages.length > 0) {
          this.pages = json.pages
          this.hasContent = true
          // 自动判断是否为 Markdown
          if (this.pages.length > 0) {
            const firstContent = this.pages[0].content || ''
            this.isMarkdown = firstContent.trim().startsWith('#') || firstContent.includes('**') || firstContent.includes('|')
            if (this.isMarkdown) {
              this.renderedPages = this.pages.map(p => marked(p.content || ''))
            } else {
              this.renderedPages = []
            }
          }
        } else {
          this.hasContent = false
          this.downloadMessage = '文档内容为空，请下载原始文件查看'
        }
      } catch (e) {
        console.error('docJson解析失败', e)
        this.hasContent = false
        this.downloadMessage = '文档解析失败，请下载原始文件查看'
      }
      this.loading = false
      // 单页文档自动上报
      if (this.hasContent && this.pages.length === 1 && !this.finished) {
        const userType = this.$store?.state?.user?.userType
        if (userType === 'STUDENT') {
          this.finished = true
          this.$emit('finished')
          this.reportDocFinished && this.reportDocFinished()
          window.dispatchEvent(new Event('doc-finished-debug'))
        }
      }
    },
    onPageChange(page) {
      this.currentPage = page
      if (this.currentPage === this.pages.length && this.pages.length > 0 && !this.finished) {
        const userType = this.$store?.state?.user?.userType
        if (userType === 'STUDENT') {
          this.finished = true
          this.$emit('finished')
          this.reportDocFinished && this.reportDocFinished()
        }
      }
    },
    async downloadFile() {
      if (this.realChapterId) {
        try {
          const res = await getDocDownloadUrl(this.realChapterId, this.userId)
          if (res.data && res.data.code === 200 && res.data.data) {
            const link = document.createElement('a')
            link.href = res.data.data
            link.download = this.fileName || 'document'
            link.target = '_blank'
            document.body.appendChild(link)
            link.click()
            document.body.removeChild(link)
            this.$message.success('开始下载文档（5分钟内有效）')
            // 下载时自动标记为学习完成（仅学生）
            const userType = this.$store?.state?.user?.userType
            if (userType === 'STUDENT') {
              this.finished = true
              this.$emit('finished')
              this.reportDocFinished && this.reportDocFinished()
            }
          } else if (res.data && res.data.msg && res.data.msg.includes('登录')) {
            this.$confirm('请先登录后下载文档', '提示', { type: 'warning' }).then(() => {
              this.$router.push('/login')
            })
          } else if (res.data && res.data.msg && res.data.msg.includes('购买')) {
            this.$confirm('请先购买课程后下载文档，是否加入购物车？', '提示', { type: 'warning' }).then(() => {
              this.$emit('add-to-cart')
            })
          } else if (res.data && res.data.msg) {
            this.$confirm(res.data.msg, '下载失败', { type: 'error' })
          } else {
            this.$message.error('获取下载链接失败')
          }
        } catch (e) {
          this.$message.error('获取下载链接失败')
        }
      } else {
        this.$message.error('下载链接无效')
      }
    },
    handleClose() {
      this.$emit('update:visible', false)
    },
    goBackToCourse() {
      // 跳转到课程详情，路由参数名是 id
      const courseId = this.$route.params.courseId
      if (courseId) {
        this.$router.push({ name: 'CourseDetail', params: { id: courseId } })
      } else {
        this.$router.push({ name: 'CourseList' }) // 兜底跳转到课程列表
      }
    },
    async reportDocFinished() {
      const courseId = this.$route.params.courseId
      const chapterId = this.realChapterId
      if (!courseId || !chapterId) return
      try {
        await reportChapterProgress({ courseId, chapterId })
      } catch {}
    }
  }
}
</script>

<style scoped>
/* 页面主容器样式 */
.doc-reader-page {
  width: 100vw;
  min-height: 100vh;
  background: linear-gradient(135deg, #fff0f6 0%, #ffe4ec 100%);
  display: flex;
  flex-direction: column;
  align-items: stretch;
  padding-bottom: 32px;
}
/* 顶部标题栏 */
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
/* 内容区样式（继承原有） */
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
/* 新增内容区flex容器，内容撑满，分页栏始终底部 */
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

/* 响应式优化 */
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
