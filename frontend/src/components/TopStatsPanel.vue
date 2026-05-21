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
</script>

<template>
  <div class="top-grid">
    <section v-for="panel in panels" :key="panel.key" class="stack-panel stats-panel" data-reveal>
      <h3>{{ panel.title }}</h3>
      <div v-if="(summary[panel.key] || []).length === 0" class="empty compact">暂无数据</div>
      <div v-else class="table-wrap elevated-table">
        <table class="stats-table">
          <thead>
            <tr>
              <th>名称</th>
              <th>次数</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in summary[panel.key]" :key="`${panel.key}-${item.name}`">
              <td>{{ item.name }}</td>
              <td>{{ item.value }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>
