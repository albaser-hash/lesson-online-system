<template>
  <div class="progress-page">
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h2>学习进度</h2>
          <p>跟踪您的学习进度</p>
        </div>
        <div class="header-actions">
          <el-button @click="goHome" class="back-btn">
            <i class="el-icon-back"></i>
            返回主页
          </el-button>
        </div>
      </div>
    </div>
    
    <el-card class="progress-card">
      <el-table :data="progressList" border style="width: 100%;" class="progress-table">
        <el-table-column prop="progress_id" label="进度ID" width="80"/>
        <el-table-column prop="course_name" label="课程名称"/>
        <el-table-column prop="chapter_name" label="章节名称"/>
        <el-table-column prop="status" label="学习状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status===1?'success':'info'" class="status-tag">
              {{ scope.row.status===1?'已学完':'未学习' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="last_watch_time" label="最后观看时间"/>
        <el-table-column label="操作" width="120">
          <template slot-scope="scope">
            <el-button size="mini" @click="viewChapter(scope.row)" class="study-btn">去学习</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>
<script>

export default {
  name: 'Progress',
  data() {
    return {
      progressList: []
    }
  },
  created() {
    this.loadProgress()
  },
  methods: {
    async loadProgress() {
      try {
        const response = await getProgressList()
        const res = response.data
        if (res.code === 200) {
          this.progressList = res.data || []
        } else {
          this.progressList = []
          this.$message.error(res.msg || '获取进度失败')
        }
      } catch (e) {
        this.progressList = []
        this.$message.error('获取进度失败')
      }
    },
    viewChapter(row) {
      this.$message.info('假跳转：章节 ' + row.chapter_name)
    },
    goHome() {
      this.$router.push('/')
    }
  }
}
</script>
<style scoped>
.progress-page {
  padding: 20px;
  min-height: 100vh;
  background: linear-gradient(135deg, #ffe4ec 0%, #ffd6e6 100%);
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

.progress-card {
  margin-bottom: 20px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 16px #f0c1d6cc;
  border: none;
  overflow: hidden;
}

.progress-table {
  border-radius: 12px;
  overflow: hidden;
}

.progress-table ::v-deep .el-table__header-wrapper {
  background: linear-gradient(90deg, #ffe4ec 0%, #ffd6e6 100%);
}

.progress-table ::v-deep .el-table__header th {
  background: transparent;
  color: #ff5c8a;
  font-weight: bold;
  border-bottom: 2px solid #ffb6d5;
}

.progress-table ::v-deep .el-table__body tr:hover > td {
  background: #fff5f8;
}

.progress-table ::v-deep .el-table__body td {
  border-bottom: 1px solid #ffe4ec;
}

.status-tag {
  border-radius: 8px;
  font-weight: bold;
}

.study-btn {
  background: #ffb6d5 !important;
  border-color: #ffb6d5 !important;
  color: #fff !important;
  font-weight: bold;
  border-radius: 8px;
}

.study-btn:hover {
  background: #ff5c8a !important;
  border-color: #ff5c8a !important;
}

@media (max-width: 768px) {
  .progress-page {
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
  
  .progress-card {
    border-radius: 10px;
    padding: 8px;
  }
  
  .progress-table ::v-deep .el-table {
    font-size: 12px;
  }
  
  .progress-table ::v-deep .el-table th,
  .progress-table ::v-deep .el-table td {
    padding: 8px 4px;
  }
}
</style> 