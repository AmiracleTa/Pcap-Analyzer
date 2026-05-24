<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { BarChart3, Download, Ellipsis, Eye, Trash2 } from '@lucide/vue'

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

const emit = defineEmits(['select', 'analyze', 'delete'])

const openMenu = ref({
  file: null,
  left: 0,
  top: 0,
})

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
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

function formatDateTime(value) {
  if (!value) {
    return '-'
  }
  const raw = String(value).trim()
  const match = raw.match(/^(\d{4}-\d{2}-\d{2})[T\s](\d{2}:\d{2})/)
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
  return isAnalyzed(file) ? '已分析' : '分析'
}

function deleteButtonText(file) {
  return props.deletingFileId === file.id ? '删除中' : '删除'
}

function formatPacketCount(file) {
  if (!isAnalyzed(file)) {
    return '-'
  }
  const value = file.packetCount
  if (value === null || value === undefined || value === '') {
    return '-'
  }
  return value
}

function isAnalyzed(file) {
  return file.status === 'analyzed'
}

function closeMoreMenu() {
  openMenu.value = { file: null, left: 0, top: 0 }
}

function toggleMoreMenu(file, event) {
  if (openMenu.value.file?.id === file.id) {
    closeMoreMenu()
    return
  }
  const rect = event.currentTarget.getBoundingClientRect()
  const menuWidth = 132
  const menuHeight = 88
  const left = Math.max(12, Math.min(rect.right - menuWidth, window.innerWidth - menuWidth - 12))
  const canOpenBelow = rect.bottom + menuHeight + 12 < window.innerHeight
  const top = canOpenBelow ? rect.bottom + 8 : Math.max(12, rect.top - menuHeight - 8)
  openMenu.value = { file, left, top }
}

function handleDownload(file) {
  downloadFile(file.id)
  closeMoreMenu()
}

function handleDelete(file) {
  emit('delete', file)
  closeMoreMenu()
}

function handleKeydown(event) {
  if (event.key === 'Escape') {
    closeMoreMenu()
  }
}

onMounted(() => {
  document.addEventListener('click', closeMoreMenu)
  document.addEventListener('keydown', handleKeydown)
  window.addEventListener('resize', closeMoreMenu)
  window.addEventListener('scroll', closeMoreMenu, true)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', closeMoreMenu)
  document.removeEventListener('keydown', handleKeydown)
  window.removeEventListener('resize', closeMoreMenu)
  window.removeEventListener('scroll', closeMoreMenu, true)
})
</script>

<template>
  <div v-if="props.files.length === 0" class="empty compact">暂无上传文件</div>
  <div v-else class="table-wrap elevated-table">
    <table>
      <thead>
        <tr>
          <th class="index-column">序号</th>
          <th class="file-name-column">文件名</th>
          <th class="size-column">大小</th>
          <th class="type-column">类型</th>
          <th class="packet-count-column">包数</th>
          <th class="upload-time-column">上传时间</th>
          <th class="actions-column">操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(file, index) in props.files" :key="file.id">
          <td class="index-cell">{{ index + 1 }}</td>
          <td class="file-name-cell" :title="file.originalName">
            <span class="file-name-text">{{ file.originalName }}</span>
          </td>
          <td class="size-cell">{{ formatSize(file.fileSize) }}</td>
          <td class="type-cell">
            <span class="file-type-pill">{{ file.fileType }}</span>
          </td>
          <td class="packet-count-cell">{{ formatPacketCount(file) }}</td>
          <td class="upload-time-cell">{{ formatDateTime(file.uploadTime) }}</td>
          <td class="actions-cell">
            <div class="actions">
              <button
                class="primary-button small"
                :class="{ 'analyzed-button': isAnalyzed(file) }"
                type="button"
                :disabled="props.analyzingFileId === file.id || isAnalyzed(file)"
                @click="emit('analyze', file)"
              >
                <BarChart3 :size="16" aria-hidden="true" />
                {{ analyzeButtonText(file) }}
              </button>
              <button class="ghost-button" type="button" @click="emit('select', file)">
                <Eye :size="16" aria-hidden="true" />
                查看
              </button>
              <button
                class="icon-more-button"
                type="button"
                aria-label="更多操作"
                aria-haspopup="menu"
                :aria-expanded="openMenu.file?.id === file.id"
                @click.stop="toggleMoreMenu(file, $event)"
              >
                <Ellipsis :size="18" aria-hidden="true" />
              </button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
    <Teleport to="body">
      <div
        v-if="openMenu.file"
        class="row-more-popover"
        role="menu"
        :style="{ left: `${openMenu.left}px`, top: `${openMenu.top}px` }"
        @click.stop
      >
        <button type="button" role="menuitem" @click="handleDownload(openMenu.file)">
          <Download :size="15" aria-hidden="true" />
          下载
        </button>
        <button
          class="danger-menu-item"
          type="button"
          role="menuitem"
          :disabled="props.deletingFileId === openMenu.file.id"
          @click="handleDelete(openMenu.file)"
        >
          <Trash2 :size="15" aria-hidden="true" />
          {{ deleteButtonText(openMenu.file) }}
        </button>
      </div>
    </Teleport>
  </div>
</template>
