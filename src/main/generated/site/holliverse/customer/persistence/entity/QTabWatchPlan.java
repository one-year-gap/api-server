package site.holliverse.customer.persistence.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTabWatchPlan is a Querydsl query type for TabWatchPlan
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTabWatchPlan extends EntityPathBase<TabWatchPlan> {

    private static final long serialVersionUID = 1524116835L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QTabWatchPlan tabWatchPlan = new QTabWatchPlan("tabWatchPlan");

    public final StringPath benefitSms = createString("benefitSms");

    public final StringPath benefitVoiceCall = createString("benefitVoiceCall");

    public final StringPath dataAmount = createString("dataAmount");

    public final QProduct product;

    public final NumberPath<Long> productId = createNumber("productId", Long.class);

    public QTabWatchPlan(String variable) {
        this(TabWatchPlan.class, forVariable(variable), INITS);
    }

    public QTabWatchPlan(Path<? extends TabWatchPlan> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QTabWatchPlan(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QTabWatchPlan(PathMetadata metadata, PathInits inits) {
        this(TabWatchPlan.class, metadata, inits);
    }

    public QTabWatchPlan(Class<? extends TabWatchPlan> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.product = inits.isInitialized("product") ? new QProduct(forProperty("product")) : null;
    }

}

