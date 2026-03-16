package site.holliverse.customer.application.usecase.counsel;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.customer.domain.model.SupportStatus;
import site.holliverse.customer.persistence.entity.Category;
import site.holliverse.customer.persistence.entity.SupportCase;
import site.holliverse.customer.persistence.repository.CategoryRepository;
import site.holliverse.customer.persistence.repository.CounselRepository;
import site.holliverse.shared.error.CustomException;
import site.holliverse.shared.error.ErrorCode;
import site.holliverse.shared.persistence.entity.Member;
import site.holliverse.shared.persistence.repository.MemberRepository;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Profile("customer")
@RequiredArgsConstructor
public class CreateCounselUseCase {
    private final CounselRepository repository;
    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 상담 데이터 생성
     */
    @Transactional
    public Long execute(Long memberId, String title, String content) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.MEMBER_NOT_FOUND,
                        "memberId",
                        "회원을 찾을 수 없습니다: " + memberId
                ));

        List<Category> categories = categoryRepository.findAll();
        if (categories.isEmpty()) {
            throw new CustomException(ErrorCode.NOT_FOUND, "category", "카테고리 마스터 데이터가 없습니다.");
        }

        Category randomCategory = categories.get(ThreadLocalRandom.current().nextInt(categories.size()));

        SupportCase supportCase = SupportCase.builder()
                .member(member)
                .category(randomCategory)
                .status(SupportStatus.OPEN)
                .title(title)
                .questionText(content)
                .build();

        return repository.save(supportCase).getCaseId();
    }
}
