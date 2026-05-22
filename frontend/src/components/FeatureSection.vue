<script setup>
import { BarChart3, Cpu, UploadCloud } from '@lucide/vue'

const features = [
  {
    title: '上传抓包',
    text: '支持 pcap、pcapng、cap 文件上传，并保存到后端文件目录。',
    icon: UploadCloud,
  },
  {
    title: 'tshark 解析',
    text: '调用 tshark 提取协议、端点、端口、长度、DNS 和 HTTP 特征。',
    icon: Cpu,
  },
  {
    title: '结果洞察',
    text: '用图表、排行、筛选表格和导出文件呈现解析结果。',
    icon: BarChart3,
  },
]

const techStack = [
  { name: 'Vue + Vite', description: '前端技术栈' },
  { name: 'Spring Boot', description: '后端框架' },
  { name: 'MySQL', description: '数据存储' },
  { name: 'tshark', description: '解析引擎' },
]
</script>

<template>
  <div class="feature-showcase" data-reveal>
    <div class="section-heading">
      <p class="eyebrow">FEATURES</p>
      <h2>查看协议分布、流量趋势和数据包明细</h2>
      <p>围绕上传、解析、统计和导出组织抓包分析流程。</p>
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

    <div id="tech-stack" class="tech-stack-grid" aria-label="技术栈">
      <article v-for="item in techStack" :key="item.name" class="tech-card">
        <strong>{{ item.name }}</strong>
        <span>{{ item.description }}</span>
      </article>
    </div>
  </div>
</template>
