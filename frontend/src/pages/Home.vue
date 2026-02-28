<script setup lang="ts">
import { ref, watchEffect } from 'vue'
import { http } from '../api/http'
import type { GuideCard, GuideCategory } from '../types'
import GuideCardCmp from '../components/GuideCard.vue'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()

const q = ref('')
const category = ref<GuideCategory | ''>('')
const guides = ref<GuideCard[]>([])
const loading = ref(false)
const error = ref('')

async function load() {
  loading.value = true
  error.value = ''
  try {
    const res = await http.get('/api/guides', {
      params: {
        q: q.value || undefined,
        category: category.value || undefined,
        page: 0,
        size: 12,
        sort: 'latest'
      }
    })
    guides.value = res.data.content
  } catch (e: any) {
    error.value = e?.response?.data?.message || '加载失败'
  } finally {
    loading.value = false
  }
}

let timer: any = null
watchEffect(() => {
  clearTimeout(timer)
  timer = setTimeout(load, 250)
})

async function deleteGuide(g: GuideCard) {
  if (!confirm(`确定要删除《${g.title}》吗？删除后不可恢复。`)) return
  try {
    await http.delete(`/api/guides/${g.id}`)
    await load()
  } catch (e: any) {
    alert(e?.response?.data?.message || '删除失败')
  }
}
</script>

<template>
  <div class="space-y-4">
    <div class="rounded-2xl border border-orange-100 bg-white/80 p-4 shadow-sm">
      <div class="flex flex-col gap-3 md:flex-row md:items-center">
        <input
          v-model="q"
          class="w-full rounded-xl border border-orange-200 bg-white px-3 py-2 text-sm text-slate-900 outline-none focus:border-orange-400 focus:ring-1 focus:ring-orange-300"
          placeholder="搜索攻略（标题/内容）"
        />
        <div class="flex gap-2 text-xs md:text-sm">
          <button
            class="rounded-full border px-3 py-1.5"
            :class="
              category === ''
                ? 'border-transparent bg-gradient-to-r from-pink-400 to-orange-400 text-white shadow-sm'
                : 'border-orange-200 bg-white text-slate-700 hover:bg-orange-50'
            "
            @click="category = ''"
          >
            全部
          </button>
          <button
            class="rounded-full border px-3 py-1.5"
            :class="
              category === 'TRAVEL'
                ? 'border-transparent bg-gradient-to-r from-pink-400 to-orange-400 text-white shadow-sm'
                : 'border-orange-200 bg-white text-slate-700 hover:bg-orange-50'
            "
            @click="category = 'TRAVEL'"
          >
            旅游
          </button>
          <button
            class="rounded-full border px-3 py-1.5"
            :class="
              category === 'GAME'
                ? 'border-transparent bg-gradient-to-r from-pink-400 to-orange-400 text-white shadow-sm'
                : 'border-orange-200 bg-white text-slate-700 hover:bg-orange-50'
            "
            @click="category = 'GAME'"
          >
            游戏
          </button>
          <button
            class="rounded-full border px-3 py-1.5"
            :class="
              category === 'STUDY'
                ? 'border-transparent bg-gradient-to-r from-pink-400 to-orange-400 text-white shadow-sm'
                : 'border-orange-200 bg-white text-slate-700 hover:bg-orange-50'
            "
            @click="category = 'STUDY'"
          >
            学习
          </button>
        </div>
      </div>
      <div v-if="error" class="mt-3 text-sm text-rose-600">{{ error }}</div>
    </div>

    <div v-if="loading" class="text-sm text-slate-500">加载中...</div>
    <div v-else class="grid gap-3 md:grid-cols-2">
      <div v-for="g in guides" :key="g.id" class="relative">
        <GuideCardCmp :guide="g" />
        <button
          v-if="auth.user && g.authorId === auth.user.id"
          type="button"
          class="absolute right-2 top-2 rounded-lg border border-rose-200 bg-white/90 px-2 py-1 text-xs text-rose-600 hover:bg-rose-50"
          @click.prevent="deleteGuide(g)"
        >
          删除
        </button>
      </div>
    </div>
  </div>
</template>

