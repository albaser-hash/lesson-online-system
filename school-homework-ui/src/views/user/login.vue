<template>
  <div class="login">
    <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form">
      <h3 class="title">在线教育系统</h3>
      <el-form-item prop="userName">
        <el-input
          v-model="loginForm.userName"
          type="text"
          auto-complete="off"
          placeholder="用户名"
        >
        </el-input>
      </el-form-item>
      <el-form-item prop="passWord">
        <el-input
          v-model="loginForm.passWord"
          type="password"
          auto-complete="off"
          placeholder="密码"
          @keyup.enter.native="handleLogin"
        >
        </el-input>
      </el-form-item>
      <el-checkbox v-model="loginForm.rememberMe" style="margin:0px 0px 25px 0px;">记住密码</el-checkbox>
      <el-form-item style="width:100%;">
        <el-button
          :loading="loading"
          size="medium"
          type="primary"
          style="width:100%;"
          @click.native.prevent="handleLogin"
        >
          <span v-if="!loading">登 录</span>
          <span v-else>登 录 中...</span>
        </el-button>
        <div style="float: right;">
          <router-link class="link-type" :to="'/register'">立即注册</router-link>
        </div>
      </el-form-item>
    </el-form>
    <!--  底部  -->
    <div class="el-login-footer">
      <span>Copyright © 2018-2025 在线教育系统 All Rights Reserved.</span>
    </div>
  </div>
</template>

<script>
import { login } from '@/api/login'

export default {
  name: "Login",
  data() {
    return {
      title: "在线教育系统",
      loginForm: {
        userName: "",
        passWord: "",
        rememberMe: false
      },
      loginRules: {
        userName: [
          { required: true, trigger: "blur", message: "请输入用户名" }
        ],
        passWord: [
          { required: true, trigger: "blur", message: "请输入密码" }
        ]
      },
      loading: false,
      register: false,
      redirect: undefined
    }
  },
  watch: {
    $route: {
      handler: function(route) {
        this.redirect = route.query && route.query.redirect
      },
      immediate: true
    }
  },
  created() {
    // 检查并显示登录提示
    const loginTip = this.$store.state.user.loginTip
    if (loginTip) {
      this.$message.info(loginTip)
      this.$store.dispatch('user/clearLoginTip')
    }
  },
  methods: {
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true
          this.$store.dispatch('Login', {
            userName: this.loginForm.userName,
            passWord: this.loginForm.passWord
          }).then(res => {
            this.$message.success('登录成功！')
            this.$router.push({ path: this.redirect || "/" })
          }).catch(error => {
            this.loading = false
            // 显示登录失败的错误信息
            this.$message.error(error.message || '登录失败，请检查用户名和密码')
            console.error('登录失败:', error)
          })
        }
      })
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss">
.login {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background-image: url("../../assets/images/login-background.jpg");
  background-size: cover;
}
.title {
  margin: 0px auto 30px auto;
  text-align: center;
  color: #707070;
}

.login-form {
  border-radius: 16px;
  background: #ffffffcc;
  width: 400px;
  padding: 25px 25px 5px 25px;
  z-index: 1;
  .el-input {
    height: 38px;
    input {
      height: 38px;
      background: #fff;
      border-radius: 20px;
      border: 1px solid #ffd6e6;
      transition: border-color 0.3s;
    }
    input:focus {
      border-color: #ffb6d5;
      box-shadow: 0 0 0 2px #ffe4ec55;
    }
  }
  .input-icon {
    height: 39px;
    width: 14px;
    margin-left: 2px;
  }
}
.login-tip {
  font-size: 13px;
  text-align: center;
  color: #bfbfbf;
}
.el-login-footer {
  height: 40px;
  line-height: 40px;
  position: fixed;
  bottom: 0;
  width: 100%;
  text-align: center;
  color: #fff;
  font-family: Arial;
  font-size: 12px;
  letter-spacing: 1px;
}
</style>
