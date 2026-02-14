package site.holliverse.customer.application.usecase.dto;

public record TabWatchPlanDetailDto(
        Long productId,
        String dataAmount,
        String benefitVoiceCall,
        String benefitSms
) {}
