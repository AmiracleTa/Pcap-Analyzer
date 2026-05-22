<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  packets: {
    type: Array,
    default: () => [],
  },
})

const emit = defineEmits(['select'])

const keyword = ref('')
const protocolFilter = ref('all')
const ipFilter = ref('')
const portFilter = ref('')
const minLength = ref('')
const maxLength = ref('')
const pageSize = ref(25)
const currentPage = ref(1)
const selectedPacketId = ref(null)
const pageSizeOptions = [25, 50, 100]

const protocolOptions = computed(() => {
  const protocols = new Set(props.packets.map((packet) => packet.protocol).filter(Boolean))
  return ['all', ...Array.from(protocols).sort()]
})

const filteredPackets = computed(() => {
  const query = keyword.value.trim().toLowerCase()
  const ipQuery = ipFilter.value.trim().toLowerCase()
  const portQuery = portFilter.value.trim()
  const min = Number(minLength.value)
  const max = Number(maxLength.value)
  const hasMin = minLength.value !== '' && Number.isFinite(min)
  const hasMax = maxLength.value !== '' && Number.isFinite(max)

  return props.packets.filter((packet) => {
    if (query) {
      const values = [
        packet.sourceIp,
        packet.destinationIp,
        packet.sourcePort,
        packet.destinationPort,
        packet.protocol,
        packet.info,
      ]
      if (!values.some((value) => String(value ?? '').toLowerCase().includes(query))) {
        return false
      }
    }
    if (protocolFilter.value !== 'all' && packet.protocol !== protocolFilter.value) {
      return false
    }
    if (ipQuery) {
      const sourceIp = String(packet.sourceIp ?? '').toLowerCase()
      const destinationIp = String(packet.destinationIp ?? '').toLowerCase()
      if (!sourceIp.includes(ipQuery) && !destinationIp.includes(ipQuery)) {
        return false
      }
    }
    if (portQuery) {
      const sourcePort = String(packet.sourcePort ?? '')
      const destinationPort = String(packet.destinationPort ?? '')
      if (sourcePort !== portQuery && destinationPort !== portQuery) {
        return false
      }
    }
    if (hasMin && Number(packet.length ?? 0) < min) {
      return false
    }
    if (hasMax && Number(packet.length ?? 0) > max) {
      return false
    }
    return true
  })
})

const totalPages = computed(() => Math.max(1, Math.ceil(filteredPackets.value.length / pageSize.value)))

const pagedPackets = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredPackets.value.slice(start, start + pageSize.value)
})

watch([keyword, protocolFilter, ipFilter, portFilter, minLength, maxLength], () => {
  currentPage.value = 1
})

watch(pageSize, () => {
  currentPage.value = 1
})

watch(
  () => props.packets,
  () => {
    selectedPacketId.value = null
    currentPage.value = 1
  },
  { deep: true },
)

watch(filteredPackets, () => {
  if (currentPage.value > totalPages.value) {
    currentPage.value = totalPages.value
  }
})

function resetFilters() {
  keyword.value = ''
  protocolFilter.value = 'all'
  ipFilter.value = ''
  portFilter.value = ''
  minLength.value = ''
  maxLength.value = ''
  currentPage.value = 1
}

function formatTimestamp(value) {
  if (!value) {
    return '-'
  }
  const numeric = Number(value)
  if (!Number.isFinite(numeric)) {
    return value
  }
  const epochMillis = numeric > 1_000_000_000_000 ? numeric : numeric * 1000
  const date = new Date(epochMillis)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  return date.toLocaleTimeString('zh-CN', {
    hour12: false,
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    fractionalSecondDigits: 3,
  })
}

function protocolClass(protocol) {
  const normalized = String(protocol ?? '').toUpperCase()
  if (normalized === 'DNS') {
    return 'protocol-dns'
  }
  if (normalized === 'TCP') {
    return 'protocol-tcp'
  }
  if (normalized === 'UDP') {
    return 'protocol-udp'
  }
  if (normalized === 'TLS' || normalized === 'SSL') {
    return 'protocol-tls'
  }
  if (normalized === 'HTTP') {
    return 'protocol-http'
  }
  return 'protocol-other'
}

function endpoint(ip, port) {
  if (ip && port) {
    return `${ip}:${port}`
  }
  if (ip) {
    return ip
  }
  if (port) {
    return `:${port}`
  }
  return '-'
}

function selectPacket(packet) {
  selectedPacketId.value = packet.id
  emit('select', packet)
}
</script>

<template>
  <div v-if="packets.length === 0" class="empty">暂无数据包记录</div>
  <div v-else>
    <div class="packet-filter-panel">
      <div class="filter-grid">
        <input v-model="keyword" type="search" placeholder="搜索摘要、IP、端口或协议" />
        <select v-model="protocolFilter" aria-label="协议筛选">
          <option v-for="protocol in protocolOptions" :key="protocol" :value="protocol">
            {{ protocol === 'all' ? '全部协议' : protocol }}
          </option>
        </select>
        <input v-model="ipFilter" type="search" placeholder="筛选 IP" />
        <input v-model="portFilter" type="search" placeholder="筛选端口" />
        <input v-model="minLength" type="number" placeholder="最小长度" />
        <input v-model="maxLength" type="number" placeholder="最大长度" />
      </div>
      <div class="filter-summary-row">
        <span class="badge">筛选结果 {{ filteredPackets.length }} / {{ packets.length }}</span>
        <button class="ghost-button" type="button" @click="resetFilters">重置筛选</button>
      </div>
    </div>
    <div v-if="filteredPackets.length === 0" class="empty">没有匹配的数据包</div>
    <div v-else class="table-wrap elevated-table">
      <table>
        <thead>
          <tr>
            <th>序号</th>
            <th>时间</th>
            <th>源端点</th>
            <th>目的端点</th>
            <th>协议</th>
            <th>长度</th>
            <th>摘要</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="packet in pagedPackets"
            :key="packet.id"
            :class="{ selected: selectedPacketId === packet.id }"
            @click="selectPacket(packet)"
          >
            <td>{{ packet.packetNo }}</td>
            <td :title="packet.timestampText">{{ formatTimestamp(packet.timestampText) }}</td>
            <td>{{ endpoint(packet.sourceIp, packet.sourcePort) }}</td>
            <td>{{ endpoint(packet.destinationIp, packet.destinationPort) }}</td>
            <td>
              <span class="protocol-pill" :class="protocolClass(packet.protocol)">
                {{ packet.protocol || '-' }}
              </span>
            </td>
            <td>{{ packet.length }}</td>
            <td class="packet-info-cell" :title="packet.info">{{ packet.info }}</td>
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
