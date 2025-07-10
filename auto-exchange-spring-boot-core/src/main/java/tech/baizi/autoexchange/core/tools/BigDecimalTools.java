package tech.baizi.autoexchange.core.tools;

import java.math.BigDecimal;

/**
 * BigDecimal工具类
 */
public class BigDecimalTools {
    /**
     * 将对象转成BigDecimal对象，如果对象不合法，会默认成0
     *
     * @param object 对象
     * @return BigDecimal
     */
    public static BigDecimal convert(Object object) {
        return convertOrDefault(object, BigDecimal.ZERO);
    }

    /**
     * 将对象转成BigDecimal对象，如果对象不合法，会默认成defaultValue
     *
     * @param object       对象
     * @param defaultValue 转换失败默认的值
     * @return BigDecimal
     */
    public static BigDecimal convertOrDefault(Object object, BigDecimal defaultValue) {
        if (object == null) {
            return defaultValue;
        }
        if (object instanceof BigDecimal) {
            return (BigDecimal) object;
        }
        if (object instanceof Number) {
            return new BigDecimal(object.toString());
        }
        // 转换失败，不是合法的数值对象
        return defaultValue;
    }

    /**
     * 两个BigDecimal乘法
     *
     * @param op1 乘数
     * @param op2 被乘数
     * @return 新的BigDecimal
     */
    public static BigDecimal multiply(BigDecimal op1, BigDecimal op2) {
        if (op1 == null || op2 == null) {
            return BigDecimal.ZERO;
        }
        return op1.multiply(op2);
    }
}
