<template>
  <div class="orders-page">
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h2>我的订单</h2>
          <p>查看您的所有订单记录</p>
        </div>
        <div class="header-actions">
          <el-button @click="goHome" class="back-btn">
            <i class="el-icon-back"></i>
            返回主页
          </el-button>
        </div>
      </div>
    </div>
    
    <el-card class="orders-card">
      <el-table :data="orders" border style="width: 100%;" class="orders-table">
        <el-table-column prop="order_id" label="订单号" width="180"/>
        <el-table-column prop="course_name" label="课程名称"/>
        <el-table-column prop="order_amount" label="金额">
          <template slot-scope="scope">
            ￥{{ scope.row.order_amount }}
          </template>
        </el-table-column>
        <el-table-column prop="payment_method" label="支付方式">
          <template slot-scope="scope">
            <el-tag :type="scope.row.payment_method === 'ALIPAY' ? 'primary' : 'success'" class="payment-tag">
              {{ scope.row.payment_method === 'ALIPAY' ? '支付宝' : '微信支付' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="order_status" label="订单状态">
          <template slot-scope="scope">
            <el-tag :type="scope.row.order_status === 'PAID' ? 'success' : 'warning'" class="status-tag">
              {{ scope.row.order_status === 'PAID' ? '已支付' : '未支付' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="create_time" label="下单时间"/>
        <el-table-column prop="pay_time" label="支付时间"/>
        <el-table-column label="操作" width="120">
          <template slot-scope="scope">
            <el-button size="mini" @click="viewDetail(scope.row)" class="detail-btn">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'Orders',
  data() {
    return {
      orders: [
        {
          order_id: '202406260001',
          course_name: '高等数学',
          order_amount: 99.00,
          payment_method: 'ALIPAY',
          order_status: 'PAID',
          create_time: '2024-06-25 10:00',
          pay_time: '2024-06-25 10:05'
        },
        {
          order_id: '202406260002',
          course_name: '大学英语',
          order_amount: 59.00,
          payment_method: 'WECHAT',
          order_status: 'UNPAID',
          create_time: '2024-06-26 09:00',
          pay_time: ''
        }
      ]
    }
  },
  methods: {
    viewDetail(row) {
      this.$message.info('假跳转：订单号 ' + row.order_id)
    },
    goHome() {
      // Implementation of goHome method
    }
  }
}
</script>

<style scoped>
.orders-page {
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

.orders-card {
  margin-bottom: 20px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 16px #f0c1d6cc;
  border: none;
  overflow: hidden;
}

.orders-table {
  border-radius: 12px;
  overflow: hidden;
}

.orders-table ::v-deep .el-table__header-wrapper {
  background: linear-gradient(90deg, #ffe4ec 0%, #ffd6e6 100%);
}

.orders-table ::v-deep .el-table__header th {
  background: transparent;
  color: #ff5c8a;
  font-weight: bold;
  border-bottom: 2px solid #ffb6d5;
}

.orders-table ::v-deep .el-table__body tr:hover > td {
  background: #fff5f8;
}

.orders-table ::v-deep .el-table__body td {
  border-bottom: 1px solid #ffe4ec;
}

.payment-tag {
  background: #ffb6d5 !important;
  color: #fff !important;
  border-radius: 8px;
  font-weight: bold;
  border: none;
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
  .orders-page {
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
  
  .orders-card {
    border-radius: 10px;
    padding: 8px;
  }
  
  .orders-table ::v-deep .el-table {
    font-size: 12px;
  }
  
  .orders-table ::v-deep .el-table th,
  .orders-table ::v-deep .el-table td {
    padding: 8px 4px;
  }
}
</style> 