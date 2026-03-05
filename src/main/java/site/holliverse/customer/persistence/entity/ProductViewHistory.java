package site.holliverse.customer.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(ProductViewHistoryId.class)
@Table(name = "product_view_history")
public class ProductViewHistory {

    @Id
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Id
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(name = "product_type", nullable = false, length = 50)
    private String productType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", columnDefinition = "jsonb")
    private String tags;

    @Builder.Default
    @Column(name = "viewed_at", nullable = false)
    private OffsetDateTime viewedAt = OffsetDateTime.now();

    @Column(name = "last_event_id", nullable = false)
    private Long lastEventId;
}
