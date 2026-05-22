<script setup>
defineProps({
  summary: {
    type: Object,
    required: true,
  },
})

function dnsAnswerText(value) {
  if (value === null || value === undefined || String(value).trim() === '') {
    return '无应答'
  }
  return value
}
</script>

<template>
  <div class="protocol-feature-stack">
    <section class="stack-panel feature-panel protocol-record-panel" data-reveal>
      <h3>DNS 记录</h3>
      <div v-if="(summary.dnsRecords || []).length === 0" class="empty compact">暂无数据</div>
      <div v-else class="table-wrap elevated-table">
        <table class="stats-table">
          <thead>
            <tr>
              <th>包序号</th>
              <th>查询域名</th>
              <th>应答地址</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="record in summary.dnsRecords" :key="`dns-${record.packetNo}-${record.queryName}-${record.answerAddress}`">
              <td>{{ record.packetNo }}</td>
              <td>{{ record.queryName }}</td>
              <td>{{ dnsAnswerText(record.answerAddress) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <section class="stack-panel feature-panel protocol-record-panel" data-reveal>
      <h3>HTTP 记录</h3>
      <div v-if="(summary.httpRecords || []).length === 0" class="empty compact">暂无数据</div>
      <div v-else class="table-wrap elevated-table">
        <table class="stats-table">
          <thead>
            <tr>
              <th>包序号</th>
              <th>方法</th>
              <th>主机</th>
              <th>URI</th>
              <th>响应码</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="record in summary.httpRecords" :key="`http-${record.packetNo}-${record.method}-${record.host}-${record.uri}-${record.responseCode}`">
              <td>{{ record.packetNo }}</td>
              <td>{{ record.method }}</td>
              <td>{{ record.host }}</td>
              <td>{{ record.uri }}</td>
              <td>{{ record.responseCode }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>
