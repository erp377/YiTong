<script setup lang="ts">
import { ref } from 'vue'
import { http } from '../api/http'
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'

const username = ref('')
const password = ref('')
const error = ref('')
const auth = useAuthStore()
const router = useRouter()

async function submit() {
  error.value = ''
  try {
    const res = await http.post('/api/auth/login', { username: username.value, password: password.value })
    auth.setAuth(res.data.token, res.data.user)
    router.push('/')
  } catch (e: any) {
    error.value = e?.response?.data?.message || '登录失败'
  }
}
</script>

<template>
  <div class="login-card mx-auto max-w-md rounded-2xl border border-orange-100 bg-white/80 p-6 shadow-sm">
    <div class="login-icon mb-3 inline-flex h-12 w-12 items-center justify-center rounded-full bg-gradient-to-r from-pink-400 to-orange-400 text-xl text-white shadow">
      易
    </div>
    <div class="text-lg font-semibold text-slate-900">登录</div>
    <div class="mt-4 space-y-3">
      <input
        v-model="username"
        class="w-full rounded-xl border border-orange-200 bg-white px-3 py-2 text-sm outline-none focus:border-orange-400 focus:ring-1 focus:ring-orange-300"
        placeholder="用户名"
      />
      <input
        v-model="password"
        type="password"
        class="w-full rounded-xl border border-orange-200 bg-white px-3 py-2 text-sm outline-none focus:border-orange-400 focus:ring-1 focus:ring-orange-300"
        placeholder="密码"
      />
      <button
        class="w-full rounded-xl bg-gradient-to-r from-pink-400 to-orange-400 px-4 py-2 text-sm font-medium text-white shadow-sm hover:opacity-90"
        @click="submit"
      >
        登录
      </button>
      <div v-if="error" class="text-sm text-rose-600">{{ error }}</div>
    </div>
  </div>
</template>

<style scoped>
.login-card {
  animation: cardIn 0.5s ease-out;
}
.login-icon {
  animation: iconFloat 3s ease-in-out infinite;
}
@keyframes cardIn {
  from {
    opacity: 0;
    transform: translateY(-12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
@keyframes iconFloat {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-6px);
  }
}
</style>

