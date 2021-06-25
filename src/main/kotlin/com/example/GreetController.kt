package com.example

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import kotlinx.coroutines.delay
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Controller
class GreetController(private val greetService: GreetService) {

    @Get("/greet")
    suspend fun greet() = withContext(MDCContext()) { greetService.withName("World") }
}

@Singleton
class GreetService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun withName(name: String): String {
        logger.info("Request Context before thread switch")
        delay(100) // "forcing" the initial thread (event loop) to suspend
        logger.info("Request Context after thread switch")
        return "Hello $name"
    }
}
