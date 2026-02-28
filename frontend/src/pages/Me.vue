<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { http } from '../api/http'
import type { User } from '../types'
import type { CheckInRecord } from '../types'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const me = ref<User | null>(null)
const favorites = ref<{ id: number; title: string; createdAt: string }[]>([])
const myGuides = ref<{ id: number; title: string; createdAt: string }[]>([])
const checkinRecords = ref<CheckInRecord[]>([])
const error = ref('')
const newUsername = ref('')
const usernameError = ref('')
const usernameSubmitting = ref(false)
const oldPassword = ref('')
const newPassword = ref('')
const passwordError = ref('')
const passwordSubmitting = ref(false)

async function load() {
  if (!auth.token) return
  error.value = ''
  try {
    me.value = (await http.get('/api/me')).data
    favorites.value = (await http.get('/api/me/favorites')).data
    myGuides.value = (await http.get('/api/me/guides')).data
    checkinRecords.value = (await http.get('/api/me/checkins')).data
  } catch (e: any) {
    error.value = e?.response?.data?.message || '加载失败'
  }
}

async function deleteGuide(g: { id: number; title: string }) {
  if (!confirm(`确定要删除《${g.title}》吗？删除后不可恢复。`)) return
  try {
    await http.delete(`/api/guides/${g.id}`)
    await load()
  } catch (e: any) {
    alert(e?.response?.data?.message || '删除失败')
  }
}

async function submitUsername() {
  const name = newUsername.value.trim()
  if (!name || name.length < 3 || name.length > 32) {
    usernameError.value = '用户名 3-32 字符'
    return
  }
  usernameError.value = ''
  usernameSubmitting.value = true
  try {
    await http.patch('/api/me/username', { username: name })
    await load()
    if (me.value && auth.user) {
      const u = { ...auth.user, username: me.value.username }
      auth.$patch({ user: u })
      localStorage.setItem('yitong_user', JSON.stringify(u))
    }
    newUsername.value = ''
  } catch (e: any) {
    usernameError.value = e?.response?.data?.message || '修改失败'
  } finally {
    usernameSubmitting.value = false
  }
}

async function submitPassword() {
  if (!oldPassword.value || !newPassword.value) {
    passwordError.value = '请填写原密码和新密码'
    return
  }
  if (newPassword.value.length < 6) {
    passwordError.value = '新密码至少 6 位'
    return
  }
  passwordError.value = ''
  passwordSubmitting.value = true
  try {
    await http.patch('/api/me/password', { oldPassword: oldPassword.value, newPassword: newPassword.value })
    oldPassword.value = ''
    newPassword.value = ''
  } catch (e: any) {
    passwordError.value = e?.response?.data?.message || '修改失败'
  } finally {
    passwordSubmitting.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="space-y-4">
    <div v-if="!auth.token" class="rounded-2xl border border-orange-100 bg-white/80 p-4 text-sm text-slate-600 shadow-sm">请先登录后查看。</div>
    <div v-else class="space-y-4">
      <div v-if="error" class="rounded-2xl border border-orange-100 bg-white/80 p-4 text-sm text-rose-600 shadow-sm">{{ error }}</div>

      <div class="rounded-2xl border border-orange-100 bg-white/80 p-4 shadow-sm">
        <div class="text-base font-semibold">我的信息</div>
        <div class="mt-2 text-sm text-slate-700">
          <div v-if="me">昵称：{{ me.displayName }}（用户名：{{ me.username }}）</div>
        </div>
        <div class="mt-3 space-y-3">
          <div class="flex flex-wrap items-center gap-2">
            <label class="text-sm">修改用户名</label>
            <input v-model="newUsername" class="rounded-lg border border-orange-200 px-3 py-1.5 text-sm" placeholder="3-32 字符" />
            <button type="button" class="rounded-lg bg-orange-500 px-3 py-1.5 text-sm text-white hover:bg-orange-600" :disabled="usernameSubmitting" @click="submitUsername">
              {{ usernameSubmitting ? '提交中…' : '保存' }}
            </button>
            <span v-if="usernameError" class="text-xs text-rose-600">{{ usernameError }}</span>
          </div>
          <div class="flex flex-wrap items-center gap-2">
            <label class="text-sm">修改密码</label>
            <input v-model="oldPassword" type="password" class="rounded-lg border border-orange-200 px-3 py-1.5 text-sm" placeholder="原密码" />
            <input v-model="newPassword" type="password" class="rounded-lg border border-orange-200 px-3 py-1.5 text-sm" placeholder="新密码（至少6位）" />
            <button type="button" class="rounded-lg bg-orange-500 px-3 py-1.5 text-sm text-white hover:bg-orange-600" :disabled="passwordSubmitting" @click="submitPassword">
              {{ passwordSubmitting ? '提交中…' : '保存' }}
            </button>
            <span v-if="passwordError" class="text-xs text-rose-600">{{ passwordError }}</span>
          </div>
          <p class="text-xs text-slate-500">修改密码后 7 天内不可再次修改。</p>
        </div>
      </div>

      <div class="rounded-2xl border border-orange-100 bg-white/80 p-4 shadow-sm">
        <div class="text-base font-semibold">我发布的攻略</div>
        <div class="mt-2 space-y-2 text-sm">
          <div v-if="myGuides.length === 0" class="text-slate-500">暂无</div>
          <div
            v-for="g in myGuides"
            :key="g.id"
            class="flex items-center justify-between gap-2 rounded-xl border border-orange-100 bg-white p-3 hover:bg-orange-50/50"
          >
            <RouterLink :to="`/guides/${g.id}`" class="min-w-0 flex-1">
              <div class="font-medium">{{ g.title }}</div>
              <div class="text-xs text-slate-500">{{ new Date(g.createdAt).toLocaleString() }}</div>
            </RouterLink>
            <button
              type="button"
              class="shrink-0 rounded-lg border border-rose-200 px-2 py-1 text-xs text-rose-600 hover:bg-rose-50"
              @click="deleteGuide(g)"
            >
              删除
            </button>
          </div>
        </div>
      </div>

      <div class="rounded-2xl border border-orange-100 bg-white/80 p-4 shadow-sm">
        <div class="text-base font-semibold">我的收藏</div>
        <div class="mt-2 space-y-2 text-sm">
          <div v-if="favorites.length === 0" class="text-slate-500">暂无</div>
          <RouterLink v-for="g in favorites" :key="g.id" class="block rounded-xl border border-orange-100 bg-white p-3 hover:bg-orange-50" :to="`/guides/${g.id}`">
            <div class="font-medium">{{ g.title }}</div>
            <div class="text-xs text-slate-500">{{ new Date(g.createdAt).toLocaleString() }}</div>
          </RouterLink>
        </div>
      </div>

      <div class="rounded-2xl border border-orange-100 bg-white/80 p-4 shadow-sm">
        <div class="text-base font-semibold">我的打卡记录</div>
        <div class="mt-2 space-y-2 text-sm">
          <div v-if="checkinRecords.length === 0" class="text-slate-500">暂无打卡记录</div>
          <RouterLink
            v-for="c in checkinRecords"
            :key="c.id"
            class="block rounded-xl border border-orange-100 bg-white p-3 hover:bg-orange-50"
            :to="`/guides/${c.guideId}`"
          >
            <div class="font-medium">{{ c.guideTitle }}</div>
            <div class="text-xs text-slate-500">打卡日期：{{ c.checkinDate }} · {{ new Date(c.createdAt).toLocaleString() }}</div>
          </RouterLink>
        </div>
      </div>
    </div>
  </div>
</template>

