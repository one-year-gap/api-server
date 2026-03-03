package site.holliverse.shared.config.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import site.holliverse.shared.logging.RequestMdcInterceptor;

@Configuration
public class WebMvcMdcConfig implements WebMvcConfigurer {

    private final RequestMdcInterceptor requestMdcInterceptor;

    public WebMvcMdcConfig(RequestMdcInterceptor requestMdcInterceptor) {
        this.requestMdcInterceptor = requestMdcInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestMdcInterceptor).addPathPatterns("/**");
    }
}
