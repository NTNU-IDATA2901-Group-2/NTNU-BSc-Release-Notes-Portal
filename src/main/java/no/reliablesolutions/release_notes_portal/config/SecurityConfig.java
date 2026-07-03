package no.reliablesolutions.release_notes_portal.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the application. This configuration is applied
 * only for 'dev' and 'prod' profiles.
 * Responsible for setting up JWT authentication and defining access rules for
 * endpoints.
 */
@Configuration
@EnableMethodSecurity()
@EnableConfigurationProperties(AuthClaimsProperties.class)
@Profile({ "dev", "prod" }) // Only load this configuration for 'dev' and 'prod' profiles
public class SecurityConfig {

  private final AuthClaimsProperties authClaimsProperties;

  public SecurityConfig(AuthClaimsProperties authClaimsProperties) {
    this.authClaimsProperties = authClaimsProperties;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) {
    http
        .csrf(csrf -> csrf.disable())
        .cors(cors -> {
        })
        .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/public/config").permitAll()
            .requestMatchers("/api/*").authenticated()
            .anyRequest().permitAll())
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

    return http.build();
  }

  private JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(new JwtRolesGrantedAuthoritiesConverter(authClaimsProperties));
    return converter;
  }
}