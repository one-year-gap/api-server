package site.holliverse.shared.config.runtime;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Arrays;

public class AdminImportsSelector implements ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return Arrays.stream(AdminInfraImports.values())
                .map(i -> i.configClass().getName())
                .toArray(String[]::new);
    }
}
