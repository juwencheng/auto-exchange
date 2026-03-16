package io.github.juwencheng.autoexchange.core.context;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AutoExchangeContext 单元测试")
class AutoExchangeContextTest {

    @Test
    @DisplayName("addAppendedData/getAppendedDataFor 应使用对象身份而非 equals 进行区分")
    void appendedData_shouldUseObjectIdentity() {
        AutoExchangeContext context = new AutoExchangeContext("CNY");

        // 两个不同的对象实例，但 equals() 和 hashCode() 相同（使用 String 做代理）
        Object obj1 = new Object();
        Object obj2 = new Object();

        context.addAppendedData(obj1, "field1", "value1");
        context.addAppendedData(obj2, "field2", "value2");

        Map<String, Object> dataForObj1 = context.getAppendedDataFor(obj1);
        Map<String, Object> dataForObj2 = context.getAppendedDataFor(obj2);

        assertThat(dataForObj1).containsKey("field1").doesNotContainKey("field2");
        assertThat(dataForObj2).containsKey("field2").doesNotContainKey("field1");
    }

    @Test
    @DisplayName("同一对象追加多个字段，应全部可读取")
    void appendedData_multipleFieldsForSameObject_shouldAllBeRetrievable() {
        AutoExchangeContext context = new AutoExchangeContext("USD");
        Object bean = new Object();

        context.addAppendedData(bean, "priceInCny", Map.of("price", 700));
        context.addAppendedData(bean, "priceInJpy", Map.of("price", 11000));

        Map<String, Object> data = context.getAppendedDataFor(bean);
        assertThat(data).containsKeys("priceInCny", "priceInJpy");
    }

    @Test
    @DisplayName("对未添加数据的对象调用 getAppendedDataFor 应返回 null")
    void getAppendedDataFor_unknownObject_shouldReturnNull() {
        AutoExchangeContext context = new AutoExchangeContext("CNY");
        Object unknownBean = new Object();

        Map<String, Object> data = context.getAppendedDataFor(unknownBean);
        assertThat(data).isNull();
    }

    @Test
    @DisplayName("getTargetCurrency 应返回构造时指定的目标货币")
    void getTargetCurrency_shouldReturnConstructedValue() {
        AutoExchangeContext context = new AutoExchangeContext("EUR");
        assertThat(context.getTargetCurrency()).isEqualTo("EUR");
    }
}
