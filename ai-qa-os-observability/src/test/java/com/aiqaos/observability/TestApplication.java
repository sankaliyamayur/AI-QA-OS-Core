package com.aiqaos.observability;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** Minimal bootstrap config so @DataJpaTest can find a @SpringBootConfiguration in this module. */
@SpringBootApplication
@EnableJpaAuditing
public class TestApplication {
}
