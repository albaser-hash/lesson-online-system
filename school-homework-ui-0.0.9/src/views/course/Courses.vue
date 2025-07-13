<template>
  <div class="courses-page">
    <!-- 通用导航栏 -->
    <CommonNav activeIndex="courses" />
    
    <div class="page-content">
      <div class="page-header">
        <div class="header-content">
          <div class="title-section">
            <h2>课程列表</h2>
            <p>探索丰富的在线课程</p>
          </div>
          <div class="header-actions">
            <el-button @click="goHome" class="back-btn">
              <i class="el-icon-back"></i>
              返回主页
            </el-button>
          </div>
        </div>
      </div>
      <el-card class="search-card">
        <div class="search-bar">
          <el-input v-model="searchKeyword" placeholder="搜索课程/教师/分类" style="width: 300px; margin-right: 16px;" @keyup.enter="doSearch" />
          <el-button type="primary" @click="doSearch" class="search-btn">搜索</el-button>
        </div>
      </el-card>
      <el-card class="courses-card">
        <el-table :data="courses" border style="width: 100%;" class="courses-table">
          <el-table-column prop="course_id" label="序号" type="index" width="60"/>
          <el-table-column prop="course_name" label="课程名称"/>
          <el-table-column prop="course_desc" label="简介"/>
          <el-table-column prop="category_name" label="分类">
            <template slot-scope="scope">
              <el-tag class="category-tag">{{ scope.row.category_name }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="teacher_name" label="教师"/>
          <el-table-column prop="price" label="价格">
            <template slot-scope="scope">
              ￥{{ scope.row.price }}
              <span v-if="scope.row.original_price" class="original-price">
                ￥{{ scope.row.original_price }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="student_count" label="学生数"/>
          <el-table-column prop="score" label="评分">
            <template slot-scope="scope">
              <el-rate v-model="scope.row.score" disabled show-score text-color="#ff9900" />
            </template>
          </el-table-column>
          <el-table-column prop="audit_status" label="审核状态">
            <template slot-scope="scope">
              <el-tag :type="scope.row.audit_status === 'APPROVED' ? 'success' : 'warning'" class="status-tag">
                {{ scope.row.audit_status === 'APPROVED' ? '已通过' : '待审核' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template slot-scope="scope">
              <el-button size="mini" @click="viewDetail(scope.row)" class="detail-btn">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>
<script>
import CommonNav from '@/components/CommonNav.vue'

export default {
  name: 'Courses',
  components: {
    CommonNav
  },
  data() {
    return {
      searchKeyword: '',
      courses: [
        {
          course_id: 1,
          course_name: 'Vue.js入门到精通',
          course_desc: '从零开始学习Vue.js框架，掌握前端开发技能',
          category_name: '前端开发',
          teacher_name: '张老师',
          price: 99.00,
          original_price: 199.00,
          student_count: 1250,
          score: 4.8,
          audit_status: 'APPROVED',
          cover_image: 'https://example.com/vue-course.jpg',
        },
        {
          course_id: 2,
          course_name: 'React全栈开发',
          course_desc: '学习React框架，掌握现代前端开发技能',
          category_name: '前端开发',
          teacher_name: '李老师',
          price: 129.00,
          original_price: 259.00,
          student_count: 890,
          score: 4.5,
          audit_status: 'APPROVED',
          cover_image: 'https://example.com/react-course.jpg',
        },
        {
          course_id: 3,
          course_name: 'Java核心技术',
          course_desc: '深入理解Java面向对象编程和核心概念',
          category_name: '后端开发',
          teacher_name: '王老师',
          price: 149.00,
          original_price: 299.00,
          student_count: 2100,
          score: 4.9,
          audit_status: 'APPROVED',
          cover_image: 'https://example.com/java-course.jpg',
        },
        {
          course_id: 4,
          course_name: 'Python数据分析',
          course_desc: 'Python数据分析与可视化实战',
          category_name: '编程语言',
          teacher_name: '陈老师',
          price: 89.00,
          original_price: 179.00,
          student_count: 756,
          score: 4.6,
          audit_status: 'PENDING',
          cover_image: 'https://example.com/python-course.jpg',
        },
        {
          course_id: 5,
          course_name: 'MySQL数据库设计',
          course_desc: '数据库设计与优化实战',
          category_name: '数据库',
          teacher_name: '刘老师',
          price: 79.00,
          original_price: 159.00,
          student_count: 432,
          score: 4.7,
          audit_status: 'APPROVED',
          cover_image: 'https://example.com/mysql-course.jpg',
        }
      ]
    }
  },
  methods: {
    viewDetail(row) {
      this.$router.push(`/course/${row.course_id}`)
    },
    doSearch() {
      if (this.searchKeyword.trim()) {
        const filteredCourses = this.courses.filter(course => 
          course.course_name.includes(this.searchKeyword) || 
          course.course_desc.includes(this.searchKeyword) ||
          course.teacher_name.includes(this.searchKeyword) ||
          course.category_name.includes(this.searchKeyword)
        )
        this.$message.success(`搜索到${filteredCourses.length} 门课程（假数据）`)
      } else {
        this.$message.info('请输入搜索关键词')
      }
    },
    goHome() {
      this.$router.push('/')
    }
  }
}
</script>
<style scoped>
.courses-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #ffe4ec 0%, #ffd6e6 100%);
}

.page-content {
  padding: 80px 20px 20px 20px;
  max-width: 1200px;
  margin: 0 auto;
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
.header-actions {
  display: flex;
  align-items: center;
}
.back-btn {
  background: #ffb6d5 !important;
  border-color: #ffb6d5 !important;
  color: #fff !important;
  font-weight: bold;
  border-radius: 12px;
  padding: 10px 20px;
  display: flex;
  align-items: center;
}
.back-btn i {
  margin-right: 5px;
}
.back-btn:hover {
  background: #ff5c8a !important;
  border-color: #ff5c8a !important;
}
.search-card {
  margin-bottom: 20px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 16px #f0c1d6cc;
  border: none;
  overflow: hidden;
}
.search-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}
.search-btn {
  background: #ffb6d5 !important;
  border-color: #ffb6d5 !important;
  color: #fff !important;
  font-weight: bold;
  border-radius: 12px;
  padding: 10px 20px;
}
.search-btn:hover {
  background: #ff5c8a !important;
  border-color: #ff5c8a !important;
}
.courses-card {
  margin-bottom: 20px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 16px #f0c1d6cc;
  border: none;
  overflow: hidden;
}
.courses-table {
  border-radius: 12px;
  overflow: hidden;
}
.courses-table ::v-deep .el-table__header-wrapper {
  background: linear-gradient(90deg, #ffe4ec 0%, #ffd6e6 100%);
}
.courses-table ::v-deep .el-table__header th {
  background: transparent;
  color: #ff5c8a;
  font-weight: bold;
  border-bottom: 2px solid #ffb6d5;
}
.courses-table ::v-deep .el-table__body tr:hover > td {
  background: #fff5f8;
}
.courses-table ::v-deep .el-table__body td {
  border-bottom: 1px solid #ffe4ec;
}
.category-tag {
  background: #ffb6d5 !important;
  color: #fff !important;
  border-radius: 8px;
  font-weight: bold;
  border: none;
}
.original-price {
  text-decoration: line-through;
  color: #bbb;
  font-size: 12px;
  margin-left: 5px;
}
.status-tag {
  border-radius: 8px;
  font-weight: bold;
}
.detail-btn {
  background: #ffb6d5 !important;
  border-color: #ffb6d5 !important;
  color: #fff !important;
  font-weight: bold;
  border-radius: 8px;
}
.detail-btn:hover {
  background: #ff5c8a !important;
  border-color: #ff5c8a !important;
}
@media (max-width: 768px) {
  .courses-page {
    padding: 8px;
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
    margin-bottom: 10px;
  }
  .search-card, .courses-card {
    border-radius: 10px;
    padding: 8px;
  }
  .search-bar {
    flex-direction: column;
    gap: 10px;
  }
  .search-bar .el-input {
    width: 100% !important;
    margin-right: 0 !important;
  }
  .search-btn {
    width: 100%;
  }
  .courses-table ::v-deep .el-table {
    font-size: 12px;
  }
  .courses-table ::v-deep .el-table th,
  .courses-table ::v-deep .el-table td {
    padding: 8px 4px;
  }
}
</style>
