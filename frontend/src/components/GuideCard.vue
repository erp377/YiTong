<script setup lang="ts">
import type { GuideCard } from '../types'

defineProps<{ guide: GuideCard }>()
</script>

<template>
  <RouterLink
    class="block rounded-2xl border border-orange-100 bg-white p-4 shadow-sm transition hover:-translate-y-0.5 hover:border-orange-300 hover:shadow"
    :to="`/guides/${guide.id}`"
  >
    <div class="flex items-start justify-between gap-3">
      <div class="min-w-0">
        <div class="truncate text-base font-semibold text-slate-900">{{ guide.title }}</div>
        <div class="mt-1 text-xs text-slate-500">
          <span class="mr-2 rounded-full bg-orange-50 px-2 py-0.5 text-orange-700">{{ guide.category }}</span>
          <span class="mr-2">作者：{{ guide.authorName }}</span>
          <span>{{ new Date(guide.createdAt).toLocaleString() }}</span>
        </div>
      </div>
      <div class="shrink-0 text-right text-xs text-slate-600">
        <div>👍 {{ guide.likeCount }}</div>
        <div>⭐ {{ guide.favoriteCount }}</div>
      </div>
    </div>
  </RouterLink>
</template>

