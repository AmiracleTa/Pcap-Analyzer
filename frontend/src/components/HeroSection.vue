<script setup>
import { ArrowDown, UploadCloud } from '@lucide/vue'

defineEmits(['select-sample-section'])
</script>

<template>
  <section id="top" class="hero">
    <div class="hero-field" aria-hidden="true">
      <div class="hero-grid"></div>
      <div class="hero-sweep"></div>
    </div>

    <div class="hero-content hero-layout" data-reveal>
      <div class="hero-copy-block">
        <div class="hero-subtitle">
          <p class="eyebrow">NETWORK PACKET ANALYSIS</p>
        </div>
        <div class="hero-title">
          <h1>
            <span>PCAP</span>
            <strong>Analyzer</strong>
          </h1>
        </div>
        <p class="hero-copy">
          在线 PCAP 数据包分析平台，支持深度协议解析与流量可视化
        </p>
        <div class="hero-actions">
          <button class="primary-button" type="button" @click="$emit('select-sample-section')">
            <UploadCloud :size="18" aria-hidden="true" />
            上传抓包文件
          </button>
          <a class="secondary-button" href="#features">
            <ArrowDown :size="18" aria-hidden="true" />
            查看功能特性
          </a>
        </div>
      </div>
    </div>
  </section>
</template>
