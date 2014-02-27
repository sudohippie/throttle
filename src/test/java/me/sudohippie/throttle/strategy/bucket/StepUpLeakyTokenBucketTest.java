package me.sudohippie.throttle.strategy.bucket;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Raghav Sidhanti
 * 9/27/13
 */
public class StepUpLeakyTokenBucketTest {
    /* test state */
    // max tokens can not be negative
    @Test(expected = IllegalArgumentException.class)
    public void testMaxTokensCanNotBeNegative() throws Exception {
        StepUpLeakyTokenBucketStrategy bucket = new StepUpLeakyTokenBucketStrategy(-1, REFILL_INTERVAL, TimeUnit.MILLISECONDS, STEP_TOKENS, STEP_INTERVAL, TimeUnit.MILLISECONDS);
    }

    // interval can not be negative
    @Test(expected = IllegalArgumentException.class)
    public void testRefillIntervalCanNotBeNegative() throws Exception {
        StepUpLeakyTokenBucketStrategy bucket = new StepUpLeakyTokenBucketStrategy(MAX_TOKENS, -1, TimeUnit.MILLISECONDS, STEP_TOKENS, STEP_INTERVAL, TimeUnit.MILLISECONDS);
    }

    // step tokens can not be negative
    @Test(expected = IllegalArgumentException.class)
    public void testStepTokensCanNotBeNegative() throws Exception {
        StepUpLeakyTokenBucketStrategy bucket = new StepUpLeakyTokenBucketStrategy(MAX_TOKENS, REFILL_INTERVAL, TimeUnit.MILLISECONDS, -1, STEP_INTERVAL, TimeUnit.MILLISECONDS);
    }

    // step interval can not be negative
    @Test(expected = IllegalArgumentException.class)
    public void testStepIntervalCanNotBeNegative() throws Exception {
        StepUpLeakyTokenBucketStrategy bucket = new StepUpLeakyTokenBucketStrategy(MAX_TOKENS, REFILL_INTERVAL, TimeUnit.MILLISECONDS, STEP_TOKENS, -1, TimeUnit.MILLISECONDS);
    }

    /* test behaviour for given state */
    // setup state
    private final long MAX_TOKENS = 10;
    private final long REFILL_INTERVAL = 5000L;
    private final long STEP_TOKENS = 2;
    private final long STEP_INTERVAL = 1000L;

    private StepUpLeakyTokenBucketStrategy bucket;
    @Before
    public void setUp() throws Exception {
        bucket = new StepUpLeakyTokenBucketStrategy(MAX_TOKENS, REFILL_INTERVAL, TimeUnit.MILLISECONDS, STEP_TOKENS, STEP_INTERVAL, TimeUnit.MILLISECONDS);
    }

    // single threaded
    // for each step, token count must increase as (i*stepTokens) where i=1 <= n
    @Test
    public void testTokenCountIsCorrectAtEachStepWithInInterval() throws Exception {
        for (int i = 1; i <= (REFILL_INTERVAL/STEP_INTERVAL); i ++){
            long tokenCount = bucket.getCurrentTokenCount();
            long expectedTokensAtStep = STEP_TOKENS * i;

            assertEquals(expectedTokensAtStep, tokenCount);

            Thread.sleep(STEP_INTERVAL);
        }
    }

    // for each step, throttle should be false when n <= stepTokens
    @Test
    public void testThrottleIsFalseAtEachStepWithInInterval() throws Exception {
        for (int i = 1; i <= (REFILL_INTERVAL/STEP_INTERVAL); i ++){
            boolean throttled = bucket.isThrottled(STEP_TOKENS);

            assertFalse(throttled);

            Thread.sleep(STEP_INTERVAL);
        }
    }

    // for each step, token should equal 0 after throttled at n=stepToken
    @Test
    public void testTokenEqualsZeroAfterThrottlingAtStepTokensAtEachInterval() throws Exception {
        for (int i = 1; i <= (REFILL_INTERVAL/STEP_INTERVAL); i ++){
            bucket.isThrottled(STEP_TOKENS);

            assertEquals(0, bucket.getCurrentTokenCount());
        }
    }

    // At the start of next interval, tokens should equal stepTokens
    @Test
    public void testTokensEqualsMaxAtStartOfNextInterval() throws Exception {
        bucket.isThrottled();

        Thread.sleep(REFILL_INTERVAL);

        assertEquals(STEP_TOKENS, bucket.getCurrentTokenCount());
    }

    // multi threaded
    // thread1 throttle n= steptoken, thread2 sleep for stepInterval. token must be 0
    @Test
    public void testMultiThreadedWhenThread1NEqualsToStepTokenAndThread2SleepForStepIntervalFollowedByNStepTokenThrottle() throws Exception {
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean throttled = bucket.isThrottled(STEP_TOKENS);

                assertFalse(throttled);
                assertEquals(0, bucket.getCurrentTokenCount());
            }
        });

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(STEP_INTERVAL);
                    boolean throttled = bucket.isThrottled(STEP_TOKENS);

                    assertFalse(throttled);
                    assertEquals(0, bucket.getCurrentTokenCount());
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        });

        t1.start();
        t2.start();

        t1.join();
        t2.join();
    }
}
