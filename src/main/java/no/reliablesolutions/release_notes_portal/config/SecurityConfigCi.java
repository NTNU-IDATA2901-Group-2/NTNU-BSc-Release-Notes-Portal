package no.reliablesolutions.release_notes_portal.config;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@Profile("ci") // Apply this security configuration only for 'ci' profile
public class SecurityConfigCi {
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfigCi.class);

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        logger.info("Applying NoSecurityConfig for CI profile");
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
            
            AbstractAuthenticationToken auth = new UsernamePasswordAuthenticationToken("ci-user", null, List.of(
              new SimpleGrantedAuthority("ROLE_ADMIN")
            ));

            SecurityContextHolder.getContext().setAuthentication(auth);

            filterChain.doFilter(request, response);
        }
    }
}