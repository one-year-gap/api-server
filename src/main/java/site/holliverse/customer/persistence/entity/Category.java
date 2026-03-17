package site.holliverse.customer.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @Column(name = "category_code", length = 20)
    private String categoryCode;

    @Column(name = "category_name", nullable = false, length = 50)
    private String categoryName;

    // 대분류 FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_group_code", nullable = false)
    private CategoryGroup categoryGroup;
}
