package me.sudohippie.throttle.strategy.bucket;

import me.sudohippie.throttle.util.Assert;

import java.util.concurrent.TimeUnit;

/**
 * LeakyTokenBucketStrategy is a representation of {@link TokenBucketStrategy}.
 *
 * Details of this strategy can be found at {@see <a href="http://en.wikipedia.org/wiki/Leaky_bucket">Leaky Bucket</a>}
 *
 * Raghav Sidhanti
 * 9/25/13
 */
public abstract class LeakyTokenBucketStrategy extends TokenBucketStrategy {

    // step time interval in millis
    protected final long stepInterval;

    // step token count
    protected final long stepTokens;

    protected LeakyTokenBucketStrategy(long maxTokens, long refillInterval, TimeUnit refillIntervalTimeUnit, long stepTokens, long stepInterval, TimeUnit stepIntervalTimeUnit) {
        super(maxTokens, refillInterval, refillIntervalTimeUnit);

        // preconditions
        Assert.isTrue(stepInterval >= 0, "Step interval can not be negative");
        Assert.isTrue(stepTokens >= 0, "Step token can not be negative");

        this.stepTokens = stepTokens;
        this.stepInterval = stepIntervalTimeUnit.toMillis(stepInterval);
    }
}
