<template>
  <div class="favorites-page">
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h2>我的收藏</h2>
          <p>管理您收藏的课程</p>
        </div>
        <el-button
          type="primary"
          @click="goHome"
          class="back-btn"
          style="background: #ffb6d5; border-color: #ffb6d5; color: #fff; font-weight: bold; border-radius: 12px; padding: 10px 20px; display: flex; align-items: center; gap: 5px; min-width: 120px; justify-content: center; box-shadow: 0 2px 8px #f0c1d6cc;"
        >
          <i class="el-icon-back"></i>
          返回主页
        </el-button>
      </div>
    </div>
    <el-card v-loading="loading" class="favorites-card">
      <div v-if="!isLogin" class="permission-error">
        <i class="el-icon-warning permission-icon"></i>
        <p>请先登录</p>
        <el-button type="primary" @click="goLogin">去登录</el-button>
      </div>
      <div v-else-if="!isStudent" class="permission-error">
        <i class="el-icon-warning permission-icon"></i>
        <p>只有学生可以访问收藏页面</p>
        <el-button type="primary" @click="goHome">返回主页</el-button>
      </div>
      <div v-else-if="favorites.length === 0 && !loading" class="empty-state">
        <i class="el-icon-star-off empty-icon"></i>
        <p>暂无收藏课程</p>
        <el-button type="primary" @click="goHome">去浏览课程</el-button>
      </div>
      <el-table v-else :data="favorites" border style="width: 100%;" class="favorites-table">
        <el-table-column prop="courseId" label="课程ID" width="80"/>
        <el-table-column prop="courseName" label="课程名称"/>
        <el-table-column prop="categoryName" label="分类" width="120">
          <template slot-scope="scope">
            <el-tag size="small" type="info">{{ scope.row.categoryName || '未分类' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="teacherName" label="教师" width="120">
          <template slot-scope="scope">
            <span class="teacher-name">{{ scope.row.teacherName || '未知教师' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="收藏时间" width="160">
          <template slot-scope="scope">
            <span class="time-text">{{ formatTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="120">
          <template slot-scope="scope">
            <span class="price-text">￥{{ scope.row.price }}</span>
            <span v-if="scope.row.originalPrice" class="original-price">
              ￥{{ scope.row.originalPrice }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" @click="viewCourse(scope.row)" class="view-btn">
              查看课程
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <!-- 分页 -->
      <div class="pagination-container" v-if="favorites.length > 0">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pagination.page"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pagination.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          background>
        </el-pagination>
      </div>
    </el-card>
  </div>
</template>
<script>
import { mapState } from 'vuex'
export default {
  name: 'Favorites',
  data() {
    return {
      loading: false,
      favorites: [
        {
          courseId: 1,
          courseName: 'Vue.js入门到精通',
          categoryName: '前端开发',
          teacherName: '张老师',
          createTime: '2024-01-10 14:30:00',
          price: 99.00,
          originalPrice: 199.00
        },
        {
          courseId: 2,
          courseName: 'React全栈开发',
          categoryName: '前端开发',
          teacherName: '李老师',
          createTime: '2024-01-12 09:15:00',
          price: 129.00,
          originalPrice: 259.00
        },
        {
          courseId: 3,
          courseName: 'Java核心技术',
          categoryName: '后端开发',
          teacherName: '王老师',
          createTime: '2024-01-08 16:45:00',
          price: 149.00,
          originalPrice: 299.00
        },
        {
          courseId: 4,
          courseName: 'Python数据分析',
          categoryName: '编程语言',
          teacherName: '陈老师',
          createTime: '2024-01-15 11:20:00',
          price: 89.00,
          originalPrice: 179.00
        }
      ],
      pagination: {
        page: 1,
        pageSize: 10,
        total: 4
      }
    }
  },
  computed: {
    ...mapState({
      token: state => state.user.token,
      userType: state => state.user.userType
    }),
    isLogin() {
      return !!this.token
    },
    isStudent() {
      return this.userType === 'STUDENT'
    }
  },
  created() {
    console.log('收藏页面使用假数据')
  },
  methods: {
    checkPermission() {
      console.log('权限检查（假数据）')
    },
    loadFavorites() {
      console.log('加载收藏列表（假数据）')
    },
    viewCourse(row) {
      this.$router.push(`/course/${row.courseId}`)
    },
    handleSizeChange(val) {
      this.pagination.pageSize = val
      this.pagination.page = 1
      this.$message.success('分页大小已更改（假数据）')
    },
    handleCurrentChange(val) {
      this.pagination.page = val
      this.$message.success('页码已更改（假数据）')
    },
    formatTime(timestamp) {
      const date = new Date(timestamp)
      return date.toLocaleString()
    },
    goHome() {
      this.$router.push('/')
    },
    goLogin() {
      this.$router.push('/login')
    }
  }
}
</script>
<style scoped>
.favorites-page {
  padding: 20px;
  min-height: 100vh;
  background: linear-gradient(135deg, #ffe4ec 0%, #ffd6e6 100%);
}
.page-header {
  margin-bottom: 20px;
}
.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  padding: 20px;
  border-radius: 16px;
  box-shadow: 0 4px 16px #f0c1d6cc;
}
.title-section h2 {
  margin: 0 0 8px 0;
  color: #ff5c8a;
  font-size: 28px;
  font-weight: bold;
}
.title-section p {
  margin: 0;
  color: #888;
  font-size: 14px;
}
.back-btn {
  background: #ffb6d5 !important;
  border-color: #ffb6d5 !important;
  color: #fff !important;
  font-weight: bold;
  border-radius: 12px;
  padding: 10px 20px;
  display: flex !important;
  align-items: center;
  gap: 5px;
  min-width: 120px;
  justify-content: center;
  box-shadow: 0 2px 8px #f0c1d6cc !important;
  z-index: 10;
  position: relative;
}
.back-btn:hover {
  background: #ff5c8a !important;
  border-color: #ff5c8a !important;
}
.favorites-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 16px #f0c1d6cc;
  border: none;
  overflow: hidden;
}
.favorites-table {
  border-radius: 12px;
  overflow: hidden;
}
.favorites-table ::v-deep .el-table__header-wrapper {
  background: linear-gradient(90deg, #ffe4ec 0%, #ffd6e6 100%);
}
.favorites-table ::v-deep .el-table__header th {
  background: transparent;
  color: #ff5c8a;
  font-weight: bold;
  border-bottom: 2px solid #ffb6d5;
}
.favorites-table ::v-deep .el-table__body tr:hover > td {
  background: #fff5f8;
}
.favorites-table ::v-deep .el-table__body td {
  border-bottom: 1px solid #ffe4ec;
}
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #888;
}
.empty-icon {
  font-size: 60px;
  color: #ffb6d5;
  margin-bottom: 20px;
  display: block;
}
.empty-state p {
  font-size: 16px;
  margin-bottom: 20px;
  color: #666;
}
.empty-state .el-button {
  background: #ffb6d5;
  border-color: #ffb6d5;
  color: #fff;
  font-weight: bold;
  border-radius: 12px;
  padding: 12px 24px;
}
.empty-state .el-button:hover {
  background: #ff5c8a;
  border-color: #ff5c8a;
}
.teacher-name {
  color: #ff5c8a;
  font-weight: bold;
  font-size: 14px;
}
.time-text {
  color: #888;
  font-size: 12px;
}
.price-text {
  color: #ff5c8a;
  font-weight: bold;
  font-size: 14px;
}
.original-price {
  text-decoration: line-through;
  color: #bbb;
  font-size: 12px;
  margin-left: 8px;
}
.view-btn {
  background: #ffb6d5;
  border-color: #ffb6d5;
  color: #fff;
  font-weight: bold;
  border-radius: 8px;
  width: 100%;
}
.view-btn:hover {
  background: #ff5c8a;
  border-color: #ff5c8a;
}
.pagination-container {
  margin-top: 20px;
  text-align: center;
  padding: 20px 0;
}
.pagination-container ::v-deep .el-pagination.is-background .el-pager li:not(.disabled).active {
  background-color: #ffb6d5;
  color: #fff;
}
.pagination-container ::v-deep .el-pagination.is-background .el-pager li:not(.disabled):hover {
  color: #ff5c8a;
}
.pagination-container ::v-deep .el-pagination.is-background .btn-prev,
.pagination-container ::v-deep .el-pagination.is-background .btn-next {
  background-color: #fff;
  color: #ff5c8a;
  border: 1px solid #ffb6d5;
}
.pagination-container ::v-deep .el-pagination.is-background .btn-prev:hover,
.pagination-container ::v-deep .el-pagination.is-background .btn-next:hover {
  background-color: #ffb6d5;
  color: #fff;
}
.permission-error {
  text-align: center;
  padding: 60px 20px;
  color: #888;
}
.permission-icon {
  font-size: 60px;
  color: #ffb6d5;
  margin-bottom: 20px;
  display: block;
}
.permission-error p {
  font-size: 16px;
  margin-bottom: 20px;
  color: #666;
}
.permission-error .el-button {
  background: #ffb6d5;
  border-color: #ffb6d5;
  color: #fff;
  font-weight: bold;
  border-radius: 12px;
  padding: 12px 24px;
}
.permission-error .el-button:hover {
  background: #ff5c8a;
  border-color: #ff5c8a;
}
@media (max-width: 768px) {
  .favorites-page {
    padding: 10px;
  }
  .header-content {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }
  .title-section h2 {
    font-size: 24px;
  }
  .back-btn {
    width: 100%;
  }
  .favorites-table ::v-deep .el-table {
    font-size: 12px;
  }
  .favorites-table ::v-deep .el-table th,
  .favorites-table ::v-deep .el-table td {
    padding: 8px 4px;
  }
}
</style>
