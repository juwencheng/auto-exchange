package tech.baizi.autoexchange.processor.test;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tech.baizi.autoexchange.processor.AutoExchangeFieldAnnotationProcessor;

import javax.tools.JavaFileObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

@DisplayName("换汇字段注解处理器测试")
class AutoExchangeFieldAnnotationProcessorTest {

    private String readFileUnderDtoAsString(String fileName) throws IOException {
        return Files.readString(Paths.get(String.format("src/test/java/tech/baizi/autoexchange/processor/test/dto/%s.java", fileName)));
    }

    private JavaFileObject buildJavaFileObject(String className) throws IOException {
        return JavaFileObjects.forSourceString("tech.baizi.autoexchange.processor.test.dto." + className, readFileUnderDtoAsString(className));
    }

    @Test
    @DisplayName("当@AutoExchangeField标注的是数值类型的数据时，应编译成功")
    void shouldCompileSuccessfullyWithZeroOrOneAnnotation() throws IOException {
        // 1. 定义一个符合规则的Java源文件作为测试输入
        JavaFileObject inValidProduct = buildJavaFileObject("ValidExchangeFieldProduct");


        // 2. 使用javac()编译器，并告诉它要运行哪个注解处理器
        Compilation compilation = javac()
                .withProcessors(new AutoExchangeFieldAnnotationProcessor()) // <-- 关键：指定要测试的处理器
                .compile(inValidProduct); // 编译我们的测试源文件

        // 3. 断言编译成功
        assertThat(compilation).succeeded();

        // 4. (可选) 断言没有任何警告或错误
        assertThat(compilation).hadErrorCount(0);
        assertThat(compilation).hadWarningCount(0);

    }

    @Test
    @DisplayName("当@AutoExchangeField标注的是非数值类型的数据时，应编译失败")
    void shouldFailCompilationWithMultipleAnnotations() throws IOException {
        // 1. 定义一个违反规则的Java源文件
        JavaFileObject invalidSource = buildJavaFileObject("InValidExchangeFieldProduct");

        // 2. 运行编译器
        Compilation compilation = javac()
                .withProcessors(new AutoExchangeFieldAnnotationProcessor())
                .compile(invalidSource);

        // 3. 断言编译失败
        assertThat(compilation).failed();

        // 4. 断言有一个错误
        assertThat(compilation).hadErrorCount(1);

        // 5. 【核心断言】断言错误信息的内容和位置
        assertThat(compilation)
                .hadErrorContaining("@AutoExchangeField 注解只能用于数值类型字段") // 检查错误信息是否包含我们的文本
                .inFile(invalidSource) // 确认错误发生在哪个文件
                .onLine(7) // 确认错误报告在哪一行（通常是类声明那一行）
                .atColumn(20); // 确认错误报告在哪一列
    }
}
