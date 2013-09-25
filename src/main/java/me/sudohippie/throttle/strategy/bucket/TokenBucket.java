package me.sudohippie.throttle.strategy.bucket;

import me.sudohippie.throttle.strategy.ThrottleStrategy;

/**
 * Abstract class representing a token bucket strategy.
 *
 * Using this strategy, throttling is enforced via the existence of tokens in a bucket.
 *
 * Raghav Sidhanti
 * 9/25/13
 */
public abstract class TokenBucket extends ThrottleStrategy {

    // bucket maxTokens
    protected final long maxTokens;
    // number of tokens in the bucket
    protected long tokens = 0;

    protected TokenBucket(long maxTokens) {
        this.maxTokens = maxTokens;
    }

    @Override
    public synchronized boolean isThrottled() {
        // update tokens
        updateTokens();

        // check token count
        if(tokens <= 0){
            return true;
        }

        tokens --;
        return false;
    }

    protected abstract void updateTokens();
}
