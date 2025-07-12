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

  </div>
</template>

<script>
import { mapState } from 'vuex'

export default {
  name: "Index",
  data() {
    return {
      // 假数据
      mockCategories: [
        { categoryId: 1, categoryName: '编程语言' },
        { categoryId: 2, categoryName: '前端开发' },
        { categoryId: 3, categoryName: '后端开发' },
        { categoryId: 4, categoryName: '数据库' },
        { categoryId: 5, categoryName: '移动开发' }
      ],
      mockMenus: {
        STUDENT: [
          { menuId: 1, menuName: '我的课程', menuPath: '/my-courses', menuIcon: 'el-icon-reading' },
          { menuId: 2, menuName: '学习进度', menuPath: '/progress', menuIcon: 'el-icon-data-line' },
          { menuId: 3, menuName: '我的收藏', menuPath: '/favorites', menuIcon: 'el-icon-star-on' },
          { menuId: 4, menuName: '个人中心', menuPath: '/profile', menuIcon: 'el-icon-user' }
        ],
        TEACHER: [
          { menuId: 5, menuName: '课程管理', menuPath: '/course-manage', menuIcon: 'el-icon-edit' },
          { menuId: 6, menuName: '学生管理', menuPath: '/student-manage', menuIcon: 'el-icon-user-solid' },
          { menuId: 7, menuName: '考试管理', menuPath: '/exam-manage', menuIcon: 'el-icon-document' },
          { menuId: 8, menuName: '个人中心', menuPath: '/profile', menuIcon: 'el-icon-user' }
        ],
        ADMIN: [
          { menuId: 9, menuName: '用户管理', menuPath: '/user-manage', menuIcon: 'el-icon-user-solid' },
          { menuId: 10, menuName: '课程审核', menuPath: '/course-audit', menuIcon: 'el-icon-view' },
          { menuId: 11, menuName: '系统设置', menuPath: '/system-settings', menuIcon: 'el-icon-setting' },
          { menuId: 12, menuName: '个人中心', menuPath: '/profile', menuIcon: 'el-icon-user' }
        ]
      },
      drawerVisible: false,
      isMobile: false,
      menuTop: 70, // 默认导航栏top
      containerPaddingTop: 180, // 默认顶部总高度
      searchKeyword: '',
      selectedCategory: '',
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
      userId: state => state.user.id
    }),
    // 使用假数据
    dynamicMenus() {
      const userType = this.userType || 'STUDENT'
      return this.mockMenus[userType] || this.mockMenus.STUDENT
    },
    categories() {
      return this.mockCategories
    },
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
    }
  },
  mounted() {
    this.checkMobile()
    this.$nextTick(() => {
      this.updateMenuTop()
      this.updateContainerPadding()
    })
    window.addEventListener('resize', this.checkMobile)
    window.addEventListener('resize', this.updateMenuTop)
    window.addEventListener('resize', this.updateContainerPadding)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.checkMobile)
    window.removeEventListener('resize', this.updateMenuTop)
    window.removeEventListener('resize', this.updateContainerPadding)
  },
  methods: {
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
      // 使用假数据退出登录
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      
      // 清空Vuex状态
      this.$store.commit('SET_TOKEN', '')
      this.$store.commit('SET_NAME', '')
      this.$store.commit('SET_USERNAME', '')
      this.$store.commit('SET_AVATAR', '')
      this.$store.commit('SET_USERTYPE', '')
      this.$store.commit('SET_ID', '')
      
      this.$message.success('欢迎再次使用在线教育系统！（假数据）')
      this.$router.replace('/login')
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
      this.$message.info('搜索功能：' + this.searchKeyword)
    },
    selectCategory(categoryId) {
      this.selectedCategory = categoryId
      this.$message.info('选择分类：' + (categoryId || '全部'))
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
::v-deep .el-menu-item:focus {
  background: linear-gradient(135deg, #ffb6d5, #ffe4ec) !important;
  color: #fff !important;
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(255, 182, 213, 0.4);
}

::v-deep .el-menu-item.is-active {
  background: linear-gradient(135deg, #ff5c8a, #ffb6d5) !important;
  color: #fff !important;
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(255, 182, 213, 0.4);
}

.main-title {
  text-align: center;
  padding: 40px 20px 20px;
  position: relative;
  z-index: 10;
}

.title-text {
  font-size: 3.5rem;
  font-weight: bold;
  background: linear-gradient(135deg, #ff5c8a, #ffb6d5);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 10px;
  display: block;
}

.title-subtitle {
  font-size: 1.2rem;
  color: #666;
  font-weight: 300;
}

.search-section {
  padding: 20px;
  position: relative;
  z-index: 10;
}

.search-container {
  max-width: 800px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  padding: 30px;
  box-shadow: 0 8px 32px rgba(255, 182, 213, 0.2);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 182, 213, 0.3);
}

.category-tags {
  margin-bottom: 20px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
}

.category-tag {
  border-radius: 20px !important;
  padding: 8px 16px !important;
  font-size: 14px !important;
  transition: all 0.3s ease;
  cursor: pointer;
  border: 2px solid #ffb6d5 !important;
  color: #ff5c8a !important;
  background: transparent !important;
}

.category-tag:hover,
.category-tag.active {
  background: linear-gradient(135deg, #ff5c8a, #ffb6d5) !important;
  color: #fff !important;
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(255, 182, 213, 0.4);
}

.search-box {
  display: flex;
  gap: 15px;
  align-items: center;
  justify-content: center;
}

.search-input {
  flex: 1;
  max-width: 400px;
}

::v-deep .search-input .el-input__inner {
  border-radius: 25px;
  border: 2px solid #ffb6d5;
  padding: 12px 20px;
  font-size: 16px;
  transition: all 0.3s ease;
}

::v-deep .search-input .el-input__inner:focus {
  border-color: #ff5c8a;
  box-shadow: 0 0 0 3px rgba(255, 182, 213, 0.2);
}

.search-btn {
  border-radius: 25px;
  padding: 12px 24px;
  background: linear-gradient(135deg, #ff5c8a, #ffb6d5);
  border: none;
  font-weight: 600;
  transition: all 0.3s ease;
}

.search-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(255, 182, 213, 0.4);
}

.login-section {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 200;
  display: flex;
  align-items: center;
  gap: 15px;
}

.guest-user {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #ffb6d5;
  transition: all 0.3s ease;
}

.avatar:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 15px rgba(255, 182, 213, 0.4);
}

.username {
  font-size: 12px;
  color: #666;
  font-weight: 500;
}

.login-buttons {
  display: flex;
  gap: 10px;
}

.login-btn,
.register-btn {
  border-radius: 20px;
  padding: 8px 16px;
  font-size: 14px;
  transition: all 0.3s ease;
}

.login-btn {
  background: linear-gradient(135deg, #ff5c8a, #ffb6d5);
  border: none;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(255, 182, 213, 0.4);
}

.register-btn {
  background: transparent;
  border: 2px solid #ffb6d5;
  color: #ff5c8a;
}

.register-btn:hover {
  background: #ffb6d5;
  color: #fff;
  transform: translateY(-2px);
}

.mobile-login-section {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 200;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.mobile-avatar-wrapper {
  margin-bottom: 5px;
}

.mobile-login-buttons {
  display: flex;
  gap: 8px;
}

.hamburger {
  position: fixed;
  top: 12px;
  left: 12px;
  z-index: 200;
  background: #ffb6d5;
  color: #fff;
  border: none;
  transition: all 0.3s ease;
}

.hamburger:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 15px rgba(255, 182, 213, 0.4);
}

::v-deep .mobile-drawer {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

::v-deep .mobile-drawer .el-menu {
  background: transparent !important;
  border: none !important;
}

::v-deep .mobile-drawer .el-menu-item {
  color: #ff5c8a !important;
  font-weight: 600;
  font-size: 18px;
  background: transparent !important;
  border-radius: 12px !important;
  transition: all 0.3s ease;
  margin: 8px 0;
  height: 50px;
  line-height: 50px;
}

::v-deep .mobile-drawer .el-menu-item:hover,
::v-deep .mobile-drawer .el-menu-item:focus {
  background: linear-gradient(135deg, #ffb6d5, #ffe4ec) !important;
  color: #fff !important;
  transform: translateX(5px);
}

.user-dropdown {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 200;
}

.notification-entry {
  position: fixed;
  top: 20px;
  right: 80px;
  z-index: 200;
}

.notification-badge {
  transition: all 0.3s ease;
}

.has-unread {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.welcome-section {
  padding: 60px 20px;
  text-align: center;
  position: relative;
  z-index: 10;
}

.welcome-content {
  max-width: 800px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 8px 32px rgba(255, 182, 213, 0.2);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 182, 213, 0.3);
}

.welcome-title {
  font-size: 2.5rem;
  font-weight: bold;
  background: linear-gradient(135deg, #ff5c8a, #ffb6d5);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 20px;
}

.welcome-subtitle {
  font-size: 1.1rem;
  color: #666;
  margin-bottom: 30px;
  line-height: 1.6;
}

.welcome-features {
  display: flex;
  justify-content: center;
  gap: 40px;
  flex-wrap: wrap;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 20px;
  border-radius: 15px;
  background: linear-gradient(135deg, #fff5f8, #ffe4ec);
  border: 2px solid #ffb6d5;
  transition: all 0.3s ease;
  min-width: 120px;
}

.feature-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(255, 182, 213, 0.4);
}

.feature-item i {
  font-size: 2rem;
  color: #ff5c8a;
}

.feature-item span {
  font-size: 14px;
  font-weight: 600;
  color: #666;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .title-text {
    font-size: 2.5rem;
  }

  .welcome-title {
    font-size: 2rem;
  }

  .welcome-features {
    gap: 20px;
  }

  .feature-item {
    min-width: 100px;
    padding: 15px;
  }
}

@media (max-width: 480px) {
  .title-text {
    font-size: 2rem;
  }

  .welcome-title {
    font-size: 1.5rem;
  }

  .welcome-features {
    flex-direction: column;
    gap: 15px;
  }

  .feature-item {
    min-width: auto;
    width: 100%;
    flex-direction: row;
    justify-content: center;
  }
}
</style>

