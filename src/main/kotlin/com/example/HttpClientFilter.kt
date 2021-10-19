package com.example

import com.example.ApplicationHeaders.TRACKING_ID
import io.micronaut.core.order.Ordered
import io.micronaut.http.HttpResponse
import io.micronaut.http.MutableHttpRequest
import io.micronaut.http.annotation.Filter
import io.micronaut.http.filter.ClientFilterChain
import io.micronaut.http.filter.HttpClientFilter
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono

@Filter("/greet")
class HttpClientFilter(private val loggingContext: LoggingContext) : HttpClientFilter {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun getOrder(): Int {
        return Ordered.HIGHEST_PRECEDENCE
    }

    override fun doFilter(request: MutableHttpRequest<*>, chain: ClientFilterChain): Publisher<out HttpResponse<*>> {
        val originalTrackingId = request.headers[TRACKING_ID] as String
        val loggingTrackingId = loggingContext.trackingId
        if (originalTrackingId != loggingTrackingId){
           throw IllegalArgumentException("TrackingIds do not match! Request: $originalTrackingId vs. Context: $loggingTrackingId")
        }
        logger.info("Remote request URL: {}, ({})", request.uri, originalTrackingId)

        return Mono.from(chain.proceed(request))
            .doOnNext { logRemoteRequestStatus(it, originalTrackingId) }
    }

    private fun logRemoteRequestStatus(response: HttpResponse<*>, trackingId: String) {
        logger.info("Response Status {} was returned ({})", response.status.code, trackingId)
    }
}
