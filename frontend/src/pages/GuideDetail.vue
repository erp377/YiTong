<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { http } from '../api/http'
import type { CheckIn, Comment, GuideDetail } from '../types'
import { marked } from 'marked'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const id = Number(route.params.id)

const guide = ref<GuideDetail | null>(null)
const comments = ref<Comment[]>([])
const checkins = ref<CheckIn[]>([])
const commentText = ref('')
const loading = ref(false)
const error = ref('')
const isFollowingAuthor = ref(false)
const checkInError = ref('')

const html = computed(() => (guide.value ? marked.parse(guide.value.contentMarkdown) : ''))
const canShowCheckIn = computed(() => guide.value && (guide.value.category === 'STUDY' || guide.value.category === 'GAME'))
const checkInTitle = computed(() => guide.value?.category === 'GAME' ? '游戏打卡' : '学习打卡')
const isAuthor = computed(() => auth.user && guide.value && auth.user.id === guide.value.authorId)

async function loadAll() {
  loading.value = true
  error.value = ''
  try {
    const g = await http.get(`/api/guides/${id}`)
    guide.value = g.data
    const c = await http.get(`/api/guides/${id}/comments`)
    comments.value = c.data
    if (canShowCheckIn.value && auth.token) {
      const ci = await http.get(`/api/guides/${id}/checkins`)
      checkins.value = ci.data
    } else {
      checkins.value = []
    }
    if (auth.token && guide.value && guide.value.authorId !== auth.user?.id) {
      const r = await http.get(`/api/users/${guide.value.authorId}/following`)
      isFollowingAuthor.value = r.data === true
    } else {
      isFollowingAuthor.value = false
    }
  } catch (e: any) {
    error.value = e?.response?.data?.message || '加载失败'
  } finally {
    loading.value = false
  }
}

async function toggleLike() {
  if (!guide.value) return
  if (!auth.token) return alert('请先登录')
  if (guide.value.liked) {
    await http.delete(`/api/guides/${id}/like`)
  } else {
    await http.post(`/api/guides/${id}/like`)
  }
  await loadAll()
}

async function toggleFav() {
  if (!guide.value) return
  if (!auth.token) return alert('请先登录')
  if (guide.value.favorited) {
    await http.delete(`/api/guides/${id}/favorite`)
  } else {
    await http.post(`/api/guides/${id}/favorite`)
  }
  await loadAll()
}

async function submitComment() {
  if (!auth.token) return alert('请先登录')
  if (!commentText.value.trim()) return
  await http.post(`/api/guides/${id}/comments`, { content: commentText.value.trim() })
  commentText.value = ''
  await loadAll()
}

const checkInDay = ref<string>(new Date().toISOString().slice(0, 10))
const checkInProgress = ref<number>(0)
const checkInNote = ref<string>('')

async function submitCheckIn() {
  if (!auth.token) return alert('请先登录')
  checkInError.value = ''
  const progress = Number(checkInProgress.value)
  if (Number.isNaN(progress) || progress < 0 || progress > 100) {
    checkInError.value = '进度请填写 0-100 的整数'
    return
  }
  try {
    await http.post(`/api/guides/${id}/checkins`, {
      day: checkInDay.value,
      progress,
      note: checkInNote.value || null
    })
    await loadAll()
  } catch (e: any) {
    checkInError.value = e?.response?.data?.message || '提交失败'
  }
}

async function toggleFollow() {
  if (!guide.value || !auth.token) return alert('请先登录')
  if (guide.value.authorId === auth.user?.id) return
  try {
    if (isFollowingAuthor.value) {
      await http.delete(`/api/users/${guide.value.authorId}/follow`)
    } else {
      await http.post(`/api/users/${guide.value.authorId}/follow`)
    }
    isFollowingAuthor.value = !isFollowingAuthor.value
  } catch (e: any) {
    alert(e?.response?.data?.message || '操作失败')
  }
}

async function deleteGuide() {
  if (!guide.value || !confirm('确定要删除这篇攻略吗？删除后不可恢复。')) return
  try {
    await http.delete(`/api/guides/${id}`)
    router.push('/')
  } catch (e: any) {
    alert(e?.response?.data?.message || '删除失败')
  }
}

onMounted(loadAll)
</script>

<template>
  <div class="space-y-4">
    <div v-if="error" class="rounded-lg border bg-white p-3 text-sm text-red-600">{{ error }}</div>
    <div v-if="loading && !guide" class="text-sm text-slate-500">加载中...</div>

    <div v-if="guide" class="rounded-xl border bg-white p-4">
      <div class="flex flex-col gap-2 md:flex-row md:items-start md:justify-between">
        <div class="min-w-0">
          <div class="text-xl font-semibold">{{ guide.title }}</div>
          <div class="mt-1 text-xs text-slate-500">
            <span class="mr-2 rounded bg-slate-100 px-2 py-0.5">{{ guide.category }}</span>
            <span class="mr-2">作者：{{ guide.authorName }}</span>
            <span>更新：{{ new Date(guide.updatedAt).toLocaleString() }}</span>
          </div>
        </div>
        <div class="flex flex-wrap gap-2">
          <button class="rounded-lg border px-3 py-2 text-sm hover:bg-slate-50" @click="toggleLike">
            {{ guide.liked ? '已点赞' : '点赞' }} ({{ guide.likeCount }})
          </button>
          <button class="rounded-lg border px-3 py-2 text-sm hover:bg-slate-50" @click="toggleFav">
            {{ guide.favorited ? '已收藏' : '收藏' }} ({{ guide.favoriteCount }})
          </button>
          <button
            v-if="auth.user && guide.authorId !== auth.user.id"
            class="rounded-lg border px-3 py-2 text-sm hover:bg-slate-50"
            @click="toggleFollow"
          >
            {{ isFollowingAuthor ? '已关注' : '关注作者' }}
          </button>
          <button
            v-if="isAuthor"
            class="rounded-lg border border-rose-200 px-3 py-2 text-sm text-rose-600 hover:bg-rose-50"
            @click="deleteGuide"
          >
            删除攻略
          </button>
        </div>
      </div>

      <div class="prose prose-slate mt-4 max-w-none" v-html="html"></div>
    </div>

    <div v-if="canShowCheckIn" class="rounded-xl border bg-white p-4">
      <div class="mb-2 flex flex-wrap items-center gap-2">
        <span class="text-base font-semibold">{{ checkInTitle }}</span>
        <span class="text-sm text-slate-500">打卡人数：{{ guide?.checkinCount ?? 0 }}</span>
        <span v-if="guide?.checkinToday" class="rounded bg-emerald-100 px-2 py-0.5 text-sm text-emerald-700">今日已打卡</span>
      </div>
      <div class="grid gap-2 md:grid-cols-3">
        <label class="text-sm">
          日期
          <input v-model="checkInDay" type="date" class="mt-1 w-full rounded-lg border px-3 py-2" />
        </label>
        <label class="text-sm">
          进度（0-100）
          <input v-model.number="checkInProgress" type="number" min="0" max="100" class="mt-1 w-full rounded-lg border px-3 py-2" />
        </label>
        <label class="text-sm md:col-span-1">
          备注
          <input v-model="checkInNote" class="mt-1 w-full rounded-lg border px-3 py-2" placeholder="今天做了什么？" />
        </label>
      </div>
      <div class="mt-2">
        <button class="rounded-lg bg-slate-900 px-4 py-2 text-sm text-white hover:bg-slate-800" @click="submitCheckIn">
          提交/更新打卡
        </button>
        <span v-if="checkInError" class="ml-2 text-sm text-rose-600">{{ checkInError }}</span>
      </div>
      <div class="mt-4 text-sm text-slate-600">
        <div v-if="!auth.token">登录后可查看自己的打卡记录。</div>
        <div v-else-if="checkins.length === 0">暂无打卡记录。</div>
        <ul v-else class="space-y-1">
          <li v-for="ci in checkins" :key="ci.id" class="flex flex-wrap items-center gap-2">
            <span class="rounded bg-slate-100 px-2 py-0.5">{{ ci.day }}</span>
            <span>进度：{{ ci.progress }}</span>
            <span v-if="ci.note" class="text-slate-500">- {{ ci.note }}</span>
          </li>
        </ul>
      </div>
    </div>

    <div class="rounded-xl border bg-white p-4">
      <div class="mb-2 text-base font-semibold">评论</div>
      <div class="flex flex-col gap-2 md:flex-row">
        <input v-model="commentText" class="w-full rounded-lg border px-3 py-2 text-sm" placeholder="说点什么…" />
        <button class="rounded-lg bg-slate-900 px-4 py-2 text-sm text-white hover:bg-slate-800" @click="submitComment">发送</button>
      </div>
      <div class="mt-4 space-y-2">
        <div v-if="comments.length === 0" class="text-sm text-slate-500">暂无评论</div>
        <div v-for="c in comments" :key="c.id" class="rounded-lg border p-3">
          <div class="text-xs text-slate-500">
            <span class="mr-2 font-medium text-slate-700">{{ c.userName }}</span>
            <span>{{ new Date(c.createdAt).toLocaleString() }}</span>
          </div>
          <div class="mt-1 text-sm">{{ c.content }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

