package com.ethyllium.roomservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RoomServiceApplication

fun main(args: Array<String>) {
    runApplication<RoomServiceApplication>(*args)
}
