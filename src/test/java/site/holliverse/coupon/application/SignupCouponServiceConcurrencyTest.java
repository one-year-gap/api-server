package site.holliverse.coupon.application;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import site.holliverse.coupon.repository.MemberCouponGrantRepository;
import site.holliverse.shared.domain.model.MemberMembership;
import site.holliverse.shared.domain.model.MemberRole;
import site.holliverse.shared.domain.model.MemberSignupType;
import site.holliverse.shared.domain.model.MemberStatus;
import site.holliverse.shared.persistence.entity.Address;
import site.holliverse.shared.persistence.entity.Member;
import site.holliverse.shared.persistence.repository.AddressRepository;
import site.holliverse.shared.persistence.repository.MemberRepository;
import site.holliverse.shared.util.EncryptionTool;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;

@ActiveProfiles({"customer", "test"})
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Disabled("Run manually against local PostgreSQL to observe the current race condition.")
class SignupCouponServiceConcurrencyTest {

    private static final Long WELCOME_COUPON_ID = 3L;

    @Autowired
    private SignupCouponService signupCouponService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EncryptionTool encryptionTool;

    @SpyBean
    private MemberCouponGrantRepository memberCouponGrantRepository;

    private final List<Long> createdMemberIds = new ArrayList<>();
    private final List<Long> createdAddressIds = new ArrayList<>();

    @AfterEach
    void tearDown() {
        for (Long memberId : createdMemberIds) {
            jdbcTemplate.update(
                    "delete from member_coupon where member_id = ? and coupon_id = ?",
                    memberId,
                    WELCOME_COUPON_ID
            );
            jdbcTemplate.update("delete from member where member_id = ?", memberId);
        }
        for (Long addressId : createdAddressIds) {
            jdbcTemplate.update("delete from address where address_id = ?", addressId);
        }
        createdMemberIds.clear();
        createdAddressIds.clear();
    }

    @Test
    @DisplayName("issueWelcomeCoupon can issue duplicate coupons when two requests race")
    void issueWelcomeCoupon_raceConditionExperiment() throws Exception {
        assertThat(
                jdbcTemplate.queryForObject(
                        "select count(*) from coupon where coupon_id = ?",
                        Integer.class,
                        WELCOME_COUPON_ID
                )
        ).isGreaterThan(0);

        Long memberId = createMember();

        CountDownLatch existsBarrier = new CountDownLatch(2);
        doAnswer(invocation -> {
            existsBarrier.countDown();
            assertThat(existsBarrier.await(5, TimeUnit.SECONDS)).isTrue();
            return false;
        }).when(memberCouponGrantRepository)
                .existsByMember_IdAndCoupon_Id(eq(memberId), eq(WELCOME_COUPON_ID));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<?> first = executor.submit(() -> signupCouponService.issueWelcomeCoupon(memberId));
            Future<?> second = executor.submit(() -> signupCouponService.issueWelcomeCoupon(memberId));

            first.get(10, TimeUnit.SECONDS);
            second.get(10, TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
        }

        Integer issuedCount = jdbcTemplate.queryForObject(
                "select count(*) from member_coupon where member_id = ? and coupon_id = ?",
                Integer.class,
                memberId,
                WELCOME_COUPON_ID
        );

        assertThat(issuedCount).isEqualTo(2);
    }

    private Long createMember() {
        String suffix = String.valueOf(System.nanoTime());
        String phoneSuffix = suffix.substring(Math.max(0, suffix.length() - 8));

        Address address = addressRepository.save(Address.builder()
                .province("Seoul")
                .city("Gangnam-gu")
                .streetAddress("Race Street " + suffix)
                .postalCode("06236")
                .build());
        createdAddressIds.add(address.getId());

        Member member = Member.builder()
                .address(address)
                .email("signup-race-" + suffix + "@example.com")
                .password("encoded-password")
                .name(encryptionTool.encrypt("race-member-" + suffix))
                .phone(encryptionTool.encrypt("010" + phoneSuffix))
                .birthDate(LocalDate.of(2000, 1, 1))
                .gender("M")
                .status(MemberStatus.ACTIVE)
                .type(MemberSignupType.FORM)
                .role(MemberRole.CUSTOMER)
                .membership(MemberMembership.GOLD)
                .build();

        Member saved = memberRepository.save(member);
        createdMemberIds.add(saved.getId());
        return saved.getId();
    }
}