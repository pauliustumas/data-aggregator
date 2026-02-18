package ai.foodscan.aggregate.db.model.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class AdditiveRecord {

    @JsonProperty("name")
    private String name;

    @JsonProperty("code")
    private String code;

    @JsonProperty("url")
    private String url;

    // Lithuanian fields
    @JsonProperty("name_lt")
    private String nameLt;

    @JsonProperty("general_usage_in_food_industry_lt")
    private String generalUsageInFoodIndustryLt;

    @JsonProperty("usage_in_foods_lt")
    private String usageInFoodsLt;

    @JsonProperty("other_information_lt")
    private String otherInformationLt;

    @JsonProperty("damage_lt")
    private String damageLt;

    // English fields
    @JsonProperty("name_en")
    private String nameEn;

    @JsonProperty("general_usage_in_food_industry_en")
    private String generalUsageInFoodIndustryEn;

    @JsonProperty("usage_in_foods_en")
    private String usageInFoodsEn;

    @JsonProperty("other_information_en")
    private String otherInformationEn;

    @JsonProperty("damage_en")
    private String damageEn;

    @JsonProperty("is_dangerous")
    private Boolean isDangerous;
}
