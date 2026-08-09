# CSV 导入 / 性能调优 / 数据库优化 总结

> 本文档记录围绕 `hotel_detail`（酒店/人员数据）大批量 CSV 导入所做的一系列工作：CSV 解析修复、按表头映射、多线程导入、性能对比调优、HikariCP 连接池优化、数据库表结构优化，以及过程中的经验教训。

---

## 1. 背景

- 目标：把大体量 CSV 数据导入 MySQL 表 `hotel_detail`（33 列）。
- 数据集：
  - 单文件 `1-200W.csv`：约 348 MB，物理行 **2,000,094**。
  - 目录 `D:\tools\2000W`：**11 个 csv**，合计约 **3.15 GB**，约 **2000 万行**。
- 源数据是**不规范的脏 CSV**：存在未加引号的逗号、字段内换行、以及落单的引号等。

---

## 2. CSV 解析的关键修复（CsvUtil / CsvConfig）

### 2.1 问题：读取端写死了引号/转义符，导致约 32% 的行被吞并

- 现象：`1-200W.csv` 物理行 200 万，但最初只解析出约 **136 万**条记录，约 **63.7 万行被“吞掉”**。
- 根因：`CsvUtil` 的**读取路径**把 opencsv 的引号符 `"` 和转义符 `\` **写死为默认值**（`buildCsvReader` / `buildCsvToBean` 用的是 `DEFAULT_QUOTE` / `DEFAULT_ESCAPE`），完全忽略了 `CsvConfig` 里的配置。对脏数据来说，一个落单的 `"` 会让 opencsv 一直吞到下一个引号为止，把很多物理行合并成一条记录。

### 2.2 修复

- 让读取端改用配置值：
  - `buildCsvReader`：`.withQuoteChar(conf.getQuoteChar()).withEscapeChar(conf.getEscapeChar())`
  - `buildCsvToBean`：同上
- `CsvConfig` 的 `quoteChar`/`escapeChar` 默认即为 `NO_QUOTE_CHARACTER`/`NO_ESCAPE_CHARACTER`（读写共用）。**默认不做引号/转义解析**，于是“每个物理行 = 一条记录”，落单的 `"`/`\` 不会再吞行。
- 若数据是规范 CSV（含被引号保护的分隔符/换行），读写时都显式设成 `'"'` 即可。

### 2.3 修复效果

| | 修复前 | 修复后 |
|---|---|---|
| 成功导入 | 1,363,104 | **1,997,802** |
| 跳过（列数≠33） | 147 | 2,292 |
| 解析记录合计 | 1,363,251 | **2,000,094**（= 物理行数） |

数据丢失问题解决，且解析更快（不再进入“引号态”拼接超长字段）。

---

## 3. 按表头映射导入（HotelDetailPo + batchReadAsObject）

### 3.1 认知纠正：“按表头读取”并不能自动解决列数问题

- opencsv 的对象映射在**字段数与表头不一致**时会抛 `CsvRequiredFieldEmptyException`，而 `CsvToBean` 默认 `throwExceptions=true`，**第一条脏行就会中断整个文件导入**。所以它比“按数组读取”更脆。

### 3.2 改动

- 修正 `HotelDetailPo` 上与真实表头对不上的 `@CsvBindByName`（否则静默映射为 null）：
  - `dDescriot` → `Descriot`
  - `District_1`~`District_6` → `District1`~`District6`
  - `LastNum` → `LastNm`
- `CsvConfig` 新增 `throwExceptions`（默认 `false`）：遇到错误行**跳过并继续**，容忍脏数据。
- `CsvUtil.buildCsvToBean` 增加 `.withThrowExceptions(conf.isThrowExceptions())`，并新增 `logSkippedRows`：读完统计 `csvToBean.getCapturedExceptions().size()` 并告警被跳过的行数，避免静默丢数据。

### 3.3 与数组方式对比（同一 1-200W.csv）

| | 数组 `batchRead` | 按表头 `batchReadAsObject` |
|---|---|---|
| 成功导入 | 1,997,802 | 1,997,802 |
| 跳过脏行 | 2,292 | 2,292 |
| 总耗时 | 194 s | 212 s |
| 平均速率 | 10,276 行/秒 | 9,405 行/秒 |
| 稳态速率 | ~14k 行/秒 | ~14k 行/秒 |

结论：正确性一致；对象映射整体慢约 9%（反射 bean 映射预热更久），稳态相当；可读性更好、不依赖列顺序。

---

## 4. 多线程目录导入（importHotelDetailFromDir）

- 并行粒度为**文件级**：每个 csv 文件由线程池中的一个线程独立读取并分批写库，文件之间并行。
- 线程数 `IMPORT_THREADS = 8`，取 `min(线程数, 文件数)`。**刻意 ≤ Hikari 的 maximum-pool-size**，因为每个线程 `batchInsert` 时会借一条连接。
- `batchInsert` 每线程各自开 `SqlSession`；`SqlSessionFactory` 线程安全；转换方法无状态 → 并发写库安全。

首次导入 2000 万时踩到列宽问题（见第 6 节），加宽后跑通：
- 结果：**20,039,769 行**，总耗时 **1323 秒（~22 分钟）**，聚合 **15,141 行/秒**。

---

## 5. 性能对比与调优结论（2000 万级目录导入）

| 版本 | 批大小 | 提交间隔 | 总耗时 | 平均速率 |
|---|---|---|---|---|
| 基线 | 1000 | 每 1,000 行提交 | 1323 s | 15,141 行/秒 |
| 调优 v2 | 2000 | 每 50,000 行提交 | **854 s** | **23,462 行/秒** |
| 调优 v3（零干扰复测） | 5000 | 每 10,000 行提交 | **817 s** | **24,511 行/秒** |

### 结论
- **降低提交频率是关键杠杆**：从“每批提交”改为“每几万行提交一次”，大幅减少 `commit`/`fsync` 次数，是提速主因（v2/v3 相对基线快约 35–38%）。
- 批大小 2000 与 5000、提交间隔 1 万与 5 万，差异在噪声范围内。
- **瓶颈在共享的 DB 写盘通道**（`commit` 的 redo-log fsync + InnoDB 写盘）：多线程只带来约 **1.5×** 提升，不是 8×——CPU/解析不是瓶颈。
  - 佐证：满负载（8 线程争）单文件 ~1,700–3,600 行/秒；末尾只剩 1–2 线程时单文件冲到 ~7,400 行/秒。
- 单线程写库的实现要点：单文件复用一个 `ExecutorType.BATCH` 的 `SqlSession`，每批 `flushStatements()` 发送到 DB，但每累计 N 行才 `commit()` 一次。
- 想再快需动 DB 层（如导入期临时 `innodb_flush_log_at_trx_commit=2`、加大 redo log），属数据库配置范畴。

> 落地建议参数：**8 线程 + 批 2000~5000 + 每 1~5 万行提交一次**，约 24k 行/秒。

---

## 6. HikariCP 连接池优化

三个环境（dev/test/prod）统一调整：

| 项 | 改前 | 改后 | 原因 |
|---|---|---|---|
| `minimum-idle` | 1 | 10 | 固定大小池，避免空闲后突发流量临时建连的延迟抖动（HikariCP 官方建议 min==max） |
| `maximum-pool-size` | 20 | 10 | 池不需要很大；连接只在极短事务内被占用；过大反增 DB 上下文开销 |
| `connection-test-query: SELECT 1` | 有 | **删除** | Connector/J 8 支持 JDBC4 `isValid()`，比每次跑查询更轻量 |
| `rewriteBatchedStatements=true` | dev 有 | test/prod 也加上 | 批量插入重写为多值 INSERT，减少往返；对非批量操作无影响 |

补充说明：
- 固定池后 `idle-timeout` 实际不再生效（无害）。
- 生产多实例时注意：总连接数 ≈ 实例数 × `maximum-pool-size`，要小于 MySQL `max_connections`（默认 151）。

---

## 7. 数据库表结构优化（hotel_detail）

### 7.1 加索引（最关键，先导入后建索引）

`utf8mb4` 下长列用**前缀索引**，够用又省空间：

```sql
ALTER TABLE hotel_detail
  ADD INDEX idx_ctf_id  (ctf_id(20)),
  ADD INDEX idx_mobile  (mobile(15)),
  ADD INDEX idx_name    (name(20)),
  ADD INDEX idx_card_no (card_no(20));
```

### 7.2 列类型瘦身（严格模式，防止静默截断）

把明确很短的码/电话/日期类字段从 `varchar(255)` 缩到 `varchar(50)`：
`gender, zip, c_zip, ctf_tp, mobile, tel, fax, c_tel, birthday, version`

> 注意：varchar 声明长度**不影响已存数据的磁盘占用**，只影响排序/临时表内存分配与索引大小。所以纯展示、且无索引/排序的长文本列（descriot/address/company 等）保持不变。

### 7.3 主键瘦身

`data_id`：`bigint` → **`int unsigned`**（2000 万 < 21 亿，够用；每行及每个二级索引都更小）。

### 7.4 dirty 列：保持不动

- `dirty` 约 **95% 是空串/非数字**（一次清洗 UPDATE 匹配到约 1917 万行才发现）。
- 转 `int` 等于把该列 95% 变成 NULL/0 —— 无意义甚至有害，因此**保留 varchar(255)**。

### 7.5 最终结构（核验通过）

- `data_id int unsigned`（主键，自增）
- `gender/zip/c_zip/ctf_tp/mobile/tel/fax/c_tel/birthday/version` = `varchar(50)`
- `dirty` = `varchar(255)`（未动），`name` `varchar(255)`、`address` `varchar(500)` 等保持
- 索引：`idx_ctf_id / idx_mobile / idx_name / idx_card_no` + `PRIMARY(data_id)`
- 数据完好：整表 COPY 重建原样复制全部 2000 万行

---

## 8. 修改的文件清单

- `src/main/java/com/bazzi/core/util/CsvUtil.java`：读取端改用 `CsvConfig` 的引号/转义；`buildCsvToBean` 加 `withThrowExceptions`；新增 `logSkippedRows`。
- `src/main/java/com/bazzi/core/util/CsvConfig.java`：`quoteChar/escapeChar` 注释更新为读写共用；新增 `throwExceptions`（默认 false）。
- `src/test/java/com/bazzi/tests/service/CsvImportTest.java`：`importHotelDetail` 改用按表头映射并加计时/进度；新增 `importHotelDetailFromDir`（多线程目录导入）与 `importHotelDetailFromDirTuned`（调优版）。
- `src/test/java/com/bazzi/tests/service/HotelDetailPo.java`：修正 3 处 `@CsvBindByName` 列名。
- `src/main/resources/application-dev.yml` / `application-test.yml` / `application-prod.yml`：HikariCP 参数调整；test/prod 补 `rewriteBatchedStatements=true`。
- 数据库 `hotel_detail`：加索引 + 列类型瘦身 + 主键改 int（DDL，非代码）。

---

## 9. 经验教训 / 注意事项

1. **不规范 CSV 一定要显式控制引号/转义**：读取端默认引号解析会“吞行”，脏数据场景应关闭引号/转义（每物理行一条记录），并统计被跳过的行数，绝不静默丢数据。
2. **批量导入的瓶颈通常在提交频率**：降低 `commit`/`fsync` 次数（每几万行提交一次）比加线程更有效；写库是共享瓶颈，多线程收益递减。
3. **列宽要按真实数据定**：脏数据里某些字段可能超出预期（如 `taste` 超 60 触发 `Data too long`）。缩列务必在**严格模式**（`STRICT_TRANS_TABLES`）下做——超长会**报错回滚**而不是**静默截断丢数据**。
4. **别用强杀进程的方式关闭 DB 客户端**：会留下未提交事务，触发**超长回滚**（本次约 1917 万行回滚，阻塞了后续所有改表约 1 小时）。清理连接请用 `KILL <id>`。
5. **慢盘上的整表重建极慢且不可中断**：本机 `ALTER`（整表 COPY + 重建 4 索引）耗时约 1.5 小时；中途中断会触发又一次巨型回滚。大表结构变更应放在低峰、并把多个变更合并到一条 `ALTER` 减少重建次数。
6. **先导入、后建索引**：导入期无二级索引最快；导入完成再建索引/改类型。
