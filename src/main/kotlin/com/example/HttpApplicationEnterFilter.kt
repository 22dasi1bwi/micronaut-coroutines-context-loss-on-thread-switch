package com.example

import io.micronaut.http.HttpRequest
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Filter
import io.micronaut.http.filter.OncePerRequestHttpServerFilter
import io.micronaut.http.filter.ServerFilterChain
import io.reactivex.Flowable
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory

@Filter(Filter.MATCH_ALL_PATTERN)
class HttpApplicationEnterFilter(private val requestContext: RequestContext) : OncePerRequestHttpServerFilter() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun doFilterOnce(request: HttpRequest<*>, chain: ServerFilterChain): Publisher<MutableHttpResponse<*>> {
        val trackingId = request.headers["X-TrackingId"]
        requestContext.trackingId = trackingId
        logger.info("Application enter ($trackingId).")

        return Flowable.fromPublisher(chain.proceed(request))
            .doOnNext() {
                logger.info("Application exit ($trackingId).")
            }
    }
}
