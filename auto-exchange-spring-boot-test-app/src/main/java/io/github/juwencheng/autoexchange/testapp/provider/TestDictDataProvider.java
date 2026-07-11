package io.github.juwencheng.autoexchange.testapp.provider;

import io.github.juwencheng.autoexchange.core.translate.IDictDataProvider;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试用字典数据提供者
 *
 * @author juwencheng
 */
@Component
public class TestDictDataProvider implements IDictDataProvider {

    private static final Map<String, Map<String, String>> DICT_DATA = new HashMap<>();

    static {
        Map<String, String> orderStatus = new HashMap<>();
        orderStatus.put("0", "待支付");
        orderStatus.put("1", "已支付");
        orderStatus.put("2", "已发货");
        orderStatus.put("3", "已完成");
        orderStatus.put("4", "已取消");
        DICT_DATA.put("order_status", orderStatus);

        Map<String, String> paymentType = new HashMap<>();
        paymentType.put("ALIPAY", "支付宝");
        paymentType.put("WECHAT", "微信支付");
        paymentType.put("BANK_CARD", "银行卡");
        DICT_DATA.put("payment_type", paymentType);
    }

    @Override
    public String getDictValue(String dictType, String key) {
        Map<String, String> dict = DICT_DATA.get(dictType);
        if (dict == null) {
            return null;
        }
        return dict.get(key);
    }
}
