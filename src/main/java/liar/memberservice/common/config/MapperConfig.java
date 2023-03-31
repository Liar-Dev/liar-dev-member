package liar.memberservice.common.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Configuration
public class MapperConfig {
    @Bean("redisObjectMapper")
    public ObjectMapper redisObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return objectMapper;
    }


    @Bean("redisJackson2HttpMessageConverter")
    public MappingJackson2HttpMessageConverter redisJackson2HttpMessageConverter() {
        ObjectMapper mapper = redisObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(mapper);
        return converter;
    }

    @Primary
    @Bean("defaultObjectMapper")
    public ObjectMapper defaultObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    @Primary
    @Bean("defaultHttpMessageConverter")
    public MappingJackson2HttpMessageConverter defaultHttpMessageConverter() {
        ObjectMapper mapper = defaultObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(mapper);
        return converter;
    }

    //    @Bean("defaultHttpMessageConverter")
//    public MappingJackson2HttpMessageConverter defaultHttpMessageConverter() {
//        ObjectMapper mapper = defaultObjectMapper();
//        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(mapper);
//        return converter;
//    }


}
