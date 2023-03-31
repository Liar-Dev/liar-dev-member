package liar.memberservice.common.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager.RedisCacheManagerBuilder;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.Duration;

import static org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig;
import static org.springframework.data.redis.cache.RedisCacheManager.RedisCacheManagerBuilder.fromConnectionFactory;
import static org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer;

@Configuration
@EnableTransactionManagement
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(host, port);
    }


    @SuppressWarnings("deprecation")
    @Bean
    public CacheManager cacheManager() {
        RedisCacheManagerBuilder builder = fromConnectionFactory(redisConnectionFactory());
        RedisCacheConfiguration configuration = defaultCacheConfig()
                .serializeValuesWith(fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .entryTtl(Duration.ofMinutes(30));
        builder.cacheDefaults(configuration);
        return builder.build();
    }

    @Profile({"default", "local"})
    @Bean
    public RedissonClient redissonLocalClient() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + host + ":" + port);
        config.useSingleServer().setDnsMonitoringInterval(30000000L);
        return Redisson.create(config);
    }

    @Profile("dev")
    @Bean
    public RedissonClient redissonDevClient() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + host + ":" + port);
        config.useSingleServer().setDnsMonitoringInterval(30000000L);
        return Redisson.create(config);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setEnableTransactionSupport(true);

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }

}
