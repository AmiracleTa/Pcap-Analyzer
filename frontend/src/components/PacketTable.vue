<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  packets: {
    type: Array,
    default: () => [],
  },
})

const emit = defineEmits(['select'])

const protocolFilter = ref('all')
const portFilter = ref('')
const minLength = ref('')
const maxLength = ref('')
const sourceIpFilter = ref('')
const destinationIpFilter = ref('')
const timeStartRatio = ref(0)
const timeEndRatio = ref(100)
const pageSize = ref(25)
const currentPage = ref(1)
const selectedPacketId = ref(null)
const suppressFocusTooltip = ref(false)
const packetTooltip = ref({
  visible: false,
  title: '',
  text: '',
  x: 0,
  y: 0,
})
const pageSizeOptions = [25, 50, 100]

const protocolOptions = computed(() => {
  const protocols = new Set(props.packets.map((packet) => packet.protocol).filter(Boolean))
  return ['all', ...Array.from(protocols).sort()]
})

const packetTimes = computed(() => {
  return props.packets
    .map((packet) => Number(packet.timestampText))
    .filter((value) => Number.isFinite(value))
})

const minPacketTime = computed(() => (packetTimes.value.length ? Math.min(...packetTimes.value) : null))
const maxPacketTime = computed(() => (packetTimes.value.length ? Math.max(...packetTimes.value) : null))
const selectedStartTime = computed(() => ratioToTime(timeStartRatio.value))
const selectedEndTime = computed(() => ratioToTime(timeEndRatio.value))

const filteredPackets = computed(() => {
  const sourceIpQuery = sourceIpFilter.value.trim().toLowerCase()
  const destinationIpQuery = destinationIpFilter.value.trim().toLowerCase()
  const portQuery = portFilter.value.trim()
  const min = Number(minLength.value)
  const max = Number(maxLength.value)
  const hasMin = minLength.value !== '' && Number.isFinite(min)
  const hasMax = maxLength.value !== '' && Number.isFinite(max)
  let startTime = selectedStartTime.value
  let endTime = selectedEndTime.value

  if (startTime !== null && endTime !== null && startTime > endTime) {
    ;[startTime, endTime] = [endTime, startTime]
  }

  return props.packets.filter((packet) => {
    if (sourceIpQuery && !String(packet.sourceIp ?? '').toLowerCase().includes(sourceIpQuery)) {
      return false
    }
    if (
      destinationIpQuery &&
      !String(packet.destinationIp ?? '').toLowerCase().includes(destinationIpQuery)
    ) {
      return false
    }
    if (protocolFilter.value !== 'all' && packet.protocol !== protocolFilter.value) {
      return false
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
    const packetTime = Number(packet.timestampText)
    if (Number.isFinite(packetTime)) {
      if (startTime !== null && packetTime < startTime) {
        return false
      }
      if (endTime !== null && packetTime > endTime) {
        return false
      }
    }
    return true
  })
})

const totalPages = computed(() => Math.max(1, Math.ceil(filteredPackets.value.length / pageSize.value)))

const pagedPackets = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredPackets.value.slice(start, start + pageSize.value)
})

watch(
  [
    sourceIpFilter,
    destinationIpFilter,
    portFilter,
    protocolFilter,
    minLength,
    maxLength,
    timeStartRatio,
    timeEndRatio,
  ],
  () => {
    currentPage.value = 1
  },
)

watch(pageSize, () => {
  currentPage.value = 1
})

watch(
  () => props.packets,
  () => {
    timeStartRatio.value = 0
    timeEndRatio.value = 100
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
  sourceIpFilter.value = ''
  destinationIpFilter.value = ''
  portFilter.value = ''
  protocolFilter.value = 'all'
  minLength.value = ''
  maxLength.value = ''
  timeStartRatio.value = 0
  timeEndRatio.value = 100
  currentPage.value = 1
}

function ratioToTime(ratio) {
  if (minPacketTime.value === null || maxPacketTime.value === null) {
    return null
  }
  if (minPacketTime.value === maxPacketTime.value) {
    return minPacketTime.value
  }
  return minPacketTime.value + (maxPacketTime.value - minPacketTime.value) * (Number(ratio) / 100)
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

function formatTimeRangeValue(value) {
  if (value === null) {
    return '-'
  }
  return formatTimestamp(String(value))
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

function markPointerFocus() {
  suppressFocusTooltip.value = true
  window.setTimeout(() => {
    suppressFocusTooltip.value = false
  }, 0)
}

function handlePacketFocus(packet, event) {
  if (suppressFocusTooltip.value) {
    return
  }
  showPacketTooltip(packet, event)
}

function packetTooltipText(packet) {
  return packet.info || '无摘要'
}

function showPacketTooltip(packet, event) {
  showTooltip('数据包摘要', packetTooltipText(packet), event)
}

function showTooltip(title, text, event) {
  const position = packetTooltipPosition(event)
  packetTooltip.value = {
    visible: true,
    title,
    text: text || '-',
    x: position.x,
    y: position.y,
  }
}

function movePacketTooltip(event) {
  if (!packetTooltip.value.visible) {
    return
  }
  const position = packetTooltipPosition(event)
  packetTooltip.value = {
    ...packetTooltip.value,
    x: position.x,
    y: position.y,
  }
}

function hidePacketTooltip() {
  packetTooltip.value = {
    ...packetTooltip.value,
    visible: false,
  }
}

function packetTooltipPosition(event) {
  const rect = event.currentTarget?.getBoundingClientRect?.()
  const baseX = Number.isFinite(event.clientX) && event.clientX > 0 ? event.clientX : rect?.left || 0
  const baseY = Number.isFinite(event.clientY) && event.clientY > 0 ? event.clientY : rect?.top || 0
  return {
    x: Math.max(12, Math.min(baseX + 14, window.innerWidth - 540)),
    y: Math.max(12, Math.min(baseY + 14, window.innerHeight - 90)),
  }
}
</script>

<template>
  <div v-if="packets.length === 0" class="empty">暂无数据包记录</div>
  <div v-else>
    <div class="packet-filter-panel">
      <div class="filter-grid">
        <input v-model="sourceIpFilter" type="search" placeholder="源 IP" />
        <input v-model="destinationIpFilter" type="search" placeholder="目的 IP" />
        <input v-model="portFilter" type="search" placeholder="端口" />
        <select v-model="protocolFilter" aria-label="协议筛选">
          <option v-for="protocol in protocolOptions" :key="protocol" :value="protocol">
            {{ protocol === 'all' ? '全部协议' : protocol }}
          </option>
        </select>
        <div class="length-filter-group">
          <input v-model="minLength" type="number" placeholder="最小长度" />
          <span>至</span>
          <input v-model="maxLength" type="number" placeholder="最大长度" />
        </div>
      </div>
      <div class="time-range-panel">
        <div class="time-range-header">
          <strong>时间范围</strong>
          <span>{{ formatTimeRangeValue(selectedStartTime) }} - {{ formatTimeRangeValue(selectedEndTime) }}</span>
        </div>
        <div class="range-stack">
          <input
            v-model.number="timeStartRatio"
            type="range"
            min="0"
            max="100"
            step="1"
            :disabled="minPacketTime === null || maxPacketTime === null"
          />
          <input
            v-model.number="timeEndRatio"
            type="range"
            min="0"
            max="100"
            step="1"
            :disabled="minPacketTime === null || maxPacketTime === null"
          />
        </div>
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
            <th>源 IP</th>
            <th>目的 IP</th>
            <th>源端口</th>
            <th>目的端口</th>
            <th>协议</th>
            <th>长度</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="packet in pagedPackets"
            :key="packet.id"
            :class="{ selected: selectedPacketId === packet.id }"
            class="packet-row"
            tabindex="0"
            @pointerdown="markPointerFocus"
            @click="selectPacket(packet)"
            @focus="handlePacketFocus(packet, $event)"
            @mouseenter="showPacketTooltip(packet, $event)"
            @mousemove="movePacketTooltip"
            @blur="hidePacketTooltip"
            @mouseleave="hidePacketTooltip"
          >
            <td
              @mouseenter.stop="showPacketTooltip(packet, $event)"
              @mousemove.stop="movePacketTooltip"
            >
              {{ packet.packetNo }}
            </td>
            <td
              @mouseenter.stop="showTooltip('时间戳', packet.timestampText || '-', $event)"
              @mousemove.stop="movePacketTooltip"
            >
              {{ formatTimestamp(packet.timestampText) }}
            </td>
            <td
              @mouseenter.stop="showTooltip('源 IP', endpoint(packet.sourceIp, packet.sourcePort), $event)"
              @mousemove.stop="movePacketTooltip"
            >
              {{ packet.sourceIp || '-' }}
            </td>
            <td
              @mouseenter.stop="showTooltip('目的 IP', endpoint(packet.destinationIp, packet.destinationPort), $event)"
              @mousemove.stop="movePacketTooltip"
            >
              {{ packet.destinationIp || '-' }}
            </td>
            <td
              @mouseenter.stop="showTooltip('源 IP', endpoint(packet.sourceIp, packet.sourcePort), $event)"
              @mousemove.stop="movePacketTooltip"
            >
              {{ packet.sourcePort ?? '-' }}
            </td>
            <td
              @mouseenter.stop="showTooltip('目的 IP', endpoint(packet.destinationIp, packet.destinationPort), $event)"
              @mousemove.stop="movePacketTooltip"
            >
              {{ packet.destinationPort ?? '-' }}
            </td>
            <td
              @mouseenter.stop="showPacketTooltip(packet, $event)"
              @mousemove.stop="movePacketTooltip"
            >
              <span class="protocol-pill" :class="protocolClass(packet.protocol)">
                {{ packet.protocol || '-' }}
              </span>
            </td>
            <td
              @mouseenter.stop="showPacketTooltip(packet, $event)"
              @mousemove.stop="movePacketTooltip"
            >
              {{ packet.length }}
            </td>
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
    <div
      v-if="packetTooltip.visible"
      class="packet-hover-tooltip"
      :style="{ left: `${packetTooltip.x}px`, top: `${packetTooltip.y}px` }"
      role="tooltip"
    >
      <strong>{{ packetTooltip.title }}</strong>
      <span>{{ packetTooltip.text }}</span>
    </div>
  </div>
</template>
