package site.holliverse.customer.persistence.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QProduct is a Querydsl query type for Product
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProduct extends EntityPathBase<Product> {

    private static final long serialVersionUID = -478646257L;

    public static final QProduct product = new QProduct("product");

    public final StringPath discountType = createString("discountType");

    public final StringPath name = createString("name");

    public final NumberPath<Integer> price = createNumber("price", Integer.class);

    public final StringPath productCode = createString("productCode");

    public final NumberPath<Long> productId = createNumber("productId", Long.class);

    public final EnumPath<ProductType> productType = createEnum("productType", ProductType.class);

    public final NumberPath<Integer> salePrice = createNumber("salePrice", Integer.class);

    public QProduct(String variable) {
        super(Product.class, forVariable(variable));
    }

    public QProduct(Path<? extends Product> path) {
        super(path.getType(), path.getMetadata());
    }

    public QProduct(PathMetadata metadata) {
        super(Product.class, metadata);
    }

}

