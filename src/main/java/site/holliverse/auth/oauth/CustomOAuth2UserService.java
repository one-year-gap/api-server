package site.holliverse.auth.oauth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.shared.domain.model.MemberRole;
import site.holliverse.shared.domain.model.MemberSignupType;
import site.holliverse.shared.domain.model.MemberStatus;
import site.holliverse.shared.persistence.entity.Member;
import site.holliverse.shared.persistence.repository.MemberRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
// Google OAuth2 사용자 정보를 우리 서비스 회원(Member)과 연결하는 서비스
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final MemberRepository memberRepository;
    private final DefaultOAuth2UserService delegate;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1) Google에서 사용자 기본 정보를 조회한다.
        OAuth2User oauth2User = delegate.loadUser(userRequest);
        Map<String, Object> attributes = new HashMap<>(oauth2User.getAttributes());

        String providerId = (String) attributes.get("sub");
        String email = (String) attributes.get("email");
        String name = (String) attributes.getOrDefault("name", email);

        if (providerId == null || providerId.isBlank()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_user_id"),
                    "Google OAuth2 user id is missing"
            );
        }

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_user_email"),
                    "Google OAuth2 user email is missing"
            );
        }

        // 2) 이메일로 회원을 조회 한 뒤, 만약 이메일이 존재하면 그대로 로그인 진행
        // 만약 이메일이 없다면 처음 회원가입 한 것이니 status = PROCESSING
        Member member = memberRepository.findByEmail(email)
                .orElseGet(() -> memberRepository.save(Member.builder()
                        .providerId(providerId)
                        .email(email)
                        .name(name)
                        .type(MemberSignupType.GOOGLE)
                        .status(MemberStatus.PROCESSING)
                        .role(MemberRole.CUSTOMER)
                        .build()));

        // 3) 이후 인증/인가에 사용할 내부 회원 정보를 attributes에 추가한다.
        attributes.put("memberId", member.getId());
        attributes.put("role", member.getRole().name());
        attributes.put("status", member.getStatus().name());

        // 4) SecurityContext에 저장될 OAuth2User principal을 만들어 반환한다.
        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole().name())),
                attributes,
                "sub"
        );
    }
}