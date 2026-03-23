package site.holliverse.customer.application.usecase.counsel;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.customer.domain.model.SupportStatus;
import site.holliverse.customer.error.CustomerErrorCode;
import site.holliverse.customer.error.CustomerException;
import site.holliverse.customer.persistence.entity.Category;
import site.holliverse.customer.persistence.entity.SupportCase;
import site.holliverse.customer.persistence.repository.CategoryRepository;
import site.holliverse.customer.persistence.repository.CounselRepository;
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
                .orElseThrow(() -> new CustomerException(CustomerErrorCode.MEMBER_NOT_FOUND));

        List<Category> categories = categoryRepository.findAll();
        if (categories.isEmpty()) {
            throw new CustomerException(CustomerErrorCode.CATEGORY_NOT_FOUND);
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
