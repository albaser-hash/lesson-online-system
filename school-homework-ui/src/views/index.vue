<template>
  <div class="app-container" :style="{paddingTop: containerPaddingTop + 'px'}">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="floating-circle circle-1"></div>
      <div class="floating-circle circle-2"></div>
      <div class="floating-circle circle-3"></div>
    </div>

    <div ref="mainTitle" class="main-title">
      <span class="title-text">在线教育系统</span>
      <div class="title-subtitle">让学习更美好</div>
    </div>

    <div class="search-section">
      <div class="search-container">
        <!-- 分类标签 -->
        <div class="category-tags">
          <el-tag
            :class="['category-tag', { active: selectedCategory === '' }]"
            @click="selectCategory('')"
            effect="plain"
          >
            <i class="el-icon-star-on"></i>
            全部
          </el-tag>
          <el-tag
            v-for="category in categories"
            :key="category.categoryId"
            :class="['category-tag', { active: selectedCategory === category.categoryId }]"
            @click="selectCategory(category.categoryId)"
            effect="plain"
          >
            {{ category.categoryName }}
          </el-tag>
        </div>

        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="🔍搜索"
            class="search-input"
            @keyup.enter="doSearch"
          />
          <el-button type="primary" @click="doSearch" class="search-btn">
            <i class="el-icon-search"></i>
            搜索
          </el-button>
        </div>
      </div>
    </div>

    <!-- PC端菜单（正式版） -->
    <el-menu
      :default-active="activeMenu"
      mode="horizontal"
      class="main-nav"
      background-color="#fff"
      text-color="#333"
      active-text-color="#ffb6d5"
      :style="menuStyle"
    >
      <el-menu-item
        v-for="item in dynamicMenus"
        :key="item.menuPath"
        :index="item.menuPath"
        @click="goMenu(item.menuPath)"
      >
        {{ item.menuName }}
      </el-menu-item>
    </el-menu>

    <!-- 登录/注册按钮 -->
    <div v-if="!isLogin && !isMobile" class="login-section">
      <div class="guest-user">
        <div class="avatar-wrapper">
          <img :src="require('@/assets/images/profile.jpg')" class="avatar" />
          <div class="username">游客</div>
        </div>
      </div>
      <div class="login-buttons">
        <el-button type="primary" @click="goLogin" size="small" class="login-btn">
          <i class="el-icon-user"></i>
          登录
        </el-button>
        <el-button @click="goRegister" size="small" class="register-btn">
          <i class="el-icon-plus"></i>
          注册
        </el-button>
      </div>
    </div>

    <!-- 移动端汉堡菜单 -->
    <template v-if="isMobile">
      <el-button class="hamburger" @click="drawerVisible = true" circle size="medium" style="position:fixed;top:12px;left:12px;z-index:200;background:#ffb6d5;color:#fff;">
        <i class="el-icon-menu"></i>
      </el-button>
      <!-- 移动端登录按钮 -->
      <div v-if="!isLogin" class="mobile-login-section">
        <div class="guest-user">
          <div class="avatar-wrapper mobile-avatar-wrapper">
            <img :src="require('@/assets/images/profile.jpg')" class="avatar" />
            <div class="username">游客</div>
          </div>
        </div>
        <div class="mobile-login-buttons">
          <el-button type="primary" @click="goLogin" size="mini">登录</el-button>
          <el-button @click="goRegister" size="mini">注册</el-button>
        </div>
      </div>
      <el-drawer :visible.sync="drawerVisible" direction="ltr" size="70%" custom-class="mobile-drawer">
        <el-menu :default-active="activeMenu" mode="vertical" @select="goMenu">
          <el-menu-item
            v-for="item in dynamicMenus"
            :key="item.menuPath"
            :index="item.menuPath"
            @click="goMenu(item.menuPath)"
          >
            {{ item.menuName }}
          </el-menu-item>
        </el-menu>
      </el-drawer>
    </template>

    <!-- 个人中心头像下拉 -->
    <template v-if="isLogin">
      <div class="notification-entry">
        <el-badge
          :value="unreadCount > 0 ? unreadCount : null"
          :max="99"
          class="notification-badge"
          type="danger"
        >
          <el-button
            icon="el-icon-bell"
            @click="goNotificationPage"
            circle
            size="medium"
            :class="{'has-unread': unreadCount > 0}"
            style="background:#fff;border:none;"
          ></el-button>
        </el-badge>
      </div>
      <el-dropdown class="user-dropdown" trigger="click">
        <span class="avatar-wrapper">
          <img :src="avatarUrl" class="avatar" @error="onAvatarError" />
          <div class="username">{{ name || userName || '用户' }}</div>
        </span>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item @click.native="goProfile">个人信息</el-dropdown-item>
          <el-dropdown-item divided @click.native="logout">退出登录</el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </template>

    <div class="course-section">
      <div class="section-header">
        <h2 class="section-title">
          <i class="el-icon-reading"></i>
          精选课程
        </h2>
        <div class="section-subtitle">发现最适合你的学习路径</div>
      </div>

      <div class="course-list">
        <el-row :gutter="24" type="flex" justify="start" class="course-row">
          <el-col :xs="24" :sm="12" :md="6" :lg="6" v-for="course in filteredCourses" :key="course.courseId" class="course-col">
            <el-card class="course-card" shadow="hover" @click.native="goCourse(course.courseId)">
              <div class="card-cover">
                <img :src="getCoverImage(course.coverImage)" class="course-cover-img" />
                <div class="card-overlay">
                  <div class="overlay-content">
                    <i class="el-icon-video-play"></i>
                    <span>立即学习</span>
                  </div>
                </div>
              </div>
              <div class="course-info">
                <div class="course-header">
                  <div class="course-title">{{ course.courseName }}</div>
                  <div class="course-desc">{{ course.courseDesc }}</div>
                </div>

                <div class="course-meta-row">
                  <div class="meta-item">
                    <i class="el-icon-user"></i>
                    <span>{{ course.teacherName || '未知教师' }}</span>
                  </div>
                  <div class="meta-item">
                    <i class="el-icon-view"></i>
                    <span>{{ course.studentCount }}人学习</span>
                  </div>
                  <div class="course-price">
                    <span class="current-price">￥{{ course.price }}</span>
                    <span v-if="course.originalPrice" class="original-price">￥{{ course.originalPrice }}</span>
                  </div>
                </div>

                <div class="course-bottom-row">
                  <div class="course-rating">
                    <i class="el-icon-star-on"></i>
                    <span>{{ course.score }}</span>
                  </div>
                  <div class="course-tags">
                    <el-tag
                      v-for="tag in (course.courseTags ? course.courseTags.split(',') : [])"
                      :key="tag"
                      class="course-tag"
                      size="mini"
                    >
                      {{ tag }}
                    </el-tag>
                  </div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <div class="pagination-container">
          <div class="pagination-wrapper">
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

              <div class="page-numbers mobile" v-else>
                <el-button
                  v-for="pageNum in mobileVisiblePages"
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
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState, mapActions } from 'vuex'
import { getCourseList } from '@/api/course'
import { favoriteCourse } from '@/api/favorite'
import { listCourseCategories } from '@/api/courseCategories'
import cache from '@/plugins/cache'
import { getUnreadCount } from '@/api/notification'
import { getNavMenu } from '@/api/menu'
import { logout as apiLogout } from '@/api/login'
export default {
  name: "Index",
  data() {
    return {
      drawerVisible: false,
      isMobile: false,
      courses: [], // 课程数据由后端获取
      menuTop: 70, // 默认导航栏top
      containerPaddingTop: 180, // 默认顶部总高度
      searchKeyword: '',
      selectedCategory: '',
      page: 1, // 当前页码
      pageSize: 8, // 每页条数
      total: 0, // 总条数
      unreadCount: 0,
      jumpPage: ''
    }
  },
  computed: {
    ...mapState({
      token: state => state.user.token,
      userName: state => state.user.userName,
      name: state => state.user.name,
      userType: state => state.user.userType,
      avatar: state => state.user.avatar,
      userId: state => state.user.id,
      dynamicMenus: state => state.dynamicMenus,
      categories: state => state.categories
    }),
    avatarUrl() {
      if (!this.avatar || (!/^https?:\/\//.test(this.avatar) && !this.avatar.startsWith('/'))) {
        return require('@/assets/images/profile.jpg')
      }
      return this.avatar
    },
    isLogin() {
      return !!this.token
    },
    // 获取用户类型，如果没有则默认学生
    displayUserType() {
      return this.userType || 'STUDENT'
    },
    activeMenu() {
      return this.$route.path
    },
    menuStyle() {
      return `position:fixed;top:${this.menuTop}px;z-index:100;`
    },
    filteredCourses() {
      return this.courses
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

      if (totalPages <= 3) {
        for (let i = 1; i <= totalPages; i++) {
          pages.push(i)
        }
      } else {
        if (currentPage <= 2) {
          for (let i = 1; i <= 3; i++) {
            pages.push(i)
          }
        } else if (currentPage >= totalPages - 1) {
          for (let i = totalPages - 2; i <= totalPages; i++) {
            pages.push(i)
          }
        } else {
          pages.push(currentPage - 1)
          pages.push(currentPage)
          pages.push(currentPage + 1)
        }
      }

      return pages
    }
  },
  mounted() {
    // 初始化store数据
    this.$store.dispatch('initializeStore')

    this.checkMobile()
    this.$nextTick(() => {
      this.updateMenuTop()
      this.updateContainerPadding()
    })
    window.addEventListener('resize', this.checkMobile)
    window.addEventListener('resize', this.updateMenuTop)
    window.addEventListener('resize', this.updateContainerPadding)
    // 获取课程列表
    this.loadCourses()
    // 拉取未读消息
    this.fetchUnreadCount()
    // 只在store无数据时拉取菜单和分类
    if (!this.dynamicMenus || this.dynamicMenus.length === 0) {
      this.fetchDynamicMenus()
    }
    if (!this.categories || this.categories.length === 0) {
      this.fetchCategories()
    }
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.checkMobile)
    window.removeEventListener('resize', this.updateMenuTop)
    window.removeEventListener('resize', this.updateContainerPadding)
  },
  methods: {
    ...mapActions(['fetchDynamicMenus', 'fetchCategories', 'forceRefreshMenus']),
    checkMobile() {
      this.isMobile = window.innerWidth <= 600
    },
    goMenu(path) {
      this.drawerVisible = false
      if (this.$route.path !== path) {
        this.$router.push(path)
      }
    },
    goLogin() {
      this.$router.push('/login');
    },
    goRegister() {
      this.$router.push('/register');
    },
    goProfile() {
      this.$router.push('/profile')
    },
    logout() {
      apiLogout().finally(() => {
        // 清理后端缓存和前端缓存
        this.$store.dispatch('clearNavAndCategories')
        this.$store.dispatch('FedLogOut').then(() => {
          this.$message.success('欢迎再次使用在线教育系统！')
          this.$router.replace('/login')
        })
      })
    },
    goCourse(id) {

      try {
        this.$router.push(`/course/${id}`)
      } catch (error) {
        this.$message.error('跳转失败，请重试')
      }
    },
    updateMenuTop() {
      this.$nextTick(() => {
        const el = this.$refs.mainTitle;
        if (el) {
          this.menuTop = el.offsetHeight;
          this.updateContainerPadding();
        }
      });
    },
    updateContainerPadding() {
      this.$nextTick(() => {
        const title = this.$refs.mainTitle;
        const nav = document.querySelector('.main-nav');
        let navH = nav ? nav.offsetHeight : 60;
        let titleH = title ? title.offsetHeight : 70;
        this.containerPaddingTop = navH + titleH;
      });
    },
    onAvatarError(e) {
      e.target.src = require('@/assets/images/profile.jpg')
    },
    doSearch() {
      this.page = 1 // 搜索时重置页码
      this.loadCourses()
    },
    loadCourses() {
      getCourseList({
        page: this.page,
        pageSize: this.pageSize,
        keyword: this.searchKeyword,
        categoryId: this.selectedCategory
      }).then(res => {
        const data = res.data.data
        this.courses = data.records || []
        this.total = data.total || 0
        // 加载完课程后，检查收藏状态
        this.checkFavoritesStatus()
      }).catch((error) => {
        this.courses = []
        this.total = 0
      })
    },
    handlePageChange(val) {
      this.page = val
      this.loadCourses()
    },
    getCoverImage(coverImage) {
      if (!coverImage || !/^https?:\/\//.test(coverImage)) {
        return require('@/assets/images/default-course.jpg')
      }
      return coverImage
    },
    // 检查收藏状态
    checkFavoritesStatus() {
      if (!this.isLogin || this.userType !== 'STUDENT') {
        return
      }

      // 从本地缓存获取收藏数据
      const allFav = cache.local.getJSON('favorites') || {}
      const userFav = allFav[this.userId] || []

      // 为每个课程设置收藏状态
      this.courses.forEach(course => {
        course.isFavorited = userFav.some(f => f.courseId === course.courseId)
      })
    },
    // 切换收藏状态
    async toggleFavorite(course) {
      if (!this.isLogin) {
        this.$message.info('请先登录后再收藏课程')
        this.$router.push('/login')
        return
      }

      if (this.userType !== 'STUDENT') {
        this.$message.info('只有学生可以收藏课程')
        return
      }

      try {
        const courseId = course.courseId
        const response = await favoriteCourse(courseId)

        // 支持多种成功状态码
        if (response.code === 1 || response.code === 200 || response.code === '1' || response.code === '200') {
          course.isFavorited = !course.isFavorited
          this.$message.success(course.isFavorited ? '收藏成功' : '取消收藏成功')

          // 更新本地缓存
          let allFav = cache.local.getJSON('favorites') || {}
          let userFav = allFav[this.userId] || []

          if (course.isFavorited) {
            userFav.push({
              courseId: course.courseId,
              courseName: course.courseName,
              createTime: new Date().toLocaleString()
            })
          } else {
            userFav = userFav.filter(f => f.courseId !== course.courseId)
          }

          allFav[this.userId] = userFav
          cache.local.setJSON('favorites', allFav)
        } else {
          this.$message.error(response.msg || '操作失败')
        }
      } catch (error) {
        this.$message.error('操作失败，请稍后重试')
      }
    },
    loadCategories() {
      listCourseCategories({ page: 1, pageSize: 100 }).then(res => {
        const data = res.data.data
        this.categories = data.records || data || []
      }).catch((error) => {
        this.categories = []
      })
    },
    selectCategory(categoryId) {
      this.selectedCategory = categoryId
      this.page = 1 // 搜索时重置页码
      this.loadCourses()
    },
    goNotificationPage() {
      this.$router.push('/notification')
    },
    fetchUnreadCount() {
      if (!this.isLogin) return
      getUnreadCount(this.userId).then(res => {
        this.unreadCount = Number(res.data.data)
      })
    },
    handleJumpPage() {
      const newPage = parseInt(this.jumpPage)
      if (newPage >= 1 && newPage <= Math.ceil(this.total / this.pageSize)) {
        this.page = newPage
        this.loadCourses()
      } else {
        this.$message.error('请输入有效的页码')
      }
    },
  },
  watch: {
    token(val) {
      // 登录状态变化时重新检查收藏状态
      if (this.courses.length > 0) {
        this.checkFavoritesStatus()
      }
      // 登录时拉取菜单和分类，退出时清空
      if (this.isLogin) {
        // 登录时使用强制刷新，确保获取最新数据
        this.$store.dispatch('forceRefreshMenus')
        this.fetchCategories()
      } else {
        this.$store.dispatch('clearNavAndCategories')
      }
    },
    userType(val) {
      // 用户类型变化时重新检查收藏状态
      if (this.courses.length > 0) {
        this.checkFavoritesStatus()
      }
      // 用户类型变化时强制刷新菜单
      if (this.isLogin && val) {
        this.$store.dispatch('forceRefreshMenus')
      }
    }
  },
};
</script>

<style scoped>
.app-container {
  height: 100%;
  background: linear-gradient(135deg, #fff5f8 0%, #ffe4ec 50%, #ffd6e6 100%);
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
}

/* 背景装饰 */
.bg-decoration {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

.floating-circle {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(45deg, #ffb6d5, #ffe4ec);
  opacity: 0.1;
  animation: float 6s ease-in-out infinite;
}

.circle-1 {
  width: 200px;
  height: 200px;
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.circle-2 {
  width: 150px;
  height: 150px;
  top: 60%;
  right: 15%;
  animation-delay: 2s;
}

.circle-3 {
  width: 100px;
  height: 100px;
  bottom: 20%;
  left: 20%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(180deg); }
}

.main-nav {
  position: fixed !important;
  left: 50%;
  transform: translateX(-50%);
  width: 90%;
  max-width: 1200px;
  z-index: 100;
  background: rgba(255, 255, 255, 0.95) !important;
  backdrop-filter: blur(10px);
  box-shadow: 0 8px 32px rgba(255, 182, 213, 0.2);
  border-radius: 20px;
  padding: 0 40px;
  min-height: 60px;
  border: 1px solid rgba(255, 182, 213, 0.3);
}

::v-deep .el-menu {
  background: transparent !important;
  border-bottom: none !important;
}

::v-deep .el-menu-item {
  color: #ff5c8a !important;
  font-weight: 600;
  font-size: 27px;
  background: transparent !important;
  border-radius: 12px !important;
  transition: all 0.3s ease;
  margin: 0 4px;
  height: 50px;
  line-height: 50px;
  white-space: nowrap;
  min-width: 80px;
  text-align: center;
}

::v-deep .el-menu-item:hover,
::v-deep .el-menu-item.is-active {
  background: linear-gradient(135deg, #ffb6d5, #ff5c8a) !important;
  color: #fff !important;
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(255, 92, 138, 0.3);
}

.login-section {
  position: fixed;
  top: 16px;
  right: 32px;
  z-index: 200;
  display: flex;
  align-items: center;
  gap: 16px;
}

.login-buttons {
  display: flex;
  gap: 8px;
}

.login-btn {
  background: linear-gradient(135deg, #ffb6d5, #ff5c8a);
  border: none;
  color: #fff;
  font-weight: 600;
  border-radius: 20px;
  padding: 8px 16px;
  transition: all 0.3s ease;
}

.login-btn:hover {
  background: linear-gradient(135deg, #ff5c8a, #ffb6d5);
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(255, 92, 138, 0.4);
}

.register-btn {
  background: rgba(255, 182, 213, 0.1);
  border: 2px solid #ffb6d5;
  color: #ff5c8a;
  font-weight: 600;
  border-radius: 20px;
  padding: 8px 16px;
  transition: all 0.3s ease;
}

.register-btn:hover {
  background: #ffb6d5;
  color: #fff;
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(255, 182, 213, 0.4);
}

.avatar-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  gap: 4px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 3px solid #ffb6d5;
  object-fit: cover;
  background: #fff;
  box-shadow: 0 4px 12px rgba(255, 182, 213, 0.3);
  transition: all 0.3s ease;
}

.avatar:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 20px rgba(255, 182, 213, 0.5);
}

.username {
  font-size: 12px;
  color: #ff5c8a;
  font-weight: 600;
  text-align: center;
  white-space: nowrap;
  max-width: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.hamburger {
  box-shadow: 0 4px 16px rgba(255, 182, 213, 0.4);
  border: none;
  background: linear-gradient(135deg, #ffb6d5, #ff5c8a) !important;
  transition: all 0.3s ease;
}

.hamburger:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 24px rgba(255, 182, 213, 0.6);
}

.mobile-avatar-wrapper {
  display: none;
}

.mobile-login-section {
  position: fixed;
  top: 12px;
  right: 12px;
  z-index: 200;
  display: flex;
  align-items: center;
  gap: 8px;
}

.mobile-login-buttons {
  display: flex;
  gap: 4px;
}

.mobile-login-buttons .el-button {
  background: linear-gradient(135deg, #ffb6d5, #ff5c8a);
  border: none;
  color: #fff;
  font-weight: 600;
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 12px;
}

.mobile-login-buttons .el-button:hover {
  background: linear-gradient(135deg, #ff5c8a, #ffb6d5);
  transform: translateY(-1px);
}

::v-deep .mobile-drawer .el-drawer__body {
  background: linear-gradient(135deg, #fff5f8, #ffe4ec);
  padding-top: 20px;
}

.mobile-drawer ::v-deep .el-menu {
  border: none !important;
}

.mobile-drawer ::v-deep .el-menu-item {
  color: #ff5c8a !important;
  font-weight: 600;
  font-size: 16px;
  background: transparent !important;
  border-radius: 12px !important;
  transition: all 0.3s ease;
  margin: 4px 8px;
}

.mobile-drawer ::v-deep .el-menu-item:hover,
.mobile-drawer ::v-deep .el-menu-item.is-active {
  background: linear-gradient(135deg, #ffb6d5, #ff5c8a) !important;
  color: #fff !important;
  transform: translateX(4px);
}

.user-dropdown {
  position: fixed;
  top: 16px;
  right: 32px;
  z-index: 200;
}

.guest-user {
  display: flex;
  align-items: center;
}

.course-section {
  position: relative;
  z-index: 10;
  padding: 40px 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.section-header {
  text-align: center;
  margin-bottom: 40px;
}

.section-title {
  font-size: 2.5rem;
  color: #ff5c8a;
  font-weight: bold;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.section-title i {
  font-size: 2rem;
  color: #ffb6d5;
}

.section-subtitle {
  font-size: 1.1rem;
  color: #ff8ab4;
  font-weight: 500;
}

.course-list {
  margin: 0 auto;
}

.course-row {
  flex-wrap: wrap !important;
}

.course-col {
  display: flex;
  flex-direction: column;
  margin-bottom: 24px;
}

.course-card {
  position: relative;
  overflow: hidden;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 182, 213, 0.3);
  box-shadow: 0 8px 32px rgba(255, 182, 213, 0.15);
  cursor: pointer;
  transition: all 0.4s ease;
  height: 100%;
}

.course-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 16px 48px rgba(255, 182, 213, 0.3);
  border-color: #ffb6d5;
}

.card-cover {
  position: relative;
  width: 100%;
  height: 180px;
  overflow: hidden;
  border-radius: 20px 20px 0 0;
  background: linear-gradient(135deg, #ffe4ec, #ffd6e6);
}

.course-cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.course-card:hover .course-cover-img {
  transform: scale(1.1);
}

.card-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(255, 182, 213, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.course-card:hover .card-overlay {
  opacity: 1;
}

.overlay-content {
  color: #fff;
  font-size: 1.2rem;
  font-weight: 600;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.overlay-content i {
  font-size: 2rem;
}

.course-info {
  padding: 20px;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.course-header {
  margin-bottom: 16px;
}

.course-title {
  color: #333;
  font-weight: bold;
  font-size: 1.2rem;
  line-height: 1.4;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.course-desc {
  color: #666;
  font-size: 0.9rem;
  line-height: 1.4;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 12px;
}

.course-meta-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  gap: 8px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #666;
  font-size: 0.85rem;
  flex: 1;
}

.meta-item i {
  color: #ffb6d5;
  font-size: 1rem;
}

.course-price {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  flex-shrink: 0;
}

.current-price {
  color: #e74c3c;
  font-weight: bold;
  font-size: 1.1rem;
  line-height: 1;
}

.original-price {
  color: #999;
  font-size: 0.8rem;
  text-decoration: line-through;
  line-height: 1;
}

.course-bottom-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.course-rating {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #ff5c8a;
  font-weight: 600;
  flex-shrink: 0;
}

.course-rating i {
  color: #ffb6d5;
  font-size: 1rem;
}

.course-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  flex: 1;
  justify-content: flex-end;
}

.course-tag {
  background: linear-gradient(135deg, #ffb6d5, #ffe4ec);
  color: #ff5c8a;
  border: none;
  border-radius: 8px;
  font-size: 0.7rem;
  font-weight: 600;
  padding: 2px 6px;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.course-tag:hover {
  background: linear-gradient(135deg, #ff5c8a, #ffb6d5);
  color: #fff;
  transform: translateY(-1px);
}

.main-title {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  z-index: 200;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 20px rgba(255, 182, 213, 0.2);
  padding: 20px 0;
  text-align: center;
  border-bottom: 1px solid rgba(255, 182, 213, 0.3);
}

.title-text {
  font-size: 2.5rem;
  color: #ff5c8a;
  font-weight: bold;
  letter-spacing: 2px;
  display: block;
  margin-bottom: 4px;
}

.title-subtitle {
  font-size: 1rem;
  color: #ff8ab4;
  font-weight: 500;
}

.search-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  margin: 20px 0;
  padding: 0 20px;
  width: 100%;
  max-width: 1400px;
  margin-left: auto;
  margin-right: auto;
}

.search-container {
  display: flex;
  align-items: center;
  gap: 10px;
  background: rgba(255, 138, 180, 0.11);
  backdrop-filter: blur(10px);
  padding: 12px 18px;
  border-radius: 25px;
  box-shadow: 0 8px 32px rgba(255, 182, 213, 0.2);
  border: 1px solid rgba(255, 182, 213, 0.3);
  transition: all 0.3s ease;
  max-width: 100%;
  flex-wrap: wrap;
  justify-content: flex-start;
}

.search-container:hover {
  box-shadow: 0 12px 40px rgba(255, 182, 213, 0.3);
  transform: translateY(-2px);
}

@media (min-width: 901px) {
  .search-section {
    flex-direction: row;
    justify-content: center;
    align-items: center;
    gap: 20px;
  }

  .category-tags {
    margin-bottom: 0;
    flex-shrink: 0;
    display: flex;
    align-items: center;
    gap: 6px;
    flex-wrap: wrap;
  }

  .search-box {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .category-tag {
    height: 45px;
    padding: 0 12px;
    font-size: 13px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: transparent;
    border: 2px solid #ffb6d5;
    border-radius: 20px;
    white-space: nowrap;
    flex-shrink: 0;
  }

  .category-tag:hover {
    background: linear-gradient(135deg, #ffb6d5, #ffe4ec);
    color: #fff;
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(255, 182, 213, 0.4);
    border-color: #ffb6d5;
  }

  .category-tag.active {
    background: linear-gradient(135deg, #ff5c8a, #ffb6d5);
    color: #fff;
    box-shadow: 0 4px 16px rgba(255, 92, 138, 0.4);
    border-color: #ff5c8a;
  }

  .search-input {
    width: 80px;
    flex-shrink: 0;
  }

  ::v-deep .search-input .el-input__inner {
    height: 45px;
    line-height: 45px;
    font-size: 11.2px;
  }

  .search-btn {
    height: 45px;
    padding: 0 12px;
    font-size: 11.2px;
    flex-shrink: 0;
  }
}

.search-box {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 24px;
  border-radius: 25px;
  transition: all 0.3s ease;
}

@media (max-width: 900px) {
  .search-box {
    flex-direction: column;
    width: 100%;
    max-width: 400px;
    gap: 16px;
    padding: 16px 24px;
    background: rgba(255, 138, 180, 0.04);
    backdrop-filter: blur(10px);
    border-radius: 25px;
    box-shadow: 0 8px 32px rgba(255, 182, 213, 0.2);
    border: 1px solid rgba(255, 182, 213, 0.3);
  }

  .search-box:hover {
    box-shadow: 0 12px 40px rgba(255, 182, 213, 0.3);
    transform: translateY(-2px);
  }
}

.search-input {
  width: 80px;
  flex-shrink: 0;
}

::v-deep .search-input .el-input__inner {
  border: none;
  background: transparent;
  font-size: 18px;
  color: #333;
  padding: 0;
}

::v-deep .search-input .el-input__inner::placeholder {
  color: #999;
}

::v-deep .search-input .el-input__inner:focus {
  box-shadow: none;
}

.search-btn {
  background: linear-gradient(135deg, #ffb6d5, #ff5c8a);
  border: none;
  color: #fff;
  font-weight: 600;
  border-radius: 20px;
  padding: 10px 20px;
  transition: all 0.3s ease;
}

.search-btn:hover {
  background: linear-gradient(135deg, #ff5c8a, #ffb6d5);
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(255, 92, 138, 0.4);
}

.category-tags {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: center;
  align-items: center;
  margin-bottom: 20px;
}

.category-tag {
  cursor: pointer;
  padding: 10px 20px;
  border: 2px solid #ffb6d5;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.95);
  color: #333;
  font-weight: 500;
  transition: all 0.3s ease;
  font-size: 16px;
  user-select: none;
  white-space: nowrap;
  display: flex;
  align-items: center;
  gap: 6px;
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 8px rgba(255, 182, 213, 0.1);
  min-width: 60px;
  justify-content: center;
  text-align: center;
  height: 40px;
}

.category-tag:hover {
  background: linear-gradient(135deg, #ffb6d5, #ffe4ec);
  color: #fff;
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(255, 182, 213, 0.4);
  border-color: #ffb6d5;
}

.category-tag.active {
  background: linear-gradient(135deg, #ff5c8a, #ffb6d5);
  color: #fff;
  box-shadow: 0 4px 16px rgba(255, 92, 138, 0.4);
  border-color: #ff5c8a;
}

.category-tag i {
  font-size: 14px;
}

.notification-entry {
  position: fixed;
  top: 16px;
  right: 90px;
  z-index: 200;
  display: flex;
  align-items: center;
}

.notification-badge {
  margin-right: 8px;
}

.has-unread {
  box-shadow: 0 0 0 2px #ff5c8a, 0 4px 16px rgba(255, 182, 213, 0.4);
  background: linear-gradient(135deg, #fff0f6, #ffe4ec) !important;
}

.pagination-container {
  margin: 40px auto 0;
  text-align: center;
}

.pagination-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-radius: 12px;
  padding: 20px 32px;
  box-shadow: 0 2px 8px rgba(255, 182, 213, 0.1);
  border: 1px solid #ffe4ec;
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
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

.jump-to-page {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: 16px;
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

@media (max-width: 1200px) {
  .course-section {
    max-width: 1000px;
  }

  .main-nav {
    width: 95%;
    max-width: 1000px;
    padding: 0 30px;
  }

  .search-container {
    gap: 8px;
    padding: 10px 15px;
  }

  .category-tags {
    gap: 5px;
  }

  .category-tag {
    padding: 0 10px;
    font-size: 12px;
    height: 40px;
  }

  .search-input {
    width: 70px;
  }

  .search-btn {
    padding: 0 10px;
    font-size: 9.8px;
    height: 40px;
  }
}

@media (max-width: 900px) {
  .course-section {
    max-width: 700px;
  }

  .section-title {
    font-size: 2rem;
  }

  .title-text {
    font-size: 2rem;
  }

  .search-container {
    flex-direction: column;
    gap: 20px;
    padding: 20px;
  }

  .search-input {
    width: 100%;
  }

  .search-btn {
    width: 100%;
    max-width: 200px;
  }

  .category-tags {
    gap: 10px;
  }

  .category-tag {
    padding: 8px 16px;
    font-size: 14px;
    height: 36px;
  }

  .course-title {
    font-size: 1.1rem;
  }

  .course-desc {
    font-size: 0.85rem;
  }

  .course-meta-row {
    gap: 6px;
  }

  .meta-item {
    font-size: 0.8rem;
  }

  .current-price {
    font-size: 1rem;
  }

  .original-price {
    font-size: 0.75rem;
  }

  ::v-deep .el-menu-item {
    font-size: 16px;
  }

  .main-nav {
    width: 98%;
    max-width: 800px;
    padding: 0 20px;
  }
}

@media (max-width: 600px) {
  .course-section {
    padding: 20px 10px;
  }

  .section-title {
    font-size: 1.5rem;
  }

  .title-text {
    font-size: 1.5rem;
  }

  .title-subtitle {
    font-size: 0.9rem;
  }

  .main-nav {
    display: none;
  }

  .user-dropdown {
    top: 10px !important;
    right: 10px !important;
  }

  .avatar {
    width: 32px;
    height: 32px;
    border-width: 2px;
  }

  .username {
    font-size: 10px;
    max-width: 50px;
  }

  .course-card {
    border-radius: 16px;
    margin-bottom: 16px;
  }

  .card-cover {
    height: 140px;
    border-radius: 16px 16px 0 0;
  }

  .course-info {
    padding: 16px;
  }

  .course-title {
    font-size: 1rem;
    margin-bottom: 6px;
  }

  .course-desc {
    font-size: 0.8rem;
    margin-bottom: 10px;
  }

  .course-meta-row {
    gap: 4px;
    margin-bottom: 10px;
    flex-wrap: wrap;
  }

  .meta-item {
    font-size: 0.75rem;
    min-width: 0;
    flex: 1;
  }

  .meta-item span {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .course-price {
    flex-shrink: 0;
  }

  .current-price {
    font-size: 0.95rem;
  }

  .original-price {
    font-size: 0.7rem;
  }

  .course-bottom-row {
    gap: 6px;
    flex-wrap: wrap;
  }

  .course-rating {
    font-size: 0.8rem;
  }

  .course-rating i {
    font-size: 0.9rem;
  }

  .course-tags {
    gap: 3px;
    flex: 1;
    min-width: 0;
  }

  .course-tag {
    font-size: 0.65rem;
    padding: 2px 4px;
    border-radius: 6px;
  }

  .search-section {
    padding: 0 10px;
    gap: 16px;
  }

  .search-box {
    padding: 12px 16px;
    border-radius: 20px;
    gap: 12px;
  }

  .search-input {
    width: 100%;
  }

  .search-btn {
    width: 100%;
    max-width: none;
    padding: 12px 20px;
  }

  .category-tags {
    gap: 8px;
    margin-bottom: 16px;
  }

  .category-tag {
    padding: 6px 12px;
    font-size: 12px;
    max-width: 80px;
    text-align: center;
    justify-content: center;
    height: 32px;
  }

  .notification-entry {
    top: 10px !important;
    right: 50px !important;
  }

  .floating-circle {
    display: none;
  }

  .course-row {
    margin: 0 -8px;
  }

  .course-col {
    padding: 0 8px;
  }

  /* 分页移动端优化 */
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

