package site.holliverse.customer.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import site.holliverse.shared.persistence.BaseEntity;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "usage_monthly")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UsageMonthly extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usage_id")
    private Long id;

    // 연관 관계 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    /** 사용 년월 */
    @Convert(converter = YearMonthConverter.class)
    @Column(name = "yyyymm", nullable = false, length = 6)
    private YearMonth yyyymm;

    /**
     * 상세 사용량 데이터 (JSONB)
     * Hibernate 6버전부터는 @JdbcTypeCode(SqlTypes.JSON)를 사용해
     * 자바의 Map이나 DTO를 바로 JSONB 컬럼에 매핑 가능
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    @Column(name = "usage_details", nullable = false)
    private Map<String, Object> usageDetails = new HashMap<>();

    @Converter
    static class YearMonthConverter implements AttributeConverter<YearMonth, String> {

        // "yyyyMM" 형식 (하이픈 없음)
        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

        @Override
        public String convertToDatabaseColumn(YearMonth attribute) {
            // 들어올 때: YearMonth(2026-02) -> 나갈 때: String("202602")
            if (attribute == null) return null;
            return attribute.format(FORMATTER);
        }

        @Override
        public YearMonth convertToEntityAttribute(String dbData) {
            // 들어올 때: String("202602") -> 나갈 때: YearMonth(2026-02)
            if (dbData == null) return null;
            return YearMonth.parse(dbData, FORMATTER);
        }
    }
}