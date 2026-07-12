package io.github.juwencheng.autoexchange.openapi;

import io.github.juwencheng.autoexchange.exchange.ExchangeFieldTranslator;
import io.github.juwencheng.fieldtranslate.core.translate.TranslateField;
import io.github.juwencheng.fieldtranslate.core.translate.TranslateResponse;
import io.swagger.v3.oas.models.Operation;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * OpenAPI OperationCustomizer，为标注了 {@link TranslateResponse} 的 Controller 方法
 * 增强响应 Schema，追加汇率翻译等动态字段的描述。
 *
 * @author juwencheng
 */
public class ExchangeOperationCustomizer implements OperationCustomizer {

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        if (!handlerMethod.hasMethodAnnotation(TranslateResponse.class)) {
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

    private void scanVirtualFieldsRecursive(Class<?> clazz, String pathPrefix,
                                            List<VirtualFieldDescriptor> result,
                                            Set<Class<?>> visited) {
        for (Field field : getAllDeclaredFields(clazz)) {
            if (field.isAnnotationPresent(TranslateField.class)) {
                TranslateField ann = field.getAnnotation(TranslateField.class);
                String fieldName = ann.value();
                if (fieldName == null || fieldName.isEmpty()) {
                    fieldName = field.getName() + "Translated";
                }
                String path = pathPrefix.isEmpty() ? fieldName : pathPrefix + "." + fieldName;
                String type;
                String description;
                if (ann.translator() == ExchangeFieldTranslator.class) {
                    type = "Object{base, trans, rate, price}";
                    description = "汇率转换结果（源自字段 " + field.getName() + "）";
                } else {
                    String translatorName = ann.translator().getSimpleName();
                    String argsStr = ann.args().length > 0 ? " (args: " + String.join(", ", ann.args()) + ")" : "";
                    type = "dynamic";
                    description = "由 " + translatorName + " 翻译" + argsStr;
                }
                result.add(new VirtualFieldDescriptor(path, type, description));
            }

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
