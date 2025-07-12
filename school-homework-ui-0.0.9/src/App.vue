<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script>
export default {
  name: "App"
}
</script>

<style>
/* 响应式调整 el-message-box 弹窗 + 粉色风格 */
@media (max-width: 700px) {
  .el-message-box {
    width: 90vw !important;
    min-width: unset !important;
    max-width: 98vw !important;
    left: 5vw !important;
    padding: 12px 8px !important;
  }
  .el-message-box__btns {
    flex-direction: column;
    gap: 8px;
  }
  .el-message-box__btns .el-button {
    width: 100%;
    font-size: 16px;
  }
  .el-message-box__content {
    font-size: 16px;
    word-break: break-word;
  }
}
/* 粉色风格 */
.el-message-box {
  border-radius: 16px;
  background: linear-gradient(135deg, #fff0f6 0%, #ffe4ec 100%);
  box-shadow: 0 8px 32px #f0c1d6cc;
  border: 2px solid #ffb6d5;
}
.el-message-box__header {
  background: #ffe4ec;
  color: #ff5c8a;
  font-weight: bold;
  border-radius: 16px 16px 0 0;
  font-size: 20px;
  padding: 18px 24px 10px 24px;
}
.el-message-box__content {
  color: #ff5c8a;
  font-size: 17px;
  padding: 20px 0;
}
.el-message-box__btns .el-button--primary {
  background: #ffb6d5 !important;
  border-color: #ffb6d5 !important;
  color: #fff !important;
  font-weight: bold;
  border-radius: 12px;
}
.el-message-box__btns .el-button--primary:hover {
  background: #ff5c8a !important;
  border-color: #ff5c8a !important;
}
</style>
