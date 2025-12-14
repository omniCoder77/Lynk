package com.ethyllium.gatewayservice.resolver

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component("ipKeyResolver")
class IpKeyResolver : KeyResolver {
    override fun resolve(exchange: ServerWebExchange): Mono<String> {
        val ip = exchange.request.remoteAddress?.address?.hostAddress ?: "unknown"
        return Mono.just(ip)
    }
}