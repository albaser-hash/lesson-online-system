<template>
  <div class="common-nav">
    <div class="nav-container">
      <div class="nav-left">
        <h1 class="nav-logo" @click="goHome">在线教育平台</h1>
      </div>
      <div class="nav-center">
        <el-menu mode="horizontal" :default-active="activeIndex" class="nav-menu">
          <el-menu-item index="home" @click="goHome">首页</el-menu-item>
          <el-menu-item index="courses" @click="goToCourses">课程</el-menu-item>
          <el-menu-item index="forum" @click="goToForum">论坛</el-menu-item>
          <el-menu-item index="qa" @click="goToQA">问答</el-menu-item>
        </el-menu>
      </div>
      <div class="nav-right">
        <template v-if="isLogin">
          <el-dropdown @command="handleCommand" class="user-dropdown">
            <span class="user-info">
              <i class="el-icon-user"></i>
              {{ userName }}
              <i class="el-icon-arrow-down el-icon--right"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item v-if="userType === 'STUDENT'" command="myCourses">我的课程</el-dropdown-item>
              <el-dropdown-item v-if="userType === 'STUDENT'" command="favorites">我的收藏</el-dropdown-item>
              <el-dropdown-item v-if="userType === 'STUDENT'" command="cart">购物车</el-dropdown-item>
              <el-dropdown-item v-if="userType === 'STUDENT'" command="exam">我的考试</el-dropdown-item>
              <el-dropdown-item v-if="userType === 'TEACHER'" command="createCourse">创建课程</el-dropdown-item>
              <el-dropdown-item v-if="userType === 'TEACHER'" command="examManage">考试管理</el-dropdown-item>
              <el-dropdown-item v-if="userType === 'ADMIN'" command="audit">课程审核</el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
          <!-- 快速切换用户类型（仅用于测试） -->
          <el-dropdown @command="switchUserType" class="switch-user-dropdown">
            <el-button size="small" type="info">
              <i class="el-icon-refresh"></i>
              切换角色
            </el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="STUDENT">学生</el-dropdown-item>
              <el-dropdown-item command="TEACHER">教师</el-dropdown-item>
              <el-dropdown-item command="ADMIN">管理员</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="primary" @click="goToLogin" class="login-btn">登录</el-button>
          <el-button @click="goToRegister" class="register-btn">注册</el-button>
        </template>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'

export default {
  name: 'CommonNav',
  props: {
    activeIndex: {
      type: String,
      default: 'home'
    }
  },
  computed: {
    ...mapState({
      token: state => state.user.token,
      userType: state => state.user.userType,
      userId: state => state.user.id,
      userName: state => state.user.userName
    }),
    isLogin() {
      return !!this.token
    }
  },
  methods: {
    // 导航方法
    goHome() {
      this.$router.push('/')
    },
    goToCourses() {
      this.$router.push('/courses')
    },
    goToForum() {
      this.$router.push('/forum')
    },
    goToQA() {
      this.$router.push('/qa')
    },
    goToLogin() {
      this.$router.push('/login')
    },
    goToRegister() {
      this.$router.push('/register')
    },
    // 用户下拉菜单处理
    handleCommand(command) {
      switch (command) {
        case 'profile':
          this.$router.push('/profile')
          break
        case 'myCourses':
          this.$router.push('/my-courses')
          break
        case 'favorites':
          this.$router.push('/favorites')
          break
        case 'cart':
          this.$router.push('/cart')
          break
        case 'exam':
          this.$router.push('/student/exam')
          break
        case 'createCourse':
          this.$router.push('/course-create')
          break
        case 'examManage':
          this.$router.push('/teacher/exam')
          break
        case 'audit':
          this.$router.push('/audit')
          break
        case 'logout':
          this.$store.dispatch('user/logout')
          this.$message.success('已退出登录')
          this.$router.push('/')
          break
      }
    },
    // 切换用户类型（仅用于测试）
    switchUserType(type) {
      this.$store.commit('SET_USERTYPE', type)
      const userNameMap = {
        'STUDENT': '学生用户',
        'TEACHER': '教师用户', 
        'ADMIN': '管理员'
      }
      this.$store.commit('SET_USERNAME', userNameMap[type])
      this.$message.success(`已切换用户类型为: ${type}`)
      console.log(`切换用户类型为: ${type}`)
    }
  }
}
</script>

<style scoped>
.common-nav {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  padding: 10px 0;
}

.nav-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30px;
}

.nav-left h1 {
  font-size: 1.8rem;
  color: #ff5c8a;
  font-weight: bold;
  margin: 0;
  cursor: pointer;
}

.nav-center .nav-menu {
  border-bottom: none;
}

.nav-menu .el-menu-item {
  font-size: 1rem;
  color: #666;
  padding: 0 20px;
}

.nav-menu .el-menu-item:hover {
  color: #ff5c8a;
}

.nav-menu .el-menu-item.is-active {
  color: #ff5c8a;
  border-bottom: 2px solid #ff5c8a;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #333;
  font-size: 0.9rem;
  cursor: pointer;
}

.user-info .el-icon-user {
  font-size: 1.1rem;
  color: #ff5c8a;
}

.login-btn, .register-btn {
  padding: 8px 15px;
  border-radius: 20px;
  font-weight: bold;
}

.login-btn {
  background: #ff5c8a;
  border-color: #ff5c8a;
  color: white;
}

.login-btn:hover {
  background: #ff7ba9;
  border-color: #ff7ba9;
}

.register-btn {
  background: transparent;
  border-color: #ff5c8a;
  color: #ff5c8a;
}

.register-btn:hover {
  background: #ff5c8a;
  border-color: #ff5c8a;
  color: white;
}

.switch-user-dropdown {
  margin-left: 10px;
}

.switch-user-dropdown .el-button {
  padding: 6px 12px;
  font-size: 12px;
  border-radius: 15px;
}

@media (max-width: 768px) {
  .common-nav {
    padding: 5px 0;
  }
  
  .nav-container {
    padding: 0 20px;
    flex-direction: column;
    align-items: flex-start;
  }
  
  .nav-left h1 {
    font-size: 1.5rem;
  }
  
  .nav-center {
    width: 100%;
    margin-top: 10px;
  }
  
  .nav-menu {
    justify-content: center;
  }
  
  .nav-menu .el-menu-item {
    padding: 0 10px;
  }
  
  .nav-right {
    width: 100%;
    justify-content: center;
    margin-top: 10px;
  }
  
  .login-btn, .register-btn {
    width: 100px;
  }
}
</style> 