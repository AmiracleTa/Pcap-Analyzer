# pcap-analyzer

在线 pcap 数据包分析网站

支持 `.pcap`、`.pcapng`、`.cap` 格式，解析数据包并展示统计图表、明细和 AI 安全报告

![首页概览](./assets/home-overview.png)

## 功能概览

![功能模块图](./assets/module-diagram.svg)

文件管理、解析执行、统计展示、安全报告、导出下载

## 业务流程

![核心业务流程](./assets/business-flow.svg)

1. 上传抓包文件，后端保存到 `uploads/`
2. 写入 `CaptureFile` 元数据，状态为 `uploaded`
3. 点击分析，前端通过 SSE 接收进度
4. `PacketAnalysisService` 调用 `capinfos` / `tshark`
5. 批量保存 `PacketRecord`，生成 `AnalysisSummary`
6. `SecurityReportService` 读取统计和部分数据包样本，生成 AI 报告
7. 前端展示图表、表格、详情和导出入口

## 架构设计

![架构设计](./assets/architecture.svg)

- 前端：Vue 页面、REST 请求、SSE、ECharts 图表
- 接口层：健康检查、文件管理、分析进度、统计、报告、导出
- 服务层：文件存储、抓包解析、统计生成、AI 报告
- 基础设施：`capinfos` / `tshark` 命令执行、AI HTTP 调用
- 数据层：MySQL 保存文件、数据包、统计、AI 报告，`uploads/` 保存原始文件

## 快速开始

### 环境要求

- JDK 25
- Node.js 与 npm
- MySQL
- `tshark`、`capinfos`

### MySQL 初始化

```bash
mysql --protocol=tcp --host=127.0.0.1 --user=root --password=mysql123123 < sql/init.sql
```

默认配置在 `src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: ${MYSQL_URL:jdbc:mysql://localhost:3306/pcap_analyzer?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&rewriteBatchedStatements=true}
    username: ${MYSQL_USERNAME:root}
    password: ${MYSQL_PASSWORD:mysql123123}
```

可用环境变量覆盖

```bash
MYSQL_URL=jdbc:mysql://localhost:3306/pcap_analyzer
MYSQL_USERNAME=root
MYSQL_PASSWORD=mysql123123
```

### AI 配置

默认配置

```yaml
ai:
  provider:
    enabled: ${AI_ENABLED:true}
    provider: ${AI_PROVIDER:deepseek}
    api-key: ${AI_API_KEY:}
    model: ${AI_MODEL:deepseek-v4-flash}
    base-url: ${AI_BASE_URL:https://api.deepseek.com}
    chat-completions-path: ${AI_CHAT_COMPLETIONS_PATH:/chat/completions}
    timeout-seconds: ${AI_TIMEOUT_SECONDS:60}
```

设置 `api key`

```bash
AI_API_KEY=你的 API Key
```

### 启动后端

```bash
./mvnw test
./mvnw spring-boot:run
```

健康检查

```bash
curl http://localhost:8080/api/health
```

```json
{"status":"ok"}
```

### 启动前端

```bash
cd frontend
npm install
npm run dev -- --host 127.0.0.1
```

访问地址

```text
http://127.0.0.1:5173
```

`/api` 会代理到 `http://localhost:8080`

## 图片预览

### 文件管理

![文件管理](./assets/file-management.png)

### **AI 安全报告**

![AI 安全报告](./assets/ai-security-report.png)

### **数据可视化**

![数据可视化](./assets/traffic-visualization.png)

### 数据包列表

![数据包列表](./assets/packet-table.png)

### 详细信息展示

![详细信息展示](./assets/packet-detail.png)

## 主要接口

### 文件管理

```bash
curl -F "file=@samples/test.pcapng" http://localhost:8080/api/files
curl http://localhost:8080/api/files
curl http://localhost:8080/api/files/1
curl http://localhost:8080/api/files/1/download
curl -X DELETE http://localhost:8080/api/files/1
```

上传限制

- 字段名为 `file`
- 仅允许 `.pcap`、`.pcapng`、`.cap`
- 默认上限 100MB
- 文件保存到 `uploads/`
- 记录写入 MySQL

### 数据包解析

```bash
curl -X POST http://localhost:8080/api/files/1/analyze
curl http://localhost:8080/api/files/1/analyze/events
curl http://localhost:8080/api/files/1/packets
curl http://localhost:8080/api/files/1/summary
curl http://localhost:8080/api/files/1/security-report
```

统计字段

- `protocols`：协议数量
- `trafficTrend`：按秒聚合的包数量
- `ips`、`ports`：源/目的 IP 与端口次数
- `lengthDistribution`：包长分布
- `sourceIpTop`、`destinationIpTop`：源/目的 IP Top
- `sourcePortTop`、`destinationPortTop`：源/目的端口 Top
- `dnsRecords`、`httpRecords`：DNS / HTTP 记录
- `startTimeText`、`endTimeText`：首尾包时间

### 结果导出

```bash
curl http://localhost:8080/api/files/1/export/csv
curl http://localhost:8080/api/files/1/export/json
```

CSV 返回表格数据，JSON 返回 `PacketRecord` 列表
