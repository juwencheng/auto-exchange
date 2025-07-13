package tech.baizi.autoexchange.core.context;

import java.util.HashMap;
import java.util.Map;

public class AutoExchangeContext {
    private final String targetCurrency;

    private final Map<Object, Map<String, Object>> appendedData = new HashMap<>();

    public AutoExchangeContext(String targetCurrency) {
        this.targetCurrency = targetCurrency;
    }

    // 追加数据到targetObject
    public void addAppendedData(Object targetObject, String fieldName, Object data) {
        appendedData.computeIfAbsent(targetObject, k -> new HashMap<>()).put(fieldName, data);
    }

    // 序列化器将调用此方法来获取待追加的数据
    public Map<String, Object> getAppendedDataFor(Object targetObject) {
        return appendedData.get(targetObject);
    }

    public String getTargetCurrency() {
        return targetCurrency;
    }
}
