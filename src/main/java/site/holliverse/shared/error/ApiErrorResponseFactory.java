package site.holliverse.shared.error;

import org.springframework.stereotype.Component;
import site.holliverse.shared.web.response.ApiErrorDetail;
import site.holliverse.shared.web.response.ApiErrorResponse;

@Component
public class ApiErrorResponseFactory {
    public ApiErrorResponse from(DomainException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        return ApiErrorResponse.error(
                errorCode.message(),
                new ApiErrorDetail(
                        errorCode.code(),
                        ex.getField(),
                        ex.getReason()
                )
        );
    }
}
