package site.holliverse.customer.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import site.holliverse.shared.persistence.BaseEntity;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "address",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_address_unique",
                        columnNames = {"province", "city", "street_address"}
                )
        }
)
public class Address extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long id;

    @Column(name = "province", nullable = false, length = 50)
    private String province; // 시/도 (예: 경기도)

    @Column(name = "city", nullable = false, length = 50)
    private String city; // 시/군/구 (예: 성남시)

    @Column(name = "street_address", nullable = false, length = 100)
    private String streetAddress; // 도로명 주소 (예: 판교역로 123)

    @Column(name = "postal_code", nullable = false, length = 10)
    private String postalCode; // 우편번호
}
