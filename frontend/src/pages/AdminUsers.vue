<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { http } from '../api/http'
import type { UserRole } from '../types'

type AdminUser = {
  id: number
  username: string
  displayName: string
  role: UserRole
  enabled: boolean
  status: number
  createdAt: string
  passwordHash: string
}

const users = ref<AdminUser[]>([])
const loading = ref(false)
const error = ref('')

async function load() {
  loading.value = true
  error.value = ''
  try {
    const res = await http.get<AdminUser[]>('/api/admin/users')
    users.value = res.data
  } catch (e: any) {
    error.value = e?.response?.data?.message || '加载失败'
  } finally {
    loading.value = false
  }
}

async function updateUser(u: AdminUser) {
  try {
    await http.put(`/api/admin/users/${u.id}`, {
      enabled: u.enabled,
      role: u.role
    })
    await load()
  } catch (e: any) {
    alert(e?.response?.data?.message || '更新失败')
  }
}

async function resetPassword(u: AdminUser) {
  const newPassword = prompt(`重置用户【${u.username}】的密码（至少6位）`, '123456')
  if (!newPassword) return
  try {
    await http.post(`/api/admin/users/${u.id}/reset-password`, { newPassword })
    alert('已重置密码')
    await load()
  } catch (e: any) {
    alert(e?.response?.data?.message || '重置失败')
  }
}

async function deleteUser(u: AdminUser) {
  if (!confirm(`确定要删除用户【${u.username}】吗？该用户及其发布的攻略、打卡记录等将一并删除且不可恢复。`)) return
  try {
    await http.delete(`/api/admin/users/${u.id}`)
    await load()
  } catch (e: any) {
    alert(e?.response?.data?.message || '删除失败')
  }
}

onMounted(load)
</script>

<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between">
      <h1 class="text-lg font-semibold">用户管理</h1>
      <button class="rounded-xl border border-orange-200 bg-white px-3 py-1.5 text-xs hover:bg-orange-50" @click="load">
        刷新
      </button>
    </div>
    <div v-if="error" class="rounded-2xl border border-orange-100 bg-white/80 px-3 py-2 text-sm text-rose-600 shadow-sm">
      {{ error }}
    </div>
    <div v-if="loading" class="text-sm text-slate-500">加载中...</div>
    <div v-else class="overflow-x-auto rounded-2xl border border-orange-100 bg-white/80 shadow-sm">
      <table class="min-w-full text-left text-sm">
        <thead class="bg-orange-50 text-xs uppercase text-orange-700">
          <tr>
            <th class="px-3 py-2">ID</th>
            <th class="px-3 py-2">用户名</th>
            <th class="px-3 py-2">昵称</th>
            <th class="px-3 py-2">角色</th>
            <th class="px-3 py-2">启用</th>
            <th class="px-3 py-2">注册时间</th>
            <th class="px-3 py-2">密码(哈希)</th>
            <th class="px-3 py-2">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="u in users"
            :key="u.id"
            class="border-t border-orange-50 text-xs hover:bg-orange-50/50"
            :class="!u.enabled ? 'opacity-60' : ''"
          >
            <td class="px-3 py-2">{{ u.id }}</td>
            <td class="px-3 py-2 font-mono text-[11px]">{{ u.username }}</td>
            <td class="px-3 py-2">{{ u.displayName }}</td>
            <td class="px-3 py-2">
              <select v-model="u.role" class="rounded-xl border border-orange-200 bg-white px-2 py-1">
                <option value="USER">普通用户</option>
                <option value="ADMIN">管理员</option>
              </select>
            </td>
            <td class="px-3 py-2">
              <label class="inline-flex items-center gap-1">
                <input v-model="u.enabled" type="checkbox" class="h-3 w-3 rounded border" />
                <span>{{ u.enabled ? '启用' : '禁用' }}</span>
              </label>
            </td>
            <td class="px-3 py-2">{{ new Date(u.createdAt).toLocaleString() }}</td>
            <td class="px-3 py-2 font-mono text-[10px] max-w-[260px] truncate" :title="u.passwordHash">{{ u.passwordHash }}</td>
            <td class="px-3 py-2">
              <div class="flex flex-wrap gap-2">
                <button
                  class="rounded-xl bg-gradient-to-r from-pink-400 to-orange-400 px-2 py-1 text-xs font-medium text-white shadow-sm hover:opacity-90"
                  @click="updateUser(u)"
                >
                  保存
                </button>
                <button
                  class="rounded-xl border border-orange-200 bg-white px-2 py-1 text-xs hover:bg-orange-50"
                  @click="resetPassword(u)"
                >
                  重置密码
                </button>
                <button
                  class="rounded-xl border border-rose-200 bg-white px-2 py-1 text-xs text-rose-600 hover:bg-rose-50"
                  @click="deleteUser(u)"
                >
                  删除用户
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

