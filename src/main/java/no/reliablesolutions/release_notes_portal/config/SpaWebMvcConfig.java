package no.reliablesolutions.release_notes_portal.config;

import java.io.IOException;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * Configuration for serving the single page application (SPA) frontend.
 * 
 * This configuration ensures that all non-API requests are forwarded to the index.html file, allowing the frontend router to handle the routing.
 */
@Configuration
public class SpaWebMvcConfig implements WebMvcConfigurer {
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/**")
        .addResourceLocations("classpath:/static/")
        .resourceChain(true)
        .addResolver(new PathResourceResolver() {
          @Override
          protected Resource getResource(String resourcePath, Resource location) throws IOException {
            if (resourcePath.startsWith("api/")) {
              return null;
            }
            Resource requested = location.createRelative(resourcePath);
            if (requested.exists() && requested.isReadable()) {
              return requested;
            }
            Resource index = location.createRelative("index.html");
            return index.exists() ? index : null;
          }
        });
  }
}
