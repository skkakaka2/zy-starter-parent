package com.zy.redis.lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import com.zy.redis.props.RedisProperties;

/**
 * {@link DistributedLock} 的纯 Mock 测试。
 */
class DistributedLockTest {

    @AfterEach
    void clearInterruptStatus() {
        Thread.interrupted();
    }

    @Test
    void executesWithThreeArgumentTryLockAndUnlocksOnlyWhenHeld() throws Exception {
        RedisProperties properties = new RedisProperties();
        RedissonClient client = mock(RedissonClient.class);
        RLock lock = mock(RLock.class);
        when(client.getLock("orders:lock:pay:1")).thenReturn(lock);
        when(lock.tryLock(2_000L, 30_000L, TimeUnit.MILLISECONDS)).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);

        boolean executed = new DistributedLock(client, properties, "orders")
                .tryExecute("pay:1", () -> { });

        assertThat(executed).isTrue();
        verify(lock).tryLock(2_000L, 30_000L, TimeUnit.MILLISECONDS);
        verify(lock).unlock();
    }

    @Test
    void doesNotUnlockWhenLockWasNotAcquired() throws Exception {
        RedisProperties properties = new RedisProperties();
        RedissonClient client = mock(RedissonClient.class);
        RLock lock = mock(RLock.class);
        when(client.getLock("orders:lock:pay:2")).thenReturn(lock);
        when(lock.tryLock(2_000L, 30_000L, TimeUnit.MILLISECONDS)).thenReturn(false);

        boolean executed = new DistributedLock(client, properties, "orders")
                .tryExecute("pay:2", () -> { });

        assertThat(executed).isFalse();
        verify(lock, never()).isHeldByCurrentThread();
        verify(lock, never()).unlock();
    }

    @Test
    void restoresInterruptStatusAndDoesNotUnlockWhenTryLockIsInterrupted() throws Exception {
        RedisProperties properties = new RedisProperties();
        RedissonClient client = mock(RedissonClient.class);
        RLock lock = mock(RLock.class);
        when(client.getLock("orders:lock:pay:3")).thenReturn(lock);
        when(lock.tryLock(2_000L, 30_000L, TimeUnit.MILLISECONDS)).thenThrow(new InterruptedException());

        boolean executed = new DistributedLock(client, properties, "orders")
                .tryExecute("pay:3", () -> { });

        assertThat(executed).isFalse();
        assertThat(Thread.currentThread().isInterrupted()).isTrue();
        verify(lock, never()).unlock();
    }

    @Test
    void unlocksWhenActionThrowsRuntimeException() throws Exception {
        RedisProperties properties = new RedisProperties();
        RedissonClient client = mock(RedissonClient.class);
        RLock lock = mock(RLock.class);
        when(client.getLock("orders:lock:pay:4")).thenReturn(lock);
        when(lock.tryLock(2_000L, 30_000L, TimeUnit.MILLISECONDS)).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);

        DistributedLock distributedLock = new DistributedLock(client, properties, "orders");

        assertThatThrownBy(() -> distributedLock.tryExecute("pay:4", () -> {
            throw new IllegalStateException("boom");
        })).isInstanceOf(IllegalStateException.class).hasMessage("boom");
        verify(lock).unlock();
    }

    @Test
    void usesConfiguredPrefixWhenPresent() throws Exception {
        RedisProperties properties = new RedisProperties();
        properties.setLockPrefix("billing:mutex:");
        RedissonClient client = mock(RedissonClient.class);
        RLock lock = mock(RLock.class);
        when(client.getLock("billing:mutex:invoice:1")).thenReturn(lock);
        when(lock.tryLock(2_000L, 30_000L, TimeUnit.MILLISECONDS)).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(false);

        boolean executed = new DistributedLock(client, properties, "orders")
                .tryExecute("invoice:1", () -> { });

        assertThat(executed).isTrue();
        verify(client).getLock(eq("billing:mutex:invoice:1"));
        verify(lock, never()).unlock();
    }
}
