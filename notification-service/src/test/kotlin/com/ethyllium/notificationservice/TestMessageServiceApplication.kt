package com.ethyllium.notificationservice

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
    fromApplication<NotificationServiceApplication>().with(TestcontainersConfiguration::class).run(*args)
}
