package liar.memberservice.common.config;

import liar.memberservice.common.principal.authority.CustomAuthorityMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class AuthenticationUtilConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public GrantedAuthoritiesMapper customAuthorityMapper() {
        return new CustomAuthorityMapper();
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.addAllowedOriginPattern("http://localhost:8000");
        corsConfiguration.addAllowedOriginPattern("http://3.39.187.39/:8000");
        corsConfiguration.addAllowedOriginPattern("http://43.201.26.3/:8000");
        corsConfiguration.addAllowedOriginPattern("http://3.39.187.39/:8080");
        corsConfiguration.addAllowedOriginPattern("http://3.39.187.39/:8761");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");

        source.registerCorsConfiguration("/member-service/**", corsConfiguration);
        return new CorsFilter(source);
    }
}
