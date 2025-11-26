package com.lynk.messageservice

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
    fromApplication<MessageServiceApplication>().with(TestcontainersConfiguration::class).run(*args)
}
