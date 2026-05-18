import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import './style.css'
import { useUserStore } from './stores/user'

if (import.meta.env.VITE_USE_MOCK === 'true') {
  await import('./mock')
}

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

const userStore = useUserStore()
userStore.loadFromStorage()

app.mount('#app')
