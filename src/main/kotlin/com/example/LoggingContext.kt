package com.example

import com.example.ApplicationHeaders.TRACKING_ID
import org.slf4j.MDC
import jakarta.inject.Singleton

@Singleton
class LoggingContext {

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

    data class RequestContextData(val map: Map<String, String>?) {

        fun isNotEmpty() : Boolean {
           return map?.isNotEmpty() ?: false
        }
    }
}

