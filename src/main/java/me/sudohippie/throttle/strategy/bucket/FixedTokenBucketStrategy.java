package me.sudohippie.throttle.strategy.bucket;

import java.util.concurrent.TimeUnit;

/**
 * FixedTokenBucketStrategy is a concrete implementation of {@link TokenBucketStrategy}.
 *
 * In this strategy, at the beginning of every refill interval the bucket
 * is filled to capacity with tokens. Every call to {@link FixedTokenBucketStrategy#isThrottled()}
 * reduces the number of tokens with in the bucket by a fixed amount.
 *
 * Details of this strategy can be found at {@see <a href="http://en.wikipedia.org/wiki/Token_bucket">Token Bucket</a>}
 *
 * Raghav Sidhanti
 * 9/25/13
 */
public class FixedTokenBucketStrategy extends TokenBucketStrategy {

	protected FixedTokenBucketStrategy(long bucketTokenCapacity, long refillInterval, TimeUnit refillIntervalTimeUnit) {
		super(bucketTokenCapacity, refillInterval, refillIntervalTimeUnit);
	}

	@Override
    protected void updateTokens() {
        // refill bucket if current time has exceed next refill time
        long currentTime = System.currentTimeMillis();
        if(currentTime < nextRefillTime) return;

        tokens = bucketTokenCapacity;
        nextRefillTime = currentTime + refillInterval;
    }
}
