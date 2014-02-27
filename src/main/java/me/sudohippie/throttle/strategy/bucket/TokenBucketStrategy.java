package me.sudohippie.throttle.strategy.bucket;

import me.sudohippie.throttle.strategy.ThrottleStrategy;
import me.sudohippie.throttle.util.Assert;

import java.util.concurrent.TimeUnit;

/**
 * Abstract class representing a token bucket strategy.
 *
 * Using this strategy, throttling is enforced via the existence of tokens in a bucket.
 *
 * Raghav Sidhanti
 * 9/25/13
 */
public abstract class TokenBucketStrategy extends ThrottleStrategy {

    protected final long bucketTokenCapacity;
	protected final long refillInterval;

	// number of tokens in the bucket
	protected long tokens = 0;
	protected long nextRefillTime = 0;

    protected TokenBucketStrategy(long bucketTokenCapacity, long refillInterval, TimeUnit refillIntervalTimeUnit) {
        Assert.isTrue(bucketTokenCapacity >= 0, "Bucket token capacity can not be negative");
		Assert.isTrue(refillInterval >= 0, "Bucket refill interval can not be negative");

        this.bucketTokenCapacity = bucketTokenCapacity;
		this.refillInterval = refillIntervalTimeUnit.toMillis(refillInterval);
    }

    @Override
    public synchronized boolean isThrottled() {
        return isThrottled(1);
    }

    @Override
    public synchronized boolean isThrottled(long n) {
        // preconditions
        Assert.isTrue(n >= 0, "Invalid argument less than 0");

        // check whether there exist at least n tokens in bucket
        if(getCurrentTokenCount() < n) return true;

        tokens -= n;
        return false;
    }

	@Override
    public long getCapacity() {
        return bucketTokenCapacity;
    }

    public synchronized long getCurrentTokenCount() {
        updateTokens();
        return tokens;
    }

	@Override
	public synchronized long timeToRelease(long n, TimeUnit timeUnit){
		// preconditions
		Assert.isTrue(n >= 0, "Invalid argument less than 0");

		// check whether tokens exist
		if(getCurrentTokenCount() >= n){
			return 0L;
		} else{
			long timeToIntervalEnd = nextRefillTime - System.currentTimeMillis();
			// edge case due to system slowness
			if(timeToIntervalEnd < 0) return timeToRelease(n, timeUnit);
			else return timeUnit.convert(timeToIntervalEnd, TimeUnit.MILLISECONDS);
		}
	}

    protected abstract void updateTokens();
}
