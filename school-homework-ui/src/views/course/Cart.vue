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
        <el-button type="primary" :disabled="!selected.length" @click="checkout" size="small" class="checkout-btn">结算（{{ selected.length }}）</el-button>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getCartList, removeFromCart, clearCart, createOrder, payOrder } from '@/api/cart'

export default {
  name: 'Cart',
  data() {
    return {
      cart: [],
      selected: []
    }
  },
  created() {
    this.loadCart()
  },
  methods: {
    goHome() {
      this.$router.push('/')
    },
    async loadCart() {
      try {
        const response = await getCartList()
        if (response.data.code === 200) {
          this.cart = response.data.data
        } else {
          this.cart = []
          this.$message.error(response.data.msg || '获取购物车失败')
        }
      } catch (error) {
        console.error('获取购物车失败:', error)
        this.cart = []
        this.$message.error('获取购物车失败')
      }
    },
    handleSelectionChange(val) {
      this.selected = val
    },
    async removeCourse(row) {
      try {
        const response = await removeFromCart(row.courseId)
        if (response.data.code === 200) {
          this.$message.success('已移除')
          this.loadCart() // 重新加载购物车
        } else {
          this.$message.error(response.data.msg || '移除失败')
        }
      } catch (error) {
        console.error('移除课程失败:', error)
        this.$message.error('移除失败')
      }
    },
    async clearCart() {
      try {
        const response = await clearCart()
        if (response.data.code === 200) {
          this.$message.success('购物车已清空')
          this.cart = []
          this.selected = []
        } else {
          this.$message.error(response.data.msg || '清空失败')
        }
      } catch (error) {
        console.error('清空购物车失败:', error)
        this.$message.error('清空失败')
      }
    },
    async checkout() {
      if (this.selected.length === 0) {
        this.$message.warning('请选择要购买的课程')
        return
      }

      try {
        // 创建订单
        const courseIds = this.selected.map(c => c.courseId)
        const orderData = {
          courseIds: courseIds,
          paymentMethod: 'ALIPAY' // 默认使用支付宝
        }

        const createResponse = await createOrder(orderData)
        if (createResponse.data.code === 200) {
          const orderId = createResponse.data.data

          // 模拟支付
          const payResponse = await payOrder(orderId)
          if (payResponse.data.code === 200) {
            this.$alert('已成功结算 ' + this.selected.length + ' 门课程！', '结算成功', {
              confirmButtonText: '确定',
              callback: () => {
                this.loadCart() // 重新加载购物车
                this.selected = []
              }
            })
          } else {
            this.$message.error(payResponse.data.msg || '支付失败')
          }
        } else {
          this.$message.error(createResponse.data.msg || '创建订单失败')
        }
      } catch (error) {
        console.error('结算失败:', error)
        this.$message.error('结算失败')
      }
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
