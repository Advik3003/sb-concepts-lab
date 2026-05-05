package sb.concepts.lab.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI applicationOpenAPI() {
        return new OpenAPI()
                .servers(List.of(new Server().url("/")))
                .info(new Info()
                        .title("SB Concepts Lab API")
                        .description("API documentation for the Spring Boot concepts lab.")
                        .version("v1"));
    }
}
