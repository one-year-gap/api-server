package site.holliverse.arch;

import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Optional;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(
        packages = "site.holliverse",
        importOptions = {
                ImportOption.DoNotIncludeTests.class,
                ImportOption.DoNotIncludeJars.class
        }
)
public class ArchitectureRulesTest {
    // ---------------------------------------------------------------------------
    // 0) 패키지 상수
    // ---------------------------------------------------------------------------
    private static final String CUSTOMER = "..customer..";
    private static final String ADMIN = "..admin..";
    private static final String SHARED = "..shared..";
    private static final String INFRA = "..infra..";

    private static final String WEB = "..web..";
    private static final String USECASE = "..application.usecase..";

    //customer/persistence/jpa, customer/jpa
    private static final String CUSTOMER_PERSISTENCE = "..customer.persistence..";
    private static final String ADMIN_QUERY = "..admin.query..";
    private static final String REPOSITORY_LIKE = "..jpa..";

    // admin/query/dao, customer/query/dao
    private static final String QUERY = "..query..";

    // runtime config: shared/config/runtime
    private static final String RUNTIME_CONFIG = "..shared.config.runtime..";

    // ---------------------------------------------------------------------------
    // 1) customer ↔ admin 의존 금지
    // ---------------------------------------------------------------------------
    @ArchTest
    static final ArchRule customer_must_not_depend_on_admin =
            noClasses()
                    .that().resideInAnyPackage(CUSTOMER)
                    .should().dependOnClassesThat().resideInAnyPackage(ADMIN)
                    .because("customer 영역이 admin 영역을 참조하면, 런타임/권한 경계가 무너진다.")
                    .allowEmptyShould(true);
    ;

    @ArchTest
    static final ArchRule admin_must_not_depend_on_customer =
            noClasses()
                    .that().resideInAnyPackage(ADMIN)
                    .should().dependOnClassesThat().resideInAnyPackage(CUSTOMER)
                    .because("admin 영역이 customer 영역을 참조하면, 경계/역할이 섞인다.")
                    .allowEmptyShould(true);
    ;

    // ---------------------------------------------------------------------------
    // 2) Controller -> Repository 직접 호출 금지
    // ---------------------------------------------------------------------------
    @ArchTest
    static final ArchRule controllers_must_not_access_persistence_directly =
            noClasses()
                    .that().resideInAnyPackage(WEB)
                    .and().areAnnotatedWith(RestController.class)
                    .should().dependOnClassesThat().resideInAnyPackage(CUSTOMER_PERSISTENCE, ADMIN_QUERY, REPOSITORY_LIKE)
                    .because("Controller는 UseCase(Service)만 호출해야 한다. DB 접근은 UseCase/Query 계층에서만 한다.")
                    .allowEmptyShould(true);
    ;

    // ---------------------------------------------------------------------------
    // 3) @Transactional은 UseCase에서만 허용
    // ---------------------------------------------------------------------------
    @ArchTest
    static final ArchRule transactional_only_in_usecase =
            classes()
                    .that().areAnnotatedWith(Transactional.class)
                    .should().resideInAnyPackage(USECASE)
                    .because("@Transactional은 UseCase 경계에서만 사용한다. web/persistence/infra에 붙이면 트랜잭션 범위가 오염된다.")
                    .allowEmptyShould(true);
    ;

    @ArchTest
    static final ArchRule no_transactional_in_web_persistence_infra =
            noClasses()
                    .that().resideInAnyPackage(WEB, CUSTOMER_PERSISTENCE, ADMIN_QUERY, INFRA, QUERY)
                    .should().beAnnotatedWith(Transactional.class)
                    .because("web/persistence/infra/query에 트랜잭션을 붙이면, 읽기/쓰기 경계가 무너지고 장기 트랜잭션 위험이 커진다.")
                    .allowEmptyShould(true);


    // ---------------------------------------------------------------------------
    // 4) customer에서 jOOQ 금지 (admin query에서만 쓰려는 정책)
    // ---------------------------------------------------------------------------
    @ArchTest
    static final ArchRule customer_must_not_use_jooq =
            noClasses()
                    .that().resideInAnyPackage(CUSTOMER)
                    .should().dependOnClassesThat().resideInAnyPackage("org.jooq..")
                    .because("customer는 JPA 중심. jOOQ는 admin 통계/집계 전용으로만 제한한다.")
                    .allowEmptyShould(true);
    ;


    // ---------------------------------------------------------------------------
    // 5) infra 의존은 runtime config에서만 허용
    //    (UseCase/Domain/Web이 infra 구현체를 직접 참조하지 못하게)
    // ---------------------------------------------------------------------------
    @ArchTest
    static final ArchRule only_runtime_config_can_depend_on_infra =
            noClasses()
                    .that().resideOutsideOfPackage(RUNTIME_CONFIG)
                    .and().resideInAnyPackage(CUSTOMER, ADMIN, SHARED)
                    .should().dependOnClassesThat().resideInAnyPackage(INFRA)
                    .because("infra 구현체는 runtime config에서만 import/enable 되어야 한다. 나머지는 Port를 통해 접근한다.")
                    .allowEmptyShould(true);
    ;

    // ---------------------------------------------------------------------------
    // 6) admin/customer 컨트롤러는 반드시 @Profile을 가져야 한다
    //    (같은 코드베이스 + 런타임 분리에서 실수 방지)
    // ---------------------------------------------------------------------------
    @ArchTest
    static final ArchRule admin_controllers_must_be_profiled_admin =
            classes()
                    .that().resideInAnyPackage("..admin.web..")
                    .and().areAnnotatedWith(RestController.class)
                    .should(beAnnotatedWithProfile("admin"))
                    .because("admin 컨트롤러는 admin 런타임에서만 로딩되어야 한다.")
                    .allowEmptyShould(true);


    @ArchTest
    static final ArchRule customer_controllers_must_be_profiled_customer =
            classes()
                    .that().resideInAnyPackage("..customer.web..")
                    .and().areAnnotatedWith(RestController.class)
                    .should(beAnnotatedWithProfile("customer"))
                    .because("customer 컨트롤러는 customer 런타임에서만 로딩되어야 한다.")
                    .allowEmptyShould(true);


    private static ArchCondition<JavaClass> beAnnotatedWithProfile(String profile) {
        return new ArchCondition<>("be annotated with @Profile(\"" + profile + "\")") {
            @Override
            public void check(JavaClass item, ConditionEvents events) {

                Optional<Profile> ann = item.tryGetAnnotationOfType(Profile.class);

                boolean ok = ann
                        .map(a -> Arrays.asList(a.value()).contains(profile))
                        .orElse(false);

                String msg = item.getName() + " must have @Profile(\"" + profile + "\")";
                events.add(new SimpleConditionEvent(item, ok, msg));
            }
        };
    }
}