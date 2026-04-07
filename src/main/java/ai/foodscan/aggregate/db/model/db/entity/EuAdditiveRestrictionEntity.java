package ai.foodscan.aggregate.db.model.db.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@Table(name = "eu_additive_restrictions", schema = "aggregate")
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Data
public class EuAdditiveRestrictionEntity implements Serializable, Persistable<Long> {

    @Id
    @Column("id")
    private Long id;

    @Column("additive_id")
    private Long additiveId;

    @Column("food_category_id")
    private String foodCategoryId;

    @Column("food_category_number")
    private String foodCategoryNumber;

    @Column("food_category_name_en")
    private String foodCategoryNameEn;

    @Column("food_category_name_lt")
    private String foodCategoryNameLt;

    @Column("restriction_type")
    private String restrictionType;

    @Column("restriction_value")
    private String restrictionValue;

    @Column("restriction_unit")
    private String restrictionUnit;

    @Column("restriction_comment_en")
    private String restrictionCommentEn;

    @Column("restriction_comment_lt")
    private String restrictionCommentLt;

    @Column("note_number")
    private String noteNumber;

    @Column("note_text_en")
    private String noteTextEn;

    @Column("note_text_lt")
    private String noteTextLt;

    @Column("legislation_id")
    private Long legislationId;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return id == null;
    }
}
