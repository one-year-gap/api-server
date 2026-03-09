package site.holliverse.auth.application.usecase;


import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import site.holliverse.auth.application.port.InitialPlanAssignmentService;
import site.holliverse.shared.persistence.entity.Member;

@Service
@Profile("admin")
public class AdminInitialPlanAssignmentUseCase implements InitialPlanAssignmentService {

    @Override
    public void assignForNewMember(Member member) {
        // admin 프로필에서는 초기 요금제 자동 할당을 수행하지 않는다.
    }
}
