package io.github.juwencheng.autoexchange.processor;

import com.google.auto.service.AutoService;
import io.github.juwencheng.autoexchange.core.annotation.AutoExchangeField;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.Set;

/**
 * BaseCurrency注解处理器，用于在编译时验证 @AutoExchangeBaseCurrency 注解的使用规则。
 * 规则：一个类中最多只能有一个字段被此注解标记。
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("io.github.juwencheng.autoexchange.core.annotation.AutoExchangeField")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class AutoExchangeFieldAnnotationProcessor extends AbstractProcessor {
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(AutoExchangeField.class);
        if (annotatedElements.isEmpty()) {
            return false;
        }
        for (Element annotatedElement : annotatedElements) {
            if (!isNumericType(annotatedElement)) {
                messager.printMessage(
                        Diagnostic.Kind.ERROR,
                        "@AutoExchangeField 注解只能用于数值类型字段，但字段 " +
                                annotatedElement.getSimpleName() + " 的类型是 " + annotatedElement.asType(),
                        annotatedElement
                );
            }
        }

        return false;
    }

    private boolean isNumericType(Element element) {
        TypeMirror typeMirror = element.asType();

        // 检查基本数值类型
        if (typeMirror.getKind().isPrimitive()) {
            TypeKind kind = typeMirror.getKind();
            return kind == TypeKind.BYTE || kind == TypeKind.SHORT ||
                    kind == TypeKind.INT || kind == TypeKind.LONG ||
                    kind == TypeKind.FLOAT || kind == TypeKind.DOUBLE;
        }

        // 检查包装类型
        String typeName = typeMirror.toString();
        return typeName.equals("java.lang.Byte") ||
                typeName.equals("java.lang.Short") ||
                typeName.equals("java.lang.Integer") ||
                typeName.equals("java.lang.Long") ||
                typeName.equals("java.lang.Float") ||
                typeName.equals("java.lang.Double") ||
                typeName.equals("java.math.BigDecimal") ||
                typeName.equals("java.math.BigInteger");
    }
}
