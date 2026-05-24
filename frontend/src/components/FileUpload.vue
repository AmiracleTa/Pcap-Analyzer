<script setup>
import { ref } from 'vue'
import { UploadCloud } from '@lucide/vue'
import { uploadFile } from '../api/files'

const emit = defineEmits(['uploaded'])

const fileInput = ref(null)
const uploading = ref(false)
const dragging = ref(false)
const error = ref('')
const currentFileName = ref('')

async function onChange(event) {
  await uploadSelectedFile(event.target.files?.[0] || null)
}

function onDragOver(event) {
  event.preventDefault()
  if (uploading.value) {
    return
  }
  dragging.value = true
}

function onDragLeave() {
  dragging.value = false
}

async function onDrop(event) {
  event.preventDefault()
  dragging.value = false
  await uploadSelectedFile(event.dataTransfer.files?.[0] || null)
}

function formatSize(value) {
  if (!value) {
    return '0 B'
  }
  if (value < 1024) {
    return `${value} B`
  }
  if (value < 1024 * 1024) {
    return `${(value / 1024).toFixed(1)} KB`
  }
  return `${(value / 1024 / 1024).toFixed(1)} MB`
}

async function uploadSelectedFile(file) {
  if (uploading.value) {
    return
  }
  if (!file) {
    error.value = '请选择抓包文件'
    return
  }

  uploading.value = true
  error.value = ''
  currentFileName.value = `${file.name} · ${formatSize(file.size)}`
  try {
    const result = await uploadFile(file)
    currentFileName.value = ''
    emit('uploaded', result)
  } catch (err) {
    error.value = err.message
  } finally {
    if (fileInput.value) {
      fileInput.value.value = ''
    }
    uploading.value = false
  }
}
</script>

<template>
  <form
    class="upload-form upload-drop"
    :class="{ dragging, uploading }"
    @submit.prevent
    @dragover="onDragOver"
    @dragleave="onDragLeave"
    @drop="onDrop"
  >
    <label for="capture-file" class="upload-picker">
      <div class="upload-icon" aria-hidden="true">
        <UploadCloud :size="30" />
      </div>
      <span class="upload-picker-copy">
        <strong>{{ uploading ? '正在上传抓包文件' : '拖拽或点击选择抓包文件' }}</strong>
        <span>{{ uploading ? '文件写入完成后会自动刷新列表' : '支持 .pcap / .pcapng / .cap，选择后自动上传' }}</span>
        <span v-if="currentFileName" class="selected-file-meta">{{ currentFileName }}</span>
      </span>
    </label>
    <input
      id="capture-file"
      ref="fileInput"
      class="sr-only-file"
      type="file"
      accept=".pcap,.pcapng,.cap"
      :disabled="uploading"
      @change="onChange"
    />
    <span v-if="error" class="badge error">{{ error }}</span>
  </form>
</template>
