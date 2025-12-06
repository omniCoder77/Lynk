package com.ethyllium.userservice.application.config

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.postgresql.client.SSLMode
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate

@Configuration
class R2dbcConfig {
    @Bean
    fun connectionFactory(
        @Value("\${db.host}") host: String,
        @Value("\${db.port}") port: Int,
        @Value("\${db.name}") database: String?,
        @Value("\${db.username}") username: String,
        @Value("\${db.ssl.root-cert:}") sslRootCert: String,
        @Value("\${db.ssl.cert:}") sslCert: String,
        @Value("\${db.ssl.key:}") sslKey: String
    ): R2dbcEntityTemplate {
        val builder =
            PostgresqlConnectionConfiguration.builder().host(host).port(port).database(database).username(username)
                .sslMode(SSLMode.VERIFY_FULL).sslRootCert(sslRootCert).sslCert(sslCert).sslKey((sslKey))
        return R2dbcEntityTemplate(PostgresqlConnectionFactory(builder.build()))
    }
}