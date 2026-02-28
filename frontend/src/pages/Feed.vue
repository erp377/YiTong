<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { http } from '../api/http'
import type { GuideCard } from '../types'
import GuideCardCmp from '../components/GuideCard.vue'

const auth = useAuthStore()
const router = useRouter()
const guides = ref<GuideCard[]>([])
const loading = ref(false)
const error = ref('')

async function load() {
  if (!auth.token) {
    router.replace('/login')
    return
  }
  loading.value = true
  error.value = ''
  try {
    const res = await http.get<GuideCard[]>('/api/me/feed', { params: { page: 0, size: 20 } })
    guides.value = res.data
  } catch (e: any) {
    if (e?.response?.status === 401) router.replace('/login')
    else error.value = e?.response?.data?.message || '加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="space-y-4">
    <div class="text-base font-semibold text-slate-900">关注动态</div>
    <p v-if="!loading && guides.length === 0 && !error" class="text-sm text-slate-500">
      暂无动态。去关注一些作者，或先发布攻略吧。
    </p>
    <p v-if="error" class="text-sm text-rose-600">{{ error }}</p>
    <div v-if="loading" class="py-8 text-center text-sm text-slate-500">加载中…</div>
    <div v-else class="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
      <GuideCardCmp v-for="g in guides" :key="g.id" :guide="g" />
    </div>
  </div>
</template>
