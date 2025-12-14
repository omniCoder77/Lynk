package com.ethyllium.roomservice.application.config

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.postgresql.client.SSLMode
import io.r2dbc.spi.ConnectionFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.transaction.reactive.TransactionalOperator

@Configuration
class R2dbcConfig {
    @Bean
    fun connectionFactory(
        @Value("\${db.host}") host: String,
        @Value("\${db.port}") port: Int,
        @Value("\${db.name}") database: String?,
        @Value("\${db.username}") username: String,
        @Value("\${db.ssl.root-cert:}") sslRootCert: String,
        @Value("\${db.ssl.cert}") sslCert: String,
        @Value("\${db.ssl.key:}") sslKey: String
    ): PostgresqlConnectionFactory {
        val builder =
            PostgresqlConnectionConfiguration.builder().host(host).port(port).database(database).username(username)

        if (sslRootCert.isNotEmpty() && sslCert.isNotEmpty() && sslKey.isNotEmpty()) {
            builder.sslMode(SSLMode.REQUIRE).sslRootCert(sslRootCert).sslCert(sslCert).sslKey(sslKey)
        }
        return PostgresqlConnectionFactory(builder.build())
    }

    @Bean
    fun r2dbcEntityTemplate(connectionFactory: PostgresqlConnectionFactory) =
        R2dbcEntityTemplate(connectionFactory)

    @Bean
    fun transactionOperator(connFactory: ConnectionFactory) =
        TransactionalOperator.create(R2dbcTransactionManager(connFactory))
}