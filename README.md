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


