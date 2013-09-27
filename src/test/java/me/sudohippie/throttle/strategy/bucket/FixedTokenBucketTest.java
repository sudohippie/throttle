package me.sudohippie.throttle.strategy.bucket;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Raghav Sidhanti
 * Date: 9/25/13
 */
public class FixedTokenBucketTest {

    /* test state */
    // test max token when negative
    @Test(expected = IllegalArgumentException.class)
    public void testWhenMaxTokenIsNegative(){
        FixedTokenBucket bucket = new FixedTokenBucket(-1, 0, TimeUnit.MILLISECONDS);
    }

    // test refill interval when negative
    @Test(expected = IllegalArgumentException.class)
    public void testWhenRefillIntervalIsNegative(){
        FixedTokenBucket bucket = new FixedTokenBucket(0, -1, TimeUnit.MILLISECONDS);
    }

    /* test logic */
    // test n throttle and fixed refill interval, fixed max tokens
    long MAX_TOKENS = 10;
    long REFILL_INTERVAL = 10;
    TimeUnit REFILL_INTERVAL_TIME_UNIT = TimeUnit.SECONDS;

    long N_LESS_THAN_MAX = 2;
    long N_GREATER_THAN_MAX = 12;
    int CUMULATIVE = 3;

    FixedTokenBucket bucket;

    @Before
    public void setUp() throws Exception {
        bucket = new FixedTokenBucket(MAX_TOKENS, REFILL_INTERVAL, TimeUnit.MILLISECONDS);
    }

    // single threaded
    // when n is less than max token
        // token=max - n
        // isThrottle=false
    @Test
    public void testWhenNIsLessThanMaxTokens(){
        boolean throttled = bucket.isThrottled(N_LESS_THAN_MAX);
        long tokens = bucket.getCurrentTokenCount();

        assertFalse(throttled);
        assertEquals(MAX_TOKENS - N_LESS_THAN_MAX, tokens);
    }

    // when n is greater than max token
    // token=max
    // isThrottle=true
    @Test
    public void testWhenNIsGreaterThanMaxTokens() {
        boolean throttled = bucket.isThrottled(N_GREATER_THAN_MAX);
        long tokens = bucket.getCurrentTokenCount();

        assertTrue(throttled);
        assertEquals(MAX_TOKENS, tokens);
    }

    // when cumulative n is less than max token
    // token=max - (n1+n2 ...)
    // isThrottle=false
    @Test
    public void testWhenCumulativeNIsLessThanMaxTokens() {
        // throttle 3 times
        for(int i = 0; i < CUMULATIVE; i++) assertFalse(bucket.isThrottled(N_LESS_THAN_MAX));

        long tokens = bucket.getCurrentTokenCount();
        assertEquals(MAX_TOKENS - (CUMULATIVE *N_LESS_THAN_MAX), tokens);
    }

            // when cumulative n is greater than max token
                // token=max
                // isThrottle=true

    @Test
    public void testWhenCumulativeNIsGreaterThanMaxTokens() {
        // throttle 3 times
        for(int i = 0; i < CUMULATIVE; i++) assertTrue(bucket.isThrottled(N_GREATER_THAN_MAX));

        long tokens = bucket.getCurrentTokenCount();
        assertEquals(MAX_TOKENS, tokens);
    }

    // when n is less than max token, sleep for refill interval, n is less than max token
                // tokens=max-n&&tokens=max-n
                // isThrottle=false && isThrottle=false

    @Test
    public void testWhenNLessThanMaxSleepNLessThanMax() throws InterruptedException {
        boolean before = bucket.isThrottled(N_LESS_THAN_MAX);
        long tokensBefore = bucket.getCurrentTokenCount();

        assertFalse(before);
        assertEquals(MAX_TOKENS - N_LESS_THAN_MAX, tokensBefore);

        Thread.sleep(REFILL_INTERVAL_TIME_UNIT.toMillis(REFILL_INTERVAL));

        boolean after = bucket.isThrottled(N_LESS_THAN_MAX);
        long afterTokens = bucket.getCurrentTokenCount();

        assertFalse(after);
        assertEquals(MAX_TOKENS - N_LESS_THAN_MAX, afterTokens);
    }

    // when n is greater than max token, sleep for refill interval, n is greater than max token
                //tokens=max&&tokens=max
                // isThrottle=true && isThrottle=true
    public void testWhenNGreaterThanMaxSleepNGreaterThanMax() throws InterruptedException {
        boolean before = bucket.isThrottled(N_GREATER_THAN_MAX);
        long tokensBefore = bucket.getCurrentTokenCount();

        assertTrue(before);
        assertEquals(MAX_TOKENS, tokensBefore);

        Thread.sleep(REFILL_INTERVAL_TIME_UNIT.toMillis(REFILL_INTERVAL));

        boolean after = bucket.isThrottled(N_GREATER_THAN_MAX);
        long afterTokens = bucket.getCurrentTokenCount();

        assertTrue(after);
        assertEquals(MAX_TOKENS, afterTokens);
    }

            // when cumulative n is less than max token, sleep for refill interval, cumulative n is less than max total
                // tokens=max-(n1+...)&&tokens=max-(n1+...)
                // isThrottle=false && isThrottle=false

    @Test
    public void testWhenCumulativeNLessThanMaxSleepCumulativeNLessThanMax() throws InterruptedException {
        // throttle 3 times
        int sum = 0;

        for(int i = 0; i < CUMULATIVE; i++){
            assertFalse(bucket.isThrottled(N_LESS_THAN_MAX));
            sum += N_LESS_THAN_MAX;
        }
        long beforeTokens = bucket.getCurrentTokenCount();
        assertEquals(MAX_TOKENS - sum, beforeTokens);

        Thread.sleep(REFILL_INTERVAL_TIME_UNIT.toMillis(REFILL_INTERVAL));

        // throttle 3 times
        for(int i = 0; i < CUMULATIVE; i++) assertFalse(bucket.isThrottled(N_LESS_THAN_MAX));
        long afterTokens = bucket.getCurrentTokenCount();
        assertEquals(MAX_TOKENS - sum, afterTokens);
    }

    // when cumulative n is less than max token, sleep for refill interval, cumulative n is greater than max token
                // tokens=max-(n1+...)&&tokens<n
                // isThrottle=false && isThrottle=true
    @Test
    public void testWhenCumulativeNLessThanMaxSleepCumulativeNGreaterThanMax() throws InterruptedException {
        int sum = 0;

        for(int i = 0; i < CUMULATIVE; i++){
            assertFalse(bucket.isThrottled(N_LESS_THAN_MAX));
            sum += N_LESS_THAN_MAX;
        }
        long beforeTokens = bucket.getCurrentTokenCount();
        assertEquals(MAX_TOKENS - sum, beforeTokens);

        Thread.sleep(REFILL_INTERVAL_TIME_UNIT.toMillis(REFILL_INTERVAL));

        for(int i = 0; i < 3* CUMULATIVE; i++) bucket.isThrottled(N_LESS_THAN_MAX);
        boolean after = bucket.isThrottled(N_LESS_THAN_MAX);
        long afterTokens = bucket.getCurrentTokenCount();

        assertTrue(after);
        assertTrue(afterTokens < N_LESS_THAN_MAX);
    }

            // when cumulative n is greater than max token, sleep for refill interval, cumulative n is less than max token
                //tokens<n&&tokens=max-(n1+..)
                // isThrottle=true&&isThrottle=max-(n1+...)
    @Test
    public void testWhenCumulativeNGreaterThanMaxSleepCumulativeNLessThanMax() throws InterruptedException {

        for(int i = 0; i < 3* CUMULATIVE; i++) bucket.isThrottled(N_LESS_THAN_MAX);
        boolean before = bucket.isThrottled(N_LESS_THAN_MAX);
        long beforeTokens = bucket.getCurrentTokenCount();

        assertTrue(before);
        assertTrue(beforeTokens < N_LESS_THAN_MAX);

        Thread.sleep(REFILL_INTERVAL_TIME_UNIT.toMillis(REFILL_INTERVAL));

        int sum = 0;
        for(int i = 0; i < CUMULATIVE; i++){
            assertFalse(bucket.isThrottled(N_LESS_THAN_MAX));
            sum += N_LESS_THAN_MAX;
        }
        long afterTokens = bucket.getCurrentTokenCount();
        assertEquals(MAX_TOKENS - sum, afterTokens);
    }

            // when cumulative n is greater than max token, sleep for refill interval, cumulative n is greater than max total
                //tokens<n&&tokens<n
                // isThrottle=true&&isThrottle=true
    @Test
    public void testWhenCumulativeNGreaterThanMaxSleepCumulativeNGreaterThanMax() throws InterruptedException {

        for(int i = 0; i < 3* CUMULATIVE; i++) bucket.isThrottled(N_LESS_THAN_MAX);
        boolean before = bucket.isThrottled(N_LESS_THAN_MAX);
        long beforeTokens = bucket.getCurrentTokenCount();

        assertTrue(before);
        assertTrue(beforeTokens < N_LESS_THAN_MAX);

        Thread.sleep(REFILL_INTERVAL_TIME_UNIT.toMillis(REFILL_INTERVAL));

        for(int i = 0; i < 3* CUMULATIVE; i++) bucket.isThrottled(N_LESS_THAN_MAX);
        boolean after = bucket.isThrottled(N_LESS_THAN_MAX);
        long afterTokens = bucket.getCurrentTokenCount();

        assertTrue(after);
        assertTrue(afterTokens < N_LESS_THAN_MAX);

    }

        // multi threaded
            // when n1+n2 is less max tokens
                // tokens=max-(n1+n2)
                // isThrottled=false

    @Test
    public void testWhenThread1NLessThanMaxAndThread2NLessThanMax() throws InterruptedException {
        boolean throttle = bucket.isThrottled(N_LESS_THAN_MAX);
        assertFalse(throttle);

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean throttle = bucket.isThrottled(N_LESS_THAN_MAX);
                assertFalse(throttle);
            }
        });

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean throttle = bucket.isThrottled(N_LESS_THAN_MAX);
                assertFalse(throttle);
            }
        });

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        assertEquals(MAX_TOKENS - 3 * N_LESS_THAN_MAX, bucket.getCurrentTokenCount());
    }

    // when n1 + n2 greater than max tokens
                // isThrottled=true
    @Test
    public void testWhenThread1NGreaterThanMaxAndThread2NGreaterThanMax() throws InterruptedException {
        boolean throttle = bucket.isThrottled(N_GREATER_THAN_MAX);
        assertTrue(throttle);

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean throttle = bucket.isThrottled(N_GREATER_THAN_MAX);
                assertTrue(throttle);
            }
        });

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean throttle = bucket.isThrottled(N_GREATER_THAN_MAX);
                assertTrue(throttle);
            }
        });

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        assertEquals(MAX_TOKENS, bucket.getCurrentTokenCount());
    }


}
