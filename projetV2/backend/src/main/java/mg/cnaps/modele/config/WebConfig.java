package mg.cnaps.modele.config;

import mg.cnaps.modele.security.AutorisationInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AutorisationInterceptor autorisation;
    private final String[] origines;

    public WebConfig(AutorisationInterceptor autorisation, @Value("${app.cors.allowed-origins}") String origines) {
        this.autorisation = autorisation;
        this.origines = origines.split("\\s*,\\s*");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(origines)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization", "Content-Disposition")
                .allowCredentials(true);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(autorisation)
                .addPathPatterns("/api/**")

                .excludePathPatterns("/api/sante");
    }
}
