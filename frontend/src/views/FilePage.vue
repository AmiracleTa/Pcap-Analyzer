<script setup>
import { computed } from 'vue'
import SectionHeading from '../components/SectionHeading.vue'
import FileTable from '../components/FileTable.vue'
import FileUpload from '../components/FileUpload.vue'

const props = defineProps({
  files: { type: Array, default: () => [] },
  health: { type: String, required: true },
  analysisProgress: { type: Object, default: null },
  analyzingFileId: { type: Number, default: null },
  deletingFileId: { type: Number, default: null },
})

defineEmits(['uploaded', 'select-file', 'analyze', 'delete'])

const progressStages = computed(() => {
  const progress = props.analysisProgress
  if (!progress) {
    return []
  }
  return [
    {
      label: '解析数据包',
      state: parseStageState(progress),
    },
    {
      label: '保存到云端',
      state: cloudSaveStageState(progress),
    },
    {
      label: '生成 AI 报告',
      state: aiStageState(progress),
    },
  ]
})

const cloudSavePhases = new Set(['database-save', 'summary-build'])

function parseStageState(progress) {
  if (progress.status === 'error') {
    return 'error'
  }
  if (cloudSavePhases.has(progress.phase) || progress.phase === 'ai-report' || progress.status === 'done') {
    return 'complete'
  }
  return 'active'
}

function cloudSaveStageState(progress) {
  if (progress.status === 'error') {
    return 'pending'
  }
  if (progress.status === 'done') {
    return progress.aiReportAvailable === false ? 'warning' : 'complete'
  }
  if (progress.phase === 'ai-report') {
    return 'complete'
  }
  return cloudSavePhases.has(progress.phase) ? 'active' : 'pending'
}

function aiStageState(progress) {
  if (progress.status === 'error') {
    return 'pending'
  }
  if (progress.status === 'done') {
    return progress.aiReportAvailable === false ? 'warning' : 'complete'
  }
  return progress.phase === 'ai-report' ? 'active' : 'pending'
}

function packetProgressText(progress) {
  const count = progress.packetCount ?? progress.processedPackets ?? 0
  const total = progress.totalPackets ?? 0
  if (progress.status === 'error') {
    return progress.message || '解析失败'
  }
  if (progress.status === 'done') {
    return `共解析 ${count} 个数据包`
  }
  if (total <= 0) {
    return '正在读取数据包总数'
  }
  return `已解析 ${progress.processedPackets ?? 0} / ${total} 个数据包`
}
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
          <div class="progress-title-block">
            <h3>解析进度</h3>
            <p class="muted">{{ analysisProgress.message }}</p>
          </div>
          <span class="badge">{{ analysisProgress.percent }}%</span>
        </div>
        <div class="progress-track" aria-label="解析进度">
          <span class="progress-fill" :style="{ width: `${analysisProgress.percent}%` }"></span>
        </div>
        <div class="progress-stage-list" aria-label="解析阶段">
          <span
            v-for="stage in progressStages"
            :key="stage.label"
            class="progress-stage"
            :class="`is-${stage.state}`"
          >
            <span class="progress-stage-dot" aria-hidden="true"></span>
            {{ stage.label }}
          </span>
        </div>
        <div class="progress-meta">
          <span>{{ packetProgressText(analysisProgress) }}</span>
        </div>
      </section>
    </div>
  </div>
</template>
