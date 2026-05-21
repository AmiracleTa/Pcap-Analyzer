<script setup>
import { ArrowDown, Database, FileSearch, ShieldCheck, UploadCloud, Zap } from '@lucide/vue'

const props = defineProps({
  health: { type: String, required: true },
  fileCount: { type: Number, required: true },
  selectedFile: { type: Object, default: null },
})

defineEmits(['select-sample-section'])

function healthText() {
  if (props.health === 'ok') {
    return '后端已连接'
  }
  if (props.health === 'error') {
    return '后端未连接'
  }
  return '正在检查后端'
}
</script>

<template>
  <section id="top" class="hero">
    <div class="hero-field" aria-hidden="true">
      <div class="hero-grid"></div>
      <div class="hero-sweep"></div>
    </div>

    <div class="hero-content hero-layout" data-reveal>
      <div class="hero-copy-block">
        <p class="eyebrow">Network Packet Insight</p>
        <h1>
          <span>PCAP</span>
          <strong>Analyzer</strong>
        </h1>
        <p class="hero-copy">
          上传 pcap/pcapng 抓包文件，调用 tshark 完成解析，并用图表、表格和协议特征展示网络通信过程。
        </p>
        <div class="hero-actions">
          <button class="primary-button" type="button" @click="$emit('select-sample-section')">
            <UploadCloud :size="18" aria-hidden="true" />
            上传抓包文件
          </button>
          <a class="secondary-button" href="#analysis">
            <ArrowDown :size="18" aria-hidden="true" />
            查看分析结果
          </a>
        </div>
        <div class="metric-strip" aria-label="Project metrics">
          <div class="metric">
            <strong>Spring Boot</strong>
            <span>后端服务</span>
          </div>
          <div class="metric">
            <strong>tshark</strong>
            <span>解析引擎</span>
          </div>
          <div class="metric">
            <strong>{{ fileCount }}</strong>
            <span>已上传文件</span>
          </div>
          <div class="metric">
            <strong>MySQL</strong>
            <span>数据存储</span>
          </div>
        </div>
      </div>

      <aside class="terminal-panel hero-terminal" aria-label="Analyzer runtime">
        <div class="traffic-lights" aria-hidden="true">
          <span></span>
          <span></span>
          <span></span>
        </div>
        <div class="terminal-status">
          <ShieldCheck :size="18" aria-hidden="true" />
          <span>{{ healthText() }}</span>
        </div>
        <pre><code>$ tshark -r sample.pcapng -T fields
ok packets extracted
ok summary generated
ok charts ready</code></pre>
        <div class="hero-terminal-grid">
          <div>
            <FileSearch :size="20" aria-hidden="true" />
            <span>{{ selectedFile?.originalName || '等待选择文件' }}</span>
          </div>
          <div>
            <Database :size="20" aria-hidden="true" />
            <span>{{ selectedFile?.packetCount ?? 0 }} packets</span>
          </div>
          <div>
            <Zap :size="20" aria-hidden="true" />
            <span>{{ selectedFile?.status || 'uploaded/analyzed' }}</span>
          </div>
        </div>
      </aside>
    </div>
  </section>
</template>
