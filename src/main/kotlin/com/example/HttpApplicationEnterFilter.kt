package com.example

import io.micronaut.http.HttpRequest
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Filter
import io.micronaut.http.filter.HttpServerFilter
import io.micronaut.http.filter.ServerFilterChain
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono

@Filter("/trigger")
class HttpApplicationEnterFilter(private val loggingContext: LoggingContext) : HttpServerFilter {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun doFilter(request: HttpRequest<*>, chain: ServerFilterChain): Publisher<MutableHttpResponse<*>> {
        loggingContext.clear()
        val trackingId = request.headers["X-TrackingId"]
        loggingContext.trackingId = trackingId
        logger.info("Application enter ($trackingId).")
        val reactorContext = ReactorContext.from(loggingContext.export().map)

        return Mono.from(chain.proceed(request))
            .contextWrite { reactorContext }
            .doOnNext { logger.info("Application exit ($trackingId).") }
    }
}
