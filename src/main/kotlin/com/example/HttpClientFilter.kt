package com.example

import com.example.ApplicationHeaders.TRACKING_ID
import io.micronaut.core.order.Ordered
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.MutableHttpRequest
import io.micronaut.http.annotation.Filter
import io.micronaut.http.filter.ClientFilterChain
import io.micronaut.http.filter.HttpClientFilter
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactor.mono
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory

@Filter("/greet")
class HttpClientFilter(private val requestContext: RequestContext) : CoroutineHttpClientFilter {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun getOrder(): Int {
        return Ordered.HIGHEST_PRECEDENCE
    }

    override suspend fun filter(request: HttpRequest<*>, chain: ClientFilterChain): HttpResponse<*> {
        val originalTrackingId = request.headers[TRACKING_ID] as String
        val loggingTrackingId = requestContext.get(TRACKING_ID)
        requestContext.import(RequestContext.RequestContextData(requestContext.asMap()))
        if (originalTrackingId != loggingTrackingId){
            throw IllegalArgumentException("TrackingIds do not match! Request: $originalTrackingId vs. Context: $loggingTrackingId")
        }
        logger.info("Remote request URL: {}, ({})", request.uri, loggingTrackingId)

        return chain.next(request).also {
            logRemoteRequestStatus(it, originalTrackingId)
        }
    }

    private fun logRemoteRequestStatus(response: HttpResponse<*>, trackingId: String) {
        logger.info("Response Status {} was returned ({})", response.status.code, trackingId)
    }
}
interface CoroutineHttpClientFilter : HttpClientFilter {

    suspend fun filter(request: HttpRequest<*>, chain: ClientFilterChain): HttpResponse<*>

    override fun doFilter(request: MutableHttpRequest<*>, chain: ClientFilterChain): Publisher<out HttpResponse<*>> {
        return mono {
            filter(request, chain)
        }
    }

}

suspend fun ClientFilterChain.next(request: HttpRequest<*>): HttpResponse<*> {
    return this.proceed(request).awaitFirst()
}
