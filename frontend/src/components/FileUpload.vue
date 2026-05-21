<script setup>
import { ref } from 'vue'
import { UploadCloud } from '@lucide/vue'
import { uploadFile } from '../api/files'

const emit = defineEmits(['uploaded'])

const selected = ref(null)
const uploading = ref(false)
const error = ref('')

function onChange(event) {
  selected.value = event.target.files?.[0] || null
  error.value = ''
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
    emit('uploaded', result)
  } catch (err) {
    error.value = err.message
  } finally {
    uploading.value = false
  }
}
</script>

<template>
  <form class="upload-form upload-drop" @submit.prevent="submit">
    <div class="upload-icon" aria-hidden="true">
      <UploadCloud :size="30" />
    </div>
    <label for="capture-file">
      <strong>{{ selected?.name || '选择抓包文件' }}</strong>
      <span>支持 .pcap / .pcapng / .cap</span>
    </label>
    <input id="capture-file" type="file" accept=".pcap,.pcapng,.cap" @change="onChange" />
    <button class="primary-button" type="submit" :disabled="uploading">
      {{ uploading ? '上传中' : '上传文件' }}
    </button>
    <span v-if="error" class="badge error">{{ error }}</span>
  </form>
</template>
