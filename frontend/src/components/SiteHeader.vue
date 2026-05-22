<script setup>
import { Menu, Moon, Network, Sun, X } from '@lucide/vue'

defineProps({
  dark: { type: Boolean, required: true },
  menuOpen: { type: Boolean, required: true },
  scrolled: { type: Boolean, required: true },
})

defineEmits(['toggle-dark', 'toggle-menu'])
</script>

<template>
  <header class="site-header" :class="{ 'is-scrolled': scrolled }">
    <div class="header-inner">
      <a class="brand" href="#top" aria-label="PCAP Analyzer home">
        <Network :size="24" aria-hidden="true" />
        <span>PCAP Analyzer</span>
      </a>

      <nav class="desktop-nav" aria-label="Primary navigation">
        <a href="#top">首页</a>
        <a href="#features">功能</a>
        <a href="#files">文件管理</a>
        <a href="#analysis">解析结果</a>
      </nav>

      <div class="header-actions">
        <button class="icon-button" type="button" aria-label="切换主题" @click="$emit('toggle-dark')">
          <Sun v-if="dark" :size="20" aria-hidden="true" />
          <Moon v-else :size="20" aria-hidden="true" />
        </button>
        <a class="nav-cta" href="#files">开始分析</a>
        <button
          class="icon-button menu-button"
          type="button"
          aria-label="切换菜单"
          :aria-expanded="menuOpen"
          @click="$emit('toggle-menu')"
        >
          <X v-if="menuOpen" :size="22" aria-hidden="true" />
          <Menu v-else :size="22" aria-hidden="true" />
        </button>
      </div>
    </div>

    <div class="mobile-panel" :class="{ open: menuOpen }">
      <a href="#top">首页</a>
      <a href="#features">功能</a>
      <a href="#files">文件管理</a>
      <a href="#analysis">解析结果</a>
      <a class="mobile-cta" href="#files">开始分析</a>
    </div>
  </header>
</template>
