<template>
  <div class="cart-page">
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h2>购物车</h2>
          <p>管理您要购买的课程</p>
        </div>
        <div class="header-actions">
          <el-button @click="goHome" class="back-btn">
            <i class="el-icon-back"></i>
            返回主页
          </el-button>
        </div>
      </div>
    </div>
    <el-card class="cart-card">
      <el-table :data="cart" border ref="cartTable" @selection-change="handleSelectionChange" style="width:100%;margin-bottom:20px;" class="cart-table">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="courseName" label="课程名称" />
        <el-table-column prop="price" label="价格" width="100">
          <template slot-scope="scope">
            ¥{{ scope.row.price }}
          </template>
        </el-table-column>
        <el-table-column label="封面" width="100">
          <template slot-scope="scope">
            <img :src="scope.row.coverImage" class="course-cover" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template slot-scope="scope">
            <el-button type="text" size="mini" @click="removeCourse(scope.row)" class="remove-btn">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="cart-actions">
        <el-button type="danger" @click="clearCart" size="small" class="clear-btn">清空购物车</el-button>
        <el-button type="primary" :disabled="!selected.length" @click="checkout" size="small" class="checkout-btn">结算（{{ selected.length }}门课程）</el-button>
      </div>
    </el-card>
  </div>
</template>
<script>
export default {
  name: 'Cart',
  data() {
    return {
      cart: [
        {
          courseId: 1,
          courseName: 'Vue.js入门到精通',
          price: 99.00,
          coverImage: 'https://example.com/vue-course.jpg',
        },
        {
          courseId: 2,
          courseName: 'React全栈开发',
          price: 129.00,
          coverImage: 'https://example.com/react-course.jpg',
        },
        {
          courseId: 3,
          courseName: 'Java核心技术',
          price: 149.00,
          coverImage: 'https://example.com/java-course.jpg',
        }
      ],
      selected: []
    }
  },
  created() {
    console.log('购物车页面使用假数据')
  },
  methods: {
    goHome() {
      this.$router.push('/')
    },
    loadCart() {
      console.log('加载购物车（假数据）')
    },
    handleSelectionChange(val) {
      this.selected = val
    },
    removeCourse(row) {
      const index = this.cart.findIndex(item => item.courseId === row.courseId)
      if (index > -1) {
        this.cart.splice(index, 1)
        this.$message.success('已移除（假数据）')
      }
    },
    clearCart() {
      this.cart = []
      this.selected = []
      this.$message.success('购物车已清空（假数据）')
    },
    checkout() {
      if (this.selected.length === 0) {
        this.$message.warning('请选择要购买的课程')
        return
      }
      this.$message.success('正在处理订单（假数据）...')
      setTimeout(() => {
        this.$alert('已成功结算' + this.selected.length + ' 门课程！（假数据）', '结算成功', {
          confirmButtonText: '确定',
          callback: () => {
            this.selected.forEach(selectedItem => {
              const index = this.cart.findIndex(item => item.courseId === selectedItem.courseId)
              if (index > -1) {
                this.cart.splice(index, 1)
              }
            })
            this.selected = []
          }
        })
      }, 1500)
    }
  }
}
</script>
<style scoped>
.cart-page {
  padding: 20px;
  min-height: 100vh;
  background: linear-gradient(135deg, #ffe4ec 0%, #ffd6e6 100%);
  max-width: 900px;
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
.cart-card {
  margin-bottom: 20px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 16px #f0c1d6cc;
  border: none;
  overflow: hidden;
}
.cart-table {
  border-radius: 12px;
  overflow: hidden;
}
.cart-table ::v-deep .el-table__header-wrapper {
  background: linear-gradient(90deg, #ffe4ec 0%, #ffd6e6 100%);
}
.cart-table ::v-deep .el-table__header th {
  background: transparent;
  color: #ff5c8a;
  font-weight: bold;
  border-bottom: 2px solid #ffb6d5;
}
.cart-table ::v-deep .el-table__body tr:hover > td {
  background: #fff5f8;
}
.cart-table ::v-deep .el-table__body td {
  border-bottom: 1px solid #ffe4ec;
}
.course-cover {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
  border: 2px solid #ffb6d5;
}
.remove-btn {
  color: #f56c6c !important;
}
.cart-actions {
  text-align: right;
  padding: 20px 0;
}
.clear-btn {
  border-radius: 8px;
  font-weight: bold;
  margin-right: 10px;
}
.checkout-btn {
  background: #ffb6d5 !important;
  border-color: #ffb6d5 !important;
  color: #fff !important;
  font-weight: bold;
  border-radius: 12px;
  padding: 10px 20px;
}
.checkout-btn:hover {
  background: #ff5c8a !important;
  border-color: #ff5c8a !important;
}
.checkout-btn:disabled {
  background: #c0c4cc !important;
  border-color: #c0c4cc !important;
  color: #fff !important;
}
@media (max-width: 768px) {
  .cart-page {
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
  .cart-card {
    border-radius: 10px;
    padding: 8px;
  }
  .cart-table ::v-deep .el-table {
    font-size: 12px;
  }
  .cart-table ::v-deep .el-table th,
  .cart-table ::v-deep .el-table td {
    padding: 8px 4px;
  }
  .cart-actions {
    text-align: center;
  }
  .clear-btn, .checkout-btn {
    width: 100%;
    margin-bottom: 10px;
  }
}
</style>
