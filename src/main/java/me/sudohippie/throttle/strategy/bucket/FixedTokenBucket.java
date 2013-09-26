package me.sudohippie.throttle.strategy.bucket;

import me.sudohippie.throttle.util.Assert;

import java.util.concurrent.TimeUnit;

/**
 * FixedTokenBucket is a concrete implementation of {@link TokenBucket}.
 *
 * In this strategy, at the beginning of every refill interval the bucket
 * is filled to capacity with tokens. Every call to {@link me.sudohippie.throttle.strategy.bucket.FixedTokenBucket#isThrottled()}
 * reduces the number of tokens with in the bucket by a fixed amount.
 *
 * Details of this strategy can be found at {@see <a href="http://en.wikipedia.org/wiki/Token_bucket">Token Bucket</a>}
 *
 * Raghav Sidhanti
 * 9/25/13
 */
public class FixedTokenBucket extends TokenBucket {

    // All time will be in milli-seconds
    protected final long refillInterval;
    protected long nextRefillTime = 0;

    public FixedTokenBucket(long maxTokens, long refillInterval, TimeUnit refillIntervalTimeUnit) {
        super(maxTokens);

        // preconditions
        Assert.isTrue(refillInterval >= 0, "Refill interval can not be negative");

        this.refillInterval = refillIntervalTimeUnit.toMillis(refillInterval);
    }

    @Override
    protected void updateTokens() {
        // refill bucket if current time has exceed next refill time
        long currentTime = System.currentTimeMillis();
        if(currentTime < nextRefillTime) return;

        tokens = maxTokens;
        nextRefillTime = currentTime + refillInterval;
    }
}
