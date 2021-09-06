package com.example

import io.micronaut.core.order.Ordered
import io.micronaut.http.HttpResponse
import io.micronaut.http.MutableHttpRequest
import io.micronaut.http.annotation.Filter
import io.micronaut.http.filter.ClientFilterChain
import io.micronaut.http.filter.HttpClientFilter
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono

@Filter(Filter.MATCH_ALL_PATTERN)
class HttpClientFilter(private val requestContext: RequestContext) : HttpClientFilter {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun getOrder(): Int {
        return Ordered.HIGHEST_PRECEDENCE
    }

    override fun doFilter(request: MutableHttpRequest<*>, chain: ClientFilterChain): Publisher<out HttpResponse<*>> {
        val trackingId : String = request.headers["X-TrackingId"] as String
        if (trackingId != requestContext.trackingId){
           throw IllegalArgumentException("TrackingIds do not match! Request: $trackingId vs. Context: ${requestContext.trackingId}")
        }
        logger.info("Remote request URL: {}, ({})", request.uri, trackingId)

        return Mono.from(chain.proceed(request))
            .doOnNext { logRemoteRequestStatus(it, trackingId) }
    }

    private fun logRemoteRequestStatus(response: HttpResponse<*>, trackingId: String) {
        logger.info("Response Status {} was returned ({})", response.status.code, trackingId)
    }
}
