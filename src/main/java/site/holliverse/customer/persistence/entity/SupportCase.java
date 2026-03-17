package site.holliverse.customer.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import site.holliverse.customer.domain.model.SupportStatus;
import site.holliverse.shared.persistence.BaseEntity;
import site.holliverse.shared.persistence.entity.Member;

import java.time.LocalDateTime;

@Entity
@Table(name = "support_case")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportCase extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "case_id")
    private Long caseId;

    // 문의 작성 고객
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 상담사
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counselor_id")
    private Member counselor;

    // 카테고리
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_code", nullable = false)
    private Category category;

    // 처리 상태
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Builder.Default
    @Column(name = "status", nullable = false)
    private SupportStatus status = SupportStatus.OPEN;

    // 문의 제목
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    // 고객 질문
    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    // 상담사 답변
    @Column(name = "answer_text", columnDefinition = "TEXT")
    private String answerText;

    // 만족도 점수
    @Column(name = "satisfaction_score")
    private Integer satisfactionScore;

    // 고객 수정 시간
    @Column(name = "customer_modified_at")
    private LocalDateTime customerModifiedAt;

}
