package com.ethyllium.registryservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer

@EnableEurekaServer
@SpringBootApplication
class RegistryServiceApplication

fun main(args: Array<String>) {
    runApplication<RegistryServiceApplication>(*args)
}
