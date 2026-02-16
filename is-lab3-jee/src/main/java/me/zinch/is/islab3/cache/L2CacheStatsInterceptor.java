package me.zinch.is.islab3.cache;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import java.util.logging.Logger;

@LogL2CacheStats
@Interceptor
@Priority(Interceptor.Priority.APPLICATION + 50)
public class L2CacheStatsInterceptor {
    private static final Logger LOGGER = Logger.getLogger(L2CacheStatsInterceptor.class.getName());

    @Inject
    private L2CacheStatsService statsService;

    @AroundInvoke
    public Object logStats(InvocationContext ctx) throws Exception {
        if (!statsService.isLoggingEnabled()) {
            return ctx.proceed();
        }

        L2CacheStatsSnapshot before = statsService.threadSnapshot();

        try {
            return ctx.proceed();
        } finally {
            L2CacheStatsSnapshot after = statsService.threadSnapshot();
            long hitDelta = after.getHitCount() - before.getHitCount();
            long missDelta = after.getMissCount() - before.getMissCount();
            long putDelta = after.getPutCount() - before.getPutCount();

            LOGGER.info(() -> String.format(
                    "L2 cache [%s#%s]: hits=%d, misses=%d, puts=%d",
                    ctx.getMethod().getDeclaringClass().getSimpleName(),
                    ctx.getMethod().getName(),
                    hitDelta,
                    missDelta,
                    putDelta
            ));
        }
    }
}
