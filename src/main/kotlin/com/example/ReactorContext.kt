package com.example

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.reactor.ReactorContext

class ReactorContext {

    companion object {
        suspend fun get(key: String) : String {
            val reactorContext = currentCoroutineContext()[ReactorContext.Key]
            return if (reactorContext != null){
                reactorContext.context[key]
            } else {
                error("No ReactorContext available.")
            }
        }

        suspend fun getOrDefault(key: String, default: String) : String {
            val reactorContext = currentCoroutineContext()[ReactorContext.Key]
            return if (reactorContext != null){
                reactorContext.context.getOrDefault(key, default)
            } else {
                error("No ReactorContext available.")
            }
        }
    }
}
