package io.github.juwencheng.autoexchange.processor.test;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import io.github.juwencheng.autoexchange.processor.BaseCurrencyAnnotationProcessor;

import javax.tools.JavaFileObject;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

import static com.google.testing.compile.Compiler.javac;
import static com.google.testing.compile.CompilationSubject.assertThat;

@DisplayName("基础货币注解处理器测试")
class BaseCurrencyAnnotationProcessorTest {

    private String readFileUnderDtoAsString(String fileName) throws IOException {
        return Files.readString(Paths.get(String.format("src/test/java/io/github/juwencheng/autoexchange/processor/test/dto/%s.java", fileName)));
    }

    private JavaFileObject buildJavaFileObject(String className) throws IOException {
        return JavaFileObjects.forSourceString("io.github.juwencheng.autoexchange.processor.test.dto." + className, readFileUnderDtoAsString(className));
    }

    @Test
    @DisplayName("当一个类中没有或只有一个@AutoExchangeBaseCurrency注解时，应编译成功")
    void shouldCompileSuccessfullyWithZeroOrOneAnnotation() throws IOException {
        // 1. 定义一个符合规则的Java源文件作为测试输入
        JavaFileObject validSource = buildJavaFileObject("ValidProduct");

        JavaFileObject anotherValidSource = buildJavaFileObject("ValidProductWithoutAutoExchangeBaseCurrency");

        // 2. 使用javac()编译器，并告诉它要运行哪个注解处理器
        Compilation compilation = javac()
                .withProcessors(new BaseCurrencyAnnotationProcessor()) // <-- 关键：指定要测试的处理器
                .compile(validSource, anotherValidSource); // 编译我们的测试源文件

        // 3. 断言编译成功
        assertThat(compilation).succeeded();

        // 4. (可选) 断言没有任何警告或错误
        assertThat(compilation).hadErrorCount(0);
        assertThat(compilation).hadWarningCount(0);
    }

    @Test
    @DisplayName("当一个类中有多个@AutoExchangeBaseCurrency注解时，应编译失败")
    void shouldFailCompilationWithMultipleAnnotations() throws IOException {
        // 1. 定义一个违反规则的Java源文件
        JavaFileObject invalidSource = buildJavaFileObject("MultiBaseCurrencyProduct");

        // 2. 运行编译器
        Compilation compilation = javac()
                .withProcessors(new BaseCurrencyAnnotationProcessor())
                .compile(invalidSource);

        // 3. 断言编译失败
        assertThat(compilation).failed();

        // 4. 断言有一个错误
        assertThat(compilation).hadErrorCount(1);

        // 5. 【核心断言】断言错误信息的内容和位置
        assertThat(compilation)
                .hadErrorContaining("在一个类中只允许有一个 @AutoExchangeBaseCurrency 注解") // 检查错误信息是否包含我们的文本
                .inFile(invalidSource) // 确认错误发生在哪个文件
                .onLine(5) // 确认错误报告在哪一行（通常是类声明那一行）
                .atColumn(8); // 确认错误报告在哪一列
    }
    @Test
    @DisplayName("@AutoExchangeBaseCurrency标注的类型不是String时，应编译失败")
    void shouldFailCompilationWithWrongElementType() throws IOException {
        // 1. 定义一个违反规则的Java源文件
        JavaFileObject invalidSource = buildJavaFileObject("ProductWithWrongBaseCurrencyType");

        // 2. 运行编译器
        Compilation compilation = javac()
                .withProcessors(new BaseCurrencyAnnotationProcessor())
                .compile(invalidSource);

        // 3. 断言编译失败
        assertThat(compilation).failed();

        // 4. 断言有一个错误
        assertThat(compilation).hadErrorCount(1);

        // 5. 【核心断言】断言错误信息的内容和位置
        assertThat(compilation)
                .hadErrorContaining("@AutoExchangeBaseCurrency 注解只能用于String类型字段") // 检查错误信息是否包含我们的文本
                .inFile(invalidSource) // 确认错误发生在哪个文件
                .onLine(10) // 确认错误报告在哪一行（通常是类声明那一行）
                .atColumn(20); // 确认错误报告在哪一列
    }
}
