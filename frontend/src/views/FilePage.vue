<script setup>
import SectionHeading from '../components/SectionHeading.vue'
import FileTable from '../components/FileTable.vue'
import FileUpload from '../components/FileUpload.vue'

defineProps({
  files: { type: Array, default: () => [] },
  health: { type: String, required: true },
  analysisProgress: { type: Object, default: null },
  analyzingFileId: { type: Number, default: null },
  deletingFileId: { type: Number, default: null },
})

defineEmits(['uploaded', 'select-file', 'analyze', 'delete'])
</script>

<template>
  <div class="workspace">
    <SectionHeading
      eyebrow="UPLOAD & ANALYSIS"
      title="上传与管理数据包"
      text="上传抓包文件后进行解析，并在下方管理已上传的数据包。"
    />

    <div class="content-grid file-section-grid">
      <section class="stack-panel upload-panel" data-reveal>
        <div class="section-header upload-section-header">
          <div>
            <h3>上传抓包</h3>
            <p class="muted">选择抓包文件后写入后端，并进入待分析状态。</p>
          </div>
          <span class="backend-inline-status" :class="{ ok: health === 'ok', error: health === 'error' }">
            <span aria-hidden="true"></span>
            后端 · {{ health === 'ok' ? '已连接' : health === 'error' ? '未连接' : '检查中' }}
          </span>
        </div>
        <FileUpload @uploaded="$emit('uploaded', $event)" />
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
          :analyzing-file-id="analyzingFileId"
          :deleting-file-id="deletingFileId"
          @select="$emit('select-file', $event)"
          @analyze="$emit('analyze', $event)"
          @delete="$emit('delete', $event)"
        />
      </section>

      <section v-if="analysisProgress" id="analysis-progress" class="stack-panel analysis-progress-panel">
        <div class="section-header">
          <div>
            <h3>解析进度</h3>
            <p class="muted">{{ analysisProgress.message }}</p>
          </div>
          <span class="badge">{{ analysisProgress.percent }}%</span>
        </div>
        <div class="progress-track" aria-label="解析进度">
          <span class="progress-fill" :style="{ width: `${analysisProgress.percent}%` }"></span>
        </div>
        <div class="progress-meta">
          <span>已解析 {{ analysisProgress.processedPackets }} / {{ analysisProgress.totalPackets }} 个数据包</span>
          <span>{{ analysisProgress.phase }}</span>
        </div>
      </section>
    </div>
  </div>
</template>
