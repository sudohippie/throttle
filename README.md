# Throttling API [![Build Status](https://travis-ci.org/sudohippie/throttle.png)](https://travis-ci.org/sudohippie/throttle)

## Overview
API to throttle/rate-limit requests

This API implements two popular throttling strategies, namely:
1. Fixed token bucket
2. Leaky token buckt

### Fixed token bucket
Details for this implementation can be found at: 

### Leaky token bucket
Details for this implementation can be found at:
With in the API, Leaky buckets have been implemented as two types
1. StepDownLeakyTokenBucket
2. StepUpLeakyTokenBucket

StepDownLeakyTokenBucket resembles a bucket which has been filled with tokens at the beginning but subsequently leaks tokens at a fixed interval.
StepUpLeakyTokenBucket resemembles an empty bucket at the beginning but get filled will tokens over a fixed interval.

## Examples

### FixedTokenBucket Example
```java
// construct strategy
ThrottleStrategy strategy = new FixedTokenBucket(100, 1, TimeUnit.MINUTES);

// provide the strategy to the throttler
Throttle throttle = new Throttle(strategy);

// throttle :)
throttle.canProceed();
```

### StepUpTokenBucket Example
```java
// construct strategy
ThrottleStrategy strategy = new StepUpLeakyTokenBucket(100, 1, TimeUnit.MINUTES, 25, 15, TimeUnit.SECONDS);

// provide the strategy to the throttler
Throttle throttle = new Throttle(strategy);

// throttle :)
throttle.canProceed();
```
### StepDownTokenBucket Example
```java
// construct strategy
ThrottleStrategy strategy = new StepDownLeakyTokenBucket(100, 1, TimeUnit.MINUTES, 25, 15, TimeUnit.SECONDS);

// provide the strategy to the throttler
Throttle throttle = new Throttle(strategy);

// throttle :)
throttle.canProceed();
```
