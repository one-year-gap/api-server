package site.holliverse.shared.config.runtime;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Arrays;

/**
 * customer 런타임에서 사용할 인프라 설정(@Configuration)들을 선택적으로 Import.
 */
public class CustomerImportsSelector implements ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return Arrays.stream(CustomerInfraImports.values())
                .map(i -> i.configClass().getName())
                .toArray(String[]::new);
    }
}

