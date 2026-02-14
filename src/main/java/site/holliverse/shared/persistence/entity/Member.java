package site.holliverse.shared.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import site.holliverse.shared.persistence.entity.enums.MemberMembershipType;
import site.holliverse.shared.persistence.entity.enums.MemberRoleType;
import site.holliverse.shared.persistence.entity.enums.MemberSignupType;
import site.holliverse.shared.persistence.entity.enums.MemberStatusType;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    /**
     * 주소 테이블은 (province, city, street_address)로 유니크 처리되어 있어서
     * 여러 member가 같은 address를 공유할 수 있음.
     * 그래서 OneToOne + unique=true 는 스키마와 충돌 가능성이 큼 → ManyToOne이 맞음.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "address_id")
    private Address address;

    // 소셜 로그인용 고유 식별자 (DDL에 추가됨)
    @Column(name = "provider_id", length = 100)
    private String providerId;

    @Column(name = "email", nullable = false, length = 100, unique = true)
    private String email;

    /**
     * DDL 변경: 소셜 로그인은 비밀번호 NULL 허용
     * (FORM이면 password NOT NULL, 소셜이면 provider_id NOT NULL은 DB 체크 제약이 보장)
     */
    @Column(name = "password", length = 100)
    private String password;

    @Column(name = "name", nullable = false, length = 50)
    private String name;


    @Column(name = "phone", length = 100, unique = true)
    private String phone;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "gender", length = 1)
    private String gender;

    // 날짜 정보
    @Column(name = "join_date", nullable = false)
    private LocalDate joinDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "status_updated_at", nullable = false)
    private Instant statusUpdatedAt;

    // -------- PostgreSQL ENUM 컬럼 --------
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "member_status_type")
    private MemberStatusType status;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "type", nullable = false, columnDefinition = "member_signup_type")
    private MemberSignupType type;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "role", nullable = false, columnDefinition = "member_role_type")
    private MemberRoleType role;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "membership", columnDefinition = "member_membership_type")
    private MemberMembershipType membership;

    // -------- lifecycle --------
    @PrePersist
    void prePersist() {
        if (joinDate == null) joinDate = LocalDate.now();
        if (statusUpdatedAt == null) statusUpdatedAt = Instant.now();

        // 기본값을 엔티티에서도 안전하게 세팅 (DB default와 일치시킴)
        if (status == null) status = MemberStatusType.PROCESSING;
        if (type == null) type = MemberSignupType.FORM;
        if (role == null) role = MemberRoleType.CUSTOMER;
    }

    // 상태 변경은 메서드로만(감사 시간 업데이트 강제)
    public void changeStatus(MemberStatusType newStatus) {
        this.status = newStatus;
        this.statusUpdatedAt = Instant.now();
    }

    //주소 변경도 메서드로 관리하면 좋음
    public void changeAddress(Address address) {
        this.address = address;
    }
}
