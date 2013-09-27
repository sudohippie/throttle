package me.sudohippie.throttle.strategy.bucket;

import org.junit.Test;

/**
 * Raghav Sidhanti
 * 9/26/13
 */
public class StepDownLeakyTokenBucketTest {
    // test state only
        // when max tokens is negative
        // when refill interval is negative
        // when step token is negative
        // when step interval is negative

    // setup state
    // test behaviour, single threaded
        // at the beginning, number of tokens should be equal to max
        // in the middle of the interval, number of tokens should be less than or equal to max - (maxInterval/stepinterval/2*stepTokens)
        // after one whole interval, at the beginning number of tokens should be equal to max
        // throttle max, zero tokens till end of interval
        // throttle max, throttled till end of interval
        // throttle max, full bucket at beginning of next interval
        // throttle max, not throttled at beginning of next interval
        // throttle n > max, should be throttled
        // throttle max/2 + n, bucket should be less than max - (max/2+n) at mid of interval
        // throttle max/2 + n, bucket should be throttled at mid of interval
        // throttle max/2 + n, bucket should be full at the beginning of next interval
        // sleep at every step interval, tokens must be equal to max - (i*stepTokens) at each interval
        // throttle n = 1 + i * stepTokens, where i is a step. Should be throttled as we keep decreasing throttle n over time
    // test behaviour, multi threaded
        // throttle n (n < max/2) each, number of tokens remaining should be max - 2*n
        // throttle t1 n > max, t2 n < max. t1 should be throttled, t2 should not be throttled.
}
