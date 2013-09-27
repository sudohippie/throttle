package me.sudohippie.throttle.strategy.bucket;

import me.sudohippie.throttle.strategy.ThrottleStrategy;
import me.sudohippie.throttle.util.Assert;

/**
 * Abstract class representing a token bucket strategy.
 *
 * Using this strategy, throttling is enforced via the existence of tokens in a bucket.
 *
 * Raghav Sidhanti
 * 9/25/13
 */
public abstract class TokenBucket extends ThrottleStrategy {

    // bucket bucketTokenCapacity
    protected final long bucketTokenCapacity;
    // number of tokens in the bucket
    protected long tokens = 0;

    protected TokenBucket(long bucketTokenCapacity) {
        Assert.isTrue(bucketTokenCapacity >= 0, "Bucket token capacity can not be negative");

        this.bucketTokenCapacity = bucketTokenCapacity;
    }

    @Override
    public synchronized boolean isThrottled() {
        return isThrottled(1);
    }

    @Override
    public synchronized boolean isThrottled(long n) {
        // preconditions
        Assert.isTrue(n >= 0, "Invalid argument less than 0");

        // update tokens
        updateTokens();

        // check whether there exist at least n tokens in bucket
        if(tokens < n) return true;

        tokens -= n;
        return false;
    }

    public long getBucketCapacity() {
        return bucketTokenCapacity;
    }

    public synchronized long getCurrentTokenCount() {
        updateTokens();
        return tokens;
    }

    protected abstract void updateTokens();
}
