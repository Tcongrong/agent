import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import { useUserStore } from '../stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
      meta: {
        requiresAuth: true // 首页需要登录才能访问
      }
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
      meta: {
        requiresAuth: false // 登录页不需要登录
      }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/RegisterView.vue'),
      meta: {
        requiresAuth: false // 注册页不需要登录
      }
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('../views/AboutView.vue'),
      meta: {
        requiresAuth: true // 关于页需要登录才能访问
      }
    },
  ],
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const requiresAuth = to.meta.requiresAuth
  
  // 如果页面需要登录但用户未登录，则跳转到登录页
  if (requiresAuth && !userStore.isAuthenticated) {
    next('/login')
  } 
  // 如果用户已登录但尝试访问登录页，则跳转到首页
  else if (userStore.isAuthenticated && to.name === 'login') {
    next('/')
  } 
  // 其他情况正常跳转
  else {
    next()
  }
})

export default router
