package me.sudohippie.throttle.strategy.bucket;

import me.sudohippie.throttle.util.Assert;

import java.util.concurrent.TimeUnit;

/**
 * LeakyTokenBucket is a representation of {@link TokenBucket}.
 *
 * Details of this strategy can be found at {@see <a href="http://en.wikipedia.org/wiki/Leaky_bucket">Leaky Bucket</a>}
 *
 * Raghav Sidhanti
 * 9/25/13
 */
public abstract class LeakyTokenBucket extends TokenBucket {

    // step time interval in millis
    protected final long stepInterval;
    protected final long refillInterval;
    protected long nextRefillTime = 0;

    // step token count
    protected final long stepTokens;

    protected LeakyTokenBucket(long maxTokens, long refillInterval, TimeUnit refillIntervalTimeUnit, long stepTokens, long stepInterval, TimeUnit stepIntervalTimeUnit) {
        super(maxTokens);

        // preconditions
        Assert.isTrue(refillInterval >= 0, "Refill interval can not be negative");
        Assert.isTrue(stepInterval >= 0, "Step interval is not negative");
        Assert.isTrue(stepTokens >= 0, "Step token can not be negative");

        this.stepTokens = stepTokens;
        this.refillInterval = refillIntervalTimeUnit.toMillis(refillInterval);
        this.stepInterval = stepIntervalTimeUnit.toMillis(stepInterval);
    }
}
