<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { http } from '../api/http'
import type { GuideCategory, Template } from '../types'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()

const templates = ref<Template[]>([])
const templateKey = ref<string>('')
const title = ref('')
const category = ref<GuideCategory>('TRAVEL')
const contentMarkdown = ref('')
const error = ref('')

// 行程表模板表单
const itineraryDays = ref(3)
const itineraryCity = ref('')

// 学习计划模板表单
const studyGoal = ref('')
const studyWeeks = ref(4)

async function loadTemplates() {
  const res = await http.get('/api/templates')
  templates.value = res.data
}

function applyTemplate() {
  const t = templates.value.find((x) => x.key === templateKey.value)
  if (!t) return
  category.value = t.categoryHint
  contentMarkdown.value = t.starterMarkdown
}

function generateFromForm() {
  if (templateKey.value === 'itinerary_table') {
    const days = Math.max(1, Math.min(14, itineraryDays.value || 1))
    const rows = Array.from({ length: days })
      .map((_, i) => `| Day${i + 1} | ${itineraryCity.value || ''} |  |  |  |  |`)
      .join('\n')
    contentMarkdown.value = `# 行程安排\n\n目的地：${itineraryCity.value || ''}\n\n| 天数 | 城市/地点 | 交通 | 住宿 | 预算 | 备注 |\n|---|---|---|---|---:|---|\n${rows}\n`
    category.value = 'TRAVEL'
  } else if (templateKey.value === 'study_plan') {
    const weeks = Math.max(1, Math.min(16, studyWeeks.value || 1))
    const rows = Array.from({ length: weeks })
      .map((_, i) => `| Week${i + 1} |  |  |  |  |`)
      .join('\n')
    contentMarkdown.value = `# 学习计划\n\n## 目标\n- ${studyGoal.value || ''}\n\n## 每周计划\n| 周次 | 主题 | 任务 | 预计用时 | 产出/验收 |\n|---|---|---|---:|---|\n${rows}\n\n## 打卡建议\n- 每天记录：进度（0-100）+ 今日完成内容 + 明日计划\n`
    category.value = 'STUDY'
  } else if (templateKey.value === 'game_build') {
    contentMarkdown.value =
      '# 游戏配装/打法\n\n## 适用场景\n- \n\n## 核心思路\n- \n\n## 装备/技能/符文\n- \n\n## 操作要点\n- \n'
    category.value = 'GAME'
  } else {
    const t = templates.value.find((x) => x.key === templateKey.value)
    if (t) {
      category.value = t.categoryHint
      contentMarkdown.value = t.starterMarkdown
    }
  }
}

const uploading = ref(false)
async function onImageSelect(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    alert('请选择图片文件')
    return
  }
  uploading.value = true
  try {
    const form = new FormData()
    form.append('file', file)
    const res = await http.post<{ url: string }>('/api/upload', form)
    const url = res.data.url
    contentMarkdown.value += `\n![图片](${url})\n`
  } catch (err: any) {
    alert(err?.response?.data?.message || '上传失败')
  } finally {
    uploading.value = false
    input.value = ''
  }
}

async function submit() {
  if (!auth.token) return alert('请先登录')
  error.value = ''
  try {
    const res = await http.post('/api/guides', {
      title: title.value,
      category: category.value,
      templateKey: templateKey.value || null,
      contentMarkdown: contentMarkdown.value
    })
    router.push(`/guides/${res.data.id}`)
  } catch (e: any) {
    error.value = e?.response?.data?.message || '发布失败'
  }
}

onMounted(loadTemplates)
</script>

<template>
  <div class="space-y-4">
    <div class="rounded-2xl border border-orange-100 bg-white/80 p-4 shadow-sm">
      <div class="text-base font-semibold text-slate-900">发布攻略</div>
      <div class="mt-3 grid gap-3 md:grid-cols-3">
        <label class="text-sm md:col-span-2">
          标题
          <input
            v-model="title"
            class="mt-1 w-full rounded-xl border border-orange-200 bg-white px-3 py-2 text-sm text-slate-900 outline-none focus:border-orange-400 focus:ring-1 focus:ring-orange-300"
            placeholder="例如：三天两夜杭州行程"
          />
        </label>
        <label class="text-sm">
          分类
          <select
            v-model="category"
            class="mt-1 w-full rounded-xl border border-orange-200 bg-white px-3 py-2 text-sm text-slate-900"
          >
            <option value="TRAVEL">旅游</option>
            <option value="GAME">游戏</option>
            <option value="STUDY">学习</option>
          </select>
        </label>
      </div>

      <div class="mt-3 grid gap-3 md:grid-cols-3">
        <label class="text-sm md:col-span-2">
          套用模板（可选）
          <select
            v-model="templateKey"
            class="mt-1 w-full rounded-xl border border-orange-200 bg-white px-3 py-2 text-sm text-slate-900"
            @change="applyTemplate"
          >
            <option value="">不使用模板</option>
            <option v-for="t in templates" :key="t.key" :value="t.key">
              {{ t.name }} ({{ t.categoryHint }})
            </option>
          </select>
        </label>
        <div class="text-xs text-slate-500 md:pt-6">
          选择模板后，可用下方表单一键生成结构化内容，再手动微调。
        </div>
      </div>

      <div v-if="templateKey === 'itinerary_table'" class="mt-4 grid gap-3 md:grid-cols-3">
        <label class="text-xs text-slate-700">
          目的地城市
          <input
            v-model="itineraryCity"
            class="mt-1 w-full rounded-xl border border-orange-200 bg-white px-3 py-2 text-xs text-slate-900"
            placeholder="如：杭州 / 成都"
          />
        </label>
        <label class="text-xs text-slate-700">
          天数
          <input
            v-model.number="itineraryDays"
            type="number"
            min="1"
            max="14"
            class="mt-1 w-full rounded-xl border border-orange-200 bg-white px-3 py-2 text-xs text-slate-900"
          />
        </label>
        <div class="flex items-end">
          <button
            class="w-full rounded-xl bg-gradient-to-r from-pink-400 to-orange-400 px-3 py-2 text-xs font-medium text-white shadow-sm hover:opacity-90"
            @click="generateFromForm"
          >
            根据行程表生成内容
          </button>
        </div>
      </div>

      <div v-else-if="templateKey === 'study_plan'" class="mt-4 grid gap-3 md:grid-cols-3">
        <label class="text-xs text-slate-700 md:col-span-2">
          学习目标
          <input
            v-model="studyGoal"
            class="mt-1 w-full rounded-xl border border-orange-200 bg-white px-3 py-2 text-xs text-slate-900"
            placeholder="如：两个月通过 Java 初级认证"
          />
        </label>
        <label class="text-xs text-slate-700">
          周数
          <input
            v-model.number="studyWeeks"
            type="number"
            min="1"
            max="16"
            class="mt-1 w-full rounded-xl border border-orange-200 bg-white px-3 py-2 text-xs text-slate-900"
          />
        </label>
        <div class="md:col-span-3">
          <button
            class="mt-1 rounded-xl bg-gradient-to-r from-pink-400 to-orange-400 px-3 py-2 text-xs font-medium text-white shadow-sm hover:opacity-90"
            @click="generateFromForm"
          >
            根据学习计划生成内容
          </button>
        </div>
      </div>
    </div>

    <div class="rounded-2xl border border-orange-100 bg-white/80 p-4 shadow-sm">
      <div class="mb-2 flex items-center gap-2">
        <span class="text-sm font-medium text-slate-900">内容（Markdown）</span>
        <label class="cursor-pointer rounded-lg border border-orange-200 bg-white px-2 py-1 text-xs hover:bg-orange-50">
          <input type="file" accept="image/*" class="hidden" @change="onImageSelect" />
          {{ uploading ? '上传中…' : '插入图片' }}
        </label>
      </div>
      <textarea
        v-model="contentMarkdown"
        class="min-h-[360px] w-full rounded-xl border border-orange-200 bg-white px-3 py-2 font-mono text-sm text-slate-900 outline-none focus:border-orange-400 focus:ring-1 focus:ring-orange-300"
        placeholder="在这里写攻略内容..."
      />
      <div class="mt-3 flex items-center justify-between">
        <div v-if="error" class="text-sm text-rose-600">{{ error }}</div>
        <button
          class="rounded-xl bg-gradient-to-r from-pink-400 to-orange-400 px-4 py-2 text-sm font-medium text-white shadow-sm hover:opacity-90"
          @click="submit"
        >
          发布
        </button>
      </div>
    </div>
  </div>
</template>

