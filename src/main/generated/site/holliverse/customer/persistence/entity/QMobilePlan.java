package site.holliverse.customer.persistence.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMobilePlan is a Querydsl query type for MobilePlan
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMobilePlan extends EntityPathBase<MobilePlan> {

    private static final long serialVersionUID = 361789387L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMobilePlan mobilePlan = new QMobilePlan("mobilePlan");

    public final StringPath benefitBrands = createString("benefitBrands");

    public final StringPath benefitMedia = createString("benefitMedia");

    public final StringPath benefitPremium = createString("benefitPremium");

    public final StringPath benefitSignatureFamilyDiscount = createString("benefitSignatureFamilyDiscount");

    public final StringPath benefitSms = createString("benefitSms");

    public final StringPath benefitVoiceCall = createString("benefitVoiceCall");

    public final StringPath dataAmount = createString("dataAmount");

    public final QProduct product;

    public final NumberPath<Long> productId = createNumber("productId", Long.class);

    public final NumberPath<Integer> tetheringSharingData = createNumber("tetheringSharingData", Integer.class);

    public QMobilePlan(String variable) {
        this(MobilePlan.class, forVariable(variable), INITS);
    }

    public QMobilePlan(Path<? extends MobilePlan> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMobilePlan(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMobilePlan(PathMetadata metadata, PathInits inits) {
        this(MobilePlan.class, metadata, inits);
    }

    public QMobilePlan(Class<? extends MobilePlan> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.product = inits.isInitialized("product") ? new QProduct(forProperty("product")) : null;
    }

}

