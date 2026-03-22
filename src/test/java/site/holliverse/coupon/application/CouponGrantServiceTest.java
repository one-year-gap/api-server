package site.holliverse.coupon.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.holliverse.customer.persistence.entity.Coupon;
import site.holliverse.customer.persistence.entity.MemberCoupon;
import site.holliverse.coupon.repository.CouponRepository;
import site.holliverse.coupon.repository.MemberCouponGrantRepository;
import site.holliverse.shared.domain.model.CouponType;
import site.holliverse.shared.persistence.entity.Member;
import site.holliverse.shared.persistence.repository.MemberRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CouponGrantServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberCouponGrantRepository memberCouponGrantRepository;

    @InjectMocks
    private CouponGrantService couponGrantService;

    @Test
    @DisplayName("grant stores issued coupon with relative expiry")
    void grant_success_withValidDays() {
        Coupon coupon = Coupon.builder()
                .id(10L)
                .name("welcome")
                .couponType(CouponType.DISCOUNT)
                .benefitValue("3000")
                .description("welcome coupon")
                .validDays(30)
                .build();
        Member member = Member.builder().id(1L).build();

        given(couponRepository.findById(10L)).willReturn(Optional.of(coupon));
        given(memberRepository.getReferenceById(1L)).willReturn(member);

        couponGrantService.grant(1L, 10L);

        ArgumentCaptor<MemberCoupon> captor = ArgumentCaptor.forClass(MemberCoupon.class);
        verify(memberCouponGrantRepository).save(captor.capture());

        MemberCoupon saved = captor.getValue();
        assertThat(saved.getMember()).isSameAs(member);
        assertThat(saved.getCoupon()).isSameAs(coupon);
        assertThat(saved.getIsUsed()).isFalse();
        assertThat(saved.getIssuedAt()).isNotNull();
        assertThat(saved.getExpiredAt()).isEqualTo(saved.getIssuedAt().plusDays(30));
    }

    @Test
    @DisplayName("grant stores fixed expiry date as-is")
    void grant_success_withValidEndDate() {
        LocalDateTime fixedExpiredAt = LocalDateTime.of(2026, 12, 31, 23, 59);
        Coupon coupon = Coupon.builder()
                .id(20L)
                .name("birthday")
                .couponType(CouponType.DATA)
                .benefitValue("2GB")
                .description("birthday coupon")
                .validEndDate(fixedExpiredAt)
                .build();
        Member member = Member.builder().id(2L).build();

        given(couponRepository.findById(20L)).willReturn(Optional.of(coupon));
        given(memberRepository.getReferenceById(2L)).willReturn(member);

        couponGrantService.grant(2L, 20L);

        ArgumentCaptor<MemberCoupon> captor = ArgumentCaptor.forClass(MemberCoupon.class);
        verify(memberCouponGrantRepository).save(captor.capture());

        assertThat(captor.getValue().getExpiredAt()).isEqualTo(fixedExpiredAt);
    }
}