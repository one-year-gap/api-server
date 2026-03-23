package site.holliverse.shared.error;

import org.springframework.stereotype.Component;
import site.holliverse.auth.error.AuthErrorCode;

@Component
public class ConstraintExceptionMapper {

    public ConstraintMapping map(String constraintName) {
        if (constraintName == null || constraintName.isBlank()) {
            return new ConstraintMapping(SharedErrorCode.CONFLICT, null);
        }

        return switch (constraintName) {
            case "uk_member_email" -> new ConstraintMapping(AuthErrorCode.DUPLICATED_EMAIL, "email");
            case "uk_member_phone" -> new ConstraintMapping(AuthErrorCode.DUPLICATED_PHONE, "phone");
            default -> new ConstraintMapping(SharedErrorCode.CONFLICT, null);
        };
    }
}
