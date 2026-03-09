package site.holliverse.auth.application.port;

import site.holliverse.shared.persistence.entity.Member;

public interface InitialPlanAssignmentService {
    void assignForNewMember(Member member);
}
