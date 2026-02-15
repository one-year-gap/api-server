package site.holliverse.customer.persistence.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAddon is a Querydsl query type for Addon
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAddon extends EntityPathBase<Addon> {

    private static final long serialVersionUID = 905891168L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAddon addon = new QAddon("addon");

    public final EnumPath<AddonType> addonType = createEnum("addonType", AddonType.class);

    public final StringPath description = createString("description");

    public final QProduct product;

    public final NumberPath<Long> productId = createNumber("productId", Long.class);

    public QAddon(String variable) {
        this(Addon.class, forVariable(variable), INITS);
    }

    public QAddon(Path<? extends Addon> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAddon(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAddon(PathMetadata metadata, PathInits inits) {
        this(Addon.class, metadata, inits);
    }

    public QAddon(Class<? extends Addon> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.product = inits.isInitialized("product") ? new QProduct(forProperty("product")) : null;
    }

}

