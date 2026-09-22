package com.zy.redis.lock;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.util.StringUtils;

import com.zy.redis.props.RedisProperties;

/**
 * 基于 Redisson 的带租约分布式锁执行器。
 */
public final class DistributedLock {

    private final RedissonClient redissonClient;
    private final RedisProperties properties;
    private final String defaultLockPrefix;

    /**
     * 使用指定应用名称创建分布式锁执行器。
     */
    public DistributedLock(RedissonClient redissonClient, RedisProperties properties, String applicationName) {
        this.redissonClient = Objects.requireNonNull(redissonClient, "redissonClient must not be null");
        this.properties = Objects.requireNonNull(properties, "properties must not be null");
        this.defaultLockPrefix = (StringUtils.hasText(applicationName) ? applicationName : "zy") + ":lock:";
    }

    /**
     * 按默认等待和租约时间尝试执行操作。
     *
     * @return 是否成功获取锁并执行操作
     */
    public boolean tryExecute(String key, Runnable action) {
        return tryExecute(key, properties.getLockWaitTime(), properties.getLockLeaseTime(), action);
    }

    /**
     * 按指定等待和租约时间尝试执行操作。
     *
     * @return 是否成功获取锁并执行操作
     */
    public boolean tryExecute(String key, Duration waitTime, Duration leaseTime, Runnable action) {
        Objects.requireNonNull(action, "action must not be null");
        return trySupply(key, waitTime, leaseTime, () -> {
            action.run();
            return Boolean.TRUE;
        }).isPresent();
    }

    /**
     * 按默认等待和租约时间尝试获取操作结果。
     *
     * <p>未获取锁时返回空；获取锁但操作返回 {@code null} 时也返回空。
     *
     * @return 操作的非空结果，或空结果
     */
    public <T> Optional<T> trySupply(String key, Supplier<T> action) {
        return trySupply(key, properties.getLockWaitTime(), properties.getLockLeaseTime(), action);
    }

    /**
     * 按指定等待和租约时间尝试获取操作结果。
     *
     * <p>未获取锁时返回空；获取锁但操作返回 {@code null} 时也返回空。
     *
     * @return 操作的非空结果，或空结果
     */
    public <T> Optional<T> trySupply(String key, Duration waitTime, Duration leaseTime, Supplier<T> action) {
        validateArguments(key, waitTime, leaseTime, action);
        RLock lock = redissonClient.getLock(lockName(key));
        try {
            if (!lock.tryLock(waitTime.toMillis(), leaseTime.toMillis(), TimeUnit.MILLISECONDS)) {
                return Optional.empty();
            }
            try {
                return Optional.ofNullable(action.get());
            } finally {
                unlockIfHeldByCurrentThread(lock);
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    private void validateArguments(String key, Duration waitTime, Duration leaseTime, Supplier<?> action) {
        if (!StringUtils.hasText(key)) {
            throw new IllegalArgumentException("key must not be blank");
        }
        Objects.requireNonNull(action, "action must not be null");
        Objects.requireNonNull(waitTime, "waitTime must not be null");
        Objects.requireNonNull(leaseTime, "leaseTime must not be null");
        if (waitTime.isNegative()) {
            throw new IllegalArgumentException("waitTime must be greater than or equal to zero");
        }
        if (leaseTime.isNegative() || leaseTime.isZero()) {
            throw new IllegalArgumentException("leaseTime must be greater than zero");
        }
    }

    private String lockName(String key) {
        return StringUtils.hasText(properties.getLockPrefix()) ? properties.getLockPrefix() + key : defaultLockPrefix + key;
    }

    private void unlockIfHeldByCurrentThread(RLock lock) {
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
