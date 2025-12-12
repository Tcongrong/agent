<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useUserStore } from '../stores/user'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()
const sessions = ref([])
const loading = ref(false)
const error = ref(null)
const showCreateForm = ref(false)
const sessionTitle = ref('')
const creatingSession = ref(false)
// 编辑会话相关状态
const editingSessionId = ref(null)
const editTitle = ref('')
const deletingSessionId = ref(null)

// 聊天相关状态
const selectedSessionId = ref(null)
const messages = ref([])
const messagesLoading = ref(false)
const messagesError = ref(null)
const newMessage = ref('')
const sendingMessage = ref(false)

// 从localStorage获取存储的消息
const getStoredMessages = (sessionId) => {
  try {
    const storedMessages = localStorage.getItem(`chat_messages_${sessionId}`)
    return storedMessages ? JSON.parse(storedMessages) : []
  } catch (err) {
    console.error('读取存储的消息失败:', err)
    return []
  }
}

// 保存消息到localStorage
const storeMessages = (sessionId, messagesArray) => {
  try {
    localStorage.setItem(`chat_messages_${sessionId}`, JSON.stringify(messagesArray))
  } catch (err) {
    console.error('保存消息失败:', err)
  }
}

// 获取会话列表
const fetchSessions = async () => {
  if (!userStore.token) {
    router.push('/login')
    return
  }
  
  loading.value = true
  error.value = null
  
  try {
    const response = await fetch('/api/v1/chat/sessions', {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${userStore.token}`,
        'Content-Type': 'application/json'
      }
    })
    
    const data = await response.json()
    
    if (data.code === 0) {
      sessions.value = data.data || []
    } else {
      error.value = data.message || '获取会话列表失败'
    }
  } catch (err) {
    error.value = '网络错误，请稍后重试'
    console.error('获取会话列表错误:', err)
  } finally {
    loading.value = false
  }
}

// 处理会话点击
const handleSessionClick = async (sessionId) => {
  selectedSessionId.value = sessionId
  await fetchMessages(sessionId)
}

// 获取会话消息
const fetchMessages = async (sessionId) => {
  if (!userStore.token) {
    router.push('/login')
    return
  }
  
  messagesLoading.value = true
  messagesError.value = null
  
  try {
    // 先尝试从localStorage加载消息，提供即时反馈
    const storedMessages = getStoredMessages(sessionId)
    if (storedMessages.length > 0) {
      messages.value = storedMessages
      await nextTick()
      scrollToBottom()
    }
    
    const response = await fetch(`/api/chat/messages/session/${sessionId}`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${userStore.token}`
      }
    })
    
    const data = await response.json()
    
    // 假设后端直接返回消息数组，而不是包含code和data的对象
    // 如果后端返回格式不同，这里需要相应调整
    if (Array.isArray(data)) {
      messages.value = data
      storeMessages(sessionId, data) // 保存到localStorage
    } else if (data.code === 0) {
      messages.value = data.data || []
      storeMessages(sessionId, data.data || []) // 保存到localStorage
    } else {
      messagesError.value = data.message || '获取消息失败'
    }
    
    // 滚动到底部
    await nextTick()
    scrollToBottom()
  } catch (err) {
    messagesError.value = '网络错误，请稍后重试'
    console.error('获取消息错误:', err)
  } finally {
    messagesLoading.value = false
  }
}

// 发送新消息
const sendMessage = async () => {
  if (!newMessage.value.trim() || !selectedSessionId.value || sendingMessage.value) {
    return
  }
  
  sendingMessage.value = true
  const messageText = newMessage.value.trim()
  newMessage.value = ''
  
  try {
    // 添加用户消息到界面
    const tempMessage = {
      id: Date.now(),
      sessionId: selectedSessionId.value,
      role: 'user',
      content: messageText,
      createdAt: new Date().toISOString()
    }
    
    messages.value.push(tempMessage)
    // 更新localStorage
    storeMessages(selectedSessionId.value, messages.value)
    
    // 滚动到底部
    await nextTick()
    scrollToBottom()
    
    // 构建messages列表，只包含role和content字段
    const messagesForApi = messages.value.map(msg => ({
      role: msg.role,
      content: msg.content
    }))
    
    // 发送POST请求到AI接口
    const response = await fetch('/api/ai/test/multi-chat', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${userStore.token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        sessionId: selectedSessionId.value,
        messages: messagesForApi,
        systemMessage: '你是一个专业的AI助手'
      })
    })
    
    const data = await response.json()
    
    // 检查响应是否成功
    if (data.code === 0 && data.data?.response) {
      // 请求成功后，重新获取会话的消息列表
      await fetchMessages(selectedSessionId.value)
    } else {
      console.error('AI接口返回错误:', data.message || '未知错误')
    }
    
  } catch (err) {
    console.error('发送消息错误:', err)
    // 显示错误提示
    alert('发送消息失败，请稍后重试')
  } finally {
    sendingMessage.value = false
  }
}

// 滚动到底部
const scrollToBottom = () => {
  const chatContainer = document.querySelector('.chat-messages-container')
  if (chatContainer) {
    chatContainer.scrollTop = chatContainer.scrollHeight
  }
}

// 开始编辑会话标题
const startEditSession = (sessionId, currentTitle) => {
  editingSessionId.value = sessionId
  editTitle.value = currentTitle
  // 在下一个渲染周期聚焦输入框
  setTimeout(() => {
    const input = document.querySelector('.edit-title-input')
    if (input) {
      input.focus()
      input.select()
    }
  }, 0)
}

// 取消编辑会话标题
const cancelEditSession = () => {
  editingSessionId.value = null
  editTitle.value = ''
}

// 更新会话标题
const updateSessionTitle = async (sessionId) => {
  if (!editTitle.value.trim() || editTitle.value.length > 100) {
    return
  }
  
  try {
    const response = await fetch(`/api/v1/chat/sessions/${sessionId}`, {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${userStore.token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ title: editTitle.value.trim() })
    })
    
    const data = await response.json()
    
    if (data.code === 0) {
      // 更新成功后重新获取会话列表
      await fetchSessions()
      editingSessionId.value = null
      editTitle.value = ''
    } else {
      error.value = data.message || '更新会话标题失败'
    }
  } catch (err) {
    error.value = '网络错误，请稍后重试'
    console.error('更新会话标题错误:', err)
  }
}

// 确认删除会话
const confirmDeleteSession = (sessionId) => {
  if (confirm('确定要删除这个会话吗？此操作不可撤销。')) {
    deleteSession(sessionId)
  }
}

// 删除会话
const deleteSession = async (sessionId) => {
  deletingSessionId.value = sessionId
  
  try {
    const response = await fetch(`/api/v1/chat/sessions/${sessionId}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${userStore.token}`
      }
    })
    
    const data = await response.json()
    
    if (data.code === 0) {
      // 删除成功后重新获取会话列表
      await fetchSessions()
    } else {
      error.value = data.message || '删除会话失败'
    }
  } catch (err) {
    error.value = '网络错误，请稍后重试'
    console.error('删除会话错误:', err)
  } finally {
    deletingSessionId.value = null
  }
}

// 显示创建会话表单
const createNewSession = () => {
  showCreateForm.value = true
  sessionTitle.value = ''
}

// 隐藏创建会话表单
const cancelCreateSession = () => {
  showCreateForm.value = false
  sessionTitle.value = ''
}

// 提交创建会话请求
const submitCreateSession = async () => {
  if (!sessionTitle.value.trim() || sessionTitle.value.length > 100 || creatingSession.value) {
    return
  }
  
  creatingSession.value = true
  
  try {
    const response = await fetch('/api/v1/chat/sessions', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${userStore.token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ title: sessionTitle.value.trim() })
    })
    
    const data = await response.json()
    
    if (data.code === 0) {
      // 创建成功后重新获取会话列表
      await fetchSessions()
      showCreateForm.value = false
      sessionTitle.value = ''
    } else {
      error.value = data.message || '创建会话失败'
    }
  } catch (err) {
    error.value = '网络错误，请稍后重试'
    console.error('创建会话错误:', err)
  } finally {
    creatingSession.value = false
  }
}

// 格式化时间
const formatTime = (dateString) => {
  const date = new Date(dateString)
  const now = new Date()
  const diffTime = Math.abs(now - date)
  const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24))
  
  if (diffDays === 0) {
    // 今天的消息只显示时间
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } else if (diffDays === 1) {
    // 昨天的消息
    return `昨天 ${date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`
  } else if (diffDays < 7) {
    // 一周内的消息显示星期
    const weekdays = ['日', '一', '二', '三', '四', '五', '六']
    return `星期${weekdays[date.getDay()]} ${date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`
  } else {
    // 更早的消息显示完整日期
    return date.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
  }
}

// 组件挂载时获取会话列表
onMounted(() => {
  fetchSessions()
})
</script>

<template>
  <div class="home-container">
    <!-- 左侧会话列表 -->
    <div class="sidebar">
      <div class="sidebar-header">
        <h2>会话</h2>
      </div>
      
      <div class="sessions-list">
        <div v-if="loading" class="loading">
          加载中...
        </div>
        
        <div v-else-if="error" class="error-message">
          {{ error }}
        </div>
        
        <div
          v-for="session in sessions"
          :key="session.id"
          class="session-item"
        >
          <div class="session-content">
            <span class="session-title" @click="handleSessionClick(session.id)">
              {{ session.title }}
            </span>
            
            <!-- 会话操作按钮 -->
            <div class="session-actions">
              <!-- 更新标题按钮 -->
              <button
                class="action-btn edit-btn"
                title="更新标题"
                @click.stop="startEditSession(session.id, session.title)"
                :data-session-id="session.id"
                :id="`edit-btn-${session.id}`"
              >
                ✏️
              </button>
              
              <!-- 删除按钮 -->
              <button
                class="action-btn delete-btn"
                title="删除会话"
                @click.stop="confirmDeleteSession(session.id)"
                :data-session-id="session.id"
                :id="`delete-btn-${session.id}`"
              >
                🗑️
              </button>
            </div>
          </div>
          
          <!-- 编辑标题表单 -->
          <div v-if="editingSessionId === session.id" class="edit-form">
            <input
              v-model="editTitle"
              type="text"
              maxlength="100"
              class="edit-title-input"
              @keyup.enter="updateSessionTitle(session.id)"
              @keyup.esc="cancelEditSession"
              ref="focusInput"
            />
            <div class="edit-form-actions">
              <button 
                class="edit-action-btn save-btn" 
                @click="updateSessionTitle(session.id)"
                :data-session-id="session.id"
                :id="`save-btn-${session.id}`"
              >
                ✓
              </button>
              <button 
                class="edit-action-btn cancel-btn" 
                @click="cancelEditSession"
                :data-session-id="session.id"
                :id="`cancel-edit-btn-${session.id}`"
              >
                ✕
              </button>
            </div>
          </div>
        </div>
        
        <div v-if="!loading && !error && sessions.length === 0" class="empty-sessions">
          暂无会话
        </div>
      </div>
    </div>
    
    <!-- 右侧主内容区 -->
    <div class="content-area">
      <!-- 欢迎界面 -->
      <div v-if="!selectedSessionId" class="welcome-section">
        <h1>欢迎回来，{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}！</h1>
        <p class="subtitle">请点击历史会话，或创建新会话</p>
        <button class="create-session-btn" @click="createNewSession" :disabled="showCreateForm">
          创建新会话
        </button>
        
        <!-- 创建会话表单 -->
        <div v-if="showCreateForm" class="create-session-form">
          <div class="form-group">
            <label for="session-title">会话标题</label>
            <input
              id="session-title"
              v-model="sessionTitle"
              type="text"
              placeholder="请输入会话标题"
              maxlength="100"
              class="session-title-input"
              @keyup.enter="submitCreateSession"
            />
            <div class="char-count">{{ sessionTitle.length }}/100</div>
          </div>
          <div class="form-actions">
            <button
              class="create-session-button confirm"
              @click="submitCreateSession"
              :disabled="!sessionTitle.trim() || sessionTitle.length > 100 || creatingSession"
            >
              {{ creatingSession ? '创建中...' : '确定' }}
            </button>
            <button class="create-session-button cancel" @click="cancelCreateSession" :disabled="creatingSession">
              取消
            </button>
          </div>
        </div>
      </div>
      
      <!-- 聊天界面 -->
      <div v-else class="chat-container">
        <!-- 聊天头部 -->
        <div class="chat-header">
          <h2>{{ sessions.find(s => s.id === selectedSessionId)?.title || '会话' }}</h2>
        </div>
        
        <!-- 聊天消息区域 -->
        <div class="chat-messages-container">
          <div v-if="messagesLoading" class="messages-loading">
            加载中...
          </div>
          
          <div v-else-if="messagesError" class="messages-error">
            {{ messagesError }}
          </div>
          
          <div v-else-if="messages.length === 0" class="empty-messages">
            暂无消息，开始聊天吧！
          </div>
          
          <div v-else class="messages-list">
            <div
              v-for="message in messages"
              :key="message.id"
              :class="['message-item', message.role]"
            >
              <div class="message-content">
                <p>{{ message.content }}</p>
                <span class="message-time">{{ formatTime(message.createdAt) }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 聊天输入区域 -->
        <div class="chat-input-container">
          <textarea
            v-model="newMessage"
            class="chat-input"
            placeholder="输入消息..."
            rows="3"
            @keyup.enter.ctrl="sendMessage"
            :disabled="sendingMessage"
          ></textarea>
          <button
            class="send-button"
            @click="sendMessage"
            :disabled="!newMessage.trim() || sendingMessage"
            :data-session-id="selectedSessionId"
            :id="`send-btn-${selectedSessionId}`"
          >
            {{ sendingMessage ? '发送中...' : '发送' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  // 可以在这里添加组件选项
}
</script>

<style scoped>
.home-container {
  display: flex;
  width: 100%;
  min-height: 800px; /* 增加最小高度 */
}

/* 左侧会话列表 */
.sidebar {
  width: 300px;
  background-color: #f8f9fa;
  border-right: 1px solid #e9ecef;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.sidebar-header {
  padding: 1.5rem 1rem;
  border-bottom: 1px solid #e9ecef;
}

.sidebar-header h2 {
  margin: 0;
  font-size: 1.4rem;
  color: #333;
}

.sessions-list {
  flex: 1;
  overflow-y: auto;
  padding: 0.5rem;
}

.session-item {
  width: 100%;
  margin-bottom: 0.5rem;
  background-color: white;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  text-align: left;
  transition: all 0.2s ease;
  overflow: hidden;
}

.session-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem;
}

.session-title {
  flex: 1;
  font-size: 1rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  cursor: pointer;
  padding-right: 0.5rem;
}

.session-actions {
  display: flex;
  gap: 0.3rem;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.session-item:hover .session-actions {
  opacity: 1;
}

.action-btn {
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  font-size: 0.9rem;
  background-color: transparent;
}

.edit-btn:hover {
  background-color: #e3f2fd;
  color: #2196f3;
}

.delete-btn:hover {
  background-color: #ffebee;
  color: #f44336;
}

/* 编辑表单样式 */
.edit-form {
  padding: 0.5rem 1rem 1rem;
  border-top: 1px solid #f0f0f0;
  background-color: #fafafa;
}

.edit-title-input {
  width: 100%;
  padding: 0.6rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 0.9rem;
  margin-bottom: 0.5rem;
}

.edit-title-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2);
}

.edit-form-actions {
  display: flex;
  gap: 0.5rem;
  justify-content: flex-end;
}

/* 完全重置并统一的按钮样式 */
.save-btn, .cancel-btn {
  /* 完全相同的尺寸定义 */
  width: 32px !important;
  height: 32px !important;
  min-width: 32px !important;
  min-height: 32px !important;
  max-width: 32px !important;
  max-height: 32px !important;
  
  /* 完全重置所有可能影响尺寸的属性 */
  border: none !important;
  border-radius: 4px !important;
  padding: 0 !important;
  margin: 0 !important;
  
  /* 确保内容居中 */
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  
  /* 统一文字和其他样式 */
  font-size: 1rem !important;
  line-height: 1 !important;
  font-weight: bold !important;
  cursor: pointer !important;
  transition: all 0.2s ease !important;
  box-sizing: border-box !important;
  
  /* 确保文本显示一致 */
  text-align: center !important;
  white-space: nowrap !important;
  overflow: hidden !important;
  
  /* 确保没有继承样式干扰 */
  position: relative !important;
  float: none !important;
}

/* 单独的背景色和颜色 */
.save-btn {
  background-color: #e8f5e9 !important;
  color: #4caf50 !important;
}

.save-btn:hover {
  background-color: #c8e6c9 !important;
}

.cancel-btn {
  background-color: #ffebee !important;
  color: #f44336 !important;
}

.cancel-btn:hover {
  background-color: #ffcdd2 !important;
}

/* 右侧主内容区 */
.content-area {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fafafa;
  min-height: 800px;
  overflow: hidden;
}

.welcome-section {
  width: 100%;
  max-width: 600px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  text-align: center;
}

.welcome-section {
  text-align: center;
  padding: 2rem;
  max-width: 600px;
}

.welcome-section h1 {
  font-size: 2rem;
  color: #333;
  margin-bottom: 1rem;
}

.subtitle {
  font-size: 1.1rem;
  color: #666;
  margin-bottom: 2rem;
}

.create-session-btn {
  padding: 0.8rem 2rem;
  font-size: 1rem;
  font-weight: 500;
  color: white;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 14px rgba(102, 126, 234, 0.3);
}

.create-session-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.create-session-btn:active:not(:disabled) {
  transform: translateY(0);
}

.create-session-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 创建会话表单样式 */
.create-session-form {
  background-color: white;
  border-radius: 8px;
  padding: 1.5rem;
  margin-top: 1rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
  margin-left: auto;
  margin-right: auto;
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #333;
}

.session-title-input {
  width: 100%;
  padding: 0.8rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 1rem;
  transition: border-color 0.2s ease;
}

.session-title-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2);
}

.char-count {
  text-align: right;
  font-size: 0.85rem;
  color: #666;
  margin-top: 0.25rem;
}

.form-actions {
  display: flex;
  gap: 0.8rem;
  justify-content: flex-end;
  margin-top: 1.5rem;
}

/* 创建会话表单按钮样式 - 使用全新类名避免冲突 */
.create-session-button {
  /* 完全相同的尺寸和布局属性 - 两个按钮共享 */
  height: 44px !important;
  min-width: 100px !important;
  padding: 0.7rem 1.5rem !important;
  border-radius: 6px !important;
  font-size: 1rem !important;
  font-weight: 500 !important;
  cursor: pointer !important;
  transition: all 0.2s ease !important;
  text-align: center !important;
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
  box-sizing: border-box !important;
  white-space: nowrap !important;
  overflow: visible !important;
  margin: 0 !important;
  position: relative !important;
  float: none !important;
  border: none !important;
  background: none !important;
  font-family: inherit !important;
}

/* 确定按钮特定样式 */
.create-session-button.confirm {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  color: white !important;
  border: none !important;
}

.create-session-button.confirm:hover:not(:disabled) {
  transform: translateY(-1px) !important;
  box-shadow: 0 4px 10px rgba(102, 126, 234, 0.3) !important;
}

.create-session-button.confirm:disabled {
  opacity: 0.6 !important;
  cursor: not-allowed !important;
}

/* 取消按钮特定样式 */
.create-session-button.cancel {
  background-color: #f5f5f5 !important;
  color: #333 !important;
  border: 1px solid #ddd !important;
}

.create-session-button.cancel:hover:not(:disabled) {
  background-color: #e0e0e0 !important;
  border-color: #bbb !important;
}

.create-session-button.cancel:disabled {
  opacity: 0.6 !important;
  cursor: not-allowed !important;
}

/* 加载和错误状态 */
.loading {
  text-align: center;
  padding: 2rem;
  color: #666;
}

.error-message {
  text-align: center;
  padding: 2rem;
  color: #e74c3c;
  background-color: #fadbd8;
  border-radius: 6px;
  margin: 1rem;
}

.empty-sessions {
  text-align: center;
  padding: 2rem;
  color: #95a5a6;
  font-style: italic;
}

/* 聊天界面样式 */
.chat-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: white;
}

.chat-header {
  padding: 1rem 2rem;
  border-bottom: 1px solid #e9ecef;
  background-color: #f8f9fa;
}

.chat-header h2 {
  margin: 0;
  font-size: 1.4rem;
  color: #333;
}

.chat-messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 2rem;
  background-color: #f5f7fa;
  display: flex;
  flex-direction: column;
}

.messages-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.message-item {
  display: flex;
  margin-bottom: 1rem;
}

/* 用户消息在右侧 */
.message-item.user {
  justify-content: flex-end;
}

/* 助手消息在左侧 */
.message-item.assistant {
  justify-content: flex-start;
}

.message-content {
  max-width: 70%;
  padding: 1rem 1.25rem;
  border-radius: 12px;
  word-wrap: break-word;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

/* 用户消息样式 */
.message-item.user .message-content {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-bottom-right-radius: 4px;
}

/* 助手消息样式 */
.message-item.assistant .message-content {
  background-color: white;
  color: #333;
  border: 1px solid #e9ecef;
  border-bottom-left-radius: 4px;
}

.message-content p {
  margin: 0 0 0.5rem 0;
  line-height: 1.5;
}

.message-time {
  font-size: 0.75rem;
  opacity: 0.7;
  display: block;
  text-align: right;
}

/* 用户消息的时间样式 */
.message-item.user .message-time {
  color: rgba(255, 255, 255, 0.8);
}

/* 助手消息的时间样式 */
.message-item.assistant .message-time {
  color: #999;
}

.messages-loading,
.messages-error,
.empty-messages {
  padding: 2rem;
  text-align: center;
  color: #666;
}

.messages-error {
  color: #e74c3c;
  background-color: #fadbd8;
  border-radius: 6px;
}

/* 聊天输入区域样式 */
.chat-input-container {
  padding: 1.5rem 2rem;
  background-color: white;
  border-top: 1px solid #e9ecef;
  display: flex;
  gap: 1rem;
  align-items: flex-end;
}

.chat-input {
  flex: 1;
  padding: 0.8rem 1rem;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 1rem;
  line-height: 1.5;
  resize: none;
  min-height: 80px;
  max-height: 200px;
  transition: border-color 0.2s ease;
}

.chat-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2);
}

.send-button {
  padding: 0.8rem 2rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  height: fit-content;
  min-height: 44px;
}

.send-button:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 10px rgba(102, 126, 234, 0.3);
}

.send-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .home-container {
    flex-direction: column;
    min-height: 600px;
  }
  
  .sidebar {
    width: 100%;
    height: 250px;
    border-right: none;
    border-bottom: 1px solid #e9ecef;
  }
  
  .content-area {
    min-height: 350px;
  }
  
  .chat-messages-container {
    padding: 1rem;
  }
  
  .message-content {
    max-width: 85%;
  }
  
  .chat-input-container {
    padding: 1rem;
  }
  
  .send-button {
    padding: 0.6rem 1rem;
  }
}
</style>
