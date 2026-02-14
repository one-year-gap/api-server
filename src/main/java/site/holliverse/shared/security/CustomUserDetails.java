package site.holliverse.shared.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import site.holliverse.shared.persistence.entity.Member;
import site.holliverse.shared.persistence.entity.enums.MemberStatusType;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final Long memberId;
    private final String email;
    private final String password;  // FORM 로그인에서만 사용 (소셜은 null 가능)
    private final String role;      // "CUSTOMER" / "ADMIN" ...
    private final MemberStatusType status;

    public CustomUserDetails(Long memberId, String email, String password, String role, MemberStatusType status) {
        this.memberId = memberId;
        this.email = email;
        this.password = password;
        this.role = role;
        this.status = status;
    }

    public static CustomUserDetails from(Member m) {
        return new CustomUserDetails(
                m.getId(),
                m.getEmail(),
                m.getPassword(),
                m.getRole().name(),
                m.getStatus()
        );
    }

    public Long getMemberId() { return memberId; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public MemberStatusType getStatus() { return status; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Spring Security 인가 규칙: "ROLE_" prefix
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        // FORM 로그인일 때만 의미 있음(소셜은 null 가능)
        return password;
    }

    @Override
    public String getUsername() {
        // username으로 email을 쓰는 게 일반적
        return email;
    }

    /**
     * 여기부터가 UserDetails의 핵심 장점 중 하나:
     * "계정 상태"를 표준 훅으로 제공해서, Provider/인증 과정에서 공통 처리 가능.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true; // 사용 안 하면 true 고정
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 사용 안 하면 true 고정
    }

    @Override
    public boolean isAccountNonLocked() {
        // BANNED는 잠금으로 처리
        return status != MemberStatusType.BANNED;
    }

    @Override
    public boolean isEnabled() {
        // ACTIVE만 활성
        return status == MemberStatusType.ACTIVE;
    }
}
