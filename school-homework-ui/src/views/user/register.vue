<template>
  <div class="register">
    <el-form ref="registerForm" :model="registerForm" :rules="registerRules" class="register-form">
      <h3 class="title">在线教育系统</h3>
      <el-form-item prop="userName">
        <el-input v-model="registerForm.userName" type="text" auto-complete="off" placeholder="用户名" />
      </el-form-item>
      <el-form-item prop="passWord">
        <el-input v-model="registerForm.passWord" type="password" auto-complete="off" placeholder="密码" />
      </el-form-item>
      <el-form-item prop="confirmPassWord">
        <el-input v-model="registerForm.confirmPassWord" type="password" auto-complete="off" placeholder="确认密码" />
      </el-form-item>
      <el-form-item prop="email">
        <el-input v-model="registerForm.email" type="email" auto-complete="off" placeholder="邮箱" />
      </el-form-item>
      <el-form-item prop="phone">
        <el-input v-model="registerForm.phone" type="text" auto-complete="off" placeholder="手机号" />
      </el-form-item>
      <el-form-item prop="name">
        <el-input v-model="registerForm.name" type="text" auto-complete="off" placeholder="昵称（可选）" />
      </el-form-item>
      <el-form-item prop="sex">
        <el-select v-model="registerForm.sex" placeholder="性别（可选）" style="width: 100%">
          <el-option label="男" :value="1" />
          <el-option label="女" :value="2" />
          <el-option label="保密" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item prop="bio">
        <el-input v-model="registerForm.bio" type="textarea" :rows="2" auto-complete="off" placeholder="个人简介（可选）" />
      </el-form-item>
      <el-form-item prop="userType">
        <el-select v-model="registerForm.userType" placeholder="请选择用户类型" style="width: 100%">
          <el-option label="学生" value="STUDENT" />
          <el-option label="老师" value="TEACHER" />
          <el-option label="管理员" value="ADMIN" />
        </el-select>
      </el-form-item>
      <el-form-item style="width:100%;">
        <el-button
          :loading="loading"
          size="medium"
          type="primary"
          style="width:100%;"
          @click.native.prevent="handleRegister"
        >
          <span v-if="!loading">注 册</span>
          <span v-else>注 册 中...</span>
        </el-button>
        <div style="float: right;">
          <router-link class="link-type" :to="'/login'">使用已有账户登录</router-link>
        </div>
      </el-form-item>
    </el-form>
    <!--  底部  -->
    <div class="el-register-footer">
      <span>Copyright © 2018-2025 在线教育系统 All Rights Reserved.</span>
    </div>
  </div>
</template>

<script>
import { register } from '@/api/login'

export default {
  name: "Register",
  data() {
    return {
      title: "在线教育系统",
      registerForm: {
        userName: "",
        passWord: "",
        confirmPassWord: "",
        email: "",
        phone: "",
        userType: "",
        name: "",
        sex: 0,
        bio: ""
      },
      registerRules: {
        userName: [
          { required: true, trigger: "blur", message: "请输入用户名" },
          { min: 2, max: 20, message: '用户名长度必须介于 2 和 20 之间', trigger: 'blur' }
        ],
        passWord: [
          { required: true, trigger: "blur", message: "请输入密码" },
          { min: 5, max: 20, message: "密码长度必须介于 5 和 20 之间", trigger: "blur" }
        ],
        confirmPassWord: [
          { required: true, trigger: "blur", message: "请再次输入密码" },
          { validator: (rule, value, callback) => {
              if (value !== this.registerForm.passWord) {
                callback(new Error("两次输入的密码不一致"))
              } else {
                callback()
              }
            }, trigger: "blur"
          }
        ],
        email: [
          { required: true, trigger: "blur", message: "请输入邮箱" },
          { type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }
        ],
        phone: [
          { required: true, trigger: "blur", message: "请输入手机号" },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
        ],
        userType: [
          { required: true, trigger: "change", message: "请选择用户类型" }
        ],
        name: [],
        sex: [],
        bio: []
      },
      loading: false
    }
  },
  methods: {
    handleRegister() {
      this.$refs.registerForm.validate(valid => {
        if (valid) {
          this.loading = true
          const { confirmPassWord, ...submitForm } = this.registerForm
          register(submitForm).then(res => {
            this.$message.success('注册成功，请登录！')
            this.$router.replace('/login')
          }).catch(() => {
            this.loading = false
          })
        }
      })
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss">
.register {
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

.register-form {
  border-radius: 16px;
  background: #ffffffcc;
  width: 400px;
  padding: 25px 25px 5px 25px;
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
.register-tip {
  font-size: 13px;
  text-align: center;
  color: #bfbfbf;
}
.el-register-footer {
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
