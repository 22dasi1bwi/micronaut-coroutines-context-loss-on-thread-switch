package com.example

import io.micronaut.core.annotation.NonNull
import io.micronaut.http.HttpRequest
import io.micronaut.http.context.ServerRequestContext
import io.micronaut.scheduling.instrument.Instrumentation
import io.micronaut.scheduling.instrument.InvocationInstrumenter


/** Copy of ServerRequestContextInvocationInstrumenter from Micronaut which has been removed with Micronaut version 2.5.1 . */
class ServerRequestContextInvocationInstrumenter(private val invocationRequest: HttpRequest<*>) :
    InvocationInstrumenter {
    @NonNull
    override fun newInstrumentation(): Instrumentation {
        val currentRequest = ServerRequestContext.currentRequest<Any>().orElse(null)
        val isSet: Boolean
        if (invocationRequest !== currentRequest) {
            isSet = true
            ServerRequestContext.set(invocationRequest)
        } else {
            isSet = false
        }
        return Instrumentation { cleanup: Boolean ->
            if (cleanup) {
                ServerRequestContext.set(null)
            } else if (isSet) {
                ServerRequestContext.set(currentRequest)
            }
        }
    }
}
