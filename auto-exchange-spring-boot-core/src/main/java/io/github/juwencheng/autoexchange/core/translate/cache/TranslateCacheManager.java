package io.github.juwencheng.autoexchange.core.translate.cache;

import io.github.juwencheng.autoexchange.core.AutoExchangeProperties;
import io.github.juwencheng.autoexchange.core.translate.FieldTranslator;
import io.github.juwencheng.autoexchange.core.translate.TranslateContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 翻译缓存管理器。根据策略选择存储、构建 key，并在翻译前后读写缓存。
 *
 * @author juwencheng
 */
public class TranslateCacheManager {

    private static final Logger log = LoggerFactory.getLogger(TranslateCacheManager.class);

    private final TranslateCacheStrategyRegistry strategyRegistry;
    private final Map<String, TranslateCacheStore> stores;
    private final AutoExchangeProperties properties;

    public TranslateCacheManager(TranslateCacheStrategyRegistry strategyRegistry,
                                 List<TranslateCacheStore> stores,
                                 AutoExchangeProperties properties) {
        this.strategyRegistry = strategyRegistry;
        this.properties = properties;
        this.stores = new HashMap<>();
        for (TranslateCacheStore store : stores) {
            this.stores.put(store.name(), store);
        }
    }

    /**
     * 带缓存地执行翻译
     */
    public Object translateWithCache(FieldTranslator translator,
                                     Class<? extends FieldTranslator> translatorClass,
                                     Class<? extends TranslateCacheStrategy> declaredStrategy,
                                     Object fieldValue,
                                     TranslateContext fieldContext,
                                     String[] args,
                                     Field field) {
        if (!properties.getTranslateCache().isEnabled()) {
            return translator.translate(fieldValue, fieldContext);
        }

        TranslateCacheStrategy strategy = strategyRegistry.resolve(declaredStrategy, translatorClass);
        if (!strategy.enabled()) {
            return translator.translate(fieldValue, fieldContext);
        }

        TranslateCacheKeyContext keyContext = new TranslateCacheKeyContext(
                translatorClass, fieldValue, fieldContext, args, field);
        String cacheKey = strategy.buildKey(keyContext);
        TranslateCacheStore store = resolveStore(strategy.storeName());

        Optional<Object> cached = store.get(cacheKey);
        if (cached.isPresent()) {
            return cached.get();
        }

        Object result = translator.translate(fieldValue, fieldContext);
        if (result != null) {
            store.put(cacheKey, result, strategy.ttl());
        }
        return result;
    }

    private TranslateCacheStore resolveStore(String storeName) {
        TranslateCacheStore store = stores.get(storeName);
        if (store == null) {
            log.warn("未找到缓存存储: {}，回退到 memory", storeName);
            store = stores.get(InMemoryTranslateCacheStore.STORE_NAME);
        }
        if (store == null) {
            throw new IllegalStateException("未配置任何 TranslateCacheStore 实现");
        }
        return store;
    }
}
