import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)


// 公共路由
export const constantRoutes = [
  {
    path: '/login',
    component: () => import('@/views/user/login.vue'),
    hidden: true
  },
  {
    path: '/register',
    component: () => import('@/views/user/register.vue'),
    hidden: true
  },
  {
    path: '/404',
    component: () => import('@/views/common/404.vue'),
    hidden: true
  },
  {
    path: '/401',
    component: () => import('@/views/common/401.vue'),
    hidden: true
  },
  {
    path: '/',
    component: () => import('@/views/index.vue'),
    name: 'Index',
    meta: { title: '首页' }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/user/profile.vue')
  },
  {
    path: '/courses',
    name: 'Courses',
    component: () => import('@/views/course/Courses.vue')
  },
  {
    path: '/course/:id',
    name: 'CourseDetail',
    component: () => import('@/views/course/CourseDetail.vue')
  },
  {
    path: '/course/:courseId/doc/:chapterId',
    name: 'DocReader',
    component: () => import('@/views/common/DocReader.vue')
  },
  {
    path: '/course-create',
    name: 'CourseCreate',
    component: () => import('@/views/course/TeacherCourseManage.vue')
  },
  {
    path: '/forum',
    name: 'Forum',
    component: () => import('@/views/forum/Forum.vue')
  },
  // 新的考试系统路由
  {
    path: '/teacher/exam',
    name: 'TeacherExamManagement',
    component: () => import('@/views/exam/TeacherExamManagement.vue'),
    meta: {
      title: '考试管理',
      requiresAuth: true,
      role: 'teacher'
    }
  },
  {
    path: '/student/exam',
    name: 'StudentExamList',
    component: () => import('@/views/exam/StudentExamList.vue'),
    meta: {
      title: '我的考试',
      requiresAuth: true,
      role: 'student'
    }
  },
  {
    path: '/favorites',
    name: 'Favorites',
    component: () => import('@/views/course/Favorites.vue')
  },
  {
    path: '/orders',
    name: 'Orders',
    component: () => import('@/views/course/Orders.vue')
  },
  {
    path: '/progress',
    name: 'Progress',
    component: () => import('@/views/course/Progress.vue')
  },
  {
    path: '/qa',
    name: 'QA',
    component: () => import('@/views/qa/QA.vue')
  },
  {
    path: '/qa/:id',
    name: 'QAQuestionDetail',
    component: () => import('@/views/qa/QAQuestionDetail.vue')
  },
  {
    path: '/forum/topic/:id',
    name: 'ForumTopicDetail',
    component: () => import('@/views/forum/ForumTopicDetail.vue')
  },
  {
    path: '/cart',
    name: 'Cart',
    component: () => import('@/views/course/Cart.vue')
  },
  {
    path: '/my-courses',
    name: 'MyCourses',
    component: () => import('@/views/course/MyCourses.vue')
  },
  {
    path: '/audit',
    name: 'Audit',
    component: () => import('@/views/course/Audit.vue')
  },
  {
    path: '/chapter-manage',
    name: 'ChapterManage',
    component: () => import('@/views/course/ChapterManage.vue')
  },
  {
    path: '/teacher/exam/review',
    name: 'ReviewPapers',
    component: () => import('@/views/exam/ReviewPapers.vue')
  },
  {
    path: '/notification',
    name: 'Notification',
    component: () => import('@/views/notification/Notification.vue')
  },
  {
    path: '/video-player/:courseId/:chapterId',
    name: 'VideoPlayerPage',
    component: () => import('@/views/common/VideoPlayerPage.vue')
  }
]

export default new Router({
  mode: 'history', // 去掉url中的#
  scrollBehavior: () => ({ y: 0 }),
  routes: constantRoutes
})
