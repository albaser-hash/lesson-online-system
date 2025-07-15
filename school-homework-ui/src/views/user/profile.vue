<template>
  <div class="profile-page">
    <div class="page-header">
      <div class="header-content">
        <el-button
          class="back-btn"
          type="primary"
          icon="el-icon-arrow-left"
          @click="$router.push('/')"
          size="medium"
        >
          返回主页
        </el-button>
        <div class="header-text">
          <h2>个人信息</h2>
          <p>管理您的个人资料和账户安全</p>
        </div>
      </div>
    </div>

    <div class="profile-content">
      <!-- 基本信息卡片 -->
      <el-card class="profile-card" shadow="hover">
        <div slot="header" class="card-header">
          <span class="header-title">
            <i class="el-icon-user"></i>
            基本信息
          </span>
          <el-button type="primary" icon="el-icon-edit" @click="editDialogVisible = true" size="small">
            修改资料
          </el-button>
        </div>

        <div class="profile-info">
          <div class="avatar-section">
            <img :src="avatarUrl" alt="头像" @error="onAvatarError" class="profile-avatar" />
            <div class="avatar-info">
              <h3>{{ displayName }}</h3>
              <el-tag :type="getUserTypeTagType()" size="small">{{ userTypeLabel }}</el-tag>
            </div>
          </div>

          <div class="info-grid">
            <div class="info-item">
              <div class="info-label">
                <i class="el-icon-user"></i>
                昵称
              </div>
              <div class="info-value">{{ name || '未填写' }}</div>
            </div>

            <div class="info-item">
              <div class="info-label">
                <i class="el-icon-user"></i>
                用户名
              </div>
              <div class="info-value">{{ userName }}</div>
            </div>

            <div class="info-item">
              <div class="info-label">
                <i class="el-icon-user"></i>
                性别
              </div>
              <div class="info-value">{{ sexLabel }}</div>
            </div>

            <div class="info-item">
              <div class="info-label">
                <i class="el-icon-user"></i>
                个人简介
              </div>
              <div class="info-value">{{ bio || '未填写' }}</div>
            </div>

            <div class="info-item">
              <div class="info-label">
                <i class="el-icon-message"></i>
                邮箱
              </div>
              <div class="info-value">{{ email || '未填写' }}</div>
            </div>

            <div class="info-item">
              <div class="info-label">
                <i class="el-icon-phone"></i>
                手机号
              </div>
              <div class="info-value">{{ phone || '未填写' }}</div>
            </div>

            <div class="info-item">
              <div class="info-label">
                <i class="el-icon-medal"></i>
                用户类型
              </div>
              <div class="info-value">{{ userTypeLabel }}</div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 账户安全卡片 -->
      <el-card class="security-card" shadow="hover">
        <div slot="header" class="card-header">
          <span class="header-title">
            <i class="el-icon-lock"></i>
            账户安全
          </span>
          <el-button type="warning" icon="el-icon-key" @click="passwordDialogVisible = true" size="small">
            修改密码
          </el-button>
        </div>

        <div class="security-info">
          <div class="security-item">
            <i class="el-icon-check security-icon success"></i>
            <div class="security-text">
              <div class="security-title">登录密码</div>
              <div class="security-desc">已设置，建议定期更换</div>
            </div>
          </div>

          <div class="security-item">
            <i class="el-icon-check security-icon success"></i>
            <div class="security-text">
              <div class="security-title">邮箱验证</div>
              <div class="security-desc">{{ email ? '已验证' : '未验证' }}</div>
            </div>
          </div>

          <div class="security-item">
            <i class="el-icon-check security-icon success"></i>
            <div class="security-text">
              <div class="security-title">手机验证</div>
              <div class="security-desc">{{ phone ? '已验证' : '未验证' }}</div>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 修改资料对话框 -->
    <el-dialog
      title="修改个人资料"
      :visible.sync="editDialogVisible"
      width="500px"
      @close="resetEditForm"
      custom-class="profile-dialog"
    >
      <el-form :model="editForm" :rules="editRules" ref="editForm" label-width="80px">
        <el-form-item label="头像">
          <el-upload
            class="avatar-uploader"
            action="#"
            :show-file-list="false"
            :before-upload="beforeAvatarUpload"
            :on-change="handleAvatarChange"
            :auto-upload="false"
          >
            <img v-if="editForm.avatar" :src="editForm.avatar" class="avatar-edit-img" @error="onAvatarError" />
            <img v-else :src="avatarUrl" class="avatar-edit-img" @error="onAvatarError" />
            <div class="avatar-upload-hint">
              <i class="el-icon-camera"></i>
              <span>点击更换头像</span>
            </div>
          </el-upload>
        </el-form-item>

        <el-form-item label="昵称">
          <el-input v-model="editForm.name" maxlength="20" placeholder="请输入昵称" />
        </el-form-item>

        <el-form-item label="性别">
          <el-select v-model="editForm.sex" placeholder="请选择性别">
            <el-option label="男" :value="1" />
            <el-option label="女" :value="2" />
            <el-option label="保密" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item label="个人简介">
          <el-input v-model="editForm.bio" type="textarea" :rows="2" placeholder="请输入个人简介" />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入邮箱地址" />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitEdit" :loading="editLoading">保 存</el-button>
      </div>
    </el-dialog>

    <!-- 修改密码对话框 -->
    <el-dialog
      title="修改密码"
      :visible.sync="passwordDialogVisible"
      width="500px"
      @close="resetPasswordForm"
      custom-class="password-dialog"
    >
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordForm" label-width="100px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入原密码"
            show-password
          />
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="passwordDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitPassword" :loading="passwordLoading">确认修改</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import request from '@/utils/request'
import { uploadImage } from '@/api/createCourse'

// 直接在组件中定义API函数
const updateUser = (data) => {
  return request({
    url: '/user/update',
    method: 'put',
    data
  })
}

const updateUserPassword = (data) => {
  return request({
    url: '/user/update/password',
    method: 'put',
    data
  })
}

export default {
  name: 'Profile',
  data() {
    // 密码确认验证
    const validateConfirmPassword = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入密码'))
      } else if (value !== this.passwordForm.newPassword) {
        callback(new Error('两次输入密码不一致'))
      } else {
        callback()
      }
    }

    return {
      editDialogVisible: false,
      passwordDialogVisible: false,
      editLoading: false,
      passwordLoading: false,
      editForm: {
        avatar: '',
        userName: '',
        email: '',
        phone: '',
        name: '',
        sex: 0,
        bio: ''
      },
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      editRules: {
        userName: [
          { required: true, trigger: "blur", message: "请输入用户名" },
          { min: 2, max: 20, message: '用户名长度必须介于 2 和 20 之间', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '邮箱格式不正确', trigger: ['blur', 'change'] }
        ],
        phone: [
          { required: true, message: '请输入手机号', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
        ],
        name: [],
        sex: [],
        bio: []
      },
      passwordRules: {
        oldPassword: [
          { required: true, message: '请输入原密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
        ],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, validator: validateConfirmPassword, trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    ...mapState({
      userName: state => state.user.userName,
      name: state => state.user.name,
      avatar: state => state.user.avatar,
      email: state => state.user.email,
      phone: state => state.user.phone,
      userType: state => state.user.userType,
      sex: state => state.user.sex,
      bio: state => state.user.bio
    }),
    userTypeLabel() {
      switch (this.userType) {
        case 'STUDENT': return '学生';
        case 'TEACHER': return '老师';
        case 'ADMIN': return '管理员';
        default: return this.userType || '未知';
      }
    },
    avatarUrl() {
      if (!this.avatar || (!/^https?:\/\//.test(this.avatar) && !this.avatar.startsWith('/'))) {
        return require('@/assets/images/profile.jpg')
      }
      return this.avatar
    },
    sexLabel() {
      switch (this.sex) {
        case 1:
          return '男';
        case 2:
          return '女';
        default:
          return '保密';
      }
    },
    displayName() {
      return this.name || this.userName;
    }
  },
  mounted() {
    // 检查用户登录状态
    const token = this.$store.state.user.token;
    if (!token) {
      this.$message.warning('请先登录');
      this.$router.push('/login');
    }
  },
  methods: {
    getUserTypeTagType() {
      switch (this.userType) {
        case 'STUDENT': return 'success';
        case 'TEACHER': return 'warning';
        case 'ADMIN': return 'danger';
        default: return 'info';
      }
    },
    resetEditForm() {
      this.editForm.avatar = this.avatarUrl;
      this.editForm.userName = this.userName;
      this.editForm.email = this.email;
      this.editForm.phone = this.phone;
      this.editForm.name = this.name;
      this.editForm.sex = this.sex;
      this.editForm.bio = this.bio;
    },
    resetPasswordForm() {
      this.passwordForm.oldPassword = '';
      this.passwordForm.newPassword = '';
      this.passwordForm.confirmPassword = '';
      this.$refs.passwordForm && this.$refs.passwordForm.clearValidate();
    },
    beforeAvatarUpload(file) {
      const isJPG = file.type === 'image/jpeg' || file.type === 'image/png';
      const isLt2M = file.size / 1024 / 1024 < 2;
      if (!isJPG) {
        this.$message.error('只能上传 JPG/PNG 格式图片!');
        return false;
      }
      if (!isLt2M) {
        this.$message.error('图片大小不能超过 2MB!');
        return false;
      }
      return true;
    },
    handleAvatarChange(file) {
      // 立即上传头像
      this.uploadAvatar(file.raw);
    },
    async uploadAvatar(file) {
      try {
        // 显示上传进度
        this.$message.info('正在上传头像...');

        const response = await uploadImage(file);

        // 检查响应结构
        if (response && response.code === 200 && response.data) {
          let imageUrl;

          // 如果response.data是对象，从中获取imageUrl
          if (typeof response.data === 'object' && response.data.imageUrl) {
            imageUrl = response.data.imageUrl;
          }
          // 如果response.data是字符串，直接使用
          else if (typeof response.data === 'string') {
            imageUrl = response.data;
          }
          // 其他情况，尝试从response.data中获取
          else {
            imageUrl = response.data.imageUrl || response.data.uploadUrl || response.data;
          }

          if (imageUrl) {
            this.editForm.avatar = imageUrl;
            this.$message.success('头像上传成功');
          } else {
            this.$message.error('头像上传失败：无法获取图片URL');
          }
        } else {
          this.$message.error('头像上传失败');
        }
      } catch (error) {
        this.$message.error('头像上传失败，请重试');
      }
    },
    async submitEdit() {
      this.$refs.editForm.validate(async valid => {
        if (!valid) return;

        // 检查用户是否已登录
        const token = this.$store.state.user.token;
        if (!token) {
          this.$message.error('请先登录');
          this.$router.push('/login');
          return;
        }

        this.editLoading = true;
        try {
          const updateData = {
            userName: this.editForm.userName,
            email: this.editForm.email,
            phone: this.editForm.phone,
            name: this.editForm.name,
            sex: this.editForm.sex,
            bio: this.editForm.bio
          };

          // 如果头像有变化，添加头像URL
          if (this.editForm.avatar && this.editForm.avatar !== this.avatarUrl) {
            updateData.avatar = this.editForm.avatar;
          }

          const response = await updateUser(updateData);

          if (response && response.data && (response.data.code === 200 || response.data.code === 1)) {
            this.$message.success(response.data.data || response.data.msg || '个人资料修改成功');

            // 更新Vuex中的用户信息
            this.$store.commit('SET_NAME', this.editForm.userName);
            if (updateData.avatar) {
              this.$store.commit('SET_AVATAR', updateData.avatar);
            }
            this.$store.commit('SET_EMAIL', this.editForm.email);
            this.$store.commit('SET_PHONE', this.editForm.phone);

            this.$store.commit('SET_NAME', this.editForm.name);
            this.$store.commit('SET_SEX', this.editForm.sex);
            this.$store.commit('SET_BIO', this.editForm.bio);

            this.editDialogVisible = false;
          } else {
            this.$message.error(response?.data?.msg || response?.data?.message || '修改失败，请重试');
          }
        } catch (error) {
          if (error.response) {
            this.$message.error(`修改失败: ${error.response.data?.msg || error.response.data?.message || error.message}`);
          } else if (error.request) {
            this.$message.error('网络连接失败，请检查网络连接');
          } else {
            this.$message.error(`修改失败: ${error.message}`);
          }
        } finally {
          this.editLoading = false;
        }
      });
    },
    async submitPassword() {
      this.$refs.passwordForm.validate(async valid => {
        if (!valid) return;

        this.passwordLoading = true;
        try {
          const passwordData = {
            oldPassword: this.passwordForm.oldPassword,
            newPassword: this.passwordForm.newPassword
          };

          const res = await updateUserPassword(passwordData);

          if (res && res.data && (res.data.code === 200 || res.data.code === 1)) {
            this.$message.success(res.data.data || res.data.msg || '密码修改成功');
            this.passwordDialogVisible = false;
            this.resetPasswordForm();
          } else {
            this.$message.error(res?.data?.msg || '密码修改失败，请重试');
          }
        } catch (error) {
          this.$message.error('密码修改失败，请重试');
        } finally {
          this.passwordLoading = false;
        }
      });
    },
    onAvatarError(e) {
      e.target.src = require('@/assets/images/profile.jpg')
    }
  },
  watch: {
    editDialogVisible(val) {
      if (val) {
        this.editForm.avatar = this.avatarUrl;
        this.editForm.userName = this.userName;
        this.editForm.email = this.email;
        this.editForm.phone = this.phone;
        this.editForm.name = this.name;
        this.editForm.sex = this.sex;
        this.editForm.bio = this.bio;
      }
    }
  }
}
</script>

<style scoped>
.profile-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 30px;
  text-align: center;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.back-btn {
  background: #ffb6d5 !important;
  border-color: #ffb6d5 !important;
  color: #fff !important;
  font-weight: bold;
  border-radius: 20px;
  padding: 10px 20px;
  transition: all 0.3s;
}

.back-btn:hover {
  background: #ff5c8a !important;
  border-color: #ff5c8a !important;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px #ffb6d5cc;
}

.header-text {
  flex: 1;
  text-align: center;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 28px;
  font-weight: bold;
}

.page-header p {
  margin: 0;
  color: #909399;
  font-size: 16px;
}

.profile-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.profile-card, .security-card {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 16px #f0c1d6cc;
  transition: box-shadow 0.3s;
}

.profile-card:hover, .security-card:hover {
  box-shadow: 0 8px 24px #ffb6d5cc;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0;
}

.header-title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-title i {
  color: #ffb6d5;
}

.profile-info {
  padding: 20px 0;
}

.avatar-section {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
  padding: 20px;
  background: linear-gradient(135deg, #ffe4ec 0%, #ffd6e6 100%);
  border-radius: 12px;
}

.profile-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  border: 4px solid #fff;
  object-fit: cover;
  box-shadow: 0 4px 12px #ffb6d5cc;
  margin-right: 20px;
}

.avatar-info h3 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 20px;
  font-weight: bold;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.info-item {
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
  border-left: 4px solid #ffb6d5;
}

.info-label {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #909399;
  font-size: 14px;
  margin-bottom: 8px;
}

.info-label i {
  color: #ffb6d5;
}

.info-value {
  color: #303133;
  font-size: 16px;
  font-weight: 500;
}

.security-info {
  padding: 20px 0;
}

.security-item {
  display: flex;
  align-items: center;
  padding: 16px;
  margin-bottom: 12px;
  background: #fafafa;
  border-radius: 8px;
  border-left: 4px solid #67c23a;
}

.security-icon {
  font-size: 20px;
  margin-right: 16px;
}

.security-icon.success {
  color: #67c23a;
}

.security-text {
  flex: 1;
}

.security-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.security-desc {
  font-size: 14px;
  color: #909399;
}

.avatar-uploader {
  text-align: center;
}

.avatar-edit-img {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  border: 3px solid #ffb6d5;
  object-fit: cover;
  cursor: pointer;
  transition: transform 0.3s;
}

.avatar-edit-img:hover {
  transform: scale(1.05);
}

.avatar-upload-hint {
  margin-top: 8px;
  color: #909399;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.avatar-upload-hint i {
  color: #ffb6d5;
}

/* 禁用Element UI默认上传行为 */
::v-deep .avatar-uploader .el-upload {
  display: block;
}

::v-deep .avatar-uploader .el-upload-dragger {
  border: none;
  background: transparent;
}

::v-deep .avatar-uploader .el-upload-dragger:hover {
  border: none;
  background: transparent;
}

/* 对话框样式 */
::v-deep .profile-dialog .el-dialog__header {
  background: linear-gradient(135deg, #ffe4ec 0%, #ffd6e6 100%);
  padding: 20px;
}

::v-deep .profile-dialog .el-dialog__title {
  color: #303133;
  font-weight: bold;
}

::v-deep .password-dialog .el-dialog__header {
  background: linear-gradient(135deg, #fff3cd 0%, #ffeaa7 100%);
  padding: 20px;
}

::v-deep .password-dialog .el-dialog__title {
  color: #303133;
  font-weight: bold;
}

/* 按钮样式 */
::v-deep .el-button--primary {
  background: #ffb6d5 !important;
  border-color: #ffb6d5 !important;
}

::v-deep .el-button--primary:hover,
::v-deep .el-button--primary:focus {
  background: #ff5c8a !important;
  border-color: #ff5c8a !important;
}

::v-deep .el-button--warning {
  background: #e6a23c !important;
  border-color: #e6a23c !important;
}

::v-deep .el-button--warning:hover,
::v-deep .el-button--warning:focus {
  background: #f39c12 !important;
  border-color: #f39c12 !important;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }

  .back-btn {
    align-self: flex-start;
    margin-bottom: 8px;
  }

  .header-text {
    order: 2;
  }

  .profile-content {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .info-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .avatar-section {
    flex-direction: column;
    text-align: center;
  }

  .profile-avatar {
    margin-right: 0;
    margin-bottom: 16px;
  }

  .page-header h2 {
    font-size: 24px;
  }

  .page-header p {
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  .profile-page {
    padding: 16px;
  }

  .header-content {
    padding: 0 10px;
  }

  .back-btn {
    font-size: 14px;
    padding: 8px 16px;
  }

  .profile-avatar {
    width: 60px;
    height: 60px;
  }

  .info-item {
    padding: 12px;
  }

  .security-item {
    padding: 12px;
  }
}
</style>
