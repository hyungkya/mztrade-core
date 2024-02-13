package com.mztrade.hki.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.host}")
    private String host;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // Lettuce Redis Client 사용 → RedisTemplate 의 메서드로 Redis 서버에 명령을 수행할 수 있음
        return new LettuceConnectionFactory(host, port);
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate() {
        // RedisTemplate을 사용하여 Redis에 데이터를 저장하고 조회할 수 있습니다.(set, get, delete)
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();

        // redis-cli로 조회할 때 key 값이 보기 좋게 나오도록 설정(알아볼 수 없는 형태 출력 방지)
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());

        redisTemplate.setConnectionFactory(redisConnectionFactory());
        return redisTemplate;
    }
}
