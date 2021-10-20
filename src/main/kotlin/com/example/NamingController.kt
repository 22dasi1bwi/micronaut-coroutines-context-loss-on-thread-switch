package com.example

import com.example.ApplicationHeaders.TRACKING_ID
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.uri.UriBuilder
import io.micronaut.reactor.http.client.ReactorHttpClient
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.net.URI

@Controller
class NamingController(private val namingService: NamingService) {

    @Post("/trigger")
    suspend fun trigger(@Body requestBody: NameRequestBody): HttpResponse<String> {
        return namingService.withName(requestBody.name)
    }
}

class NameRequestBody(val name: String)

@Singleton
class NamingService(private val namingClient: NamingClient) {

    suspend fun withName(name: String): HttpResponse<String> {
        return withContext(Dispatchers.IO) {
            delay(50) // "forcing" the initial thread (event loop) to suspend
            namingClient.getFor(name)
        }
    }
}

const val namingClientId = "name"

@Singleton
class NamingClient(@Client(id = namingClientId) private val client: ReactorHttpClient,
                   private val requestContext: RequestContext
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun getFor(name: String): HttpResponse<String> {
        val trackingId = requestContext.get(TRACKING_ID)
        logger.info("blub $trackingId")
        val uri: URI = UriBuilder.of("http://localhost:8080/greet")
            .queryParam("name", name)
            .build()

        val request = HttpRequest.GET<String>(uri).apply {
            header("X-TrackingId", trackingId)
        }

        return client.exchange(request, String::class.java).awaitFirst()
    }
}
