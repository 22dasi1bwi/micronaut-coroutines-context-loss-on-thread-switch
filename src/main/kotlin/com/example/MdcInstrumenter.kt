package com.example

import io.micronaut.scheduling.instrument.Instrumentation
import io.micronaut.scheduling.instrument.InvocationInstrumenter
import io.micronaut.scheduling.instrument.InvocationInstrumenterFactory
import io.micronaut.scheduling.instrument.ReactiveInvocationInstrumenterFactory
import javax.inject.Singleton

@Singleton
class MdcInstrumenter(private val requestContext: RequestContext) : InvocationInstrumenterFactory,
    ReactiveInvocationInstrumenterFactory {

    /**
     * Inspired by
     * https://github.com/micronaut-projects/micronaut-core/blob/2.4.x/tracing/src/main/java/io/micronaut/tracing/instrument/util/MdcInstrumenter.java .
     *
     * We wanted this to be as minimalistic as possible and did therefore dispense with `micronaut-tracing`dependency.
     */
    override fun newInvocationInstrumenter(): InvocationInstrumenter {
        val currentRequestContext = requestContext.export()
        return InvocationInstrumenter {
            val oldRequestContext = requestContext.export()
            requestContext.import(currentRequestContext)

            Instrumentation {
                if (oldRequestContext.isNotEmpty()) {
                    requestContext.import(oldRequestContext)
                } else {
                    requestContext.clear()
                }
            }
        }
    }

    override fun newReactiveInvocationInstrumenter(): InvocationInstrumenter {
        return newInvocationInstrumenter()
    }
}
