package com.example

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.uri.UriBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.net.URI
import javax.inject.Singleton

@Controller
class NamingController(private val namingService: NamingService ) {

    @Post("/trigger")
    suspend fun trigger(request: HttpRequest<*>, @Body requestBody: NameRequestBody) : HttpResponse<String> {
        return withContext(Dispatchers.IO + MDCContext()) {
           namingService.withName(requestBody.name, request.headers["X-TrackingId"] as String)
        }
    }
}
class NameRequestBody(val name: String)

@Singleton
class NamingService(private val namingClient: NamingClient) {

    suspend fun withName(name: String, trackingId: String): HttpResponse<String> {
        return withContext(Dispatchers.IO){
            delay(100) // "forcing" the initial thread (event loop) to suspend
            namingClient.getFor(name, trackingId)
        }
    }
}

const val namingClientId = "name"

@Singleton
class NamingClient(@Client(id = namingClientId) private val client: HttpClient) {

    suspend fun getFor(name: String, trackingId: String): HttpResponse<String> {
        return withContext(Dispatchers.IO) {
            val uri: URI = UriBuilder.of("http://localhost:8081/greet")
                .queryParam("name", name)
                .build()

            val request = HttpRequest.GET<String>(uri).apply {
                header("my-header", "foo")
                header("X-TrackingId", trackingId)
            }

            client.exchange(request, String::class.java).awaitFirst()
        }
    }
}
