package site.holliverse.customer.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import site.holliverse.shared.persistence.BaseEntity;
import site.holliverse.shared.domain.model.MemberMembership;
import site.holliverse.shared.domain.model.MemberRole;
import site.holliverse.shared.domain.model.MemberStatus;
import site.holliverse.shared.domain.model.MemberSignupType;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address address;

    @Column(name = "provider_id", length = 100)
    private String providerId; // 구글 등 소셜 고유 ID (일반 가입은 null)

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

    @Builder.Default
    @Column(name = "join_date", nullable = false)
    private LocalDate joinDate = LocalDate.now();

    @Builder.Default
    @Column(name = "status_updated_at", nullable = false)
    private LocalDateTime statusUpdatedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false, length = 20)
    private MemberStatus status = MemberStatus.PROCESSING;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "type", nullable = false, length = 20)
    private MemberSignupType type = MemberSignupType.FORM;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "role", nullable = false, length = 20)
    private MemberRole role = MemberRole.CUSTOMER;

    @Enumerated(EnumType.STRING)
    @Column(name = "membership", length = 20)
    private MemberMembership membership;
}
