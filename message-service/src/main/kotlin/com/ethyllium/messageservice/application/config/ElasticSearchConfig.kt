package com.ethyllium.messageservice.application.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchConfiguration

@Configuration
class ElasticSearchConfig(
    @Value("\${spring.elasticsearch.username}") private val username: String,
    @Value("\${spring.elasticsearch.password}") private val password: String,
) : ReactiveElasticsearchConfiguration() {

    override fun clientConfiguration(): ClientConfiguration {
        return ClientConfiguration.builder().connectedToLocalhost().withBasicAuth(username, password).build()
    }
}