# Auto Exchange - Spring Boot Starter

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

**Phase 3**：`auto-exchange` 已精简为基于 [field-translate](https://github.com/juwencheng/field-translate) 的**汇率转换插件**。通用字段翻译、Jackson 序列化、字典翻译等能力由 `field-translate` 提供。

- Spring Boot **3.3.7** / Java **17**
- 版本：**1.0.0-SNAPSHOT**
- 依赖：`field-translate-spring-boot-starter` + `auto-exchange-spring-boot-starter`

## 项目模块

| 模块 | 说明 |
|------|------|
| `auto-exchange-spring-boot-core` | 汇率插件核心：`ExchangeManager`、`ExchangeFieldTranslator` 等 |
| `auto-exchange-spring-boot-autoconfigure` | `ExchangeAutoConfiguration` 自动配置 |
| `auto-exchange-spring-boot-starter` | 聚合 starter（含 field-translate） |
| `auto-exchange-spring-boot-openapi` | springdoc 2.x OpenAPI 文档增强 |
| `auto-exchange-spring-boot-test-app` | 演示与集成测试 |

> `auto-exchange-spring-boot-processor` 已移除；编译时注解校验由 field-translate 承担。

## 快速上手

### 1. 添加依赖

```xml
<dependency>
    <groupId>io.github.juwencheng</groupId>
    <artifactId>auto-exchange-spring-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
<!-- 字典翻译（可选） -->
<dependency>
    <groupId>io.github.juwencheng</groupId>
    <artifactId>field-translate-dict</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. 启用框架

```java
@SpringBootApplication
@EnableFieldTranslate  // 来自 field-translate
public class MyApplication { }
```

`@EnableAutoExchange` 已废弃（空标记），请使用 `@EnableFieldTranslate` + starter 自动配置。

### 3. 汇率转换

```java
public class Product {
    @TranslateField(value = "priceInCny", translator = ExchangeFieldTranslator.class)
    private BigDecimal priceUsd;

    @ExchangeBaseCurrency
    private String currency;
}

@GetMapping("/product")
@TranslateResponse
public Product getProduct() { ... }
```

## 配置

```yaml
auto:
  exchange:
    refresh-on-launch: true
    default-base-currency: CNY
    default-target-currency: CNY
    target-currency-param-name: currency
    target-currency-header-name: X-Target-Currency
    missing-rate:
      missing-rate-strategy: return_null
    translate-cache:
      enabled: true
      exchange-ttl: 1d
    rate-refresh:
      enabled: false
      cron: "0 30 0 * * ?"

field:
  translate:
    enabled: true
```

## 从 0.3.x 迁移（Phase 3）

| 旧 API | 新 API |
|--------|--------|
| `@EnableAutoExchange` | `@EnableFieldTranslate` + starter |
| `@AutoExchangeResponse` | `@TranslateResponse`（field-translate） |
| `@AutoExchangeField` | `@TranslateField(translator = ExchangeFieldTranslator.class)` |
| `@AutoExchangeBaseCurrency` | `@ExchangeBaseCurrency` |
| `IApplyExchange` 原地修改 | 移除，统一使用 Append 模式 |
| `IDictDataProvider`（core） | `field-translate-dict` 的 `IDictDataProvider` |
| `DictFieldTranslator`（core） | `field-translate-dict` |
| `auto.exchange.translate-cache.dict-ttl` | 移至 `field.translate.dict` |
| `auto.exchange.aspect-order` | 移至 `field.translate.aspect-order` |
| `springdoc-openapi-ui` 1.x | `springdoc-openapi-starter-webmvc-ui` 2.6.0 |

**破坏性变更**：
- 移除双轨汇率（`AutoExchangeAspect`、`@AutoExchangeField`、In-place 模式）
- 移除 `processor` 模块
- 需同时升级 Spring Boot 3 与 Java 17

## OpenAPI

引入 `auto-exchange-spring-boot-openapi` + `springdoc-openapi-starter-webmvc-ui`，通过 `AutoConfiguration.imports` 自动注册 `ExchangeOperationCustomizer`，为 `@TranslateField(translator = ExchangeFieldTranslator.class)` 字段生成 `Object{base, trans, rate, price}` 文档说明。

## 许可证

MIT License
