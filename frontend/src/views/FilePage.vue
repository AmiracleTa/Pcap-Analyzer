<script setup>
import SectionHeading from '../components/SectionHeading.vue'
import FileTable from '../components/FileTable.vue'
import FileUpload from '../components/FileUpload.vue'

defineProps({
  files: { type: Array, default: () => [] },
  health: { type: String, required: true },
  message: { type: String, default: '' },
})

defineEmits(['refresh', 'uploaded', 'select-file', 'analyze', 'delete'])
</script>

<template>
  <div class="workspace">
    <SectionHeading
      eyebrow="UPLOAD"
      title="上传与管理数据包"
      text="上传抓包文件后进行解析，并在下方管理已上传的数据包。"
    />

    <div class="content-grid file-section-grid">
      <section class="stack-panel upload-panel" data-reveal>
        <div class="section-header">
          <div>
            <h3>上传抓包</h3>
            <p class="muted">选择抓包文件后写入后端，并进入待分析状态。</p>
          </div>
          <span class="badge" :class="{ ok: health === 'ok', error: health === 'error' }">
            后端：{{ health === 'ok' ? '已连接' : health === 'error' ? '未连接' : '检查中' }}
          </span>
        </div>
        <FileUpload @uploaded="$emit('uploaded', $event)" />
        <div class="status-row upload-actions">
          <button class="secondary-button" type="button" @click="$emit('refresh')">刷新</button>
          <span v-if="message" class="badge ok">{{ message }}</span>
        </div>
      </section>

      <section class="stack-panel file-list-panel" data-reveal>
        <div class="section-header">
          <div>
            <h3>文件列表</h3>
            <p class="muted">查看、分析、下载或删除已上传的抓包文件。</p>
          </div>
          <span class="badge">{{ files.length }} 个文件</span>
        </div>
        <FileTable
          :files="files"
          @select="$emit('select-file', $event)"
          @analyze="$emit('analyze', $event)"
          @delete="$emit('delete', $event)"
        />
      </section>
    </div>
  </div>
</template>
