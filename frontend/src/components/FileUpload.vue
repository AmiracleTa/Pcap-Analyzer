<script setup>
import { ref } from 'vue'
import { UploadCloud } from '@lucide/vue'
import { uploadFile } from '../api/files'

const emit = defineEmits(['uploaded'])

const selected = ref(null)
const fileInput = ref(null)
const uploading = ref(false)
const dragging = ref(false)
const error = ref('')

function onChange(event) {
  selected.value = event.target.files?.[0] || null
  error.value = ''
}

function onDragOver(event) {
  event.preventDefault()
  dragging.value = true
}

function onDragLeave() {
  dragging.value = false
}

function onDrop(event) {
  event.preventDefault()
  dragging.value = false
  selected.value = event.dataTransfer.files?.[0] || null
  error.value = ''
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

async function submit() {
  if (!selected.value) {
    error.value = '请选择抓包文件'
    return
  }

  uploading.value = true
  error.value = ''
  try {
    const result = await uploadFile(selected.value)
    selected.value = null
    if (fileInput.value) {
      fileInput.value.value = ''
    }
    emit('uploaded', result)
  } catch (err) {
    error.value = err.message
  } finally {
    uploading.value = false
  }
}
</script>

<template>
  <form
    class="upload-form upload-drop"
    :class="{ dragging }"
    @submit.prevent="submit"
    @dragover="onDragOver"
    @dragleave="onDragLeave"
    @drop="onDrop"
  >
    <label for="capture-file" class="upload-picker">
      <div class="upload-icon" aria-hidden="true">
        <UploadCloud :size="30" />
      </div>
      <span class="upload-picker-copy">
        <strong>{{ selected ? '已选择抓包文件' : '拖拽或点击选择抓包文件' }}</strong>
        <span>支持 .pcap / .pcapng / .cap</span>
        <span v-if="selected" class="selected-file-meta">
          {{ selected.name }} · {{ formatSize(selected.size) }}
        </span>
      </span>
    </label>
    <input
      id="capture-file"
      ref="fileInput"
      class="sr-only-file"
      type="file"
      accept=".pcap,.pcapng,.cap"
      @change="onChange"
    />
    <button class="primary-button" type="submit" :disabled="uploading">
      {{ uploading ? '上传中' : '上传文件' }}
    </button>
    <span v-if="error" class="badge error">{{ error }}</span>
  </form>
</template>
