package me.zinch.is.islab3.models.dao.support;

import me.zinch.is.islab3.server.cache.InfinispanL2CacheBridge;
import me.zinch.is.islab3.models.fields.EntityField;
import me.zinch.is.islab3.models.fields.Filter;

import java.util.List;

public final class DaoQueryCacheFallbackSupport {
    private DaoQueryCacheFallbackSupport() {
    }

    public static String filterKey(Filter<? extends EntityField> filter) {
        if (filter == null) {
            return "null";
        }
        String field = filter.getField() == null ? "null" : filter.getField().getValue();
        String value = filter.getValue() == null ? "null" : filter.getValue();
        String direction = filter.getSortDirection() == null ? "null" : filter.getSortDirection().getValue();
        return field + "|" + value + "|" + direction;
    }

    public static <T> List<T> fallbackPageFromFindAllCache(Class<T> entityClass,
                                                            Integer page,
                                                            Integer pageSize,
                                                            Filter<? extends EntityField> filter) {
        if (!isUnfiltered(filter)) {
            return null;
        }
        Object allCached = InfinispanL2CacheBridge.getQueryResult("findAll:" + entityClass.getName());
        if (!(allCached instanceof List<?> all)) {
            return null;
        }
        int from = Math.max(0, page * pageSize);
        if (from >= all.size()) {
            return List.of();
        }
        int to = Math.min(all.size(), from + pageSize);
        @SuppressWarnings("unchecked")
        List<T> pageSlice = (List<T>) all.subList(from, to);
        return pageSlice;
    }

    public static Long fallbackCountFromFindAllCache(Class<?> entityClass, Filter<? extends EntityField> filter) {
        if (!isUnfiltered(filter)) {
            return null;
        }
        Object countCached = InfinispanL2CacheBridge.getQueryResult("count:" + entityClass.getName());
        if (countCached instanceof Long count) {
            return count;
        }
        Object allCached = InfinispanL2CacheBridge.getQueryResult("findAll:" + entityClass.getName());
        if (allCached instanceof List<?> all) {
            return (long) all.size();
        }
        return null;
    }

    private static boolean isUnfiltered(Filter<? extends EntityField> filter) {
        if (filter == null) {
            return true;
        }
        if (filter.getField() != null) {
            return false;
        }
        String value = filter.getValue();
        return value == null || value.isBlank();
    }
}
