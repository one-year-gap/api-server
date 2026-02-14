package site.holliverse.shared.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "address",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_address_unique",
                columnNames = {"province", "city", "street_address"}
        )
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long id;

    @Column(name = "province", nullable = false, length = 50)
    private String province;      // 예: 서울특별시, 경기도

    @Column(name = "city", nullable = false, length = 50)
    private String city;          // 예: 강남구, 성남시

    @Column(name = "street_address", nullable = false, length = 100)
    private String streetAddress; // 예: 테헤란로 123

    @Column(name = "postal_code", nullable = false, length = 10)
    private String postalCode;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Address(String province, String city, String streetAddress, String postalCode) {
        this.province = province;
        this.city = city;
        this.streetAddress = streetAddress;
        this.postalCode = postalCode;
    }
}
