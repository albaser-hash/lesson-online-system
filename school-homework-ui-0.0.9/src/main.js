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


Vue.use(Element, {
  size: Cookies.get('size') || 'medium' // set element-ui default size
})

Vue.use(plugins)

Vue.config.productionTip = false

Vue.prototype.$alert = MessageBox.alert
Vue.prototype.$message = Message
Vue.prototype.$notify = Notification



new Vue({
  el: '#app',
  router,
  store,
  render: h => h(App)
})
