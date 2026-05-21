<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { analyzeFile, deleteFile, getHealth, listFiles } from './api/files'
import AnalysisPage from './views/AnalysisPage.vue'
import FilePage from './views/FilePage.vue'
import HeroSection from './components/HeroSection.vue'
import SiteHeader from './components/SiteHeader.vue'

const selectedFile = ref(null)
const files = ref([])
const health = ref('checking')
const message = ref('')
const dark = ref(false)
const menuOpen = ref(false)
const scrolled = ref(false)
let revealObserver = null

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

function scrollToSection(id) {
  document.getElementById(id)?.scrollIntoView({ block: 'start', behavior: 'smooth' })
  menuOpen.value = false
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
  await Promise.all([loadHealth(), loadFiles()])
}

async function handleUploaded(file) {
  message.value = `已上传 ${file.originalName}`
  await loadFiles()
  selectedFile.value = file
  queueReveal()
  scrollToSection('analysis')
}

function handleSelectFile(file) {
  selectedFile.value = file
  queueReveal()
  scrollToSection('analysis')
}

async function handleAnalyze(file) {
  const result = await analyzeFile(file.id)
  message.value = `分析完成，数据包数量：${result.packetCount ?? 0}`
  const updatedFiles = await loadFiles()
  selectedFile.value = updatedFiles.find((item) => item.id === file.id) || file
  queueReveal()
  scrollToSection('analysis')
}

async function handleDelete(file) {
  await deleteFile(file.id)
  message.value = `已删除 ${file.originalName}`
  await loadFiles()
  if (selectedFile.value?.id === file.id) {
    selectedFile.value = null
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
  revealObserver?.disconnect()
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
    <main>
      <HeroSection
        :health="health"
        :file-count="files.length"
        :selected-file="selectedFile"
        @select-sample-section="scrollToSection('files')"
      />
      <section id="files" class="section">
        <FilePage
          :files="files"
          :health="health"
          :message="message"
          @refresh="refresh"
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
