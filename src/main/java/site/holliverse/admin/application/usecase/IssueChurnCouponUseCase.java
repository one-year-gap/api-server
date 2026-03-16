package site.holliverse.admin.application.usecase;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import site.holliverse.admin.web.dto.churn.IssueChurnCouponRequestDto;
import site.holliverse.admin.web.dto.churn.IssueChurnCouponResponseDto;
import site.holliverse.admin.web.dto.churn.SkippedCouponIssueMemberDto;
import site.holliverse.shared.error.CustomException;
import site.holliverse.shared.error.ErrorCode;

import java.util.ArrayList;
import java.util.List;

@Profile("Admin")
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
                    SkippedCouponIssueMemberDto.builder()
                            .memberId(result.memberId())
                            .reason(result.reason())
                            .build()
            );
        }

        return IssueChurnCouponResponseDto.builder()
                .requestedCount(requestDto.memberIds().size())
                .issuedCount(issuedMemberIds.size())
                .skippedCount(skippedMembers.size())
                .issuedMemberIds(issuedMemberIds)
                .skippedMembers(skippedMembers)
                .build();


    }

}
