package io.github.juwencheng.autoexchange.core.translate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认字典数据提供者。仅返回 null，用于没有配置实际字典源的情况。
 * <p>
 * 业务项目应提供自己的 {@link IDictDataProvider} 实现来覆盖此默认实现。
 *
 * @author juwencheng
 */
public class DefaultDictDataProvider implements IDictDataProvider {

    private static final Logger log = LoggerFactory.getLogger(DefaultDictDataProvider.class);

    @Override
    public String getDictValue(String dictType, String key) {
        log.debug("DefaultDictDataProvider: 未配置字典数据源，dictType={}, key={}", dictType, key);
        return null;
    }
}
