package site.holliverse.auth.oauth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import site.holliverse.shared.domain.model.MemberRole;
import site.holliverse.shared.domain.model.MemberStatus;
import site.holliverse.shared.persistence.entity.Member;
import site.holliverse.shared.persistence.repository.MemberRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private DefaultOAuth2UserService delegate;
    @Mock
    private OAuth2UserRequest userRequest;

    private CustomOAuth2UserService customOAuth2UserService;

    @BeforeEach
    void setUp() {
        customOAuth2UserService = new CustomOAuth2UserService(memberRepository, delegate);
    }

    @Test
    @DisplayName("기존 회원이면 memberId/role/status를 attributes에 넣어 반환한다")
    void loadUserWithExistingMember() {
        Map<String, Object> googleAttributes = new HashMap<>();
        googleAttributes.put("sub", "google-123");
        googleAttributes.put("email", "user@test.com");
        googleAttributes.put("name", "테스트유저");

        OAuth2User googleUser = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                googleAttributes,
                "sub"
        );

        Member member = Member.builder()
                .id(1L)
                .email("user@test.com")
                .name("테스트유저")
                .role(MemberRole.CUSTOMER)
                .status(MemberStatus.ACTIVE)
                .build();

        when(delegate.loadUser(any())).thenReturn(googleUser);
        when(memberRepository.findByEmail("user@test.com")).thenReturn(Optional.of(member));

        OAuth2User result = customOAuth2UserService.loadUser(userRequest);

        Long memberId = result.getAttribute("memberId");
        String role = result.getAttribute("role");
        String status = result.getAttribute("status");

        assertThat(memberId).isEqualTo(1L);
        assertThat(role).isEqualTo("CUSTOMER");
        assertThat(status).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("이메일이 없으면 OAuth2AuthenticationException을 던진다")
    void throwsWhenEmailMissing() {
        Map<String, Object> googleAttributes = new HashMap<>();
        googleAttributes.put("sub", "google-123");

        OAuth2User googleUser = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                googleAttributes,
                "sub"
        );

        when(delegate.loadUser(any())).thenReturn(googleUser);

        assertThatThrownBy(() -> customOAuth2UserService.loadUser(userRequest))
                .isInstanceOf(OAuth2AuthenticationException.class)
                .hasMessageContaining("email is missing");
    }
}
