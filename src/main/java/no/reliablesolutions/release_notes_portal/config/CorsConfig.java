package no.reliablesolutions.release_notes_portal.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
  @Value("${CORS_ALLOWED_ORIGINS:http://localhost:5173}")
  private String corsAllowedOrigins;

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    String[] allowedOrigins = corsAllowedOrigins.split(",");

    registry.addMapping("/api/**")
        .allowedOrigins(allowedOrigins)
        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        .allowedHeaders("*")
        .allowCredentials(true);
  }
}