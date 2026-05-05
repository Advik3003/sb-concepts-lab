package sb.concepts.lab.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    private final CorsProperties corsProperties;

    public CorsConfig(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                addApplicationCorsMapping(registry, "/app/**", corsProperties.getAllowedMethods().toArray(String[]::new));
                addApplicationCorsMapping(registry, "/v3/api-docs/**", "GET", "OPTIONS");
            }
        };
    }

    private void addApplicationCorsMapping(CorsRegistry registry, String pathPattern, String... methods) {
        registry.addMapping(pathPattern)
                .allowedOrigins(corsProperties.getAllowedOrigins().toArray(String[]::new))
                .allowedMethods(methods)
                .allowedHeaders(corsProperties.getAllowedHeaders().toArray(String[]::new))
                .exposedHeaders(corsProperties.getExposedHeaders().toArray(String[]::new))
                .allowCredentials(corsProperties.isAllowCredentials())
                .maxAge(corsProperties.getMaxAge());
    }
}
