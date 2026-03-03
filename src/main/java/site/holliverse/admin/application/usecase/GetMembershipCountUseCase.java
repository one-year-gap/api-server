package site.holliverse.admin.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.admin.query.dao.AdminMembershipStatDao;
import site.holliverse.admin.query.dao.AdminMembershipStatRawData;
import site.holliverse.admin.web.dto.member.TotalMembershipResponseDto;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Profile("admin")
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMembershipCountUseCase {

    private final AdminMembershipStatDao adminMembershipStatDao;

    public TotalMembershipResponseDto execute() {
        // DB에서 등급별 원시 카운트를 1행으로 조회
        AdminMembershipStatRawData raw = adminMembershipStatDao.getMembershipStats();

        long total = nvl(raw.totalCount());
        long vvip = nvl(raw.vvipCount());
        long vip = nvl(raw.vipCount());
        long gold = nvl(raw.goldCount());

        // 프론트 표시용: 총 회원 수를 k 단위(소수 1자리)로 변환
        BigDecimal totalInK = BigDecimal.valueOf(total)
                .divide(BigDecimal.valueOf(1000), 1, RoundingMode.HALF_UP);

        // 분모 0 방어: 퍼센트 계산 불가 시 0으로 응답
        if (total == 0L) {
            return new TotalMembershipResponseDto(
                    totalInK,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
            );
        }

        // 100.0%를 1000틱(0.1% 단위)으로 환산해 정수 연산
        long totalTicks = 1000L;

        long vvipTickFloor = (vvip * totalTicks) / total;
        long vipTickFloor = (vip * totalTicks) / total;
        long goldTickFloor = (gold * totalTicks) / total;

        long used = vvipTickFloor + vipTickFloor + goldTickFloor;
        long remain = totalTicks - used;

        // floor 연산으로 버려진 잔여량
        long vvipRem = (vvip * totalTicks) % total;
        long vipRem = (vip * totalTicks) % total;
        long goldRem = (gold * totalTicks) % total;

        // 잔여량이 큰 순으로 1틱씩 분배해 합계가 정확히 100.0이 되도록 보정
        for (long i = 0; i < remain; i++) {
            if (vvipRem >= vipRem && vvipRem >= goldRem) {
                vvipTickFloor++;
                vvipRem = -1;
            } else if (vipRem >= vvipRem && vipRem >= goldRem) {
                vipTickFloor++;
                vipRem = -1;
            } else {
                goldTickFloor++;
                goldRem = -1;
            }
        }

        BigDecimal vvipRate = BigDecimal.valueOf(vvipTickFloor)
                .divide(BigDecimal.TEN, 1, RoundingMode.UNNECESSARY);
        BigDecimal vipRate = BigDecimal.valueOf(vipTickFloor)
                .divide(BigDecimal.TEN, 1, RoundingMode.UNNECESSARY);
        BigDecimal goldRate = BigDecimal.valueOf(goldTickFloor)
                .divide(BigDecimal.TEN, 1, RoundingMode.UNNECESSARY);

        return new TotalMembershipResponseDto(totalInK, vvipRate, vipRate, goldRate);
    }

    // jOOQ fetch 결과 null 방어
    private long nvl(Long value) {
        return value == null ? 0L : value;
    }
}
