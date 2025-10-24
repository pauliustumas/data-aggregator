package ai.foodscan.aggregate.db.model.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@ToString
public class NutritionPer100g {

    @JsonProperty("energy_value_kj")
    private Long energyValueKj;

    @JsonProperty("fats")
    private BigDecimal fats;

    @JsonProperty("fatty_acids")
    private BigDecimal fatty_acids;

    @JsonProperty("saturated_fats")
    private BigDecimal saturatedFats;

    @JsonProperty("carbohydrates")
    private BigDecimal carbohydrates;

    @JsonProperty("sugars")
    private BigDecimal sugars;

    @JsonProperty("proteins")
    private BigDecimal proteins;

    @JsonProperty("salt")
    private BigDecimal salt;

    @JsonProperty("fiber")
    private BigDecimal fiber;

    @JsonProperty("energy_value_kcal")
    private BigDecimal energyValueKcal;
}
