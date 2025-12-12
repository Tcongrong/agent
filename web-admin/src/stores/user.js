import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('userToken') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
  const isAuthenticated = ref(!!token.value)

  // 登录方法
  function login(newToken, userData) {
    token.value = newToken
    userInfo.value = userData
    isAuthenticated.value = true
    
    // 存储到localStorage以便页面刷新后保持登录状态
    localStorage.setItem('userToken', newToken)
    localStorage.setItem('userInfo', JSON.stringify(userData))
  }

  // 登出方法
  function logout() {
    token.value = ''
    userInfo.value = null
    isAuthenticated.value = false
    
    // 清除localStorage中的数据
    localStorage.removeItem('userToken')
    localStorage.removeItem('userInfo')
  }

  return {
    token,
    userInfo,
    isAuthenticated,
    login,
    logout
  }
})