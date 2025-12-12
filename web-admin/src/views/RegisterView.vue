<template>
  <div class="register-container">
    <div class="register-wrapper">
      <div class="register-form">
        <div class="form-header">
          <h1>创建账号</h1>
          <p class="subtitle">欢迎加入智能Agent系统</p>
        </div>
        
        <div class="form-body">
          <!-- 错误消息显示 -->
          <div v-if="errorMessage" class="error-message">
            <svg class="error-icon" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="12" y1="8" x2="12" y2="12"></line>
              <line x1="12" y1="16" x2="12.01" y2="16"></line>
            </svg>
            {{ errorMessage }}
          </div>
          
          <!-- 用户名输入框 -->
          <div class="form-group">
            <label for="username" class="form-label">用户名 <span class="required">*</span></label>
            <div class="input-wrapper">
              <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
              <input 
                type="text" 
                id="username" 
                v-model="formData.username" 
                placeholder="请输入用户名（3-20个字符）"
                class="form-input"
                required
                :class="{ 'input-error': errors.username }"
                @input="validateField('username')"
              />
            </div>
            <span v-if="errors.username" class="field-error">{{ errors.username }}</span>
          </div>
          
          <!-- 密码输入框 -->
          <div class="form-group">
            <label for="password" class="form-label">密码 <span class="required">*</span></label>
            <div class="input-wrapper">
              <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
              </svg>
              <input 
                type="password" 
                id="password" 
                v-model="formData.password" 
                placeholder="请输入密码（6-20个字符）"
                class="form-input"
                required
                :class="{ 'input-error': errors.password }"
                @input="validateField('password')"
              />
            </div>
            <span v-if="errors.password" class="field-error">{{ errors.password }}</span>
          </div>
          
          <!-- 确认密码输入框 -->
          <div class="form-group">
            <label for="confirmPassword" class="form-label">确认密码 <span class="required">*</span></label>
            <div class="input-wrapper">
              <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="20 6 9 17 4 12"></polyline>
              </svg>
              <input 
                type="password" 
                id="confirmPassword" 
                v-model="formData.confirmPassword" 
                placeholder="请再次输入密码"
                class="form-input"
                required
                :class="{ 'input-error': errors.confirmPassword }"
                @input="validateField('confirmPassword')"
              />
            </div>
            <span v-if="errors.confirmPassword" class="field-error">{{ errors.confirmPassword }}</span>
          </div>
          
          <!-- 邮箱输入框 -->
          <div class="form-group">
            <label for="email" class="form-label">邮箱 <span class="required">*</span></label>
            <div class="input-wrapper">
              <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="2" y="4" width="20" height="16" rx="2"></rect>
                <path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7"></path>
              </svg>
              <input 
                type="email" 
                id="email" 
                v-model="formData.email" 
                placeholder="请输入邮箱地址"
                class="form-input"
                required
                :class="{ 'input-error': errors.email }"
                @input="validateField('email')"
              />
            </div>
            <span v-if="errors.email" class="field-error">{{ errors.email }}</span>
          </div>
          
          <!-- 电话号码输入框 -->
          <div class="form-group">
            <label for="phone" class="form-label">电话号码</label>
            <div class="input-wrapper">
              <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path>
              </svg>
              <input 
                type="tel" 
                id="phone" 
                v-model="formData.phone" 
                placeholder="请输入电话号码（选填）"
                class="form-input"
                :class="{ 'input-error': errors.phone }"
                @input="validateField('phone')"
              />
            </div>
            <span v-if="errors.phone" class="field-error">{{ errors.phone }}</span>
          </div>
          
          <!-- 昵称输入框 -->
          <div class="form-group">
            <label for="nickname" class="form-label">昵称</label>
            <div class="input-wrapper">
              <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
              <input 
                type="text" 
                id="nickname" 
                v-model="formData.nickname" 
                placeholder="请输入昵称（选填）"
                class="form-input"
                :class="{ 'input-error': errors.nickname }"
                @input="validateField('nickname')"
              />
            </div>
            <span v-if="errors.nickname" class="field-error">{{ errors.nickname }}</span>
          </div>
          
          <!-- 按钮区域 -->
          <div class="buttons-container">
            <!-- 注册按钮 -->
            <button 
              class="register-button" 
              @click="handleRegister"
              :disabled="isLoading || !isFormValid"
            >
              <span v-if="!isLoading" class="button-text">注册</span>
              <span v-else class="button-loading">
                <span class="loading-spinner"></span>
                注册中...
              </span>
            </button>
            
            <!-- 返回登录按钮 - 更明显的样式 -->
            <button class="back-button highlighted" @click="goBack">
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="m15 18-6-6 6-6"></path>
              </svg>
              返回登录
            </button>
          </div>
        </div>
        
        <div class="form-footer">
          <p>智能Agent © 2024</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

// 表单数据
const formData = ref({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: '',
  nickname: ''
})

// 错误信息
const errors = ref({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: '',
  nickname: ''
})

const isLoading = ref(false)
const errorMessage = ref('')

// 验证单个字段
const validateField = (fieldName) => {
  errors.value[fieldName] = ''
  
  switch (fieldName) {
    case 'username':
      if (!formData.value.username.trim()) {
        errors.value.username = '用户名不能为空'
      } else if (formData.value.username.length < 3 || formData.value.username.length > 20) {
        errors.value.username = '用户名长度必须在3-20个字符之间'
      }
      break
    case 'password':
      if (!formData.value.password) {
        errors.value.password = '密码不能为空'
      } else if (formData.value.password.length < 6 || formData.value.password.length > 20) {
        errors.value.password = '密码长度必须在6-20个字符之间'
      } else if (formData.value.confirmPassword && formData.value.password !== formData.value.confirmPassword) {
        errors.value.confirmPassword = '两次输入的密码不一致'
      } else {
        errors.value.confirmPassword = ''
      }
      break
    case 'confirmPassword':
      if (!formData.value.confirmPassword) {
        errors.value.confirmPassword = '请确认密码'
      } else if (formData.value.password !== formData.value.confirmPassword) {
        errors.value.confirmPassword = '两次输入的密码不一致'
      }
      break
    case 'email':
      if (!formData.value.email.trim()) {
        errors.value.email = '邮箱不能为空'
      } else {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
        if (!emailRegex.test(formData.value.email)) {
          errors.value.email = '请输入有效的邮箱地址'
        }
      }
      break
    case 'phone':
      // 电话号码是选填的，但如果填写了需要验证格式
      if (formData.value.phone && !/^1[3-9]\d{9}$/.test(formData.value.phone)) {
        errors.value.phone = '请输入有效的手机号码'
      }
      break
    case 'nickname':
      // 昵称是选填的，但如果填写了限制长度
      if (formData.value.nickname && formData.value.nickname.length > 20) {
        errors.value.nickname = '昵称长度不能超过20个字符'
      }
      break
  }
}

// 验证整个表单
const validateForm = () => {
  // 验证必填字段
  validateField('username')
  validateField('password')
  validateField('confirmPassword')
  validateField('email')
  // 验证选填字段（如果有值）
  validateField('phone')
  validateField('nickname')
  
  // 检查是否有错误
  return !Object.values(errors.value).some(error => error !== '')
}

// 计算属性：表单是否有效
const isFormValid = computed(() => {
  // 必填字段不能为空
  if (!formData.value.username.trim() || 
      !formData.value.password || 
      !formData.value.confirmPassword || 
      !formData.value.email.trim()) {
    return false
  }
  
  // 所有字段没有错误
  return !Object.values(errors.value).some(error => error !== '')
})

// 处理注册
const handleRegister = async () => {
  errorMessage.value = ''
  
  // 验证表单
  if (!validateForm()) {
    errorMessage.value = '请修正表单中的错误后再提交'
    return
  }
  
  isLoading.value = true
  
  try {
    // 发送注册请求到后端 - 使用正确的请求体格式
    const response = await fetch('/api/v1/auth/register', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        username: formData.value.username,
        email: formData.value.email,
        password: formData.value.password,
        confirmPassword: formData.value.confirmPassword,
        phone: formData.value.phone || '', // 空字符串而不是null
        nickname: formData.value.nickname || '' // 空字符串而不是默认用户名
      })
    })
    
    const result = await response.json()
    
    // 处理响应结果
    if (response.ok && result.code === 0) {
      // 注册成功
      alert('注册成功！请登录')
      // 跳转到登录页面
      router.push('/login')
    } else {
      // 注册失败 - 显示具体错误信息
      // 处理常见错误码和消息
      if (result.code === 40002) {
        // 用户名已存在的特定处理
        errorMessage.value = result.message || '用户名已被注册，请更换用户名'
        // 可以聚焦到用户名输入框
        document.getElementById('username')?.focus()
      } else if (result.code === 40003) {
        // 邮箱已存在的特定处理
        errorMessage.value = result.message || '邮箱已被注册，请更换邮箱'
        document.getElementById('email')?.focus()
      } else {
        // 其他错误
        errorMessage.value = result.message || '注册失败，请稍后重试'
      }
      
      console.error('注册失败:', result)
    }
  } catch (error) {
    console.error('注册请求失败:', error)
    errorMessage.value = '网络错误，请检查网络连接或稍后重试'
  } finally {
    isLoading.value = false
  }
}

// 返回登录页面
const goBack = () => {
  router.push('/login')
}

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
/* 注册容器 - 强制完全居中 */
.register-container {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  min-height: 100vh;
  width: 100vw;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 1rem;
  margin: 0;
  box-sizing: border-box;
  position: fixed;
  top: 0;
  left: 0;
  z-index: 10;
  overflow-y: auto;
}

/* 确保在小屏幕上可以滚动 */
.register-container {
  -webkit-overflow-scrolling: touch;
}

/* 注册包装器 */
.register-wrapper {
  width: 100%;
  max-width: 450px;
  perspective: 1000px;
  margin: 0.5rem 0;
}

/* 注册表单卡片 */
.register-form {
  background: rgba(255, 255, 255, 0.98);
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
  overflow: hidden;
  transform-style: preserve-3d;
  transform: translateY(0) rotateX(0);
}

.register-form:hover {
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.15);
  transform: translateY(-5px);
}

/* 表单头部 */
.form-header {
  text-align: center;
  padding: 1.5rem 1.5rem 1rem;
  background: linear-gradient(135deg, #3498db, #2980b9);
  color: white;
}

.form-header h1 {
  font-size: 1.75rem;
  font-weight: 700;
  margin: 0 0 0.5rem;
  letter-spacing: -0.5px;
}

.subtitle {
  font-size: 1rem;
  opacity: 0.9;
  margin: 0;
  font-weight: 300;
}

/* 表单主体 */
.form-body {
  padding: 1.5rem;
}

/* 必填标记 */
.required {
  color: #e74c3c;
}

/* 表单组 */
.form-group {
  margin-bottom: 1.2rem;
  position: relative;
}

.form-label {
  display: block;
  font-size: 0.9rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 0.5rem;
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
  padding: 0.9rem 0.9rem 0.9rem 2.75rem;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 0.95rem;
  font-weight: 400;
  color: #333;
  background: #fafafa;
  transition: all 0.3s ease;
  outline: none;
  box-sizing: border-box;
}

.form-input:focus {
  border-color: #3498db;
  background: white;
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.1);
}

.form-input:focus + .input-icon {
  color: #3498db;
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

/* 字段错误消息 */
.field-error {
  display: block;
  font-size: 0.85rem;
  color: #e74c3c;
  margin-top: 0.5rem;
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

/* 注册按钮 */
.register-button {
  width: 100%;
  padding: 1rem;
  background: linear-gradient(135deg, #3498db, #2980b9);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.5rem;
  margin-top: 0;
  box-sizing: border-box;
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

/* 按钮容器 */
.buttons-container {
  margin-top: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
}

/* 返回按钮 - 更明显的样式 */
.back-button {
  width: 100%;
  padding: 0.9rem;
  background: transparent;
  color: #6c757d;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  font-size: 0.95rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.5rem;
  margin-top: 0;
  box-sizing: border-box;
}

/* 高亮的返回按钮 */
.back-button.highlighted {
  background: #3498db;
  border-color: #3498db;
  color: white;
  font-weight: 600;
  border-width: 2px;
}

.back-button:hover {
  background: #f8f9fa;
  border-color: #3498db;
  color: #3498db;
}

.back-button.highlighted:hover {
  background: linear-gradient(135deg, #3498db, #2980b9);
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(52, 152, 219, 0.3);
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
  padding: 1rem 1.5rem 1.5rem;
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

/* 响应式设计 - 进一步优化小屏幕显示 */
@media (max-width: 768px) {
  .register-container {
    padding: 0.5rem;
  }
  
  .register-wrapper {
    max-width: 100%;
    margin: 0;
  }
  
  .form-body {
    padding: 1.25rem;
  }
  
  .form-header {
    padding: 1.25rem 1.25rem 0.75rem;
  }
  
  .form-header h1 {
    font-size: 1.6rem;
  }
}

@media (max-width: 480px) {
  .register-container {
    padding: 0.25rem;
  }
  
  .form-header h1 {
    font-size: 1.5rem;
  }
  
  .subtitle {
    font-size: 0.95rem;
  }
  
  .form-body {
    padding: 1rem;
  }
  
  .form-group {
    margin-bottom: 1rem;
  }
  
  .form-input {
    padding: 0.8rem 0.8rem 0.8rem 2.5rem;
    font-size: 0.9rem;
  }
  
  .register-button, .back-button {
    padding: 0.9rem;
    font-size: 0.95rem;
  }
  
  .buttons-container {
    margin-top: 1.25rem;
    gap: 0.75rem;
  }
}
</style>