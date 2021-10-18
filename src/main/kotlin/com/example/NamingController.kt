package com.example

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.uri.UriBuilder
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import java.net.URI

@Controller
class NamingController(private val namingService: NamingService ) {

    @Post("/trigger")
    suspend fun trigger(request: HttpRequest<*>, @Body requestBody: NameRequestBody) : HttpResponse<String> {
        return withContext(Dispatchers.IO + MDCContext()) {
           namingService.withName(requestBody.name)
        }
    }
}
class NameRequestBody(val name: String)

@Singleton
class NamingService(private val namingClient: NamingClient) {

    suspend fun withName(name: String): HttpResponse<String> {
        return withContext(Dispatchers.IO){
            delay(50) // "forcing" the initial thread (event loop) to suspend
            namingClient.getFor(name)
        }
    }
}

const val namingClientId = "name"

@Singleton
class NamingClient(@Client(id = namingClientId) private val client: HttpClient) {

    suspend fun getFor(name: String): HttpResponse<String> {
        return withContext(Dispatchers.IO) {
            val trackingId = ReactorContext.getOrDefault("X-TrackingId", "UNKNOWN")
            val uri: URI = UriBuilder.of("http://localhost:8080/greet")
                .queryParam("name", name)
                .build()

            val request = HttpRequest.GET<String>(uri).apply {
                header("X-TrackingId", trackingId)
            }

            client.exchange(request, String::class.java).awaitFirst()
        }
    }
}
