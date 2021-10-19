package com.example

import io.micronaut.scheduling.instrument.Instrumentation
import io.micronaut.scheduling.instrument.InvocationInstrumenter
import io.micronaut.scheduling.instrument.InvocationInstrumenterFactory
import io.micronaut.scheduling.instrument.ReactiveInvocationInstrumenterFactory
import jakarta.inject.Singleton

@Singleton
class MdcInstrumenter(private val loggingContext: LoggingContext) : InvocationInstrumenterFactory,
    ReactiveInvocationInstrumenterFactory {

    /**
     * Inspired by
     * https://github.com/micronaut-projects/micronaut-core/blob/2.4.x/tracing/src/main/java/io/micronaut/tracing/instrument/util/MdcInstrumenter.java .
     *
     * We wanted this to be as minimalistic as possible and did therefore dispense with `micronaut-tracing`dependency.
     */
    override fun newInvocationInstrumenter(): InvocationInstrumenter {
        val currentLoggingContext = loggingContext.export()
        return InvocationInstrumenter {
            val oldLoggingContext = loggingContext.export()
            loggingContext.import(currentLoggingContext)

            Instrumentation {
                if (oldLoggingContext.isNotEmpty()) {
                    loggingContext.import(oldLoggingContext)
                } else {
                    loggingContext.clear()
                }
            }
        }
    }

    override fun newReactiveInvocationInstrumenter(): InvocationInstrumenter {
        return newInvocationInstrumenter()
    }
}
