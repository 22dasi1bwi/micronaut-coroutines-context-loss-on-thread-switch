package com.example

import org.slf4j.MDC
import javax.inject.Singleton

const val TRACKING_ID: String = "tracking_id"

@Singleton
class RequestContext {

    var trackingId: String?
        get() = MDC.get(TRACKING_ID)
        set(value) = MDC.put(TRACKING_ID, value)
}
