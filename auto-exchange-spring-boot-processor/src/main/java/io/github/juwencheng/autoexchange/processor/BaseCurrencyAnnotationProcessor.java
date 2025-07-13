package io.github.juwencheng.autoexchange.processor;

import com.google.auto.service.AutoService;
import io.github.juwencheng.autoexchange.core.annotation.AutoExchangeBaseCurrency;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * BaseCurrency注解处理器，用于在编译时验证 @AutoExchangeBaseCurrency 注解的使用规则。
 * 规则：一个类中最多只能有一个字段被此注解标记。
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("io.github.juwencheng.autoexchange.core.annotation.AutoExchangeBaseCurrency")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class BaseCurrencyAnnotationProcessor extends AbstractProcessor {
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(AutoExchangeBaseCurrency.class);
        // 按照所在类进行分组
        Map<Element, ? extends List<? extends Element>> elementsByClass = annotatedElements.stream().collect(Collectors.groupingBy(Element::getEnclosingElement));
        elementsByClass.forEach((classElement, fields) -> {
            if (fields.size() > 1) {
                // 如果一个类中有多个被注解的字段，就报告一个编译错误！
                String fieldNames = fields.stream()
                        .map(Element::getSimpleName)
                        .collect(Collectors.joining(", "));

                // 【核心】向编译器报告一个错误
                messager.printMessage(
                        Diagnostic.Kind.ERROR, // 错误级别，会导致编译失败
                        "在一个类中只允许有一个 @AutoExchangeBaseCurrency 注解，但在 " +
                                classElement.getSimpleName() + " 中发现了 " + fields.size() + " 个: [" + fieldNames + "]",
                        classElement // 将错误信息关联到类元素上
                );
            } else {
                Element element = fields.get(0);
                // 判断element是字符串类型
                if (!isStringType(element)) {
                    messager.printMessage(
                            Diagnostic.Kind.ERROR,
                            "@AutoExchangeBaseCurrency 注解只能用于String类型字段，但字段 " +
                                    element.getSimpleName() + " 的类型是 " + element.asType(),
                            element
                    );
                }
            }
        });
        return false;
    }

    /**
     * 简单高效的String类型检查
     */
    private boolean isStringType(Element element) {
        return "java.lang.String".equals(element.asType().toString());
    }
}
