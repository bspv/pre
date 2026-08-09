# Arthas 调试时 Feign 客户端字段为 null 问题排查总结

## 一、问题背景

在使用 Arthas（`vmtool` / `ognl` 等命令）抓取 `HotelOrderController` 实例进行接口调试时，发现注入的
`orderFeignClient`、`orderSnapshotFeignClient` 字段为 `null`；但通过 Apifox 正常发起 HTTP 请求时接口完全可用。

- 相关类：`com.hotel.web.controller.order.HotelOrderController`
- 技术栈：Spring Boot 2.7.18 / Spring Cloud 2021.0.5 / Spring Cloud Alibaba 2.2.9.RELEASE / Lombok 1.18.30

## 二、根本原因

**看到的 `null` 是 CGLIB 代理壳上的字段，真实值在被代理的 target 上。**

1. 字段是通过**构造器注入**进去的，不是缺 `@Autowired`：
   - 类上有 `@RequiredArgsConstructor` + `@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)`；
   - `@FieldDefaults(makeFinal=true)` 把字段变成 `private final`，`@RequiredArgsConstructor` 再把所有 final 字段生成进构造器（已用 javac 反编译验证，构造器包含全部字段）。
   - 因此应用能启动成功就证明这些 Bean 一定注入成功，字段在真实 Bean 上非 null。

2. 该 Controller 会被 **CGLIB 代理**：
   - `SecurityConfig` 上 `@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)`；
   - Controller 方法上有 `@PreAuthorize` / `@Log`（切面）；
   - 启动类 `HotelAdminApplication` 上 `@EnableAspectJAutoProxy(proxyTargetClass = true)` 强制 CGLIB。

3. CGLIB 代理是"空壳代理 + 转发到 target"结构：代理对象由 Objenesis 创建、不走构造器，
   所以**代理壳自身的字段永远是 null**；真正的依赖在它内部转发的 target 上。

## 三、决定性证据

对 `getOrderDetailForOutInterface` 执行 `watch ... 'target.orderFeignClient'`，一次请求打出两条记录：

| 执行对象（method） | target.orderFeignClient |
| --- | --- |
| `HotelOrderController$$EnhancerBySpringCGLIB$$51fe838a`（代理壳） | `null` |
| `com.hotel.web.controller.order.HotelOrderController`（真实 target） | `@$Proxy205`（Feign 客户端，非 null） |

其中真实 target 上的值是 Feign 的 JDK 动态代理：
`HardCodedTarget(type=OrderFeignClient, name=hotel-order, url=http://hotel-order/client/order)`。

结论：问题不在代码、不在注入、不在类加载器，**只在于"抓到了代理壳还是真实 target"**。

## 四、解决办法

### 方案 A：tt 录制 + 回放（推荐，用于"调用接口调试"）

`tt` 捕获方法真实执行时的 `this`，回放时落到真实 target 上执行，字段非 null，Feign 正常发出。

```bash
# 1. 录制
tt -t com.hotel.web.controller.order.HotelOrderController getOrderDetailForOutInterface
# 2. 用 Apifox 触发一次真实请求
# 3. 拿到 INDEX
tt -l
# 4. 回放（会真正调用 hotel-order 服务）
tt -i <INDEX> -p
```

### 方案 B：watch 观察真实请求（只看入参/字段/返回值）

```bash
watch com.hotel.web.controller.order.HotelOrderController getOrderDetailForOutInterface 'target.orderFeignClient' -x 2 -n 3
```

### 方案 C：vmtool 抓真实 target（需要读字段时）

`getInstances` 会同时返回代理壳和真实 target。**用"精确类名白名单"筛选，而不是靠下标或排除代理名。**

```bash
vmtool --action getInstances --className com.hotel.web.controller.order.HotelOrderController \
  --express 'instances.{? #this.getClass().getName().equals("com.hotel.web.controller.order.HotelOrderController")}[0].orderFeignClient' -x 2
```

要点：`{? ...}` 是**选择**（挑出满足条件的元素），`{ ...}` 是**投影**（逐个做变换）。这里用"选择 + 精确类名相等"，直接把真身元素挑出来。

> 不要用 `AopProxyUtils.getSingletonTarget` 去"解包"：
> ```bash
> # 反例：这条可能仍返回代理壳
> --express 'instances.{#t=@...AopProxyUtils@getSingletonTarget(#this), #t==null?#this:#t}[0]...'
> ```
> 原因有二：(1) `getSingletonTarget` 只对标准 `Advised` + `SingletonTargetSource` 代理有效，否则返回
> `null`，`#t==null?#this:#t` 兜底又把壳交回来；(2) 带赋值/逗号序列的 OGNL 投影 `{ }` 套用不稳，可能直接返回原始元素。
> **`getInstances` 已经把真实 target 作为独立元素放进列表了，直接"选真身"即可，无需解包。**

排查时可先打印各实例类名，确认哪个是真身：

```bash
vmtool --action getInstances --className com.hotel.web.controller.order.HotelOrderController \
  --express 'instances.{#this.getClass().getName()}'
# 例: [com.hotel...HotelOrderController, com.hotel...HotelOrderController$$EnhancerBySpringCGLIB$$xxx]
```

## 五、关键认知

### 关键不是"调方法还是读字段"，而是"在谁身上读字段"

壳只转发、不执行——真正的方法体只会在 target 上跑。由此：

- **方法内部的字段读取也是在 target 上进行的**。方法体里的 `orderFeignClient.getOrderDetail(...)` 本质就是读 `this.orderFeignClient` 字段，但此时 `this` = target，读到的是 target 的字段 → 非 null，正常。
- **只有你从外部直接读"壳对象"的字段**（`instances[0].orderFeignClient`、`watch 'target.xxx'` 命中壳那一帧、调试器展开壳）时，才会读到壳那块从没赋值的空槽 → null。
- **推论：在壳上直接调方法同样没问题**——调用会被转发到 target，方法体（连同其内部的字段读取）全落在 target 上。已在 pre 项目验证：对 CGLIB 壳调 `login` 能正常调到 `userService` 并返回业务结果。

| 谁在读、读谁的字段 | 结果 |
| --- | --- |
| 方法体内部读 `this.xxx`（方法在 target 上跑） | 非 null ✅ |
| 你从外部直接读 `壳.xxx`（`instances[壳].xxx`、`watch` 命中壳帧、调试器展开壳） | null ❌ |

所以最初 hotel 的 null 不是"方法读了字段"造成的，而是**你从外部直接读了壳的字段**；若当初直接在壳上调方法，一样能跑通。

### 其它两条

- **真实 target 永远是精确类，任何代理都是另一个类名**：筛选真身用"精确类名匹配"（白名单）最稳，别用"排除 CGLIB"（黑名单）。
- **不用纠结挑实例就用 tt/watch**：它们捕获真实执行的 `this`，天然拿到 target。

## 六、踩过的坑与教训

| 坑 | 说明 | 正确做法 |
| --- | --- | --- |
| `instances[0]` / `instances[1]` 靠下标 | 返回顺序按堆扫描，不稳定，代理和 target 可能对调 | 用精确类名过滤表达式 |
| `!contains("CGLIB")` 黑名单过滤 | 只认 CGLIB 命名；JDK 代理（`com.sun.proxy.$ProxyNN`）、ByteBuddy 等漏网 | 用"精确类名相等"白名单选择（`{? ...equals(...)}`） |
| `getSingletonTarget` 解包 | 非标准 `SingletonTargetSource` 代理会返回 `null`，兜底又交回壳；带赋值的 OGNL 投影也套用不稳 | getInstances 场景无需解包，直接"选真身"元素 |
