package com.example

import com.example.ApplicationHeaders.TRACKING_ID
import org.slf4j.MDC
import jakarta.inject.Singleton
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.reactor.ReactorContext
import reactor.util.context.Context

private val applicationHeaders = setOf(TRACKING_ID, "X-ApplicationName", "X-FinOrVin")

@Singleton
class RequestContext {

    var trackingId: String?
        get() = MDC.get(TRACKING_ID)
        set(value) = MDC.put(TRACKING_ID, value)

    fun export(): RequestContextData {
        return RequestContextData(MDC.getCopyOfContextMap())
    }

    fun import(contextData: RequestContextData) {
        contextData.map?.let { MDC.setContextMap(it) }
    }

    fun clear() {
        MDC.clear()
    }

    fun toReactorContext(): Context {
        val contextMap = export().map
        return if (contextMap != null) {
            Context.of(contextMap)
        } else {
            Context.empty()
        }
    }

    suspend fun asMap(): Map<String, String> {
        val reactorContext = currentCoroutineContext()[ReactorContext.Key]
        return if (reactorContext != null) {
            applicationHeaders.associateBy({ it }, { getOrDefault(it, "") })
        } else {
            error("No ReactorContext available.")
        }
    }

    suspend fun get(key: String): String {
        val reactorContext = currentCoroutineContext()[ReactorContext.Key]
        return if (reactorContext != null) {
            reactorContext.context[key]
        } else {
            error("No ReactorContext available.")
        }
    }

    suspend fun getOrDefault(key: String, default: String): String {
        val reactorContext = currentCoroutineContext()[ReactorContext.Key]
        return if (reactorContext != null) {
            reactorContext.context.getOrDefault(key, default)
        } else {
            error("No ReactorContext available.")
        }
    }

    data class RequestContextData(val map: Map<String, String>?) {

        fun isNotEmpty(): Boolean {
            return map?.isNotEmpty() ?: false
        }
    }
}

