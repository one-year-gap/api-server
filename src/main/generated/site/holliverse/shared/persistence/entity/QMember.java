package site.holliverse.shared.persistence.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMember is a Querydsl query type for Member
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMember extends EntityPathBase<Member> {

    private static final long serialVersionUID = 1970819649L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMember member = new QMember("member1");

    public final QAddress address;

    public final DatePath<java.time.LocalDate> birthDate = createDate("birthDate", java.time.LocalDate.class);

    public final DateTimePath<java.time.Instant> createdAt = createDateTime("createdAt", java.time.Instant.class);

    public final StringPath email = createString("email");

    public final StringPath gender = createString("gender");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DatePath<java.time.LocalDate> joinDate = createDate("joinDate", java.time.LocalDate.class);

    public final EnumPath<site.holliverse.shared.persistence.entity.enums.MemberMembershipType> membership = createEnum("membership", site.holliverse.shared.persistence.entity.enums.MemberMembershipType.class);

    public final StringPath name = createString("name");

    public final StringPath password = createString("password");

    public final StringPath phone = createString("phone");

    public final StringPath providerId = createString("providerId");

    public final EnumPath<site.holliverse.shared.persistence.entity.enums.MemberRoleType> role = createEnum("role", site.holliverse.shared.persistence.entity.enums.MemberRoleType.class);

    public final EnumPath<site.holliverse.shared.persistence.entity.enums.MemberStatusType> status = createEnum("status", site.holliverse.shared.persistence.entity.enums.MemberStatusType.class);

    public final DateTimePath<java.time.Instant> statusUpdatedAt = createDateTime("statusUpdatedAt", java.time.Instant.class);

    public final EnumPath<site.holliverse.shared.persistence.entity.enums.MemberSignupType> type = createEnum("type", site.holliverse.shared.persistence.entity.enums.MemberSignupType.class);

    public final DateTimePath<java.time.Instant> updatedAt = createDateTime("updatedAt", java.time.Instant.class);

    public QMember(String variable) {
        this(Member.class, forVariable(variable), INITS);
    }

    public QMember(Path<? extends Member> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMember(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMember(PathMetadata metadata, PathInits inits) {
        this(Member.class, metadata, inits);
    }

    public QMember(Class<? extends Member> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.address = inits.isInitialized("address") ? new QAddress(forProperty("address")) : null;
    }

}

