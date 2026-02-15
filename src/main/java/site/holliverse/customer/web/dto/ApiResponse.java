package site.holliverse.customer.web.dto;

import java.time.LocalDateTime;

public record ApiResponse<T>(
        String status,
        T data,
        LocalDateTime timestamp
) {}
