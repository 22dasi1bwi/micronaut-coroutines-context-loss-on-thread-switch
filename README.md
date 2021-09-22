## Problem declaration

On our productive systems we regularly experienced messed up context information in our logging. Thus, making it pretty
hard for us to provide an accurate issue analysis for our customers.
Having a closer look into the problem showed, that the context loss happens more often than not during a client call
when boundaries of Kotlin Coroutines and Reactive Streams merge. This happens with all Micronaut versions >= 2.5.1 .

## One way to solve the problem (Micronaut version: 2.x)

As mentioned in [this issue](https://github.com/micronaut-projects/micronaut-core/issues/5656) Micronaut versions >= 2.5.1
have a class named [ServerRequestContextFilter](https://github.com/micronaut-projects/micronaut-core/blob/2.4.x/http-server/src/main/java/io/micronaut/http/server/context/ServerRequestContextFilter.java) removed (because of performance reasons) and in a different way 
implemented into the routing routine of Micronaut.
We tried different ways but finally ended up reintroducing that class into our code, because no other way provided
a more consistent result especially when stress-testing the application. Please refer to [Howto](#Howto) for more 
details.

*Important Note*: In order for the whole context propagation to work properly, 3 classes of this project are necessary:
1. `com.example.MdcInstrumenter` (adapted version of `io.micronaut:micronaut-tracing` dependency's [MdcInstrumenter](https://github.com/micronaut-projects/micronaut-core/blob/2.5.x/tracing/src/main/java/io/micronaut/tracing/instrument/util/MdcInstrumenter.java).)
2. `com.example.ServerRequestContextFilterCopy.kt`
3. `com.example.ServerRequestContextInstrumenterCopy.kt`

## Broken after upgrade to Micronaut 3.0

After upgrading to Micronaut 3.0 the two classes required to get things working in Micronaut 2.5.x `com.example.ServerRequestContextFilterCopy.kt` and `com.example.ServerRequestContextInstrumenterCopy.kt` are preventing the MDC
to have proper information once processes are resumed on a different thread after hitting a suspension point.

The removal of those two classes makes stress testing (see [Howto](#Howto) and `execute_benchmark.sh`) only work at a
rate of approximately *40%*.

Also `com.example.MdcInstrumener` doesn't seem to have an impact whatsoever.

## Working after testing Micronaut 3.1.0-SNAPSHOT
After testing the [fix](https://github.com/micronaut-projects/micronaut-core/issues/6113) provided by the Micronaut
team, I can confirm that the scenario described in this project is working!
However, it requires `com.example.MdcInstrumener` to be there.

## Howto

- Install [WRK](https://github.com/wg/wrk).
- Fire up the application.
- Execute `execute_benchmark.sh`. You can adapt the headers and body in the `post.lua` file.
- Check WRK's output and verify that *NO* request has failed. An invalid would look like this:
```
Running 20s test @ http://localhost:8080/trigger
  24 threads and 800 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   129.04ms   21.27ms 264.48ms   64.31%
    Req/Sec   256.21     62.48   340.00     54.72%
  122490 requests in 20.08s, 34.93MB read
  Non-2xx or 3xx responses: 120214
Requests/sec:   6101.26
Transfer/sec:      1.74MB
```
- A valid output would look like this:
```
Running 20s test @ http://localhost:8080/trigger
  24 threads and 800 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   185.64ms  200.15ms   1.71s    92.73%
    Req/Sec   234.08     90.40   434.00     64.19%
  105805 requests in 20.09s, 16.72MB read
Requests/sec:   5267.13
Transfer/sec:    852.56KB
```


