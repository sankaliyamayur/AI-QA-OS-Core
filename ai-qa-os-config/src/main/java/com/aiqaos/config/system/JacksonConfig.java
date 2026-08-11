package com.aiqaos.config.system;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * Jackson 3 (Spring Boot 4) note — two behavioural changes are folded into this bean:
 *
 * <ul>
 *   <li><b>The mapper is immutable.</b> {@code objectMapper.configure(...)} and {@code disable(...)}
 *       no longer exist; configuration happens on {@link JsonMapper#builder()} before {@code build()}.
 *       This used to construct a mapper and then mutate it.</li>
 *   <li><b>{@code WRITE_DATES_AS_TIMESTAMPS} is gone,</b> along with {@code JavaTimeModule}. Jackson 3
 *       has java.time support built into databind and writes ISO-8601 by default — which is exactly
 *       what disabling that feature achieved before, so the behaviour is unchanged by its removal.</li>
 * </ul>
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .build();
    }
}
