# test-app 集成 Swagger / OpenAPI 增强

**日期:** 2026-07-12  
**状态:** 已批准  
**范围:** 仅 `auto-exchange-spring-boot-test-app`

## 背景

项目已有 `auto-exchange-spring-boot-openapi` 模块，通过 springdoc 的 `OperationCustomizer` 在 API 文档中描述 Append 模式动态追加的虚拟字段。test-app 尚未引入 springdoc 与该模块，无法直观查看文档呈现效果。

openapi 模块当前缺少 `spring.factories` / `AutoConfiguration.imports`，不会自动装配；本次在 test-app 侧用 `@Import` 显式激活，不修改库本身。

## 目标

在 test-app 中集成 springdoc UI 与 openapi 增强模块，启动后可通过 Swagger UI 查看带 `@AutoExchangeResponse` / `@TranslateResponse` 接口的动态字段文档说明。

## 方案

选用「test-app 加依赖 + `@Import` 配置类」：

1. `pom.xml` 增加依赖：
   - `org.springdoc:springdoc-openapi-ui`（版本由父 POM `springdoc.version` = 1.7.0 管理）
   - `io.github.juwencheng:auto-exchange-spring-boot-openapi`（同版本）
2. `TestApp` 增加 `@Import(TranslateOpenApiAutoConfiguration.class)`，注册 `TranslateOperationCustomizer`
3. 不修改 Controller、DTO、`application.yml`（端口保持 `9999`）

## 非目标

- 不为 openapi 模块补充自动配置注册（`spring.factories` 等）
- 不改动库代码或新增额外 OpenAPI 元信息 Bean
- 不调整现有测试用例（除非集成导致编译/启动失败）

## 验证方式

1. 启动 `TestApp`
2. 访问 `http://localhost:9999/swagger-ui.html`
3. 打开带 `@AutoExchangeResponse` / `@TranslateResponse` 的接口，确认 description 中出现「动态追加字段（Append 模式）」表格

## 涉及文件

| 文件 | 变更 |
|------|------|
| `auto-exchange-spring-boot-test-app/pom.xml` | 增加 springdoc 与 openapi 模块依赖 |
| `auto-exchange-spring-boot-test-app/.../TestApp.java` | `@Import(TranslateOpenApiAutoConfiguration.class)` |
