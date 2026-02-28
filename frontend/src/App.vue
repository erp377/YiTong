<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from './stores/auth'
import { useRouter } from 'vue-router'

const auth = useAuthStore()
const router = useRouter()
const meText = computed(() =>
  auth.user ? `你好，${auth.user.displayName}${auth.user.role === 'ADMIN' ? '（管理员）' : ''}` : '未登录'
)

function logout() {
  auth.logout()
  router.push('/')
}
</script>

<template>
  <div class="min-h-screen bg-gradient-to-b from-pink-50 via-orange-50 to-white text-slate-900">
    <header class="sticky top-0 z-10 border-b border-orange-100 bg-white/80 backdrop-blur">
      <div class="mx-auto flex max-w-6xl items-center justify-between gap-3 px-4 py-3">
        <RouterLink to="/" class="flex items-center gap-2 text-lg font-semibold tracking-wide">
          <span
            class="inline-flex h-7 w-7 items-center justify-center rounded-lg bg-gradient-to-tr from-pink-400 to-orange-400 text-xs font-bold text-white shadow"
            >易</span
          >
          <span>易通攻略</span>
        </RouterLink>
        <nav class="flex items-center gap-2 text-xs md:text-sm">
          <RouterLink class="rounded-full px-3 py-1.5 hover:bg-orange-50" to="/">
            社区
          </RouterLink>
          <RouterLink class="rounded-full px-3 py-1.5 hover:bg-orange-50" to="/editor">
            发布攻略
          </RouterLink>
          <RouterLink
            v-if="auth.user"
            class="rounded-full px-3 py-1.5 hover:bg-orange-50"
            to="/feed"
          >
            关注
          </RouterLink>
          <RouterLink class="rounded-full px-3 py-1.5 hover:bg-orange-50" to="/me">
            我的
          </RouterLink>
          <RouterLink
            v-if="auth.user?.role === 'ADMIN'"
            class="rounded-full px-3 py-1.5 text-orange-700 hover:bg-orange-50"
            to="/admin/users"
          >
            管理后台
          </RouterLink>
          <RouterLink
            v-if="!auth.user"
            class="rounded-full border border-orange-200 px-3 py-1.5 hover:border-orange-300 hover:bg-orange-50"
            to="/login"
          >
            登录
          </RouterLink>
          <RouterLink
            v-if="!auth.user"
            class="rounded-full bg-gradient-to-r from-pink-400 to-orange-400 px-3 py-1.5 font-medium text-white shadow-sm hover:opacity-90"
            to="/register"
          >
            注册
          </RouterLink>
          <button
            v-if="auth.user"
            class="rounded-full border border-orange-200 px-3 py-1.5 text-xs hover:border-orange-300 hover:bg-orange-50"
            @click="logout"
          >
            退出
          </button>
        </nav>
      </div>
    </header>

    <main class="mx-auto max-w-6xl px-4 py-6">
      <div
        class="mb-4 flex flex-col justify-between gap-3 rounded-2xl border border-orange-100 bg-white/70 p-4 text-sm text-slate-700 shadow-sm md:flex-row md:items-center"
      >
        <div>
          <div class="text-xs uppercase tracking-wide text-orange-600/80">多场景攻略分享平台</div>
          <div class="mt-1 text-base font-medium text-slate-900">
            聚合旅游 / 游戏 / 学习攻略，一站式创作与查找。
          </div>
        </div>
        <div class="text-right text-xs text-slate-500">
          <div>{{ meText }}</div>
        </div>
      </div>

      <div class="rounded-2xl border border-orange-100 bg-white/80 p-4 shadow-sm md:p-6">
        <RouterView />
      </div>
    </main>
  </div>
</template>

