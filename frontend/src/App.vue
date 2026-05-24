<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { createAnalyzeEventSource, deleteFile, getHealth, listFiles } from './api/files'
import AnalysisPage from './views/AnalysisPage.vue'
import FeatureSection from './components/FeatureSection.vue'
import FilePage from './views/FilePage.vue'
import HeroSection from './components/HeroSection.vue'
import SiteHeader from './components/SiteHeader.vue'

const selectedFile = ref(null)
const files = ref([])
const health = ref('checking')
const message = ref('')
const messageTone = ref('neutral')
const dark = ref(false)
const menuOpen = ref(false)
const scrolled = ref(false)
const analysisProgress = ref(null)
const analyzingFileId = ref(null)
const deletingFileId = ref(null)
let revealObserver = null
let analysisEventSource = null
let messageTimer = null

function toggleDark() {
  dark.value = !dark.value
  document.documentElement.classList.toggle('dark', dark.value)
  document.documentElement.style.colorScheme = dark.value ? 'dark' : 'light'
}

function toggleMenu() {
  menuOpen.value = !menuOpen.value
}

function updateHeader() {
  scrolled.value = window.scrollY > 24
}

function setupReveal() {
  revealObserver?.disconnect()
  revealObserver = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.add('is-visible')
          revealObserver?.unobserve(entry.target)
        }
      })
    },
    { threshold: 0.16 },
  )
  document.querySelectorAll('[data-reveal]').forEach((item) => revealObserver.observe(item))
}

function queueReveal() {
  window.setTimeout(setupReveal, 0)
}

function closeAnalysisEventSource() {
  if (analysisEventSource) {
    analysisEventSource.close()
    analysisEventSource = null
  }
}

function clearMessageTimer() {
  if (messageTimer) {
    window.clearTimeout(messageTimer)
    messageTimer = null
  }
}

function showMessage(text, duration = 3200, tone = 'neutral') {
  clearMessageTimer()
  message.value = text
  messageTone.value = tone
  if (duration > 0) {
    messageTimer = window.setTimeout(() => {
      message.value = ''
      messageTimer = null
    }, duration)
  }
}

function scrollToSection(id) {
  document.getElementById(id)?.scrollIntoView({ block: 'start', behavior: 'smooth' })
  menuOpen.value = false
}

function scrollToAnalysisProgress() {
  window.setTimeout(() => {
    document.getElementById('analysis-progress')?.scrollIntoView({ block: 'nearest', behavior: 'smooth' })
  }, 0)
}

async function loadHealth() {
  try {
    await getHealth()
    health.value = 'ok'
  } catch {
    health.value = 'error'
  }
}

async function loadFiles() {
  files.value = await listFiles()
  return files.value
}

async function refresh() {
  message.value = ''
  clearMessageTimer()
  await Promise.all([loadHealth(), loadFiles()])
}

async function handleUploaded(file) {
  showMessage(`${file.originalName} 已上传`, 3200, 'success')
  await loadFiles()
}

function handleSelectFile(file) {
  if (file.status !== 'analyzed') {
    showMessage('请先分析该文件，再查看解析结果', 3600)
    return
  }
  selectedFile.value = file
  queueReveal()
  scrollToSection('analysis')
}

function handleAnalyze(file) {
  closeAnalysisEventSource()
  analyzingFileId.value = file.id
  analysisProgress.value = {
    status: 'starting',
    phase: 'prepare',
    message: '正在准备解析文件',
    totalPackets: 0,
    processedPackets: 0,
    percent: 0,
    packetCount: null,
  }
  showMessage(`开始分析 ${file.originalName}`, 2400)
  scrollToAnalysisProgress()

  const source = createAnalyzeEventSource(file.id)
  analysisEventSource = source

  source.addEventListener('progress', async (event) => {
    const payload = JSON.parse(event.data)
    analysisProgress.value = payload

    if (payload.status === 'done') {
      closeAnalysisEventSource()
      analyzingFileId.value = null
      showMessage(`分析完成，数据包数量：${payload.packetCount ?? payload.processedPackets ?? 0}`, 4200, 'success')
      await loadFiles()
      queueReveal()
    }

    if (payload.status === 'error') {
      closeAnalysisEventSource()
      analyzingFileId.value = null
      showMessage(payload.message || '解析失败', 4200)
      await loadFiles()
    }
  })

  source.onerror = async () => {
    if (analysisProgress.value?.status === 'done') {
      return
    }
    closeAnalysisEventSource()
    analyzingFileId.value = null
    showMessage('分析连接中断，请重新点击分析', 4200)
    await loadFiles()
  }
}

async function handleDelete(file) {
  deletingFileId.value = file.id
  showMessage(`正在删除 ${file.originalName}`, 1800)
  try {
    await deleteFile(file.id)
    showMessage(`已删除 ${file.originalName}`, 3200, 'success')
    await loadFiles()
    if (selectedFile.value?.id === file.id) {
      selectedFile.value = null
    }
  } catch (error) {
    showMessage(error.message || '删除失败', 4200)
  } finally {
    deletingFileId.value = null
  }
}

onMounted(() => {
  refresh()
  updateHeader()
  window.addEventListener('scroll', updateHeader, { passive: true })
  setupReveal()
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', updateHeader)
  closeAnalysisEventSource()
  revealObserver?.disconnect()
  clearMessageTimer()
})
</script>

<template>
  <div class="site-shell">
    <div class="noise" aria-hidden="true"></div>
    <SiteHeader
      :dark="dark"
      :menu-open="menuOpen"
      :scrolled="scrolled"
      @toggle-dark="toggleDark"
      @toggle-menu="toggleMenu"
    />
    <Transition name="toast">
      <div v-if="message" class="app-toast" :class="`is-${messageTone}`" role="status" aria-live="polite">
        <span v-if="messageTone === 'success'" class="app-toast-icon" aria-hidden="true">✓</span>
        <span>{{ message }}</span>
      </div>
    </Transition>
    <main>
      <HeroSection
        @select-sample-section="scrollToSection('files')"
      />
      <section id="features" class="section features-section">
        <FeatureSection />
      </section>
      <section id="files" class="section">
        <FilePage
          :files="files"
          :health="health"
          :analysis-progress="analysisProgress"
          :analyzing-file-id="analyzingFileId"
          :deleting-file-id="deletingFileId"
          @uploaded="handleUploaded"
          @select-file="handleSelectFile"
          @analyze="handleAnalyze"
          @delete="handleDelete"
        />
      </section>
      <section id="analysis" class="section analysis-section">
        <AnalysisPage :selected-file="selectedFile" />
      </section>
    </main>
  </div>
</template>
