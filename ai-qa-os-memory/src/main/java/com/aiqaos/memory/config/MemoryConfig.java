package com.aiqaos.memory.config;

import com.aiqaos.memory.store.MemoryStore;
import com.aiqaos.memory.store.CaffeineMemoryStore;
import com.aiqaos.memory.store.RedisMemoryStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration for Memory Stores.
 *
 * <p>Vector Store Provider Hierarchy (SCALE-3):
 * <ul>
 *   <li><b>Production:</b> {@code aiqaos.memory.vector.provider=qdrant} (QdrantStoreClient)</li>
 *   <li><b>Dev/Test Default:</b> {@code aiqaos.memory.vector.provider=in-memory} or unset (InMemoryVectorStoreClient)</li>
 *   <li><b>Experimental:</b> {@code aiqaos.memory.vector.experimental.enabled=true} (Chroma, Milvus, PgVector)</li>
 * </ul>
 */
@Configuration
public class MemoryConfig {

    @Bean
    @Primary
    @ConditionalOnProperty(name = "aiqaos.memory.shortterm.provider", havingValue = "caffeine", matchIfMissing = true)
    public MemoryStore caffeineMemoryStore() {
        return new CaffeineMemoryStore();
    }

    @Bean
    @ConditionalOnProperty(name = "aiqaos.memory.shortterm.provider", havingValue = "redis")
    public MemoryStore redisMemoryStore() {
        return new RedisMemoryStore();
    }
}