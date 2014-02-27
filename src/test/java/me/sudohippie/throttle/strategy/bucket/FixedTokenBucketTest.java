package me.sudohippie.throttle.strategy.bucket;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Raghav Sidhanti
 * Date: 9/25/13
 */
public class FixedTokenBucketTest {

    /* test state */
    // test max token when negative
    @Test(expected = IllegalArgumentException.class)
    public void testWhenMaxTokenIsNegative(){
        FixedTokenBucketStrategy bucket = new FixedTokenBucketStrategy(-1, 0, TimeUnit.MILLISECONDS);
    }

    // test refill interval when negative
    @Test(expected = IllegalArgumentException.class)
    public void testWhenRefillIntervalIsNegative(){
        FixedTokenBucketStrategy bucket = new FixedTokenBucketStrategy(0, -1, TimeUnit.MILLISECONDS);
    }

    /* test logic */
    // test n throttle and fixed refill interval, fixed max tokens
    long MAX_TOKENS = 10;
    long REFILL_INTERVAL = 10;
    TimeUnit REFILL_INTERVAL_TIME_UNIT = TimeUnit.SECONDS;

    long N_LESS_THAN_MAX = 2;
    long N_GREATER_THAN_MAX = 12;
    int CUMULATIVE = 3;

    FixedTokenBucketStrategy bucket;

    @Before
    public void setUp() throws Exception {
        bucket = new FixedTokenBucketStrategy(MAX_TOKENS, REFILL_INTERVAL, REFILL_INTERVAL_TIME_UNIT);
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

    public void testWhenThread1NLessThanMaxAndThread2NLessThanMax() throws InterruptedException {
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

        assertEquals(MAX_TOKENS - 2 * N_LESS_THAN_MAX, bucket.getCurrentTokenCount());
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

	// test next release, when input param is negative
	@Test(expected = IllegalArgumentException.class)
	public void testNextReleaseWhenInputParamIsNegative(){
		bucket.timeToRelease(-1L, TimeUnit.MILLISECONDS);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNextReleaseWhenInputParamIsNull(){
		bucket.timeToRelease(1L, null);
	}

	// test next release, when appropriate tokens exist with in interval
	@Test
	public void testNextReleaseWhenTokensExistInInterval(){
		// set state
		// check release is 0
		assertEquals(0L, bucket.timeToRelease(1, TimeUnit.MILLISECONDS));
		assertEquals(0L, bucket.timeToRelease(MAX_TOKENS/2, TimeUnit.MILLISECONDS));
		assertEquals(0L, bucket.timeToRelease(MAX_TOKENS - 1, TimeUnit.MILLISECONDS));
		assertEquals(0L, bucket.timeToRelease(MAX_TOKENS, TimeUnit.MILLISECONDS));
	}

	// test next release, when tokens don't exist in interval
	@Test
	public void testNextReleaseWhenTokensAreExhaustedWithInInterval(){
		bucket.isThrottled(MAX_TOKENS);
		long nextRelease = bucket.timeToRelease(MAX_TOKENS, REFILL_INTERVAL_TIME_UNIT);

		assertTrue(nextRelease > 0);
		assertTrue(nextRelease <= REFILL_INTERVAL);
	}

	// test when number of tokens requested are greater than existing in bucket
	@Test
	public void testNextReleaseWhenNumberOfTokensInBucketIsLessThanRequested(){
		long limit = 3;
		bucket.isThrottled(MAX_TOKENS - limit);

		assertTrue(bucket.timeToRelease(limit + 1, TimeUnit.MILLISECONDS) > 0);
	}

	// test next release, when tokens don't exit in interval, sleep and check before start of next interval
	@Test
	public void testNextReleaseMultipleTimesAfterTokensAreExhaustedWithInInterval() throws InterruptedException {

		// overly glorified test. Realized all the tests are based on timing and system slowness can cause any of the
		// tests to fail.

		boolean hasExeededInterval = false;
		int repeats = 3;
		long releaseTime = 0L;

		do{
			// max out tokens
			bucket.isThrottled(MAX_TOKENS);
			releaseTime = bucket.timeToRelease(1, TimeUnit.MILLISECONDS);

			assertTrue(releaseTime > 0);

			// sleep for a fraction
			long sleepInt = REFILL_INTERVAL_TIME_UNIT.toMillis(REFILL_INTERVAL) / 2;
			long start = System.currentTimeMillis();
			Thread.sleep(sleepInt);
			long end = System.currentTimeMillis();

			// check whether sleep time exceeded next release due to system slowness
			if(end - start >= releaseTime){
				// if so, sleep until next release
				Thread.sleep(releaseTime);
				// repeat
				repeats --;
				hasExeededInterval = true;
			}else {
				hasExeededInterval = false;
				releaseTime = bucket.timeToRelease(1L, TimeUnit.MILLISECONDS);
			}
		}while(hasExeededInterval && repeats > 0);

		// test condition
		assertFalse(hasExeededInterval);
		assertTrue(releaseTime > 0);
	}

	// test next release, when tokens don't exit in interval, sleep for next release time and check after start of next interval
	@Test
	public void testNextReleaseWhenTokensHaveExhaustedButInNextInterval() throws InterruptedException {
		bucket.isThrottled(MAX_TOKENS);
		long nextRelease = bucket.timeToRelease(1L, TimeUnit.MILLISECONDS);

		assertTrue(nextRelease > 0L);

		Thread.sleep(nextRelease);

		assertEquals(0L, bucket.timeToRelease(1L, TimeUnit.MILLISECONDS));
	}

	// test next release, unit conversion
	@Test
	public void testNextReleaseWhenUnitIsToMilliSeconds(){
		bucket.isThrottled(MAX_TOKENS);
		long nextRelease = bucket.timeToRelease(1L, TimeUnit.MILLISECONDS);

		assertTrue(nextRelease > 0L);
		assertTrue(REFILL_INTERVAL_TIME_UNIT.convert(nextRelease, TimeUnit.MILLISECONDS) <= REFILL_INTERVAL);
	}

	@Test
	public void testNextReleaseWhenTimeUnitIsToSeconds(){
		bucket.isThrottled(MAX_TOKENS);
		long nextRelease = bucket.timeToRelease(1L, TimeUnit.SECONDS);

		assertTrue(nextRelease > 0L);
		assertTrue(nextRelease <= REFILL_INTERVAL_TIME_UNIT.convert(nextRelease, TimeUnit.SECONDS));
	}

	@Test
	public void testNextReleaseWhenTimeUnitIsHigherThanIntervalUnit(){
		TimeUnit unit;

		switch (REFILL_INTERVAL_TIME_UNIT){
			case NANOSECONDS:
				unit = TimeUnit.MICROSECONDS;
				break;
			case MICROSECONDS:
				unit = TimeUnit.MILLISECONDS;
				break;
			case MILLISECONDS:
				unit = TimeUnit.SECONDS;
				break;
			case SECONDS:
				unit = TimeUnit.MINUTES;
				break;
			case MINUTES:
				unit = TimeUnit.HOURS;
				break;
			case HOURS:
				unit = TimeUnit.DAYS;
				break;
			default:
				unit = TimeUnit.DAYS;
				break;
		}

		bucket.isThrottled(MAX_TOKENS);

		long nextRelease = bucket.timeToRelease(1L, unit);

		assertEquals(0L, nextRelease);

	}

	// test in multi threaded scenario, when both have tokens
	@Test
	public void testNextReleaseThreadedWhenTokensExist() throws InterruptedException {
		final long lessThanHalfTokens = MAX_TOKENS / 2 - 1;

		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				bucket.isThrottled(lessThanHalfTokens);

				assertEquals(0L, bucket.timeToRelease(1L, TimeUnit.MILLISECONDS));
			}
		});

		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				bucket.isThrottled(lessThanHalfTokens);

				assertEquals(0L, bucket.timeToRelease(1L, TimeUnit.MILLISECONDS));
			}
		});

		t1.start();
		t2.start();

		t1.join();
		t2.join();
	}

	// test in multi threaded scenario, when one thread gobbles all tokens and exits
	@Test
	public void testNextReleaseInAnIntervalWhenThread1GobblesAllTokens() throws InterruptedException {
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				bucket.isThrottled(MAX_TOKENS);

				assertTrue(bucket.timeToRelease(1L, TimeUnit.MILLISECONDS) > 0);
			}
		});

		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					assertTrue(bucket.timeToRelease(1L, TimeUnit.MILLISECONDS) > 0);

					Thread.sleep(REFILL_INTERVAL_TIME_UNIT.toMillis(REFILL_INTERVAL/2));

					assertTrue(bucket.timeToRelease(1L, TimeUnit.MILLISECONDS) > 0);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		});

		t1.start();
		t2.start();

		t1.join();
		t2.join();
	}

	// test in multi threaded scenario, when one thread gobbles all tokens and exits other waits for next release
	@Test
	public void testNextReleaseWhenThread1GobblesAllTokensThread2WaitTillNextInterval() throws InterruptedException {

		final CountDownLatch latch = new CountDownLatch(1);

		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				bucket.isThrottled(MAX_TOKENS);

				assertTrue(bucket.timeToRelease(1L, TimeUnit.MILLISECONDS) > 0);

				latch.countDown();
			}
		});

		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					latch.await();

					assertTrue(bucket.timeToRelease(1L, TimeUnit.MILLISECONDS) > 0);

					Thread.sleep(REFILL_INTERVAL_TIME_UNIT.toMillis(REFILL_INTERVAL));

					assertEquals(0L, bucket.timeToRelease(1L, TimeUnit.MILLISECONDS));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		});

		t1.start();
		t2.start();

		t1.join();
		t2.join();
	}
}
