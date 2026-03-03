package site.holliverse.shared.alert.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import site.holliverse.shared.alert.annotation.AlertOwner;
import site.holliverse.shared.alert.config.AlertProperties;

@Component
public class AlertOwnerResolver {

    private final AlertProperties alertProperties;

    public AlertOwnerResolver(AlertProperties alertProperties) {
        this.alertProperties = alertProperties;
    }

    public String resolveOwner(HttpServletRequest request) {
        Object handler = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);
        if (handler instanceof HandlerMethod handlerMethod) {
            AlertOwner methodAnnotation = AnnotatedElementUtils.findMergedAnnotation(
                    handlerMethod.getMethod(),
                    AlertOwner.class
            );
            if (methodAnnotation != null && StringUtils.hasText(methodAnnotation.value())) {
                return methodAnnotation.value();
            }

            AlertOwner classAnnotation = AnnotatedElementUtils.findMergedAnnotation(
                    handlerMethod.getBeanType(),
                    AlertOwner.class
            );
            if (classAnnotation != null && StringUtils.hasText(classAnnotation.value())) {
                return classAnnotation.value();
            }
        }
        return alertProperties.getDefaultOwner();
    }
}
