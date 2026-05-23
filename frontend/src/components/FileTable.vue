<script setup>
import { BarChart3, Download, Eye, Trash2 } from '@lucide/vue'

const props = defineProps({
  files: {
    type: Array,
    default: () => [],
  },
  analyzingFileId: {
    type: Number,
    default: null,
  },
  deletingFileId: {
    type: Number,
    default: null,
  },
})

defineEmits(['select', 'analyze', 'delete'])

function downloadFile(id) {
  window.location.href = `/api/files/${id}/download`
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

function formatDateObject(date) {
  if (!(date instanceof Date) || Number.isNaN(date.getTime())) {
    return '-'
  }
  const pad = (value) => String(value).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

function formatDateTime(value) {
  if (!value) {
    return '-'
  }
  const raw = String(value).trim()
  const match = raw.match(/^(\d{4}-\d{2}-\d{2})[T\s](\d{2}:\d{2}:\d{2})/)
  if (match) {
    return `${match[1]} ${match[2]}`
  }
  const numeric = Number(raw)
  if (Number.isFinite(numeric)) {
    return formatDateObject(new Date(numeric * 1000))
  }
  return raw.replace('T', ' ').replace(/\.\d+$/, '')
}

function analyzeButtonText(file) {
  if (props.analyzingFileId === file.id) {
    return '分析中'
  }
  return file.status === 'analyzed' ? '已分析' : '分析'
}

function deleteButtonText(file) {
  return props.deletingFileId === file.id ? '删除中' : '删除'
}
</script>

<template>
  <div v-if="props.files.length === 0" class="empty compact">暂无上传文件</div>
  <div v-else class="table-wrap elevated-table">
    <table>
      <thead>
        <tr>
          <th>序号</th>
          <th>文件名</th>
          <th>大小</th>
          <th>类型</th>
          <th>上传时间</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(file, index) in props.files" :key="file.id">
          <td>{{ index + 1 }}</td>
          <td>{{ file.originalName }}</td>
          <td>{{ formatSize(file.fileSize) }}</td>
          <td>{{ file.fileType }}</td>
          <td>{{ formatDateTime(file.uploadTime) }}</td>
          <td>
            <div class="actions">
              <button class="ghost-button" type="button" @click="$emit('select', file)">
                <Eye :size="16" aria-hidden="true" />
                查看
              </button>
              <button
                class="primary-button small"
                :class="{ 'analyzed-button': file.status === 'analyzed' }"
                type="button"
                :disabled="props.analyzingFileId === file.id || file.status === 'analyzed'"
                @click="$emit('analyze', file)"
              >
                <BarChart3 :size="16" aria-hidden="true" />
                {{ analyzeButtonText(file) }}
              </button>
              <button class="ghost-button" type="button" @click="downloadFile(file.id)">
                <Download :size="16" aria-hidden="true" />
                下载
              </button>
              <button
                class="danger-button"
                type="button"
                :disabled="props.deletingFileId === file.id"
                @click="$emit('delete', file)"
              >
                <Trash2 :size="16" aria-hidden="true" />
                {{ deleteButtonText(file) }}
              </button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
