package com.example

import com.example.ApplicationHeaders.TRACKING_ID
import io.micronaut.http.HttpRequest
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Filter
import io.micronaut.http.filter.HttpServerFilter
import io.micronaut.http.filter.ServerFilterChain
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactor.asCoroutineContext
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory

@Filter("/trigger")
class HttpApplicationEnterFilter(private val loggingContext: LoggingContext) : CoroutineHttpServerFilter {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun filter(request: HttpRequest<*>, chain: ServerFilterChain): MutableHttpResponse<*> {
        loggingContext.clear()
        val trackingId = request.headers[TRACKING_ID]
        loggingContext.trackingId = trackingId
        logger.info("Application enter ($trackingId).")
        val reactorContext = ReactorContext.from(loggingContext.export().map)

        return withContext(MDCContext() + reactorContext.asCoroutineContext()) {
            chain.next(request).also {
                logger.info("Application exit ($trackingId).")
            }
        }
    }
}

interface CoroutineHttpServerFilter : HttpServerFilter {

    suspend fun filter(request: HttpRequest<*>, chain: ServerFilterChain): MutableHttpResponse<*>

    override fun doFilter(request: HttpRequest<*>, chain: ServerFilterChain): Publisher<MutableHttpResponse<*>> {
        return mono {
            filter(request, chain)
        }
    }

}

suspend fun ServerFilterChain.next(request: HttpRequest<*>): MutableHttpResponse<*> {
    return this.proceed(request).awaitFirst()
}
