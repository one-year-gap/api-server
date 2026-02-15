package site.holliverse.customer.persistence.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QInternet is a Querydsl query type for Internet
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QInternet extends EntityPathBase<Internet> {

    private static final long serialVersionUID = -378959839L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QInternet internet = new QInternet("internet");

    public final StringPath addonBenefit = createString("addonBenefit");

    public final QProduct product;

    public final NumberPath<Long> productId = createNumber("productId", Long.class);

    public final NumberPath<Integer> speedMbps = createNumber("speedMbps", Integer.class);

    public QInternet(String variable) {
        this(Internet.class, forVariable(variable), INITS);
    }

    public QInternet(Path<? extends Internet> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QInternet(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QInternet(PathMetadata metadata, PathInits inits) {
        this(Internet.class, metadata, inits);
    }

    public QInternet(Class<? extends Internet> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.product = inits.isInitialized("product") ? new QProduct(forProperty("product")) : null;
    }

}

