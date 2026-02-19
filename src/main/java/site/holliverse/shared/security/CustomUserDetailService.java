package site.holliverse.shared.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import site.holliverse.shared.persistence.entity.Member;
import site.holliverse.shared.persistence.repository.MemberRepository;

@Service
@RequiredArgsConstructor
// Spring Security 인증 과정에서 email로 사용자 정보를 조회해주는 서비스
public class CustomUserDetailService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    // AuthenticationManager가 호출하는 진입점: email로 Member를 찾아 CustomUserDetails로 변환
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member m = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Member not found for email: " + email));
        return CustomUserDetails.from(m);
    }
}
