import Vue from 'vue'
import Vuex from 'vuex'
import app from './modules/app'
import user from './modules/user'
import getters from './getters'

Vue.use(Vuex)

// 缓存配置
const CACHE_KEY_MENUS = 'nav_menus_cache'
const CACHE_KEY_CATEGORIES = 'categories_cache'
const CACHE_EXPIRE_TIME = 30 * 60 * 1000 // 30分钟

const store = new Vuex.Store({
  modules: {
    app,
    user
  },
  getters,
  state: {
    dynamicMenus: [],
    categories: [],
    menuLoading: false,
    categoryLoading: false
  },
  mutations: {
    SET_DYNAMIC_MENUS(state, menus) {
      state.dynamicMenus = menus || [];
      // 同时保存到本地缓存
      if (menus && menus.length > 0) {
        const cacheData = {
          data: menus,
          timestamp: Date.now(),
          userType: store.state.user.userType
        }
        localStorage.setItem(CACHE_KEY_MENUS, JSON.stringify(cacheData))
      }
    },
    SET_CATEGORIES(state, categories) {
      state.categories = categories || [];
      // 同时保存到本地缓存
      if (categories && categories.length > 0) {
        const cacheData = {
          data: categories,
          timestamp: Date.now()
        }
        localStorage.setItem(CACHE_KEY_CATEGORIES, JSON.stringify(cacheData))
      }
    },
    CLEAR_NAV_AND_CATEGORIES(state) {
      state.dynamicMenus = [];
      state.categories = [];
      // 清除本地缓存
      localStorage.removeItem(CACHE_KEY_MENUS)
      localStorage.removeItem(CACHE_KEY_CATEGORIES)
    },
    SET_MENU_LOADING(state, loading) {
      state.menuLoading = loading
    },
    SET_CATEGORY_LOADING(state, loading) {
      state.categoryLoading = loading
    }
  },
  actions: {
    async fetchDynamicMenus({ commit, state, rootState }) {
      // 如果正在加载，直接返回当前数据
      if (state.menuLoading) {
        return state.dynamicMenus
      }
      
      commit('SET_MENU_LOADING', true)
      const userType = rootState.user.userType || 'STUDENT';
      
      // 检查本地缓存
      const cachedData = localStorage.getItem(CACHE_KEY_MENUS)
      if (cachedData) {
        try {
          const cache = JSON.parse(cachedData)
          // 检查缓存是否过期且用户类型匹配
          if (Date.now() - cache.timestamp < CACHE_EXPIRE_TIME && cache.userType === userType) {
            commit('SET_DYNAMIC_MENUS', cache.data)
            commit('SET_MENU_LOADING', false)
            return cache.data
          }
        } catch (e) {
          // 缓存数据损坏，清除缓存
          localStorage.removeItem(CACHE_KEY_MENUS)
        }
      }
      
      // 缓存无效或过期，重新请求
      const { getNavMenu } = require('@/api/menu');
      try {
        const res = await getNavMenu(userType);
        let menus = Array.isArray(res) ? res : (Array.isArray(res.data) ? res.data : []);
        commit('SET_DYNAMIC_MENUS', menus);
        return menus;
      } catch (e) {
        commit('SET_DYNAMIC_MENUS', []);
        return [];
      } finally {
        commit('SET_MENU_LOADING', false)
      }
    },
    
    // 添加强制刷新方法
    async forceRefreshMenus({ commit, rootState }) {
      const userType = rootState.user.userType || 'STUDENT';
      // 清除本地缓存
      localStorage.removeItem(CACHE_KEY_MENUS)
      
      const { getNavMenu } = require('@/api/menu');
      try {
        const res = await getNavMenu(userType);
        let menus = Array.isArray(res) ? res : (Array.isArray(res.data) ? res.data : []);
        commit('SET_DYNAMIC_MENUS', menus);
        return menus;
      } catch (e) {
        commit('SET_DYNAMIC_MENUS', []);
        return [];
      }
    },
    
    async fetchCategories({ commit, state }) {
      // 如果正在加载，直接返回当前数据
      if (state.categoryLoading) {
        return state.categories
      }
      
      commit('SET_CATEGORY_LOADING', true)
      
      // 检查本地缓存
      const cachedData = localStorage.getItem(CACHE_KEY_CATEGORIES)
      if (cachedData) {
        try {
          const cache = JSON.parse(cachedData)
          // 检查缓存是否过期
          if (Date.now() - cache.timestamp < CACHE_EXPIRE_TIME) {
            commit('SET_CATEGORIES', cache.data)
            commit('SET_CATEGORY_LOADING', false)
            return cache.data
          }
        } catch (e) {
          // 缓存数据损坏，清除缓存
          localStorage.removeItem(CACHE_KEY_CATEGORIES)
        }
      }
      
      const { listCourseCategories } = require('@/api/courseCategories');
      try {
        const res = await listCourseCategories({ page: 1, pageSize: 100 });
        const data = res.data.data;
        const categories = data.records || data || [];
        commit('SET_CATEGORIES', categories);
        return categories;
      } catch (e) {
        commit('SET_CATEGORIES', []);
        return [];
      } finally {
        commit('SET_CATEGORY_LOADING', false)
      }
    },
    
    clearNavAndCategories({ commit }) {
      commit('CLEAR_NAV_AND_CATEGORIES');
    },
    
    // 初始化store数据
    initializeStore({ commit, rootState }) {
      // 恢复菜单缓存
      const menuCache = localStorage.getItem(CACHE_KEY_MENUS)
      if (menuCache) {
        try {
          const cache = JSON.parse(menuCache)
          if (Date.now() - cache.timestamp < CACHE_EXPIRE_TIME && cache.userType === rootState.user.userType) {
            commit('SET_DYNAMIC_MENUS', cache.data)
          } else {
            localStorage.removeItem(CACHE_KEY_MENUS)
          }
        } catch (e) {
          localStorage.removeItem(CACHE_KEY_MENUS)
        }
      }
      
      // 恢复分类缓存
      const categoryCache = localStorage.getItem(CACHE_KEY_CATEGORIES)
      if (categoryCache) {
        try {
          const cache = JSON.parse(categoryCache)
          if (Date.now() - cache.timestamp < CACHE_EXPIRE_TIME) {
            commit('SET_CATEGORIES', cache.data)
          } else {
            localStorage.removeItem(CACHE_KEY_CATEGORIES)
          }
        } catch (e) {
          localStorage.removeItem(CACHE_KEY_CATEGORIES)
        }
      }
    }
  },
})

export default store
