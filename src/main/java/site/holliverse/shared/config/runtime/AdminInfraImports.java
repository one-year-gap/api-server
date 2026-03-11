package site.holliverse.shared.config.runtime;


import site.holliverse.infra.kafka.AnalysisResponseKafkaInfraConfig;

public enum AdminInfraImports {
    ANALYSIS_RESPONSE_KAFKA(AnalysisResponseKafkaInfraConfig.class),
    ;

    private final Class<?> configClass;

    AdminInfraImports(Class<?> configClass) {
        this.configClass = configClass;
    }

    public Class<?> configClass() {
        return configClass;
    }
}
