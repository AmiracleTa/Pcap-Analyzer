# pcap-analyzer 后端文档

## 1. 后端定位

`pcap-analyzer` 后端是一个 Spring Boot REST API 服务，负责抓包文件管理、调用 `tshark` 解析 `pcap/pcapng/cap` 文件、保存数据包记录、生成统计结果，并向 Vue 前端提供 JSON、CSV 和原始文件下载接口。

后端源码位于：

```text
pcap-analyzer/src/main/java/com/hzcu/pcap/
```

主要技术栈：

| 类别 | 技术 |
|---|---|
| 运行环境 | JDK 25 |
| Web 框架 | Spring Boot 3.5.14 |
| Web 依赖 | Spring Web |
| 数据访问 | Spring Data JPA |
| 数据库 | MySQL |
| JSON 处理 | Jackson |
| 抓包解析 | tshark |
| 构建工具 | Maven Wrapper |

## 2. 目录结构

```text
src/main/java/com/hzcu/pcap/
  PcapAnalyzerApplication.java
  controller/
    HealthController.java
    FileController.java
    AnalysisController.java
    ApiExceptionHandler.java
  entity/
    CaptureFile.java
    PacketRecord.java
    AnalysisSummary.java
  repository/
    CaptureFileRepository.java
    PacketRecordRepository.java
    AnalysisSummaryRepository.java
  service/
    FileStorageService.java
    PacketAnalysisService.java
    SummaryService.java
    TsharkCommandRunner.java

src/main/resources/
  application.yml

src/test/java/com/hzcu/pcap/
  controller/ApiControllerTests.java
  service/PacketAnalysisServiceTests.java
```

## 3. 配置说明

配置文件：

```text
src/main/resources/application.yml
```

当前配置：

```yaml
server:
  port: 8080

spring:
  datasource:
    url: ${MYSQL_URL:jdbc:mysql://localhost:3306/pcap_analyzer?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai}
    username: ${MYSQL_USERNAME:root}
    password: ${MYSQL_PASSWORD:mysql123123}
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB

app:
  upload-dir: uploads
```

说明：

| 配置项 | 作用 |
|---|---|
| `server.port` | 后端监听端口，当前为 `8080` |
| `spring.datasource.url` | MySQL 连接地址，默认连接 `pcap_analyzer` |
| `spring.datasource.username` | MySQL 用户名，默认 `root` |
| `spring.datasource.password` | MySQL 密码，默认 `mysql123123` |
| `spring.jpa.hibernate.ddl-auto` | 当前为 `update`，应用启动时自动同步实体表结构 |
| `spring.servlet.multipart.max-file-size` | 单文件上传上限，当前为 `100MB` |
| `app.upload-dir` | 抓包文件保存目录，当前为 `uploads` |

数据库初始化脚本：

```text
sql/init.sql
```

脚本只创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS pcap_analyzer
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
```

表结构由 JPA 根据实体自动维护。

## 4. 后端分层

### 4.1 Controller 层

| 文件 | 职责 |
|---|---|
| `HealthController` | 提供健康检查接口 |
| `FileController` | 上传、列表、详情、下载、删除抓包文件 |
| `AnalysisController` | 触发分析、查询数据包、查询统计、导出 CSV/JSON |
| `ApiExceptionHandler` | 统一把异常转换为 JSON 错误响应 |

### 4.2 Service 层

| 文件 | 职责 |
|---|---|
| `FileStorageService` | 保存上传文件、维护文件元数据、删除文件和关联数据 |
| `PacketAnalysisService` | 调用 tshark、解析字段、保存数据包、生成统计 |
| `SummaryService` | 将数据库中的统计 JSON 转换为前端需要的 summary 响应 |
| `TsharkCommandRunner` | 封装 tshark 进程执行、超时、错误处理和输出解析 |

### 4.3 Repository 层

| 文件 | 职责 |
|---|---|
| `CaptureFileRepository` | 操作上传文件记录 |
| `PacketRecordRepository` | 操作数据包记录，支持按 `fileId` 查询和删除 |
| `AnalysisSummaryRepository` | 操作统计结果，支持按 `fileId` 查询和删除 |

## 5. 数据模型

### 5.1 CaptureFile

对应上传文件记录。

| 字段 | 类型 | 含义 |
|---|---|---|
| `id` | `Long` | 主键 |
| `storedName` | `String` | 保存到 `uploads/` 下的文件名，使用 UUID |
| `originalName` | `String` | 用户上传时的原始文件名 |
| `fileSize` | `Long` | 文件大小，单位字节 |
| `fileType` | `String` | 文件扩展名，例如 `.pcapng` |
| `uploadTime` | `LocalDateTime` | 上传时间 |
| `packetCount` | `Long` | 已解析的数据包数量 |
| `status` | `String` | 文件状态 |

`status` 当前使用值：

| 状态 | 含义 |
|---|---|
| `uploaded` | 文件已上传，尚未成功分析 |
| `analyzed` | 文件已成功分析 |
| `failed` | 分析失败 |

### 5.2 PacketRecord

对应单条数据包记录。

| 字段 | 类型 | 含义 |
|---|---|---|
| `id` | `Long` | 主键 |
| `fileId` | `Long` | 所属抓包文件 ID |
| `packetNo` | `Long` | 数据包序号，对应 `frame.number` |
| `timestampText` | `String` | 时间戳文本，对应 `frame.time_epoch` |
| `sourceIp` | `String` | 源 IP，IPv4 优先，IPv6 兜底 |
| `destinationIp` | `String` | 目的 IP，IPv4 优先，IPv6 兜底 |
| `sourcePort` | `Integer` | 源端口，TCP 优先，UDP 兜底 |
| `destinationPort` | `Integer` | 目的端口，TCP 优先，UDP 兜底 |
| `protocol` | `String` | 协议列，对应 `_ws.col.Protocol` |
| `length` | `Integer` | 包长度，对应 `frame.len` |
| `info` | `String` | 摘要信息，对应 `_ws.col.Info` |
| `detailJson` | `String` | tshark `-T json -x` 返回的单包详情 JSON |

### 5.3 AnalysisSummary

对应一个抓包文件的统计结果。该实体将复杂统计保存为 JSON 字符串。

| 字段 | 含义 |
|---|---|
| `id` | 主键 |
| `fileId` | 所属抓包文件 ID |
| `protocolJson` | 协议分布 |
| `trafficTrendJson` | 按秒聚合的流量趋势 |
| `ipJson` | 源/目的 IP 合并统计 |
| `portJson` | 源/目的端口合并统计 |
| `lengthDistributionJson` | 包长度分布 |
| `sourceIpTopJson` | 源 IP Top10 |
| `destinationIpTopJson` | 目的 IP Top10 |
| `sourcePortTopJson` | 源端口 Top10 |
| `destinationPortTopJson` | 目的端口 Top10 |
| `dnsRecordsJson` | DNS 查询/应答记录 |
| `httpRecordsJson` | HTTP 请求/响应记录 |
| `startTimeText` | 首包时间戳 |
| `endTimeText` | 尾包时间戳 |

## 6. API 文档

后端统一前缀为 `/api`。

### 6.1 健康检查

```http
GET /api/health
```

响应：

```json
{"status":"ok"}
```

### 6.2 上传抓包文件

```http
POST /api/files
Content-Type: multipart/form-data
```

表单字段：

| 字段名 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `file` | file | 是 | 抓包文件 |

允许扩展名：

```text
.pcap
.pcapng
.cap
```

成功响应示例：

```json
{
  "id": 1,
  "storedName": "ae856360-01bb-4f7b-b18a-063030941238.pcapng",
  "originalName": "test.pcapng",
  "fileSize": 31812,
  "fileType": ".pcapng",
  "uploadTime": "2026-05-21T20:00:00",
  "packetCount": 0,
  "status": "uploaded"
}
```

失败条件：

| 条件 | HTTP 状态 |
|---|---|
| 文件为空 | `400` |
| 扩展名不是 `.pcap/.pcapng/.cap` | `400` |
| 文件写入失败 | `500` |

### 6.3 查询文件列表

```http
GET /api/files
```

响应：`CaptureFile[]`。

### 6.4 查询单个文件

```http
GET /api/files/{id}
```

响应：`CaptureFile`。

文件不存在时返回 `404`。

### 6.5 下载原始抓包文件

```http
GET /api/files/{id}/download
```

响应：原始文件二进制流。

响应头包含：

```http
Content-Disposition: attachment; filename="<originalName>"
```

### 6.6 删除文件

```http
DELETE /api/files/{id}
```

删除内容：

- `uploads/` 下的磁盘文件
- `packet_record` 中对应 `fileId` 的记录
- `analysis_summary` 中对应 `fileId` 的记录
- `capture_file` 文件记录

响应：

```json
{"deleted":true}
```

### 6.7 分析文件

```http
POST /api/files/{id}/analyze
```

成功响应：

```json
{
  "status": "ok",
  "packetCount": 42
}
```

执行过程：

1. 查询 `CaptureFile`。
2. 定位磁盘文件路径。
3. 删除该文件旧的 `PacketRecord` 和 `AnalysisSummary`。
4. 调用 tshark 读取数据包基础字段。
5. 调用 tshark 读取每个包的详情 JSON。
6. 调用 tshark 读取 DNS/HTTP 特征字段。
7. 将基础字段转换为 `PacketRecord`。
8. 批量保存 `PacketRecord`。
9. 生成并保存 `AnalysisSummary`。
10. 更新 `CaptureFile.packetCount`。
11. 更新 `CaptureFile.status = analyzed`。

已成功查询到 `CaptureFile` 后，如果后续定位文件、执行 tshark、解析字段、保存记录或生成统计失败：

1. 更新 `CaptureFile.status = failed`。
2. 抛出 `IllegalStateException`。
3. `ApiExceptionHandler` 返回 JSON 错误响应。

如果文件 ID 不存在，`FileStorageService.getFile(id)` 会直接抛出 `ResponseStatusException(404)`，不会更新文件状态。

### 6.8 查询数据包列表

```http
GET /api/files/{id}/packets
```

响应：`PacketRecord[]`。

示例：

```json
[
  {
    "id": 1,
    "fileId": 1,
    "packetNo": 1,
    "timestampText": "1716260000.100000",
    "sourceIp": "192.168.1.2",
    "destinationIp": "93.184.216.34",
    "sourcePort": 51514,
    "destinationPort": 443,
    "protocol": "TLS",
    "length": 128,
    "info": "Client Hello",
    "detailJson": "{\"layers\":{}}"
  }
]
```

### 6.9 查询统计结果

```http
GET /api/files/{id}/summary
```

响应字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| `protocols` | object | 协议数量统计 |
| `trafficTrend` | array | 按 epoch 秒统计的数据包数量 |
| `ips` | object | 源/目的 IP 合并统计 |
| `ports` | object | 源/目的端口合并统计 |
| `lengthDistribution` | array | 包长度区间统计 |
| `sourceIpTop` | array | 源 IP Top10 |
| `destinationIpTop` | array | 目的 IP Top10 |
| `sourcePortTop` | array | 源端口 Top10 |
| `destinationPortTop` | array | 目的端口 Top10 |
| `dnsRecords` | array | DNS 查询/应答记录，最多 100 条 |
| `httpRecords` | array | HTTP 请求/响应记录，最多 100 条 |
| `startTimeText` | string | 首包时间戳 |
| `endTimeText` | string | 尾包时间戳 |

示例：

```json
{
  "protocols": {"TLS": 2, "DNS": 1},
  "trafficTrend": [
    {"time": "1716260000", "value": 1},
    {"time": "1716260001", "value": 2}
  ],
  "ips": {"192.168.1.2": 3, "93.184.216.34": 2},
  "ports": {"443": 2, "51514": 2, "53": 1},
  "lengthDistribution": [
    {"range": "0-63", "value": 0},
    {"range": "64-127", "value": 1},
    {"range": "128-255", "value": 1},
    {"range": "256-511", "value": 1},
    {"range": "512-1023", "value": 0},
    {"range": "1024-1518", "value": 0},
    {"range": "1519+", "value": 0}
  ],
  "sourceIpTop": [{"name": "192.168.1.2", "value": 2}],
  "destinationIpTop": [{"name": "93.184.216.34", "value": 1}],
  "sourcePortTop": [{"name": "51514", "value": 1}],
  "destinationPortTop": [{"name": "443", "value": 1}],
  "dnsRecords": [
    {"packetNo": 1, "queryName": "example.com", "answerAddress": "93.184.216.34"}
  ],
  "httpRecords": [
    {"packetNo": 2, "method": "GET", "host": "example.com", "uri": "/index.html", "responseCode": ""}
  ],
  "startTimeText": "1716260000.100000",
  "endTimeText": "1716260001.300000"
}
```

未找到统计记录时返回空结构：

```json
{
  "protocols": {},
  "trafficTrend": [],
  "ips": {},
  "ports": {},
  "lengthDistribution": [],
  "sourceIpTop": [],
  "destinationIpTop": [],
  "sourcePortTop": [],
  "destinationPortTop": [],
  "dnsRecords": [],
  "httpRecords": [],
  "startTimeText": "",
  "endTimeText": ""
}
```

### 6.10 导出 CSV

```http
GET /api/files/{id}/export/csv
```

响应类型：

```http
Content-Type: text/csv
```

表头固定为：

```csv
packetNo,timestamp,sourceIp,destinationIp,sourcePort,destinationPort,protocol,length,info
```

每个字段会使用双引号包裹，并对内部双引号做 CSV 转义。

### 6.11 导出 JSON

```http
GET /api/files/{id}/export/json
```

响应：

```json
{
  "packets": [
    {
      "id": 1,
      "fileId": 1,
      "packetNo": 1,
      "timestampText": "1716260000.100000",
      "sourceIp": "192.168.1.2",
      "destinationIp": "93.184.216.34",
      "sourcePort": 51514,
      "destinationPort": 443,
      "protocol": "TLS",
      "length": 128,
      "info": "Client Hello",
      "detailJson": "{\"layers\":{}}"
    }
  ]
}
```

## 7. tshark 解析说明

后端不手写 pcap 二进制解析，统一通过 `TsharkCommandRunner` 调用系统 PATH 中的 `tshark`。

### 7.1 数据包基础字段命令

```bash
tshark -n -r <capture-file> -T fields \
  -E header=n \
  -E separator=/t \
  -E occurrence=f \
  -e frame.number \
  -e frame.time_epoch \
  -e ip.src \
  -e ipv6.src \
  -e ip.dst \
  -e ipv6.dst \
  -e tcp.srcport \
  -e udp.srcport \
  -e tcp.dstport \
  -e udp.dstport \
  -e _ws.col.Protocol \
  -e frame.len \
  -e _ws.col.Info
```

字段映射：

| 索引 | tshark 字段 | 后端字段 |
|---:|---|---|
| 0 | `frame.number` | `packetNo` |
| 1 | `frame.time_epoch` | `timestampText` |
| 2 | `ip.src` | IPv4 源地址候选 |
| 3 | `ipv6.src` | IPv6 源地址候选 |
| 4 | `ip.dst` | IPv4 目的地址候选 |
| 5 | `ipv6.dst` | IPv6 目的地址候选 |
| 6 | `tcp.srcport` | TCP 源端口候选 |
| 7 | `udp.srcport` | UDP 源端口候选 |
| 8 | `tcp.dstport` | TCP 目的端口候选 |
| 9 | `udp.dstport` | UDP 目的端口候选 |
| 10 | `_ws.col.Protocol` | `protocol` |
| 11 | `frame.len` | `length` |
| 12 | `_ws.col.Info` | `info` |

选择规则：

| 目标字段 | 规则 |
|---|---|
| `sourceIp` | `ip.src` 非空使用 `ip.src`，否则使用 `ipv6.src` |
| `destinationIp` | `ip.dst` 非空使用 `ip.dst`，否则使用 `ipv6.dst` |
| `sourcePort` | `tcp.srcport` 非空使用 `tcp.srcport`，否则使用 `udp.srcport` |
| `destinationPort` | `tcp.dstport` 非空使用 `tcp.dstport`，否则使用 `udp.dstport` |

`_ws.col.Info` 中可能包含制表符。后端在字段数量超过 13 时，会把第 12 个索引之后的内容重新合并回 `info` 字段。

### 7.2 数据包详情 JSON 命令

```bash
tshark -n -r <capture-file> -T json -x
```

返回值是 JSON 数组。后端按数组顺序将第 `i` 个 JSON 元素写入第 `i` 条 `PacketRecord.detailJson`。

当详情 JSON 数量少于基础数据包数量时，缺失的详情写入：

```json
{}
```

### 7.3 DNS/HTTP 特征命令

```bash
tshark -n -r <capture-file> -T fields \
  -E header=n \
  -E separator=/t \
  -E occurrence=f \
  -e frame.number \
  -e dns.qry.name \
  -e dns.a \
  -e http.request.method \
  -e http.host \
  -e http.request.uri \
  -e http.response.code
```

字段映射：

| 索引 | tshark 字段 | summary 字段 |
|---:|---|---|
| 0 | `frame.number` | `packetNo` |
| 1 | `dns.qry.name` | `queryName` |
| 2 | `dns.a` | `answerAddress` |
| 3 | `http.request.method` | `method` |
| 4 | `http.host` | `host` |
| 5 | `http.request.uri` | `uri` |
| 6 | `http.response.code` | `responseCode` |

DNS 记录保留规则：

- `queryName` 非空时保留
- `answerAddress` 非空时保留
- 两者都为空时丢弃
- 最多保留 100 条

HTTP 记录保留规则：

- `method`、`host`、`uri`、`responseCode` 任意一个非空时保留
- 四个字段都为空时丢弃
- 最多保留 100 条

### 7.4 tshark 进程错误处理

`TsharkCommandRunner` 的统一规则：

| 情况 | 处理 |
|---|---|
| 命令超过 60 秒未结束 | 强制结束进程，抛出 `IllegalStateException` |
| 退出码不是 0 | 将 stderr 写入异常消息 |
| stdout/stderr 读取失败 | 抛出 `IllegalStateException` |
| 当前线程被中断 | 恢复中断标记并抛出 `IllegalStateException` |

## 8. 统计规则

### 8.1 协议分布

来源字段：`PacketRecord.protocol`

格式：

```json
{"TLS": 12, "DNS": 5}
```

### 8.2 流量趋势

来源字段：`PacketRecord.timestampText`

处理方式：

1. 使用 `frame.time_epoch` 文本。
2. 小数点前的整数部分作为 epoch 秒。
3. 按秒统计数据包数量。

格式：

```json
[{"time":"1716260001","value":2}]
```

### 8.3 IP 和端口统计

`ips` 合并统计源 IP 和目的 IP。

`ports` 合并统计源端口和目的端口。

Top10 统计拆分为：

- `sourceIpTop`
- `destinationIpTop`
- `sourcePortTop`
- `destinationPortTop`

排序规则：

1. `value` 降序
2. `value` 相同按 `name` 升序
3. 最多取 10 条

### 8.4 包长度分布

固定区间：

| 区间 | 条件 |
|---|---|
| `0-63` | `length < 64` |
| `64-127` | `64 <= length <= 127` |
| `128-255` | `128 <= length <= 255` |
| `256-511` | `256 <= length <= 511` |
| `512-1023` | `512 <= length <= 1023` |
| `1024-1518` | `1024 <= length <= 1518` |
| `1519+` | `length >= 1519` |

返回顺序固定为上表顺序。

## 9. 统一错误响应

所有 Controller 抛出的异常由 `ApiExceptionHandler` 处理。

错误响应格式：

```json
{
  "status": 500,
  "error": "Internal Server Error",
  "message": "Failed to analyze capture file 1",
  "path": "/api/files/1/analyze",
  "timestamp": "2026-05-21T20:00:00"
}
```

处理规则：

| 异常类型 | HTTP 状态 |
|---|---|
| `ResponseStatusException` | 使用异常自带状态码 |
| `IllegalStateException` | `500` |
| 其他 `Exception` | `500` |

## 10. 启动与测试

### 10.1 初始化数据库

```powershell
cd F:\mine\amirhzcu\course\report\计算机网络实验\fnexp\pcap-analyzer
mysql --protocol=tcp --host=127.0.0.1 --user=root --password=mysql123123 < sql/init.sql
```

### 10.2 检查 tshark

```powershell
tshark -v
```

### 10.3 运行后端测试

```powershell
cd F:\mine\amirhzcu\course\report\计算机网络实验\fnexp\pcap-analyzer
.\mvnw.cmd test
```

### 10.4 启动后端

```powershell
cd F:\mine\amirhzcu\course\report\计算机网络实验\fnexp\pcap-analyzer
.\mvnw.cmd spring-boot:run
```

### 10.5 常用接口测试

健康检查：

```powershell
curl.exe http://localhost:8080/api/health
```

上传样例文件：

```powershell
curl.exe -F "file=@samples/test.pcapng" http://localhost:8080/api/files
```

分析文件：

```powershell
curl.exe -X POST http://localhost:8080/api/files/<id>/analyze
```

查询统计：

```powershell
curl.exe http://localhost:8080/api/files/<id>/summary
```

导出 CSV：

```powershell
curl.exe http://localhost:8080/api/files/<id>/export/csv
```

导出 JSON：

```powershell
curl.exe http://localhost:8080/api/files/<id>/export/json
```

其中 `<id>` 使用上传样例文件接口返回的 `id`。

## 11. 测试覆盖

当前测试文件：

| 测试文件 | 覆盖内容 |
|---|---|
| `ApiControllerTests` | 健康检查、上传扩展名校验、删除响应、分析接口、数据包列表/统计/导出接口、统一错误响应 |
| `PacketAnalysisServiceTests` | tshark 字段转换、数据包保存、统计生成、Top10、包长度分布、DNS/HTTP 特征记录 |

`PacketAnalysisServiceTests` 使用 `FakeTsharkCommandRunner` 替代真实 tshark，因此单元测试不依赖真实抓包工具。

## 12. 后端当前边界

当前后端满足课程项目的核心要求：上传、存储、下载、解析、统计、展示数据源、导出和错误响应。

当前实现边界：

- `GET /api/files/{id}/packets` 返回该文件全部数据包，分页由前端本地完成。
- `analysis_summary` 中复杂统计以 JSON 字符串保存，不拆成多张明细统计表。
- `detailJson` 保存 tshark 原始单包详情，后端不再深度拆解所有协议层。
- DNS/HTTP 特征来自 tshark fields 命令，不从 `detailJson` 字符串中用正则提取。
- 单次 tshark 命令超时时间固定为 60 秒。
