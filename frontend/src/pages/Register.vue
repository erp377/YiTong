<script setup lang="ts">
import { ref } from 'vue'
import { http } from '../api/http'
import { useRouter } from 'vue-router'

const username = ref('')
const displayName = ref('')
const password = ref('')
const error = ref('')
const router = useRouter()

async function submit() {
  error.value = ''
  try {
    await http.post('/api/auth/register', {
      username: username.value,
      password: password.value,
      displayName: displayName.value
    })
    router.push('/login')
  } catch (e: any) {
    error.value = e?.response?.data?.message || '注册失败'
  }
}
</script>

<template>
  <div class="mx-auto max-w-md rounded-2xl border border-orange-100 bg-white/80 p-6 shadow-sm">
    <div class="text-lg font-semibold text-slate-900">注册</div>
    <div class="mt-4 space-y-3">
      <input
        v-model="username"
        class="w-full rounded-xl border border-orange-200 bg-white px-3 py-2 text-sm outline-none focus:border-orange-400 focus:ring-1 focus:ring-orange-300"
        placeholder="用户名（3-32）"
      />
      <input
        v-model="displayName"
        class="w-full rounded-xl border border-orange-200 bg-white px-3 py-2 text-sm outline-none focus:border-orange-400 focus:ring-1 focus:ring-orange-300"
        placeholder="昵称"
      />
      <input
        v-model="password"
        type="password"
        class="w-full rounded-xl border border-orange-200 bg-white px-3 py-2 text-sm outline-none focus:border-orange-400 focus:ring-1 focus:ring-orange-300"
        placeholder="密码（≥6）"
      />
      <button
        class="w-full rounded-xl bg-gradient-to-r from-pink-400 to-orange-400 px-4 py-2 text-sm font-medium text-white shadow-sm hover:opacity-90"
        @click="submit"
      >
        注册
      </button>
      <div v-if="error" class="text-sm text-rose-600">{{ error }}</div>
    </div>
  </div>
</template>

