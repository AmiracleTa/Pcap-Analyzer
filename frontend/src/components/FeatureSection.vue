<script setup>
import { BarChart3, Cloud, Cpu, Download, Filter, Monitor, Server, Terminal } from '@lucide/vue'

const features = [
  {
    title: 'tshark 深度解析',
    text: '提取协议、端点、端口、长度、DNS、HTTP 和报文详情。',
    icon: Cpu,
  },
  {
    title: '流量可视化统计',
    text: '展示协议分布、流量趋势、包长度分布和端点排行。',
    icon: BarChart3,
  },
  {
    title: '多条件数据包筛选',
    text: '按协议、源宿地址、端口、时间和包长度快速定位数据包。',
    icon: Filter,
  },
  {
    title: '文件云端存储',
    text: '上传后的抓包文件由后端统一保存和管理。',
    icon: Cloud,
  },
  {
    title: 'CSV JSON 导出',
    text: '解析结果支持 CSV 和 JSON 格式导出，便于归档和复用。',
    icon: Download,
  },
]

const architecture = [
  { name: '前端', items: ['Vue', 'Vite', 'ECharts'], icon: Monitor },
  { name: '后端', items: ['Spring Boot', 'Spring Web', 'MySQL'], icon: Server },
  { name: '解析引擎', items: ['tshark', 'capinfos'], icon: Terminal },
]
</script>

<template>
  <div class="feature-showcase" data-reveal>
    <div class="section-heading">
      <p class="eyebrow">FEATURES</p>
      <h2>抓包解析、流量统计和结果导出</h2>
      <p>围绕协议解析、数据筛选、文件管理和可视化分析组织平台能力。</p>
    </div>

    <div class="feature-card-grid">
      <article v-for="feature in features" :key="feature.title" class="feature-card">
        <span class="feature-icon">
          <component :is="feature.icon" :size="24" aria-hidden="true" />
        </span>
        <h3>{{ feature.title }}</h3>
        <p>{{ feature.text }}</p>
      </article>
    </div>

    <div class="architecture-section">
      <div class="architecture-heading">
        <span>系统架构</span>
        <p>浏览器交互、后端服务和命令行解析引擎协同完成抓包分析链路。</p>
      </div>
      <div id="tech-stack" class="architecture-grid" aria-label="系统架构">
        <article v-for="(item, index) in architecture" :key="item.name" class="architecture-card">
          <div class="architecture-card-head">
            <span class="architecture-index">{{ String(index + 1).padStart(2, '0') }}</span>
            <span class="architecture-icon">
              <component :is="item.icon" :size="20" aria-hidden="true" />
            </span>
          </div>
          <strong>{{ item.name }}</strong>
          <div class="architecture-tags">
            <span v-for="tech in item.items" :key="tech">{{ tech }}</span>
          </div>
        </article>
      </div>
    </div>
  </div>
</template>
