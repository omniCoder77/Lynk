package com.ethyllium.authservice.application.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing

@Configuration
@EnableR2dbcAuditing
class R2dbcAuditingConfiguration