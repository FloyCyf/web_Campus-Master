import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const userId = ref(null)
  const username = ref('')
  const phone = ref('')
  const role = ref('')
  const creditScore = ref(100)
  const token = ref('')
  const balance = ref(0)
  const frozenBalance = ref(0)
  const unreadCount = ref(0)

  const isAuthenticated = computed(() => !!token.value)

  const login = (data) => {
    userId.value = data.userId
    username.value = data.username
    phone.value = data.phone
    role.value = data.role
    creditScore.value = data.creditScore
    token.value = data.token
    localStorage.setItem('userInfo', JSON.stringify({
      userId: data.userId,
      username: data.username,
      phone: data.phone,
      role: data.role,
      creditScore: data.creditScore,
      token: data.token
    }))
    localStorage.setItem('token', data.token)
  }

  const logout = () => {
    userId.value = null
    username.value = ''
    phone.value = ''
    role.value = ''
    creditScore.value = 100
    token.value = ''
    balance.value = 0
    frozenBalance.value = 0
    unreadCount.value = 0
    localStorage.removeItem('userInfo')
    localStorage.removeItem('token')
  }

  const setAccount = (data) => {
    balance.value = data.balance
    frozenBalance.value = data.frozenBalance
  }

  const setUnreadCount = (count) => {
    unreadCount.value = count
  }

  const loadFromStorage = () => {
    const userInfo = localStorage.getItem('userInfo')
    if (userInfo) {
      const data = JSON.parse(userInfo)
      userId.value = data.userId
      username.value = data.username
      phone.value = data.phone
      role.value = data.role
      creditScore.value = data.creditScore
      token.value = data.token
    }
  }

  return {
    userId,
    username,
    phone,
    role,
    creditScore,
    token,
    balance,
    frozenBalance,
    unreadCount,
    isAuthenticated,
    login,
    logout,
    setAccount,
    setUnreadCount,
    loadFromStorage
  }
})
