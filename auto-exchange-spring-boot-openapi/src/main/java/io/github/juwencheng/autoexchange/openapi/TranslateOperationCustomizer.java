package io.github.juwencheng.autoexchange.openapi;

import io.github.juwencheng.autoexchange.core.annotation.AutoExchangeBaseCurrency;
import io.github.juwencheng.autoexchange.core.annotation.AutoExchangeField;
import io.github.juwencheng.autoexchange.core.annotation.AutoExchangeResponse;
import io.github.juwencheng.autoexchange.core.translate.TranslateField;
import io.github.juwencheng.autoexchange.core.translate.TranslateResponse;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * OpenAPI OperationCustomizer，自动为标注了 @AutoExchangeResponse 或 @TranslateResponse
 * 的 Controller 方法增强其响应 Schema，追加动态字段的描述。
 * <p>
 * 解决的问题：Append 模式下 JSON 输出结构与 Java DTO 类不一致，导致 Swagger 文档不准确。
 * <p>
 * 条件激活：仅在项目引入 springdoc-openapi 依赖时生效。
 *
 * @author juwencheng
 */
public class TranslateOperationCustomizer implements OperationCustomizer {

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        boolean hasAutoExchange = handlerMethod.hasMethodAnnotation(AutoExchangeResponse.class);
        boolean hasTranslate = handlerMethod.hasMethodAnnotation(TranslateResponse.class);

        if (!hasAutoExchange && !hasTranslate) {
            return operation;
        }

        Class<?> returnType = resolveReturnType(handlerMethod);
        if (returnType == null) {
            return operation;
        }

        List<VirtualFieldDescriptor> virtualFields = scanVirtualFields(returnType, new HashSet<>());
        if (virtualFields.isEmpty()) {
            return operation;
        }

        // 在 operation 描述中追加虚拟字段说明
        StringBuilder description = new StringBuilder();
        if (operation.getDescription() != null) {
            description.append(operation.getDescription());
        }
        description.append("\n\n**动态追加字段（Append 模式）：**\n\n");
        description.append("| 字段名 | 类型 | 说明 |\n");
        description.append("|--------|------|------|\n");
        for (VirtualFieldDescriptor vf : virtualFields) {
            description.append("| `").append(vf.path).append("` | ")
                    .append(vf.type).append(" | ")
                    .append(vf.description).append(" |\n");
        }
        operation.setDescription(description.toString());

        return operation;
    }

    private Class<?> resolveReturnType(HandlerMethod handlerMethod) {
        Type genericReturnType = handlerMethod.getMethod().getGenericReturnType();
        if (genericReturnType instanceof ParameterizedType) {
            Type[] typeArguments = ((ParameterizedType) genericReturnType).getActualTypeArguments();
            if (typeArguments.length > 0 && typeArguments[0] instanceof Class) {
                return (Class<?>) typeArguments[0];
            }
        }
        Class<?> returnType = handlerMethod.getMethod().getReturnType();
        if (returnType == void.class || returnType == Void.class) {
            return null;
        }
        return returnType;
    }

    private List<VirtualFieldDescriptor> scanVirtualFields(Class<?> clazz, Set<Class<?>> visited) {
        List<VirtualFieldDescriptor> result = new ArrayList<>();
        if (clazz == null || visited.contains(clazz) || isLeafType(clazz)) {
            return result;
        }
        visited.add(clazz);

        scanVirtualFieldsRecursive(clazz, "", result, visited);
        return result;
    }

    private void scanVirtualFieldsRecursive(Class<?> clazz, String pathPrefix, List<VirtualFieldDescriptor> result, Set<Class<?>> visited) {
        for (Field field : getAllDeclaredFields(clazz)) {
            // @AutoExchangeField
            if (field.isAnnotationPresent(AutoExchangeField.class)) {
                AutoExchangeField ann = field.getAnnotation(AutoExchangeField.class);
                String fieldName = ann.value();
                if (fieldName == null || fieldName.isEmpty()) {
                    fieldName = field.getName() + "AutoExchange";
                }
                String path = pathPrefix.isEmpty() ? fieldName : pathPrefix + "." + fieldName;
                result.add(new VirtualFieldDescriptor(path, "Object{base, trans, rate, price}",
                        "汇率转换结果（源自字段 " + field.getName() + "）"));
            }

            // @TranslateField
            if (field.isAnnotationPresent(TranslateField.class)) {
                TranslateField ann = field.getAnnotation(TranslateField.class);
                String fieldName = ann.value();
                if (fieldName == null || fieldName.isEmpty()) {
                    fieldName = field.getName() + "Translated";
                }
                String path = pathPrefix.isEmpty() ? fieldName : pathPrefix + "." + fieldName;
                String translatorName = ann.translator().getSimpleName();
                String argsStr = ann.args().length > 0 ? " (args: " + String.join(", ", ann.args()) + ")" : "";
                result.add(new VirtualFieldDescriptor(path, "dynamic",
                        "由 " + translatorName + " 翻译" + argsStr));
            }

            // 递归进入非叶子类型字段
            Class<?> fieldType = field.getType();
            if (!isLeafType(fieldType) && !visited.contains(fieldType)
                    && !Collection.class.isAssignableFrom(fieldType)
                    && !Map.class.isAssignableFrom(fieldType)) {
                String newPrefix = pathPrefix.isEmpty() ? field.getName() : pathPrefix + "." + field.getName();
                scanVirtualFieldsRecursive(fieldType, newPrefix, result, visited);
            }
        }
    }

    private Field[] getAllDeclaredFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return fields.toArray(new Field[0]);
    }

    private boolean isLeafType(Class<?> clazz) {
        return clazz.isPrimitive()
                || clazz.getName().startsWith("java.lang")
                || clazz.getName().startsWith("java.math")
                || clazz.getName().startsWith("java.time")
                || clazz.isEnum();
    }

    private static class VirtualFieldDescriptor {
        final String path;
        final String type;
        final String description;

        VirtualFieldDescriptor(String path, String type, String description) {
            this.path = path;
            this.type = type;
            this.description = description;
        }
    }
}
