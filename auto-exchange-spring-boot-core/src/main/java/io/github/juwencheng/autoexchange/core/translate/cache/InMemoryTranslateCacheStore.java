package io.github.juwencheng.autoexchange.core.translate.cache;

import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认内存缓存实现，支持 TTL。
 *
 * @author juwencheng
 */
public class InMemoryTranslateCacheStore implements TranslateCacheStore {

    public static final String STORE_NAME = "memory";

    private static final class CacheEntry {
        final Object value;
        final long expireAtMillis;

        CacheEntry(Object value, long expireAtMillis) {
            this.value = value;
            this.expireAtMillis = expireAtMillis;
        }

        boolean isExpired() {
            return expireAtMillis > 0 && System.currentTimeMillis() > expireAtMillis;
        }
    }

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    @Override
    public String name() {
        return STORE_NAME;
    }

    @Override
    public Optional<Object> get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return Optional.empty();
        }
        if (entry.isExpired()) {
            cache.remove(key, entry);
            return Optional.empty();
        }
        return Optional.of(entry.value);
    }

    @Override
    public void put(String key, Object value, Duration ttl) {
        long expireAt = 0;
        if (ttl != null && !ttl.isZero() && !ttl.isNegative()) {
            expireAt = System.currentTimeMillis() + ttl.toMillis();
        }
        cache.put(key, new CacheEntry(value, expireAt));
    }

    @Override
    public void evict(String key) {
        cache.remove(key);
    }

    @Override
    public void evictByPrefix(String prefix) {
        Iterator<Map.Entry<String, CacheEntry>> it = cache.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, CacheEntry> entry = it.next();
            if (entry.getKey().startsWith(prefix)) {
                it.remove();
            }
        }
    }
}
