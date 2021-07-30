package com.example

import io.micronaut.http.HttpRequest
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Filter
import io.micronaut.http.filter.HttpServerFilter
import io.micronaut.http.filter.ServerFilterChain
import io.micronaut.http.filter.ServerFilterPhase
import io.micronaut.scheduling.instrument.InvocationInstrumenter
import io.micronaut.scheduling.instrument.InvocationInstrumenterFactory
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber


/** Copy of ServerRequestContextFilter from Micronaut which has been removed with Micronaut version 2.5.1 . */
@Filter(Filter.MATCH_ALL_PATTERN)
class ServerRequestContextFilter(
    private val invocationInstrumenterFactories: List<InvocationInstrumenterFactory>
) : HttpServerFilter {
    override fun doFilter(request: HttpRequest<*>, chain: ServerFilterChain): Publisher<MutableHttpResponse<*>> {
        InvocationInstrumenter.combine(getInvocationInstrumenter(request)).newInstrumentation().use { instrumentation ->
            val actual = chain.proceed(request)
            val invocationInstrumenterAfterProceed =
                InvocationInstrumenter.combine(getInvocationInstrumenter(request))
            return Publisher { actualSubscriber: Subscriber<in MutableHttpResponse<*>>? ->
                invocationInstrumenterAfterProceed.newInstrumentation().use { ignored ->
                    actual.subscribe(actualSubscriber)
                }
            }
        }
    }

    override fun getOrder(): Int {
        return ServerFilterPhase.FIRST.order()
    }

    private fun getInvocationInstrumenter(request: HttpRequest<*>): List<InvocationInstrumenter> {
        val instrumenters: MutableList<InvocationInstrumenter> = ArrayList(
            invocationInstrumenterFactories.size + 1
        )
        instrumenters.add(ServerRequestContextInvocationInstrumenter(request))
        for (instrumenterFactory in invocationInstrumenterFactories) {
            val instrumenter = instrumenterFactory.newInvocationInstrumenter()
            if (instrumenter != null) {
                instrumenters.add(instrumenter)
            }
        }
        return instrumenters
    }
}
