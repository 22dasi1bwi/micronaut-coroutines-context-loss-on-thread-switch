package com.example

import io.micronaut.http.HttpRequest
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Filter
import io.micronaut.http.filter.OncePerRequestHttpServerFilter
import io.micronaut.http.filter.ServerFilterChain
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import reactor.kotlin.core.publisher.toMono
import java.util.*

@Filter(Filter.MATCH_ALL_PATTERN)
class HttpApplicationEnterFilter(private val requestContext: RequestContext) : OncePerRequestHttpServerFilter() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun doFilterOnce(request: HttpRequest<*>, chain: ServerFilterChain): Publisher<MutableHttpResponse<*>> {
        requestContext.trackingId = request.headers["X-TrackingId"] ?: UUID.randomUUID().toString()
        logger.info("Application enter.")

        return chain.proceed(request).toMono()
            .doOnNext {
                logger.info("Application exit.")
            }
    }
}
