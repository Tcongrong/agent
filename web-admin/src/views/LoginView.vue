<template>
  <div class="login-container">
    <div class="login-wrapper">
      <div class="login-form">
        <div class="form-header">
          <h1>智能Agent系统</h1>
          <p class="subtitle">欢迎回来，请登录您的账号</p>
        </div>
        
        <div class="form-body">
          <div class="form-group">
            <label for="username" class="form-label">用户名</label>
            <div class="input-wrapper">
              <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
              <input 
                type="text" 
                id="username" 
                v-model="formData.username" 
                placeholder="请输入用户名"
                class="form-input"
                required
                :class="{ 'input-error': errorMessage && !formData.username }"
              />
            </div>
          </div>
          
          <div class="form-group">
            <label for="password" class="form-label">密码</label>
            <div class="input-wrapper">
              <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
              </svg>
              <input 
                type="password" 
                id="password" 
                v-model="formData.password" 
                placeholder="请输入密码"
                class="form-input"
                required
                :class="{ 'input-error': errorMessage && !formData.password }"
              />
            </div>
          </div>
          
          <div v-if="errorMessage" class="error-message">
            <svg class="error-icon" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="12" y1="8" x2="12" y2="12"></line>
              <line x1="12" y1="16" x2="12.01" y2="16"></line>
            </svg>
            {{ errorMessage }}
          </div>
          
          <button 
            class="login-button" 
            @click="handleLogin"
            :disabled="isLoading"
          >
            <span v-if="!isLoading" class="button-text">登录</span>
            <span v-else class="button-loading">
              <span class="loading-spinner"></span>
              登录中...
            </span>
          </button>
          
          <button 
            class="register-button" 
            @click="handleRegister"
            :disabled="isLoading"
          >
            <span class="button-text">注册账号</span>
          </button>
        </div>
        
        <div class="form-footer">
          <p>智能Agent © 2024</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const formData = ref({
  username: '',
  password: ''
})
const isLoading = ref(false)
const errorMessage = ref('')

// 添加登录页面特殊样式类
onMounted(() => {
  document.getElementById('app').classList.add('login-page')
  document.body.classList.add('login-body')
})

// 移除登录页面特殊样式类
onUnmounted(() => {
  document.getElementById('app').classList.remove('login-page')
  document.body.classList.remove('login-body')
})

const handleLogin = async () => {
  // 表单验证
  if (!formData.value.username || !formData.value.password) {
    errorMessage.value = '请填写所有必填字段'
    return
  }
  
  errorMessage.value = ''
  isLoading.value = true
  
  try {
    const response = await fetch('/api/v1/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        username: formData.value.username,
        password: formData.value.password
      })
    })
    
    const result = await response.json()
    
    if (result.code === 0) {
      // 登录成功，存储token和用户信息
      userStore.login(result.data.token, result.data.user)
      // 跳转到首页
      router.push('/')
    } else {
      // 登录失败
      errorMessage.value = result.message || '登录失败，请检查用户名和密码'
    }
  } catch (error) {
    console.error('登录请求失败:', error)
    errorMessage.value = '网络错误，请稍后重试'
  } finally {
    isLoading.value = false
  }
}

// 处理注册按钮点击，跳转到注册页面
const handleRegister = () => {
  router.push('/register')
}
</script>

<style>
/* 重置全局样式 - 确保登录页面完全不受外部样式影响 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
  width: 100%;
  overflow-x: hidden;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
}

#app {
  height: 100%;
  width: 100%;
  max-width: none !important;
  margin: 0 !important;
  padding: 0 !important;
  display: block !important;
  grid-template-columns: none !important;
}

body {
  display: block !important;
  place-items: normal !important;
}
</style>

<style scoped>
/* 登录容器 - 强制完全居中 */
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  width: 100vw;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 2rem;
  margin: 0;
  box-sizing: border-box;
  position: fixed;
  top: 0;
  left: 0;
  z-index: 10;
}

/* 登录包装器 */
.login-wrapper {
  width: 100%;
  max-width: 500px;
  perspective: 1000px;
}

/* 登录表单卡片 */
.login-form {
  background: rgba(255, 255, 255, 0.98);
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
  overflow: hidden;
  transform-style: preserve-3d;
  transform: translateY(0) rotateX(0);
}

.login-form:hover {
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.15);
  transform: translateY(-5px);
}

/* 表单头部 */
.form-header {
  text-align: center;
  padding: 2.5rem 2rem 1.5rem;
  background: linear-gradient(135deg, #42b983, #2c3e50);
  color: white;
}

.form-header h1 {
  font-size: 2rem;
  font-weight: 700;
  margin: 0 0 0.5rem;
  letter-spacing: -0.5px;
}

.subtitle {
  font-size: 1.1rem;
  opacity: 0.9;
  margin: 0;
  font-weight: 300;
}

/* 表单主体 */
.form-body {
  padding: 2.5rem 2rem;
}

/* 表单组 */
.form-group {
  margin-bottom: 1.5rem;
  position: relative;
}

.form-label {
  display: block;
  font-size: 0.95rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 0.75rem;
}

/* 输入框包装器 */
.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

/* 输入框图标 */
.input-icon {
  position: absolute;
  left: 1rem;
  color: #888;
  z-index: 1;
  transition: all 0.3s ease;
}

/* 表单输入框 */
.form-input {
  width: 100%;
  padding: 1.1rem 1rem 1.1rem 3rem;
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  font-size: 1rem;
  font-weight: 400;
  color: #333;
  background: #fafafa;
  transition: all 0.3s ease;
  outline: none;
}

.form-input:focus {
  border-color: #42b983;
  background: white;
  box-shadow: 0 0 0 3px rgba(66, 185, 131, 0.1);
}

.form-input:focus + .input-icon {
  color: #42b983;
  transform: scale(1.1);
}

.form-input::placeholder {
  color: #aaa;
}

/* 错误状态 */
.input-error {
  border-color: #e74c3c !important;
}

.input-error + .input-icon {
  color: #e74c3c !important;
}

/* 错误消息 */
.error-message {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background: #fdf2f2;
  border: 1px solid #fecaca;
  border-radius: 8px;
  color: #dc2626;
  font-size: 0.9rem;
  margin-bottom: 1.5rem;
  animation: shake 0.5s ease;
}

.error-icon {
  flex-shrink: 0;
}

/* 登录按钮 */
.login-button {
  width: 100%;
  padding: 1.1rem;
  background: linear-gradient(135deg, #42b983, #3aa876);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.5rem;
}

.login-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(66, 185, 131, 0.3);
  background: linear-gradient(135deg, #3aa876, #2d8f65);
}

.login-button:disabled {
  background: #ccc;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* 注册按钮样式 */
.register-button {
  width: 100%;
  margin-top: 1rem;
  padding: 1.1rem;
  background: linear-gradient(135deg, #3498db, #2980b9);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.5rem;
}

.register-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(52, 152, 219, 0.3);
  background: linear-gradient(135deg, #2980b9, #1f6da1);
}

.register-button:disabled {
  background: #ccc;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* 按钮加载状态 */
.button-loading {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

/* 表单底部 */
.form-footer {
  text-align: center;
  padding: 1.5rem 2rem 2rem;
  background: #f8f9fa;
  border-top: 1px solid #e9ecef;
}

.form-footer p {
  margin: 0;
  color: #6c757d;
  font-size: 0.9rem;
}

/* 动画 */
@keyframes shake {
  0%, 100% { transform: translateX(0); }
  10%, 30%, 50%, 70%, 90% { transform: translateX(-5px); }
  20%, 40%, 60%, 80% { transform: translateX(5px); }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-container {
    padding: 1rem;
  }
  
  .login-wrapper {
    max-width: 100%;
  }
}

@media (max-width: 480px) {
  .form-header h1 {
    font-size: 1.5rem;
  }
  
  .form-input {
    padding: 1rem 0.75rem 1rem 2.5rem;
  }
}
</style>