# Auto Exchange - Spring Boot Starter

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
![CI](https://github.com/juwencheng/auto-exchange/actions/workflows/maven-publish.yml/badge.svg)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.juwencheng/auto-exchange.svg?style=flat-square)](https://search.maven.org/artifact/io.github.juwencheng/auto-exchange)

一个为 Spring Boot 应用设计的、功能强大且高度可扩展的**自动字段翻译框架**。

框架最初面向**汇率转换**场景，现已抽象为通用的**插件化翻译架构**：对象图遍历、上下文管理和 JSON 序列化只有一套基础设施，汇率转换、字典翻译等不同业务需求以 `FieldTranslator` 插件形式接入。

`auto-exchange-spring-boot-starter` 允许您以一种非侵入的、声明式的方式，为 API 接口返回的数据动态附加或修改翻译结果。无论是为电商应用的价格添加多币种展示，还是将字典表的 key 翻译成 value，本框架都能提供优雅、健壮且高性能的解决方案。

## 项目模块

| 模块 | 说明 |
|------|------|
| `auto-exchange-spring-boot-core` | 核心逻辑：通用翻译框架（`translate` 包）+ 汇率转换实现 |
| `auto-exchange-spring-boot-processor` | 编译时注解校验 |
| `auto-exchange-spring-boot-autoconfigure` | Spring Boot 自动配置 |
| `auto-exchange-spring-boot-starter` | 聚合依赖，业务项目直接引入 |
| `auto-exchange-spring-boot-openapi` | 可选模块，自动增强 Swagger/OpenAPI 文档 |
| `auto-exchange-spring-boot-test-app` | 演示与集成测试 |

## 架构概览

```
触发层   @AutoExchangeResponse / @TranslateResponse
           ↓
遍历层   TranslateStrategy（通用对象图遍历，只有一套）
           ↓
翻译层   FieldTranslator 插件
           ├── ExchangeFieldTranslator   汇率转换
           ├── DictFieldTranslator       字典翻译
           └── 自定义 FieldTranslator    业务扩展
           ↓
序列化层 TranslateAppendingBeanSerializer（统一追加 JSON 字段）
```

## ✨ 核心特性

- **零侵入式设计**: 通过注解即可启用，无需修改现有业务逻辑。
- **插件化翻译架构**: 对象图遍历、ThreadLocal 上下文、Jackson 序列化器各只有一套，新增翻译类型只需实现 `FieldTranslator` 接口。
- **两种转换模式**（汇率场景）:
    - **追加模式 (Append)**: 动态地为返回的 JSON 对象添加新的翻译结果字段，不改变原始 DTO 结构。
    - **原地修改模式 (In-place)**: 通过实现 `IApplyExchange` 接口，在原始 DTO 对象上直接设置汇率计算结果。
- **多种翻译器内置**:
    - **汇率转换**: `@AutoExchangeField` / `@TranslateField(translator = ExchangeFieldTranslator.class)`
    - **字典翻译**: `@TranslateField(translator = DictFieldTranslator.class, args = "dict_type")`
- **动态上下文感知**:
    - **目标货币**: 自动从 HTTP 请求（Header 或 Parameter）中解析目标货币，并提供全局默认值。
    - **基础货币**: 支持通过动态字段（`@AutoExchangeBaseCurrency`）或全局配置来指定原始价格的货币单位。
- **高度可扩展**:
    - **自定义汇率数据源**: 实现 `IExchangeDataProvider` 对接外部汇率 API。
    - **自定义字典数据源**: 实现 `IDictDataProvider` 对接字典表或缓存。
    - **自定义翻译器**: 实现 `FieldTranslator` 接口，注册为 Spring Bean 即可使用。
- **OpenAPI 文档增强**（可选）: 引入 `auto-exchange-spring-boot-openapi` 模块后，自动在 Swagger 文档中描述 Append 模式动态追加的虚拟字段。
- **向后兼容**: 旧注解 `@AutoExchangeField`、`@AutoExchangeResponse` 完全保留，现有项目无需改动。
- **健壮与安全**:
    - **编译时校验**: 通过注解处理器在编译阶段发现不规范的用法。
    - **动态定时任务**: 支持通过 CRON 表达式配置汇率数据的定时刷新。
    - **风险控制**: 提供多种汇率缺失处理策略（抛异常、返回保护值、返回 null）。

## 🚀 快速上手

### 1. 添加依赖

在您的 `pom.xml` 中，添加 `auto-exchange-spring-boot-starter` 依赖。

```xml
<dependency>
    <groupId>io.github.juwencheng</groupId>
    <artifactId>auto-exchange-spring-boot-starter</artifactId>
    <version>0.3.4</version>
</dependency>
```

如需 OpenAPI 文档增强，额外引入：

```xml
<dependency>
    <groupId>io.github.juwencheng</groupId>
    <artifactId>auto-exchange-spring-boot-openapi</artifactId>
    <version>0.3.4</version>
</dependency>
```

### 2. 启用框架

在您的 Spring Boot 主启动类或任何一个 `@Configuration` 类上，添加 `@EnableAutoExchange` 注解。

```java
import io.github.juwencheng.autoexchange.autoconfigure.annotation.EnableAutoExchange;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAutoExchange
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

### 3. 汇率转换（追加模式）

假设您有一个 `Product` DTO，您希望为 `price` 字段动态添加一个名为 `exchangePrice` 的转换结果。

**修改 DTO：**

```java
import io.github.juwencheng.autoexchange.core.annotation.AutoExchangeField;
import java.math.BigDecimal;

public class Product {
    private Long id;
    private String name;

    @AutoExchangeField(value = "exchangePrice")
    private BigDecimal price;

    // ... getters and setters
}
```

**在 Controller 方法上标记：**

```java
import io.github.juwencheng.autoexchange.core.annotation.AutoExchangeResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductController {

    @GetMapping("/product/1")
    @AutoExchangeResponse
    public Product getProduct() {
        // ...返回一个 Product 实例
    }
}
```

**请求与响应：**

原始 JSON：

```json
{
  "id": 1,
  "name": "Laptop",
  "price": 999.00
}
```

框架处理后的 JSON 响应（假设目标货币是 CNY，汇率是 7.0）：

```json
{
  "id": 1,
  "name": "Laptop",
  "price": 999.00,
  "exchangePrice": {
    "price": 6993.00,
    "rate": 7.0,
    "base": "USD",
    "trans": "CNY"
  }
}
```

### 4. 字典翻译

使用通用 `@TranslateField` 注解配合 `DictFieldTranslator`，将字典 key 翻译为 value。

**修改 DTO：**

```java
import io.github.juwencheng.autoexchange.core.translate.DictFieldTranslator;
import io.github.juwencheng.autoexchange.core.translate.TranslateField;

public class Order {
    private String orderId;

    @TranslateField(value = "statusText", translator = DictFieldTranslator.class, args = "order_status")
    private Integer status;

    @TranslateField(value = "paymentTypeText", translator = DictFieldTranslator.class, args = "payment_type")
    private String paymentType;

    // ... getters and setters
}
```

**提供字典数据源：**

```java
@Component
public class MyDictDataProvider implements IDictDataProvider {
    @Override
    public String getDictValue(String dictType, String key) {
        // 从字典表或缓存中查找
        return dictService.getLabel(dictType, key);
    }
}
```

**触发翻译：**

```java
@GetMapping("/order/1")
@TranslateResponse  // 纯通用翻译，只处理 @TranslateField 标注的字段
public Order getOrder() { ... }

// 或者使用 @AutoExchangeResponse，同时触发汇率转换和通用翻译
@GetMapping("/order/1")
@AutoExchangeResponse
public Order getOrder() { ... }
```

**响应示例：**

```json
{
  "orderId": "ORDER-100",
  "status": 1,
  "paymentType": "ALIPAY",
  "statusText": "已支付",
  "paymentTypeText": "支付宝"
}
```

### 5. 汇率转换 + 字典翻译共存

两种注解可以在同一个 DTO 上混用：

```java
public class OrderWithDict {
    @AutoExchangeField("amountInCny")
    private BigDecimal amount;

    @AutoExchangeBaseCurrency
    private String currency = "USD";

    @TranslateField(value = "statusText", translator = DictFieldTranslator.class, args = "order_status")
    private Integer status;
}
```

在 Controller 方法上使用 `@AutoExchangeResponse` 即可同时触发汇率转换和字典翻译。

### 6. 目标币种的设置

目标币种和客户端的需求有关系，所以设计了两种设置目标币种的方式，**如果同时在参数和 Header 中设置，参数的优先级高于 Header**。

1. **参数**：在接口后面增加参数 `currency=CNY` 指定。接收的 key 默认是 `currency`，可以通过 `auto.exchange.target-currency-param-name` 修改。
2. **Header**：在 HTTP 请求的 `header` 中指定。接收的 key 默认是 `X-Target-Currency`，可以通过 `auto.exchange.target-currency-header-name` 修改。

### 7. 自定义汇率数据源

实现接口 `IExchangeDataProvider`，即可传入汇率数据：

```java
@Component
public class CustomerExchangeDataProvider implements IExchangeDataProvider {
    @Override
    public List<ExchangeInfoRateDto> fetchData() {
        return List.of(
            new ExchangeInfoRateDto("USD", "CNY", new BigDecimal("7.3")),
            new ExchangeInfoRateDto("CNY", "CNY", BigDecimal.ONE)
        );
    }

    @Override
    public List<ExchangeInfoRateDto> fetchData(LocalDateTime time) {
        return fetchData();
    }
}
```

## 🔧 高级配置

您可以在 `application.yml` 或 `application.properties` 中对框架进行详细配置。

```yaml
auto:
  exchange:
    refresh-on-launch: true
    default-base-currency: "USD"
    default-target-currency: "CNY"

    target-currency-param-name: "currency"
    target-currency-header-name: "X-Target-Currency"

    missing-rate:
      missing-rate-strategy: return_null  # THROW_EXCEPTION | PROTECTIVE | RETURN_NULL
      protective-rate-value: 999999.99

    aspect-order: 2147483647

    # 翻译结果缓存（@TranslateField 链路）
    translate-cache:
      enabled: true
      dict-ttl: 1h      # 字典翻译默认缓存 1 小时
      exchange-ttl: 1d  # 汇率翻译结果默认缓存 1 天

    rate-refresh:
      enabled: false
      cron: "0 0 1 * * ?"
```

### 动态基础货币

对于计价币种不固定的场景，您可以使用 `@AutoExchangeBaseCurrency`：

```java
public class DynamicProduct {
    @AutoExchangeField("exchangePrice")
    private BigDecimal price;

    @AutoExchangeBaseCurrency
    private String currency;
}
```

### 定时刷新汇率

要启用定时刷新，除了在配置文件中设置 `enabled: true`，您还必须在主启动类上添加 `@EnableScheduling` 注解。

```java
@SpringBootApplication
@EnableAutoExchange
@EnableScheduling
public class MyApplication { }
```

## 🧩 扩展与自定义

本框架通过接口和 SPI 提供了极高的可扩展性。您只需在 Spring 容器中提供您自己的 Bean，框架的 `@ConditionalOnMissingBean` 机制就会自动使用您的实现来替换默认实现。

### 内置扩展点

| 接口 | 用途 |
|------|------|
| `IExchangeDataProvider` | 对接外部汇率 API 或数据源 |
| `IDictDataProvider` | 对接字典表或缓存 |
| `TranslateContextContributor` | 向翻译上下文贡献请求级属性（如 locale） |
| `FieldTranslator` | 实现自定义翻译逻辑 |
| `TranslateCacheStrategy` | 定义缓存 key、TTL、存储绑定 |
| `TranslateCacheStore` | 实现缓存存储（内存、Redis 等） |

### 自定义翻译器

只需 3 步即可扩展新的翻译类型：

**1. 实现 `FieldTranslator` 接口：**

```java
public class RegionFieldTranslator implements FieldTranslator {
    @Override
    public Object translate(Object fieldValue, TranslateContext context) {
        String regionCode = String.valueOf(fieldValue);
        return regionService.getRegionName(regionCode);
    }
}
```

**2. 注册为 Spring Bean：**

```java
@Bean
public RegionFieldTranslator regionFieldTranslator(RegionService regionService) {
    return new RegionFieldTranslator(regionService);
}
```

**3. 在 DTO 字段上使用：**

```java
@TranslateField(value = "regionName", translator = RegionFieldTranslator.class)
private String regionCode;
```

对象图遍历逻辑无需重复编写，`BeanSerializerModifier` 只有一个，ThreadLocal 上下文统一管理。

### 翻译缓存策略

框架提供**可插拔的翻译结果缓存**，支持按翻译器/注解分别配置 TTL 和存储后端。

#### 两层缓存说明

| 层级 | 作用范围 | 实现 | 典型 TTL |
|------|----------|------|----------|
| **数据源缓存** | 汇率全量表 | `ExchangeManager` 内存 Map | 由定时刷新 CRON 决定（如 1 天） |
| **翻译结果缓存** | 单个 `@TranslateField` 的 translate 输出 | `TranslateCacheManager` | 字典 1h / 汇率结果 1d（可配） |

字典翻译只有翻译结果缓存层；汇率场景两层并存。

#### 默认行为

- **存储**：默认 `InMemoryTranslateCacheStore`（进程内内存）
- **策略绑定**：
  - `DictFieldTranslator` → `DictTranslateCacheStrategy`，key：`dict:{dictType}:{key}`，TTL 1h
  - `ExchangeFieldTranslator` → `ExchangeTranslateCacheStrategy`，key：`exchange:{base}:{target}:{value}`，TTL 1d
- **注解级覆盖**：在 `@TranslateField` 上指定 `cacheStrategy`

```java
// 使用翻译器默认策略（字典 1h）
@TranslateField(value = "statusText", translator = DictFieldTranslator.class, args = "order_status")
private Integer status;

// 显式禁用缓存
@TranslateField(value = "statusText", translator = DictFieldTranslator.class,
        args = "order_status", cacheStrategy = NoCacheStrategy.class)
private Integer status;

// 自定义策略（自行实现 TranslateCacheStrategy）
@TranslateField(value = "regionName", translator = RegionFieldTranslator.class,
        cacheStrategy = RegionTranslateCacheStrategy.class)
private String regionCode;
```

#### 自定义 Redis 存储

实现 `TranslateCacheStore` 并注册为 Spring Bean，`name()` 返回 `"redis"`；在自定义 `TranslateCacheStrategy` 中 `storeName()` 返回 `"redis"` 即可绑定：

```java
@Component
public class RedisTranslateCacheStore implements TranslateCacheStore {
    @Override
    public String name() { return "redis"; }

    @Override
    public Optional<Object> get(String key) { /* ... */ }

    @Override
    public void put(String key, Object value, Duration ttl) { /* ... */ }

    @Override
    public void evict(String key) { /* ... */ }
}

public class RegionTranslateCacheStrategy implements TranslateCacheStrategy {
    @Override
    public String storeName() { return "redis"; }

    @Override
    public Duration ttl() { return Duration.ofMinutes(30); }

    @Override
    public String buildKey(TranslateCacheKeyContext ctx) {
        return "region:" + ctx.getFieldValue();
    }
}
```

框架只约定 **key 构建** 和 **TTL** 的 SPI；具体存内存还是 Redis 由 `TranslateCacheStore` 实现决定。

### OpenAPI 文档增强

Append 模式下 JSON 输出结构与 Java DTO 类不一致，Swagger 自动生成的文档无法反映实际响应字段。引入 `auto-exchange-spring-boot-openapi` 模块后，框架会通过 springdoc 的 `OperationCustomizer` 自动在 API 文档中描述动态追加的虚拟字段。

该模块仅在项目引入 springdoc 依赖时激活（`@ConditionalOnClass`），不影响未使用 Swagger 的项目。

## 📋 注解参考

| 注解 | 作用域 | 说明 |
|------|--------|------|
| `@EnableAutoExchange` | 类 | 启用框架（入口） |
| `@AutoExchangeResponse` | 方法 | 触发汇率转换 + 通用翻译 |
| `@TranslateResponse` | 方法 | 仅触发通用翻译（`@TranslateField`） |
| `@AutoExchangeField` | 字段 | 标记需要汇率转换的字段（旧注解，向后兼容） |
| `@AutoExchangeBaseCurrency` | 字段 | 标记对象的基础货币字段 |
| `@TranslateField` | 字段 | 通用翻译注解，指定翻译器和参数 |

## 💡 设计哲学

1. **无侵入性是最高优先级**: 核心目标是让用户以最小的代码改动来集成和使用本框架。
2. **插件化优于重复**: 对象图遍历、序列化、上下文管理各只有一套，不同翻译逻辑通过 `FieldTranslator` 插件接入。
3. **确定性优于强大**: 框架在遇到模棱两可的配置时，**优先使用 Append 模式，再使用 In-place 模式**，避免重复计算。
4. **约定优于配置**: 提供了一套合理的默认行为，但允许用户通过配置文件和自定义 Bean 来覆盖每一个细节。
5. **向后兼容**: 旧注解和 API 完全保留，新功能通过通用翻译框架渐进式接入。
6. **编译时防御**: 利用注解处理器在编译阶段发现潜在问题，将错误扼杀在摇篮里。

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

本项目采用 [MIT License](https://opensource.org/licenses/MIT).
