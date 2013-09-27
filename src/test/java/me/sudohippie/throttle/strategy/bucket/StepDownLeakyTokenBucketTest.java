package me.sudohippie.throttle.strategy.bucket;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Raghav Sidhanti
 * 9/26/13
 */
public class StepDownLeakyTokenBucketTest {
    // test state only
        // when max tokens is negative
    @Test(expected = IllegalArgumentException.class)
    public void testWhenMaxTokensIsNegative() throws Exception {
        StepDownLeakyTokenBucket bucket = new StepDownLeakyTokenBucket(-1, REFILL_INTERVAL, TimeUnit.MILLISECONDS, STEP_TOKENS, STEP_INTERVAL, TimeUnit.MILLISECONDS);
    }

        // when refill interval is negative
    @Test(expected = IllegalArgumentException.class )
    public void testWhenRefillIntervalIsNegative() throws Exception {
        StepDownLeakyTokenBucket bucket = new StepDownLeakyTokenBucket(MAX_TOKENS, -1, TimeUnit.MILLISECONDS, STEP_TOKENS, STEP_INTERVAL, TimeUnit.MILLISECONDS);
    }

    // when step token is negative
    @Test(expected = IllegalArgumentException.class)
    public void testWhenStepTokenIsNegative() throws Exception {
        StepDownLeakyTokenBucket bucket = new StepDownLeakyTokenBucket(MAX_TOKENS, REFILL_INTERVAL, TimeUnit.MILLISECONDS, -1, STEP_INTERVAL, TimeUnit.MILLISECONDS);
    }

    // when step interval is negative
    @Test(expected = IllegalArgumentException.class)
    public void testWhenStepIntervalIsNegative() throws Exception {
        StepDownLeakyTokenBucket bucket = new StepDownLeakyTokenBucket(MAX_TOKENS, REFILL_INTERVAL, TimeUnit.MILLISECONDS, STEP_TOKENS, -1, TimeUnit.MILLISECONDS);
    }

    @Before
    public void setUp() throws Exception {
        bucket = new StepDownLeakyTokenBucket(MAX_TOKENS, REFILL_INTERVAL, TimeUnit.MILLISECONDS, STEP_TOKENS, STEP_INTERVAL, TimeUnit.MILLISECONDS);
    }

    // setup state
    private final long MAX_TOKENS = 10;
    private final long REFILL_INTERVAL = 5000L;
    private final long STEP_TOKENS = 2;
    private final long STEP_INTERVAL = 1000L;

    private StepDownLeakyTokenBucket bucket;

    // test behaviour, single threaded
        // at the beginning, number of tokens should be equal to max

    @Test
    public void testTokensEqualsMaxAtFirstStep() throws Exception {
        assertEquals(MAX_TOKENS, bucket.getCurrentTokenCount());
    }

    // after one whole interval, at the beginning number of tokens should be equal to max
    @Test
    public void testTokensEqualsMaxAtTheStartOfNextInterval() throws Exception {
        Thread.sleep(REFILL_INTERVAL);
        assertEquals(MAX_TOKENS, bucket.getCurrentTokenCount());
    }

    // throttle max, full bucket at beginning of next interval
    @Test
    public void testTokensEqualsMaxAtTheStartOfNextIntervalAfterMaxThrottle() throws Exception {
        bucket.isThrottled(MAX_TOKENS);

        Thread.sleep(REFILL_INTERVAL);

        assertEquals(MAX_TOKENS, bucket.getCurrentTokenCount());
    }

    // throttle max, not throttled at beginning of next interval

    @Test
    public void testNotThrottledWhenThrottledToMaxOnceWithInInterval() throws Exception {
        boolean throttled = bucket.isThrottled(MAX_TOKENS);

        assertFalse(throttled);
    }

    // throttle max, zero tokens till end of interval
    @Test
    public void testZeroTokensTillEndOfIntervalAfterThrottleMaxOnce() throws Exception {
        bucket.isThrottled(MAX_TOKENS);

        final long SLEEP_TIME = 1000L;
        for(int i = 1; i < (REFILL_INTERVAL / STEP_INTERVAL); i ++){
            Thread.sleep(SLEEP_TIME);
            assertEquals(0, bucket.getCurrentTokenCount());
        }
    }

    // throttle max, throttled till end of interval
    @Test
    public void testThrottledTillEndOfIntervalAfterThrottleMaxOnce() throws Exception {
        boolean throttled = bucket.isThrottled(MAX_TOKENS);
        assertFalse(throttled);

        for(int i = 1; i < (REFILL_INTERVAL / STEP_INTERVAL); i ++){
            Thread.sleep(STEP_INTERVAL);
            assertTrue(bucket.isThrottled());
        }
    }
    // sleep at every step interval, tokens must be equal to max - (i*stepTokens) at each interval

    @Test
    public void testTokensAtEachStepIntervalIsCorrect() throws Exception {
        for(int i = 0; i < (REFILL_INTERVAL / STEP_INTERVAL); i ++){
            long expectedTokens = MAX_TOKENS - (i * STEP_TOKENS);

            assertEquals(expectedTokens, bucket.getCurrentTokenCount());
            Thread.sleep(STEP_INTERVAL);
        }
    }

    // throttle n = 1 + max - (i * stepTokens), where i is a step. Should be throttled as we keep decreasing throttle n over time
    @Test
    public void testThrottledWhenNGreaterThanTokensAtEachStep() throws Exception {
        for(int i = 0; i < (REFILL_INTERVAL / STEP_INTERVAL); i ++){
            long excessTokensForStep = MAX_TOKENS - (i * STEP_TOKENS) + 1;

            assertTrue(bucket.isThrottled(excessTokensForStep));
            Thread.sleep(STEP_INTERVAL);
        }
    }

    // test behaviour, multi threaded
        // throttle n (n < max/2) each, number of tokens remaining should be max - 2*n

    @Test
    public void testTokenCountInMultiThreadedThrottleThread1AndThread2WithInAnInterval() throws Exception {

        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                bucket.isThrottled();
            }
        });

        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                bucket.isThrottled();
            }
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        assertEquals(MAX_TOKENS - 2, bucket.getCurrentTokenCount());
    }

    // throttle t1 n > max, t2 n < max. t1 should be throttled, t2 should not be throttled.

    @Test
    public void testMultiThreadedWhenThread1ThrottledAboveMaxAndThread2BelowMax() throws Exception {
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean throttled = bucket.isThrottled(MAX_TOKENS + 1);
                assertTrue(throttled);
            }
        });

        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean throttled = bucket.isThrottled(MAX_TOKENS - 1);
                assertFalse(throttled);
            }
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();
    }
}
