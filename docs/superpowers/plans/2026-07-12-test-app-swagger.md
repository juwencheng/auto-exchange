# test-app Swagger 集成 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 test-app 中接入 springdoc UI 与 openapi 增强模块，启动后可通过 Swagger UI 查看 Append 模式动态字段文档。

**Architecture:** test-app 引入 `springdoc-openapi-ui` 与 `auto-exchange-spring-boot-openapi`；因 openapi 模块无自动配置注册，在 `TestApp` 上 `@Import(TranslateOpenApiAutoConfiguration.class)` 显式激活 `TranslateOperationCustomizer`。

**Tech Stack:** Spring Boot 2.7、springdoc-openapi-ui 1.7.0、Maven

---

## File Structure

| 文件 | 职责 |
|------|------|
| `auto-exchange-spring-boot-test-app/pom.xml` | 声明 springdoc 与 openapi 模块依赖 |
| `auto-exchange-spring-boot-test-app/src/main/java/.../TestApp.java` | `@Import` 激活 OpenAPI 自动配置 |

---

### Task 1: 添加 Maven 依赖

**Files:**
- Modify: `auto-exchange-spring-boot-test-app/pom.xml`

- [ ] **Step 1: 在 dependencies 中增加两个依赖**

在现有 `auto-exchange-spring-boot-starter` 与 `spring-boot-starter-test` 之间插入：

```xml
        <dependency>
            <groupId>io.github.juwencheng</groupId>
            <artifactId>auto-exchange-spring-boot-openapi</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-ui</artifactId>
        </dependency>
```

说明：`springdoc-openapi-ui` 版本由父 POM `dependencyManagement` 的 `springdoc.version`（1.7.0）管理，无需写死 version。

- [ ] **Step 2: 验证依赖可解析**

Run: `mvn -pl auto-exchange-spring-boot-test-app -am dependency:resolve -q`
Expected: exit code 0

- [ ] **Step 3: Commit**

```bash
git add auto-exchange-spring-boot-test-app/pom.xml
git commit -m "chore(test-app): 引入 springdoc 与 openapi 模块依赖"
```

---

### Task 2: 激活 OpenAPI 增强配置

**Files:**
- Modify: `auto-exchange-spring-boot-test-app/src/main/java/io/github/juwencheng/autoexchange/testapp/TestApp.java`

- [ ] **Step 1: 更新 TestApp**

完整文件内容应为：

```java
package io.github.juwencheng.autoexchange.testapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import io.github.juwencheng.autoexchange.autoconfigure.annotation.EnableAutoExchange;
import io.github.juwencheng.autoexchange.openapi.TranslateOpenApiAutoConfiguration;

@SpringBootApplication
@EnableAutoExchange
@EnableScheduling
@Import(TranslateOpenApiAutoConfiguration.class)
public class TestApp {
    public static void main(String[] args) {
        SpringApplication.run(TestApp.class, args);
    }
}
```

- [ ] **Step 2: 编译 test-app**

Run: `mvn -pl auto-exchange-spring-boot-test-app -am compile -q`
Expected: exit code 0

- [ ] **Step 3: Commit**

```bash
git add auto-exchange-spring-boot-test-app/src/main/java/io/github/juwencheng/autoexchange/testapp/TestApp.java
git commit -m "feat(test-app): 导入 OpenAPI 增强配置以启用 Swagger 文档"
```

---

### Task 3: 启动验证

**Files:** 无代码变更

- [ ] **Step 1: 启动 TestApp（后台）**

Run: `mvn -pl auto-exchange-spring-boot-test-app -am spring-boot:run`
Expected: 日志出现 `Tomcat started on port(s): 9999`

- [ ] **Step 2: 检查 OpenAPI JSON 含动态字段说明**

Run: `curl -s http://localhost:9999/v3/api-docs | grep -o '动态追加字段' | head -1`
Expected: 输出 `动态追加字段`

- [ ] **Step 3: 确认 Swagger UI 可访问**

Run: `curl -s -o /dev/null -w "%{http_code}" http://localhost:9999/swagger-ui.html`
Expected: `200` 或 `302`

- [ ] **Step 4: 停止 Spring Boot 进程**

---

## Spec Coverage Self-Review

| Spec 要求 | 对应 Task |
|-----------|-----------|
| pom 增加 springdoc + openapi | Task 1 |
| TestApp @Import TranslateOpenApiAutoConfiguration | Task 2 |
| 不改 Controller/DTO/yml | 无对应改动（符合） |
| 启动后 Swagger UI 可看动态字段 | Task 3 |
