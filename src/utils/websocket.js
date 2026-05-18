import { ref, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/user'

const websocket = ref(null)
const isConnected = ref(false)
const reconnectAttempts = ref(0)
const maxReconnectAttempts = 5

const connect = () => {
  if (isConnected.value) return
  
  const userStore = useUserStore()
  if (!userStore.token) return

  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const wsUrl = `${protocol}//${window.location.host}/ws/notifications?token=${userStore.token}`
  
  websocket.value = new WebSocket(wsUrl)

  websocket.value.onopen = () => {
    console.log('WebSocket connected')
    isConnected.value = true
    reconnectAttempts.value = 0
  }

  websocket.value.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      handleMessage(data)
    } catch (error) {
      console.error('Failed to parse WebSocket message:', error)
    }
  }

  websocket.value.onerror = (error) => {
    console.error('WebSocket error:', error)
  }

  websocket.value.onclose = (event) => {
    console.log('WebSocket closed:', event.code, event.reason)
    isConnected.value = false
    
    if (reconnectAttempts.value < maxReconnectAttempts) {
      reconnectAttempts.value++
      setTimeout(() => {
        console.log(`Reconnecting attempt ${reconnectAttempts.value}...`)
        connect()
      }, reconnectAttempts.value * 2000)
    } else {
      console.log('Max reconnection attempts reached')
    }
  }
}

const handleMessage = (data) => {
  const userStore = useUserStore()
  
  switch (data.type) {
    case 'task_accepted':
      userStore.setUnreadCount(userStore.unreadCount + 1)
      break
    case 'task_submitted':
      userStore.setUnreadCount(userStore.unreadCount + 1)
      break
    case 'task_completed':
      userStore.setUnreadCount(userStore.unreadCount + 1)
      break
    case 'dispute_created':
      userStore.setUnreadCount(userStore.unreadCount + 1)
      break
    case 'system_notification':
      userStore.setUnreadCount(userStore.unreadCount + 1)
      break
    default:
      console.log('Unknown message type:', data.type)
  }
}

const send = (message) => {
  if (websocket.value && isConnected.value) {
    websocket.value.send(JSON.stringify(message))
  }
}

const disconnect = () => {
  if (websocket.value) {
    websocket.value.close()
    websocket.value = null
    isConnected.value = false
  }
}

export function useWebSocket() {
  onMounted(() => {
    connect()
  })

  onUnmounted(() => {
    disconnect()
  })

  return {
    isConnected,
    connect,
    disconnect,
    send
  }
}

export { connect as connectWebSocket, disconnect as disconnectWebSocket }
