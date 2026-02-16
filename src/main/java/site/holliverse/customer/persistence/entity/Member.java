package site.holliverse.customer.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import site.holliverse.shared.persistence.BaseEntity;
import site.holliverse.shared.domain.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member")
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    // 연관 관계 (N:1)
    // Member가 주인(FK 보유). 주소 정보는 필요할 때 조회(LAZY)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address address;

    // 소셜 로그인 정보
    @Column(name = "provider_id", length = 100)
    private String providerId; // 구글의 고유 ID (일반 가입은 null)

    // 회원 기본 정보
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password", length = 100)
    private String password; // 암호화된 비밀번호 (소셜 가입은 null)

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "phone", unique = true, length = 100)
    private String phone;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "gender", length = 1)
    private String gender; // 'M', 'W'

    // 기본값 설정 필드 (@Builder.Default)
    @Builder.Default // 빌더 사용 시에도 이 값이 기본으로 들어감
    @Column(name = "join_date", nullable = false)
    private LocalDate joinDate = LocalDate.now();

    @Builder.Default
    @Column(name = "status_updated_at", nullable = false)
    private LocalDateTime statusUpdatedAt = LocalDateTime.now();

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Builder.Default
    @Column(name = "status", nullable = false, length = 20)
    private MemberStatus status = MemberStatus.PROCESSING;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Builder.Default
    @Column(name = "type", nullable = false, length = 20)
    private MemberSignupType type = MemberSignupType.FORM;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Builder.Default
    @Column(name = "role", nullable = false, length = 20)
    private MemberRole role = MemberRole.CUSTOMER;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "membership", length = 20)
    private MemberMembership membership; 
}