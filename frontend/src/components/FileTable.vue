<script setup>
import { BarChart3, Download, Eye, Trash2 } from '@lucide/vue'

defineProps({
  files: {
    type: Array,
    default: () => [],
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
</script>

<template>
  <div v-if="files.length === 0" class="empty compact">暂无上传文件</div>
  <div v-else class="table-wrap elevated-table">
    <table>
      <thead>
        <tr>
          <th>ID</th>
          <th>文件名</th>
          <th>大小</th>
          <th>类型</th>
          <th>上传时间</th>
          <th>状态</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="file in files" :key="file.id">
          <td>{{ file.id }}</td>
          <td>{{ file.originalName }}</td>
          <td>{{ formatSize(file.fileSize) }}</td>
          <td>{{ file.fileType }}</td>
          <td>{{ file.uploadTime || '-' }}</td>
          <td><span class="badge">{{ file.status }}</span></td>
          <td>
            <div class="actions">
              <button class="ghost-button" type="button" @click="$emit('select', file)">
                <Eye :size="16" aria-hidden="true" />
                查看
              </button>
              <button class="primary-button small" type="button" @click="$emit('analyze', file)">
                <BarChart3 :size="16" aria-hidden="true" />
                分析
              </button>
              <button class="ghost-button" type="button" @click="downloadFile(file.id)">
                <Download :size="16" aria-hidden="true" />
                下载
              </button>
              <button class="danger-button" type="button" @click="$emit('delete', file)">
                <Trash2 :size="16" aria-hidden="true" />
                删除
              </button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
