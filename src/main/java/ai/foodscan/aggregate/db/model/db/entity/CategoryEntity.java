package ai.foodscan.aggregate.db.model.db.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.io.Serializable;


@Table(name = "additives", schema = "aggregate")
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Data
public class CategoryEntity {

    // Assuming a composite key consisting of main, sub, and sub_sub categories.
    // Since Spring Data R2DBC doesn't natively support composite keys,
    // we'll use a surrogate key by concatenating the slugs.

    @Id
    @Column("id") // Ensure this column exists in your table or adjust accordingly.
    private String id;

    @Column("main_category_slug_en")
    private String mainCategorySlugEn;
    @Column("sub_category_slug_en")
    private String subCategorySlugEn;
    @Column("sub_sub_category_slug_en")
    private String subSubCategorySlugEn;
    @Column("main_category_slug_lt")
    private String mainCategorySlugLt;
    @Column("sub_category_slug_lt")
    private String subCategorySlugLt;
    @Column("sub_sub_category_slug_lt")
    private String subSubCategorySlugLt;
    @Column("main_category_en")
    private String mainCategoryEn;
    @Column("sub_category_en")
    private String subCategoryEn;
    @Column("sub_sub_category_en")
    private String subSubCategoryEn;
    @Column("main_category_lt")
    private String mainCategoryLt;
    @Column("sub_category_lt")
    private String subCategoryLt;
    @Column("sub_sub_category_lt")
    private String subSubCategoryLt;
}
