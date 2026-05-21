<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  packets: {
    type: Array,
    default: () => [],
  },
})

defineEmits(['select'])

const keyword = ref('')
const pageSize = ref(25)
const currentPage = ref(1)
const pageSizeOptions = [25, 50, 100]

const filteredPackets = computed(() => {
  const query = keyword.value.trim().toLowerCase()
  if (!query) {
    return props.packets
  }
  return props.packets.filter((packet) => {
    const values = [
      packet.sourceIp,
      packet.destinationIp,
      packet.sourcePort,
      packet.destinationPort,
      packet.protocol,
      packet.info,
    ]
    return values.some((value) => String(value ?? '').toLowerCase().includes(query))
  })
})

const totalPages = computed(() => Math.max(1, Math.ceil(filteredPackets.value.length / pageSize.value)))

const pagedPackets = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredPackets.value.slice(start, start + pageSize.value)
})

watch(keyword, () => {
  currentPage.value = 1
})

watch(pageSize, () => {
  currentPage.value = 1
})

watch(
  () => props.packets,
  () => {
    currentPage.value = 1
  },
  { deep: true },
)

watch(filteredPackets, () => {
  if (currentPage.value > totalPages.value) {
    currentPage.value = totalPages.value
  }
})
</script>

<template>
  <div v-if="packets.length === 0" class="empty">暂无数据包记录</div>
  <div v-else>
    <div class="toolbar search-bar packet-toolbar">
      <input
        v-model="keyword"
        type="search"
        placeholder="搜索 IP、端口、协议或摘要"
      />
      <span class="badge">{{ filteredPackets.length }} / {{ packets.length }}</span>
    </div>
    <div v-if="filteredPackets.length === 0" class="empty">没有匹配的数据包</div>
    <div v-else class="table-wrap elevated-table">
      <table>
        <thead>
          <tr>
            <th>序号</th>
            <th>时间</th>
            <th>源地址</th>
            <th>目的地址</th>
            <th>源端口</th>
            <th>目的端口</th>
            <th>协议</th>
            <th>长度</th>
            <th>摘要</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="packet in pagedPackets" :key="packet.id" @click="$emit('select', packet)">
            <td>{{ packet.packetNo }}</td>
            <td>{{ packet.timestampText }}</td>
            <td>{{ packet.sourceIp }}</td>
            <td>{{ packet.destinationIp }}</td>
            <td>{{ packet.sourcePort ?? '-' }}</td>
            <td>{{ packet.destinationPort ?? '-' }}</td>
            <td>{{ packet.protocol }}</td>
            <td>{{ packet.length }}</td>
            <td>{{ packet.info }}</td>
          </tr>
        </tbody>
      </table>
    </div>
    <div class="pager">
      <label>
        每页
        <select v-model.number="pageSize">
          <option v-for="option in pageSizeOptions" :key="option" :value="option">{{ option }}</option>
        </select>
      </label>
      <button class="ghost-button" type="button" :disabled="currentPage === 1" @click="currentPage -= 1">上一页</button>
      <span class="badge">{{ currentPage }} / {{ totalPages }}</span>
      <button class="ghost-button" type="button" :disabled="currentPage === totalPages" @click="currentPage += 1">下一页</button>
    </div>
  </div>
</template>
