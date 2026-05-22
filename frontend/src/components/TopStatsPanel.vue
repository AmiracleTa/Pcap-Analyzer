<script setup>
defineProps({
  summary: {
    type: Object,
    required: true,
  },
})

const panels = [
  { title: '源 IP Top10', key: 'sourceIpTop' },
  { title: '目的 IP Top10', key: 'destinationIpTop' },
  { title: '源端口 Top10', key: 'sourcePortTop' },
  { title: '目的端口 Top10', key: 'destinationPortTop' },
]

function maxValue(items) {
  return Math.max(...items.map((item) => Number(item.value) || 0), 0)
}

function rankPercent(item, items) {
  const max = maxValue(items)
  if (max <= 0) {
    return 0
  }
  return Math.round(((Number(item.value) || 0) / max) * 100)
}
</script>

<template>
  <div class="top-grid">
    <section v-for="panel in panels" :key="panel.key" class="stack-panel stats-panel" data-reveal>
      <h3>{{ panel.title }}</h3>
      <div v-if="(summary[panel.key] || []).length === 0" class="empty compact">暂无数据</div>
      <div v-else class="rank-list">
        <div v-for="item in summary[panel.key]" :key="`${panel.key}-${item.name}`" class="rank-row">
          <span class="rank-bar" :style="{ width: `${rankPercent(item, summary[panel.key])}%` }"></span>
          <span class="rank-name">{{ item.name || '-' }}</span>
          <strong class="rank-value">{{ item.value }}</strong>
        </div>
      </div>
    </section>
  </div>
</template>
