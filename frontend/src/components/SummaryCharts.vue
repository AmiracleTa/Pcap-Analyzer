<script setup>
import * as echarts from 'echarts'
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'

const props = defineProps({
  summary: {
    type: Object,
    default: () => ({
      protocols: {},
      trafficTrend: [],
      ips: {},
      ports: {},
      lengthDistribution: [],
    }),
  },
})

const protocolRef = ref(null)
const trendRef = ref(null)
const lengthRef = ref(null)
let protocolChart = null
let trendChart = null
let lengthChart = null

function protocolData() {
  const entries = Object.entries(props.summary?.protocols || {})
  return entries.map(([name, value]) => ({ name, value }))
}

function trendData() {
  return props.summary?.trafficTrend || []
}

function lengthData() {
  return props.summary?.lengthDistribution || []
}

function renderCharts() {
  if (!protocolChart || !trendChart || !lengthChart) {
    return
  }

  const trend = trendData()
  const lengths = lengthData()
  const color = ['#1d8bff', '#06b6d4', '#10b981', '#8b5cf6', '#f59e0b', '#f43f5e']
  protocolChart.setOption({
    color,
    title: {
      text: protocolData().length === 0 ? '暂无数据' : '',
      left: 'center',
      top: 'middle',
      textStyle: { color: '#667085', fontSize: 14, fontWeight: 400 },
    },
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}',
    },
    series: [
      {
        name: '协议',
        type: 'pie',
        radius: ['32%', '56%'],
        center: ['50%', '56%'],
        avoidLabelOverlap: true,
        label: {
          show: true,
          position: 'outer',
          alignTo: 'edge',
          edgeDistance: 8,
          formatter: '{b}',
          overflow: 'none',
          width: 120,
        },
        labelLine: {
          show: true,
          length: 20,
          length2: 24,
        },
        data: protocolData(),
      },
    ],
  })
  trendChart.setOption({
    color,
    title: {
      text: trend.length === 0 ? '暂无数据' : '',
      left: 'center',
      top: 'middle',
      textStyle: { color: '#667085', fontSize: 14, fontWeight: 400 },
    },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: trend.map((item) => item.time || '') },
    yAxis: { type: 'value' },
    series: [
      {
        name: '流量',
        type: 'line',
        smooth: true,
        data: trend.map((item) => item.value || 0),
      },
    ],
  })
  lengthChart.setOption({
    color,
    title: {
      text: lengths.length === 0 ? '暂无数据' : '',
      left: 'center',
      top: 'middle',
      textStyle: { color: '#667085', fontSize: 14, fontWeight: 400 },
    },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: lengths.map((item) => item.range || '') },
    yAxis: { type: 'value' },
    series: [
      {
        name: '包数量',
        type: 'bar',
        data: lengths.map((item) => item.value || 0),
      },
    ],
  })
}

function resizeCharts() {
  protocolChart?.resize()
  trendChart?.resize()
  lengthChart?.resize()
}

onMounted(async () => {
  await nextTick()
  protocolChart = echarts.init(protocolRef.value)
  trendChart = echarts.init(trendRef.value)
  lengthChart = echarts.init(lengthRef.value)
  renderCharts()
  window.addEventListener('resize', resizeCharts)
})

watch(() => props.summary, renderCharts, { deep: true })

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  protocolChart?.dispose()
  trendChart?.dispose()
  lengthChart?.dispose()
})
</script>

<template>
  <div class="dashboard-grid charts-grid">
    <section class="stack-panel chart-panel" data-reveal>
      <h3>协议统计</h3>
      <div ref="protocolRef" class="chart"></div>
    </section>
    <section class="stack-panel chart-panel" data-reveal>
      <h3>包长度分布</h3>
      <div ref="lengthRef" class="chart"></div>
    </section>
  </div>
  <section class="stack-panel chart-panel trend-panel" data-reveal>
    <h3>流量趋势</h3>
    <div ref="trendRef" class="chart trend-chart"></div>
  </section>
</template>
