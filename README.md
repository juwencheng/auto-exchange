# Auto Exchange Rate - Spring Boot Starter

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
![CI](https://github.com/juwencheng/auto-exchange/actions/workflows/maven-publish.yml/badge.svg)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.juwencheng/auto-exchange.svg?style=flat-square)](https://search.maven.org/artifact/io.github.juwencheng/auto-exchange)

一个为Spring Boot应用设计的、功能强大且高度可扩展的自动汇率转换框架。

`auto-exchange-starter`
允许您以一种非侵入的、声明式的方式，为API接口返回的数据动态地附加或修改汇率转换信息。无论是为电商应用的价格添加多币种展示，还是为金融服务的报表提供汇率计算，本框架都能提供优雅、健壮且高性能的解决方案。

## ✨ 核心特性

- **零侵入式设计**: 通过注解即可启用，无需修改现有业务逻辑。
- **两种转换模式**:
    - **追加模式 (Append)**: 动态地为返回的JSON对象添加新的汇率信息字段，不改变原始DTO结构。
    - **原地修改模式 (In-place)**: 通过实现接口，在原始DTO对象上直接设置汇率计算结果。
- **智能模式推断**: 框架可自动根据您的DTO设计（使用注解 vs. 实现接口）选择合适的转换模式，无需手动配置。
- **动态上下文感知**:
    - **目标货币**: 自动从HTTP请求（Header或Parameter）中解析目标货币，并提供全局默认值。
    - **基础货币**: 支持通过动态字段（`@AutoExchangeBaseCurrency`）或全局配置来指定原始价格的货币单位。
- **高度可扩展**:
    - **自定义数据源**: 可轻松替换默认的汇率数据提供者（`IExchangeDataProvider`）。
- **健壮与安全**:
    - **编译时校验**: 通过注解处理器在编译阶段就发现不规范的用法，防止运行时错误。
    - **动态定时任务**: 支持通过CRON表达式配置汇率数据的定时刷新。
    - **风险控制**: 提供多种汇率缺失处理策略（抛异常、返回保护值），防止业务漏洞。
- **专业的API设计**:
    - **清晰的注解体系**: `@EnableAutoExchange`, `@AutoExchangeResponse`, `@AutoExchangeField`等职责分明。
    - **强大的异常处理**: 提供默认的`@ControllerAdvice`，将框架异常转换为结构化的HTTP错误响应。
    - **兼容性**: 允许通过配置文件动态调整AOP切面顺序，以更好地与其他框架集成。

## 🚀 快速上手

### 1. 添加依赖

在您的`pom.xml`中，添加`auto-exchange-spring-boot-starter`依赖。

```xml

<dependency>
    <groupId>io.github.juwencheng</groupId>
    <artifactId>auto-exchange-spring-boot-starter</artifactId>
    <version>${version}</version>
</dependency>
```

### 2. 启用框架

在您的Spring Boot主启动类或任何一个`@Configuration`类上，添加`@EnableAutoExchange`注解。

```java
import io.github.juwencheng.autoexchange.autoconfigure.annotation.EnableAutoExchange;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAutoExchange // 启用自动汇率转换功能
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

### 3. 使用注解

现在，您可以开始使用注解来标记需要处理的数据了。

#### 示例：追加模式 (Append)

假设您有一个`Product` DTO，您希望为`price`字段动态添加一个名为`exchangePrice`的转换结果。

1. **修改您的DTO**:
   ```java
   import io.github.juwencheng.autoexchange.core.annotation.AutoExchangeField;
   import java.math.BigDecimal;

   public class Product {
       private Long id;
       private String name;

       // 标记这个字段需要转换，转换后的新字段名为"exchangePrice"
       // 假设这个价格的基础货币是美元
       @AutoExchangeField(value = "exchangePrice")
       private BigDecimal price;
       
       // ... getters and setters
   }
   ```

2. **在Controller方法上标记**:
   ```java
   import io.github.juwencheng.autoexchange.core.annotation.AutoExchangeResponse;
   import org.springframework.web.bind.annotation.GetMapping;
   import org.springframework.web.bind.annotation.RestController;

   @RestController
   public class ProductController {

       @GetMapping("/product/1")
       @AutoExchangeResponse // 告诉框架需要处理这个方法的返回值
       public Product getProduct() {
           // ...返回一个Product实例
       }
   }
   ```

**请求与响应**:
当您请求 `/product/1` 时，如果原始`Product`对象是：

```json
{
  "id": 1,
  "name": "Laptop",
  "price": 999.00
}
```

框架处理后的JSON响应将是（假设目标货币是CNY，汇率是7.0）：

```json
{
  "id": 1,
  "name": "Laptop",
  "price": 999.00,
  "exchangePrice": {
    "price": 6993.00,
    // <-- 动态添加的字段
    "rate": 7.0,
    "base": "USD",
    "trans": "CNY"
  }
}
```

### 4. 目标币种的设置
目标币种和客户端的需求有关系，所以设计了两种设置目标币种的方式，**如果同时在参数和Header中设置，参数的优先级高于Header**。

1. **参数**，在接口后面增加参数`currency=CNY`指定。接收的key默认是`currency`可以通过`auto.exchange.target-currency-param-name`修改。
2. **Header**，在HTTP请求的`header`中指定。接收的key默认是`X-Target-Currency`，可以通过`auto.exchange.target-currency-header-name`修改。

### 5. 自定义汇率数据源
实现接口`IExchangeDataProvider`，即可传入汇率数据，如
```java
@Component
public class CustomerExchangeDataProvider implements IExchangeDataProvider {
    @Override
    public List<ExchangeInfoRateDto> fetchData() {
        // 返回具体的汇率数据
        return List.of();
    }

    @Override
    public List<ExchangeInfoRateDto> fetchData(LocalDateTime time) {
        return List.of();
    }
}
```

## 🔧 高级配置

您可以在`application.yml`或`application.properties`中对框架进行详细配置。

```yaml
auto:
  exchange:
    # 启动项目后，立即刷新汇率数据，框架没有持久化数据，需要在启动的时候传入
    refresh-on-launch: true
    # 全局默认的基础货币和目标货币
    default-base-currency: "USD"
    default-target-currency: "CNY"

    # 目标货币的解析方式 (按优先级)
    target-currency-param-name: "currency"           # 1. 从URL参数获取
    target-currency-header-name: "X-Target-Currency" # 2. 从HTTP Header获取

    missing-rate:
      # 汇率缺失时的处理策略 (THROW_EXCEPTION, PROTECTIVE, RETURN_NULL)
      # THROW_EXCEPTION: 抛出异常（默认）
      # PROTECTIVE: 使用保护性策略，乘一个很大的数(protective-rate-value)避免造成损失
      # RETURN_NULL: 返回null
      missing-rate-strategy: return_null
      # 当策略为PROTECTIVE时使用的保护性汇率值
      protective-rate-value: 999999.99

    # AOP切面执行顺序，用于解决与其他AOP框架的冲突
    aspect-order: 2147483647 # (默认最后执行)

    # 汇率定时刷新配置
    rate-refresh:
      enabled: false
      cron: "0 0 1 * * ?" # 每天凌晨1点刷新
```

### 动态基础货币

对于计价币种不固定的场景，例如不同商品采用不同的计价币种，您可以使用`@AutoExchangeBaseCurrency`。

```java
import io.github.juwencheng.autoexchange.core.annotation.AutoExchangeField;
import io.github.juwencheng.autoexchange.core.annotation.AutoExchangeBaseCurrency;

public class DynamicProduct {

    @AutoExchangeField("exchangePrice")
    private BigDecimal price;

    @AutoExchangeBaseCurrency // 标记这个字段是上面价格的币种
    private String currency;

    // ...
}
```

### 定时刷新汇率

要启用定时刷新，除了在配置文件中设置`enabled: true`，您还必须在主启动类上添加`@EnableScheduling`注解。

```java

@SpringBootApplication
@EnableAutoExchange
@EnableScheduling // <-- 必须添加以启用Spring的调度功能
public class MyApplication {
}
```

## 🧩 扩展与自定义

本框架通过接口提供了极高的可扩展性。您只需在Spring容器中提供您自己的Bean，框架的`@ConditionalOnMissingBean`
机制就会自动使用您的实现来替换默认实现。

- **`IExchangeDataProvider`**: 实现此接口以对接您自己的外部汇率API或数据源。

## 💡 设计哲学

1. **无侵入性是最高优先级**: 核心目标是让用户以最小的代码改动来集成和使用本框架。
2. **确定性优于强大**: 框架在遇到模棱两可的配置（例如，在父子链上混合使用In-place和Append模式）时，
   **优先使用Append模式，再使用In-place模式**，避免重复计算。
3. **约定优于配置**: 提供了一套合理的默认行为，但允许用户通过配置文件和自定义Bean来覆盖每一个细节。
4. **编译时防御**: 利用注解处理器在编译阶段发现潜在问题，将错误扼杀在摇篮里。

## 🤝 贡献

欢迎提交Issue和Pull Request！

## 📄 许可证

本项目采用 [MIT License](https://opensource.org/licenses/MIT).