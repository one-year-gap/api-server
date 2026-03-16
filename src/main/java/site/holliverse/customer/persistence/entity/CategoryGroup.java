package site.holliverse.customer.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "category_group")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryGroup {

    @Id
    @Column(name = "category_group_code", length = 20)
    private String categoryGroupCode;

    @Column(name = "category_name", nullable = false, length = 50, unique = true)
    private String categoryName;

    // 소분류 목록
    @OneToMany(mappedBy = "categoryGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Category> categories;
}