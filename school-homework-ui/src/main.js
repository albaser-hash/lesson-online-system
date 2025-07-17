import Vue from 'vue'
import Cookies from 'js-cookie'
import Element from 'element-ui'
import './assets/styles/element-variables.scss'
import '@/assets/styles/index.scss' // global css
import App from './App'
import store from './store'
import router from './router'
import './permission' // permission control
import 'element-ui/lib/theme-chalk/index.css'
import { MessageBox, Message, Notification } from 'element-ui'
import plugins from '@/plugins'
import webSocketService from '@/utils/websocket'

Vue.use(Element, {
  size: Cookies.get('size') || 'medium' // set element-ui default size
})

Vue.use(plugins)

Vue.config.productionTip = false

Vue.prototype.$alert = MessageBox.alert
Vue.prototype.$message = Message
Vue.prototype.$notify = Notification

// 全局WebSocket自动连接
store.watch(
  (state) => state.user.id,
  (userId) => {
    if (userId) {
      webSocketService.connect(userId)
    } else {
      webSocketService.disconnect()
    }
  },
  { immediate: true }
)

// 全局WebSocket通知弹窗
const showNotify = (msg) => {
  if (msg && msg.message && Vue.prototype.$notify) {
    Vue.prototype.$notify({
      title: msg.examName ? '考试通知' : '新消息',
      message: msg.message,
      type: 'info',
      duration: 6000
    })
  }
}
webSocketService.addListener('EXAM_NOTIFICATION', showNotify)
webSocketService.addListener('SYSTEM_NOTIFICATION', showNotify)
webSocketService.addListener('COURSE_NOTIFICATION', showNotify)
webSocketService.addListener('REVIEW_NOTIFICATION', showNotify)

new Vue({
  el: '#app',
  router,
  store,
  render: h => h(App)
})
