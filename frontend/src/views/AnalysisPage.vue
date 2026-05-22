<script setup>
import { computed, ref, watch } from 'vue'
import { Download, FileJson, LoaderCircle } from '@lucide/vue'
import { exportCsv, exportJson, getSummary, listPackets } from '../api/files'
import PacketTable from '../components/PacketTable.vue'
import ProtocolFeaturePanel from '../components/ProtocolFeaturePanel.vue'
import SectionHeading from '../components/SectionHeading.vue'
import SummaryCharts from '../components/SummaryCharts.vue'
import TopStatsPanel from '../components/TopStatsPanel.vue'

const props = defineProps({
  selectedFile: {
    type: Object,
    default: null,
  },
})

const summary = ref({
  protocols: {},
  trafficTrend: [],
  ips: {},
  ports: {},
  lengthDistribution: [],
  sourceIpTop: [],
  destinationIpTop: [],
  sourcePortTop: [],
  destinationPortTop: [],
  dnsRecords: [],
  httpRecords: [],
})
const packets = ref([])
const selectedPacket = ref(null)
const loading = ref(false)
const error = ref('')

const fileTitle = computed(() => props.selectedFile?.originalName || '未选择文件')

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

async function loadAnalysis(file) {
  selectedPacket.value = null
  if (!file) {
    packets.value = []
    return
  }

  loading.value = true
  error.value = ''
  try {
    const [summaryResult, packetResult] = await Promise.all([
      getSummary(file.id),
      listPackets(file.id),
    ])
    summary.value = summaryResult
    packets.value = packetResult
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
}

watch(() => props.selectedFile, loadAnalysis, { immediate: true })
</script>

<template>
  <div class="workspace">
    <SectionHeading
      eyebrow="Packet Analysis"
      title="解析结果与协议洞察"
      text="查看协议分布、流量趋势、包长度、DNS/HTTP 特征和原始数据包详情。"
    />

    <div v-if="!selectedFile" class="empty hero-empty">请选择一个已上传文件</div>
    <div v-else class="analysis-workbench">
      <div class="stack-panel overview-panel" data-reveal>
        <header class="section-header">
          <div>
            <h3>{{ fileTitle }}</h3>
            <p class="muted">真实 tshark 解析数据</p>
          </div>
          <div class="actions">
            <button class="secondary-button" type="button" @click="exportCsv(selectedFile.id)">
              <Download :size="17" aria-hidden="true" />
              导出 CSV
            </button>
            <button class="secondary-button" type="button" @click="exportJson(selectedFile.id)">
              <FileJson :size="17" aria-hidden="true" />
              导出 JSON
            </button>
            <span class="badge">
              <LoaderCircle v-if="loading" class="spin" :size="15" aria-hidden="true" />
              {{ loading ? '加载中' : '真实解析数据' }}
            </span>
          </div>
        </header>

        <p v-if="error" class="badge error">{{ error }}</p>
        <dl class="file-overview">
          <div>
            <dt>文件名</dt>
            <dd>{{ selectedFile.originalName }}</dd>
          </div>
          <div>
            <dt>文件大小</dt>
            <dd>{{ formatSize(selectedFile.fileSize) }}</dd>
          </div>
          <div>
            <dt>数据包总数</dt>
            <dd>{{ selectedFile.packetCount ?? 0 }}</dd>
          </div>
          <div>
            <dt>状态</dt>
            <dd>{{ selectedFile.status }}</dd>
          </div>
          <div>
            <dt>开始时间</dt>
            <dd>{{ summary.startTimeText || '-' }}</dd>
          </div>
          <div>
            <dt>结束时间</dt>
            <dd>{{ summary.endTimeText || '-' }}</dd>
          </div>
        </dl>
      </div>

      <div class="section-spacer"></div>
      <SummaryCharts :summary="summary" />
      <div class="section-spacer"></div>
      <TopStatsPanel :summary="summary" />
      <div class="section-spacer"></div>
      <ProtocolFeaturePanel :summary="summary" />
      <div class="section-spacer"></div>
      <PacketTable :packets="packets" @select="selectedPacket = $event" />
      <div class="section-spacer"></div>
      <section class="stack-panel detail-panel" data-reveal>
        <h3>选中数据包详情</h3>
        <pre class="detail-json">{{
          selectedPacket ? JSON.stringify(selectedPacket, null, 2) : '点击上方数据包行查看完整 JSON 详情'
        }}</pre>
      </section>
    </div>
  </div>
</template>
