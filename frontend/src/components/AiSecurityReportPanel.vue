<script setup>
import { computed } from 'vue'
import { BrainCircuit, CircleAlert } from '@lucide/vue'

const props = defineProps({
  report: { type: Object, default: null },
  error: { type: String, default: '' },
})

const riskLevelText = {
  low: '低风险',
  medium: '中风险',
  high: '高风险',
  critical: '严重风险',
  unknown: '未知',
}

const categoryText = {
  protocol_ratio_anomaly: '协议占比异常',
  excessive_ip_communication: 'IP 通信过多',
  unusual_port: '非常见端口',
  weak_tls: '弱 TLS 版本',
  cleartext_http: '明文通信',
  encrypted_quic: 'QUIC 加密流量',
  management_protocol: '管理协议通信',
  traffic_pattern: '流量时间分布异常',
  other: '其他风险',
}

const noReportMessage = '暂无安全报告，请先分析该文件。'

const isEmptyReport = computed(() => !props.report || props.report.message === noReportMessage)
const isUnavailableReport = computed(() => props.report && !props.report.available && !isEmptyReport.value)
const findings = computed(() => props.report?.findings || [])
const normalObservations = computed(() => props.report?.normalObservations || [])
const recommendations = computed(() => props.report?.recommendations || [])
const riskText = computed(() => riskLevelText[props.report?.riskLevel] || riskLevelText.unknown)

function categoryName(value) {
  return categoryText[value] || categoryText.other
}

function severityText(value) {
  return riskLevelText[value] || value || '未知'
}

function evidenceSamples(evidence) {
  return (evidence || [])
    .map((item) => {
      const parts = []
      if (item?.packetNo) {
        parts.push(`序号 ${item.packetNo}`)
      }
      if (item?.time) {
        parts.push(item.time)
      }
      const source = endpointText(item?.sourceIp, item?.sourcePort)
      const destination = endpointText(item?.destinationIp, item?.destinationPort)
      if (source || destination) {
        parts.push(`${source || '未知源'} -> ${destination || '未知目的'}`)
      }
      if (item?.protocol) {
        parts.push(item.protocol)
      }
      if (item?.length) {
        parts.push(`长度 ${item.length}`)
      }
      if (item?.packetCount) {
        parts.push(`${item.packetCount} 包`)
      }
      if (item?.byteCount) {
        parts.push(`${item.byteCount} 字节`)
      }
      if (item?.packetPercent) {
        parts.push(`包占比 ${item.packetPercent}%`)
      }
      if (item?.bytePercent) {
        parts.push(`字节占比 ${item.bytePercent}%`)
      }
      if (item?.firstTime || item?.lastTime) {
        parts.push(`${item.firstTime || '未知开始'} 至 ${item.lastTime || '未知结束'}`)
      }
      return parts.join('  ')
    })
    .filter((sample) => sample.trim() !== '')
}

function hasEvidenceSamples(evidence) {
  return evidenceSamples(evidence).length > 0
}

function endpointText(ip, port) {
  if (!ip && !port) {
    return ''
  }
  return port ? `${ip || '未知 IP'}:${port}` : ip
}
</script>

<template>
  <section class="stack-panel security-report-panel" data-reveal>
    <div class="security-report-head">
      <div class="security-title-group">
        <span class="security-icon">
          <BrainCircuit :size="20" aria-hidden="true" />
        </span>
        <div>
          <h3>AI 安全报告</h3>
          <p>基于部分数据包、整体流量趋势和协议占比生成。</p>
        </div>
      </div>

      <div v-if="!isEmptyReport && !isUnavailableReport" class="security-score">
        <span class="security-score-label">风险分数</span>
        <div class="security-score-value">
          <strong>{{ report.riskScore ?? 0 }}</strong>
          <span class="security-risk-pill" :data-risk="report.riskLevel || 'unknown'">{{ riskText }}</span>
        </div>
      </div>
    </div>

    <p v-if="error" class="badge error security-message">
      <CircleAlert :size="15" aria-hidden="true" />
      {{ error }}
    </p>

    <div v-else-if="isEmptyReport" class="empty compact security-empty">
      暂无安全报告，请先在文件管理中点击分析。
    </div>

    <div v-else-if="isUnavailableReport" class="security-report-grid">
      <div class="security-summary-block">
        <span>报告状态</span>
        <p>{{ report.message || 'AI 安全报告暂不可用。' }}</p>
      </div>

      <div v-if="recommendations.length" class="security-block">
        <h4>处理建议</h4>
        <ul class="security-recommendation-list">
          <li v-for="item in recommendations" :key="item">
            <span>{{ item }}</span>
          </li>
        </ul>
      </div>
    </div>

    <div v-else class="security-report-grid">
      <div class="security-summary-block">
        <span>安全摘要</span>
        <p>{{ report.summary || report.message || '暂无摘要。' }}</p>
      </div>

      <div class="security-block">
        <h4>主要风险点</h4>
        <div v-if="findings.length" class="security-finding-list">
          <article v-for="(finding, index) in findings" :key="`${finding.title || 'finding'}-${index}`" class="security-finding-card">
            <div class="security-finding-head">
              <div>
                <span>{{ categoryName(finding.category) }}</span>
                <h5>{{ finding.title || '未命名风险' }}</h5>
              </div>
              <em>{{ severityText(finding.severity) }}</em>
            </div>
            <p>{{ finding.explanation || '暂无解释。' }}</p>
            <div v-if="hasEvidenceSamples(finding.evidence)" class="security-evidence-list" aria-label="证据样本列表">
              <span v-for="(sample, evidenceIndex) in evidenceSamples(finding.evidence)" :key="`${sample}-${evidenceIndex}`">{{ sample }}</span>
            </div>
            <p class="security-recommendation">{{ finding.recommendation || '暂无单项处置建议。' }}</p>
          </article>
        </div>
        <p v-else class="muted security-muted">暂无 AI 发现。</p>
      </div>

      <div v-if="normalObservations.length" class="security-block">
        <h4>相对正常的现象</h4>
        <ul class="security-observation-list">
          <li v-for="item in normalObservations" :key="item">{{ item }}</li>
        </ul>
      </div>

      <div class="security-block">
        <h4>处理建议</h4>
        <ul v-if="recommendations.length" class="security-recommendation-list">
          <li v-for="item in recommendations" :key="item">
            <span>{{ item }}</span>
          </li>
        </ul>
        <p v-else class="muted security-muted">暂无处理建议。</p>
      </div>
    </div>
  </section>
</template>
