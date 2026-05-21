# pcap-analyzer

网络协议抓包分析实验项目。当前版本完成前后端启动、健康检查、文件上传、文件记录入库、`tshark` 解析、统计展示、文件下载、CSV/JSON 导出和删除联动。

## 项目结构

```text
pcap-analyzer/
  mvnw
  mvnw.cmd
  pom.xml
  uploads/
  sql/
    init.sql
  src/main/java/com/hzcu/pcap/
    PcapAnalyzerApplication.java
    controller/
    entity/
    repository/
    service/
    dto/
  src/main/resources/application.yml
  src/test/java/com/hzcu/pcap/
  frontend/
    package.json
    vite.config.js
    index.html
    src/
      main.js
      App.vue
      api/
      views/
      components/
```

## MySQL 初始化

```bash
mysql --protocol=tcp --host=127.0.0.1 --user=root --password=mysql123123 < sql/init.sql
```

默认连接配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/pcap_analyzer?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: mysql123123
```

也可以通过环境变量覆盖：

```bash
MYSQL_URL=jdbc:mysql://localhost:3306/pcap_analyzer
MYSQL_USERNAME=root
MYSQL_PASSWORD=mysql123123
```

## 后端启动

Windows 侧环境需要 JDK 25 和 `tshark`。

```bash
java -version
tshark -v
./mvnw test
./mvnw spring-boot:run
```

健康检查：

```bash
curl http://localhost:8080/api/health
```

期望输出：

```json
{"status":"ok"}
```

## 前端启动

```bash
cd frontend
npm install
npm run dev -- --host 127.0.0.1
```

访问：

```text
http://127.0.0.1:5173
```

## 上传接口

```bash
curl -F "file=@sample.pcap" http://localhost:8080/api/files
```

限制：

- 字段名必须为 `file`
- 仅允许 `.pcap`、`.pcapng`、`.cap`
- 上传文件保存到 `uploads/`
- 上传记录写入 MySQL

## 分析接口

上传后使用返回的 `id` 执行解析：

```bash
curl -X POST http://localhost:8080/api/files/1/analyze
```

返回示例：

```json
{"status":"ok","packetCount":42}
```

查询数据包：

```bash
curl http://localhost:8080/api/files/1/packets
```

查询统计：

```bash
curl http://localhost:8080/api/files/1/summary
```

统计包含：

- `protocols`：协议数量
- `trafficTrend`：按 epoch 秒聚合的数据包数量
- `ips`：源/目的 IP 出现次数
- `ports`：源/目的端口出现次数
- `startTimeText`、`endTimeText`：首尾包时间

## 导出接口

```bash
curl http://localhost:8080/api/files/1/export/csv
curl http://localhost:8080/api/files/1/export/json
```

CSV 返回固定表头和真实数据行。JSON 返回：

```json
{"packets":[]}
```

其中 `packets` 为解析后的 `PacketRecord` 列表。

## 前端分析页

前端访问：

```text
http://127.0.0.1:5173
```

功能：

- 上传 `.pcap`、`.pcapng`、`.cap`
- 点击“分析”触发后端 `tshark` 解析
- 展示文件概览、协议饼图、流量趋势折线图、数据包表格和详情 JSON
- 支持按 IP、端口、协议、摘要搜索数据包
- 支持导出 CSV 和 JSON
