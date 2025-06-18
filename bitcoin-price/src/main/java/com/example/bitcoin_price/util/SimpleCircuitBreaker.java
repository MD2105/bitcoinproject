package com.example.bitcoin_price.util;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleCircuitBreaker {

    public enum State { CLOSED, OPEN, HALF_OPEN }

    private State state = State.CLOSED;
    private final int failureThreshold;
    private final long retryTimePeriodMillis;
    private AtomicInteger failureCount = new AtomicInteger(0);
    private Instant lastFailureTime = Instant.now();

    public SimpleCircuitBreaker(int failureThreshold, long retryTimePeriodMillis) {
        this.failureThreshold = failureThreshold;
        this.retryTimePeriodMillis = retryTimePeriodMillis;
    }

    public synchronized boolean allowRequest() {
        switch (state) {
            case OPEN:
                if (Instant.now().isAfter(lastFailureTime.plusMillis(retryTimePeriodMillis))) {
                    state = State.HALF_OPEN;
                    return true;
                }
                return false;
            case HALF_OPEN:
            case CLOSED:
                return true;
        }
        return true;
    }

    public synchronized void recordSuccess() {
        state = State.CLOSED;
        failureCount.set(0);
    }

    public synchronized void recordFailure() {
        lastFailureTime = Instant.now();
        if (failureCount.incrementAndGet() >= failureThreshold) {
            state = State.OPEN;
        }
    }
}