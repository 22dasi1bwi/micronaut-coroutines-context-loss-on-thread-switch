package com.example

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue

@Controller
class GreetController {

    @Get("/greet")
    fun greet(@QueryValue("name") name: String) : String = "Hello $name!"
}