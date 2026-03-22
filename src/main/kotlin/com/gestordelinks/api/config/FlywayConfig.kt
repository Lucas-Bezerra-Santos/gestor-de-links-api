package com.gestordelinks.api.config

import org.flywaydb.core.Flyway
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
class FlywayConfig {

    @Bean(initMethod = "migrate")
    fun flyway(
        dataSource: DataSource,
        @Value("\${spring.flyway.baseline-on-migrate:false}") baselineOnMigrate: Boolean
    ): Flyway {
        return Flyway.configure()
            .dataSource(dataSource)
            .baselineOnMigrate(baselineOnMigrate)
            .locations("classpath:db/migration")
            .load()
    }
}
