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

@Table(name = "additives", schema = "aggregate")
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Data
public class AdditiveEntity implements Serializable, Persistable<String> {

    @Id
    @Column("code")
    private String code;   // Primary key

    @Column("url")
    private String url;

    @Column("name_lt")
    private String nameLt;  // Lithuanian name

    @Column("is_dangerous")
    private Boolean isDangerous;

    @Column("general_usage_in_food_industry_lt")
    private String generalUsageInFoodIndustryLt;

    @Column("usage_in_foods_lt")
    private String usageInFoodsLt;

    @Column("other_information_lt")
    private String otherInformationLt;

    @Column("damage_lt")
    private String damageLt;

    @Column("name_en")
    private String nameEn;

    @Column("general_usage_in_food_industry_en")
    private String generalUsageInFoodIndustryEn;

    @Column("usage_in_foods_en")
    private String usageInFoodsEn;

    @Column("other_information_en")
    private String otherInformationEn;

    @Column("damage_en")
    private String damageEn;

    @Override
    public String getId() {
        return code;
    }

    @Override
    public boolean isNew() {
        return code == null || code.isEmpty();
    }
}
