package liar.memberservice.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import liar.memberservice.member.controller.filter.LoginSessionFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterRegisterConfig {

    @Qualifier("defaultObjectMapper")
    private final ObjectMapper objectMapper;

    public FilterRegisterConfig(@Qualifier("defaultObjectMapper") ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public FilterRegistrationBean<LoginSessionFilter> loginSessionFilter(@Qualifier("defaultObjectMapper") ObjectMapper objectMapper) {
        FilterRegistrationBean<LoginSessionFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new LoginSessionFilter(objectMapper));
        registrationBean.addUrlPatterns("/member-service/login/**");

        return registrationBean;
    }

}
