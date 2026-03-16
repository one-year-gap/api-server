package site.holliverse.admin.application.usecase;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import site.holliverse.admin.query.dao.AdminChurnCouponDao;
import site.holliverse.admin.web.dto.churn.IssueChurnCouponRequestDto;
import site.holliverse.admin.web.dto.churn.IssueChurnCouponResponseDto;
import site.holliverse.admin.web.dto.churn.SkippedCouponIssueMemberDto;
import site.holliverse.shared.error.CustomException;
import site.holliverse.shared.error.ErrorCode;

import java.util.ArrayList;
import java.util.List;

/**
 ==========================
 * $NAME
 * 전체 발송 흐름 관리을 관리하는 usecase 입니다 .
 * 쿠폰 1번이 있는지 확인
 * 회원별 처리 호출
 * 최종 응답 집계
 * @author nonstop
 * @version 1.0.0
 * @since 2026-03-16
 * ========================== */

@Profile("admin")
@Service
@RequiredArgsConstructor
public class IssueChurnCouponUseCase {

    private final AdminChurnCouponDao adminChurnCouponDao;
    private final ChurnCouponIssueProcessor churnCouponIssueProcessor;

    public IssueChurnCouponResponseDto execute(IssueChurnCouponRequestDto requestDto) {
        boolean couponExists = adminChurnCouponDao.existsCouponById(requestDto.couponId());

        if (!couponExists) {
            throw new CustomException(
                    ErrorCode.NOT_FOUND,
                    "couponId",
                    "존재하지 않는 쿠폰입니다."
            );
        }

        List<Long> issuedMemberIds = new ArrayList<>();
        List<SkippedCouponIssueMemberDto> skippedMembers = new ArrayList<>();

        for (Long memberId : requestDto.memberIds()) {
            IssueOneChurnCouponResult result =
                    churnCouponIssueProcessor.issue(memberId, requestDto.couponId());


            if (result.issued()) {
                issuedMemberIds.add(result.memberId());
                continue;
            }

            skippedMembers.add(
                    new SkippedCouponIssueMemberDto(
                            result.memberId(),
                            result.reason()
                    )
            );
        }

        return new IssueChurnCouponResponseDto(
                requestDto.memberIds().size(),
                issuedMemberIds.size(),
                skippedMembers.size(),
                issuedMemberIds,
                skippedMembers
        );


    }

}
