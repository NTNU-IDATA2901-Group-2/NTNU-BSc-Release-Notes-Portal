package no.reliablesolutions.release_notes_portal.config;

import java.io.IOException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Security configuration for CI profile.
 */
@Configuration
@EnableMethodSecurity()
@Profile("ci") // Apply this security configuration only for 'ci' profile
public class SecurityConfigCi {
  private static final Logger logger = LoggerFactory.getLogger(SecurityConfigCi.class);
  private static final String ROLE_HEADER_NAME = "X-Test-Role";
  private static final String CUSTOMERS_HEADER_NAME = "X-Test-Customers";


  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) {
    logger.info("Applying SecurityConfigCi for CI profile");
    http.csrf(csrf -> csrf.disable())
        .addFilterBefore(new DummyRolesFilter(), UsernamePasswordAuthenticationFilter.class)
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
    return http.build();
  }

  private class DummyRolesFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain)
        throws IOException, ServletException {

      ArrayList<GrantedAuthority> authorities = new ArrayList<>();

      String roleHeader = request.getHeader(ROLE_HEADER_NAME);
      if (roleHeader != null) {
        authorities.add(new SimpleGrantedAuthority("ROLE_" + roleHeader.trim().toUpperCase()));
      }

      String customersHeader = request.getHeader(CUSTOMERS_HEADER_NAME);
      if (customersHeader != null) {
        for (String customer : customersHeader.split(",")) {
          authorities.add(new SimpleGrantedAuthority("ROLE_CUSTOMER_" + customer.trim().toUpperCase()));
        }
      }

      AbstractAuthenticationToken auth = new UsernamePasswordAuthenticationToken("ci-user", null, authorities);

      SecurityContextHolder.getContext().setAuthentication(auth);

      filterChain.doFilter(request, response);
    }
  }
}