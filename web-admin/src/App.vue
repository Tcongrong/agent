<template>
  <!-- 登录页面完全独立的根容器 -->
  <template v-if="$route.path === '/login'">
    <div id="login-root">
      <router-view v-slot="{ Component }">
        <component :is="Component" />
      </router-view>
    </div>
  </template>
  <!-- 其他页面保持原布局 -->
  <template v-else>
    <div id="app">
      <!-- 导航栏 -->
      <nav v-if="userStore.isAuthenticated" class="navbar">
        <div class="nav-content">
          <div class="logo">智能Agent</div>
          <div class="nav-links">
            <router-link to="/">首页</router-link>
            <router-link to="/about">关于</router-link>
          </div>
          <div class="user-info">
            <span class="username">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
            <button class="logout-btn" @click="handleLogout">登出</button>
          </div>
        </div>
      </nav>
      
      <!-- 主内容区域 -->
      <main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </template>
</template>

<script setup>
import { useUserStore } from './stores/user'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

// 登出处理函数
const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style>
/* 导入基础样式 */
@import './assets/main.css';

/* 登录页面根容器样式 */
#login-root {
  height: 100vh;
  width: 100vw;
  max-width: none;
  margin: 0;
  padding: 0;
  display: block;
}

/* 重置基础样式，确保非登录页面正常显示 */
#app {
  width: 100%;
  min-height: 100vh;
}

/* 导航栏样式 */
.navbar {
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-content {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
}

.logo {
  font-size: 1.5rem;
  font-weight: bold;
  color: #42b983;
}

.nav-links {
  display: flex;
  gap: 1.5rem;
}

.nav-links a {
  text-decoration: none;
  color: #333;
  transition: color 0.3s;
}

.nav-links a:hover {
  color: #42b983;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.username {
  color: #333;
}

.logout-btn {
  padding: 0.5rem 1rem;
  background-color: #e74c3c;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.logout-btn:hover {
  background-color: #c0392b;
}

/* 主内容样式 */
.main-content {
  width: 100%;
  margin: 0;
  padding: 0;
  min-height: calc(100vh - 60px);
}

/* 过渡动画 */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>
