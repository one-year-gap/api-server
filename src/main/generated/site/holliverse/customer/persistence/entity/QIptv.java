package site.holliverse.customer.persistence.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QIptv is a Querydsl query type for Iptv
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QIptv extends EntityPathBase<Iptv> {

    private static final long serialVersionUID = 1969135305L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QIptv iptv = new QIptv("iptv");

    public final StringPath addonBenefit = createString("addonBenefit");

    public final NumberPath<Integer> channelCount = createNumber("channelCount", Integer.class);

    public final QProduct product;

    public final NumberPath<Long> productId = createNumber("productId", Long.class);

    public QIptv(String variable) {
        this(Iptv.class, forVariable(variable), INITS);
    }

    public QIptv(Path<? extends Iptv> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QIptv(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QIptv(PathMetadata metadata, PathInits inits) {
        this(Iptv.class, metadata, inits);
    }

    public QIptv(Class<? extends Iptv> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.product = inits.isInitialized("product") ? new QProduct(forProperty("product")) : null;
    }

}

