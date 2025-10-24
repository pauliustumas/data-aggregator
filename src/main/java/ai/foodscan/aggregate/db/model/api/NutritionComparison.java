package ai.foodscan.aggregate.db.model.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NutritionComparison {
    // Fats
    private BigDecimal avgFats;
    private BigDecimal fatsDiffPercentage;

    // Saturated Fats
    private BigDecimal avgSaturatedFats;
    private BigDecimal saturatedFatsDiffPercentage;

    // Carbohydrates
    private BigDecimal avgCarbohydrates;
    private BigDecimal carbohydratesDiffPercentage;

    // Sugars
    private BigDecimal avgSugars;
    private BigDecimal sugarsDiffPercentage;

    // Proteins
    private BigDecimal avgProteins;
    private BigDecimal proteinsDiffPercentage;

    // Salt
    private BigDecimal avgSalt;
    private BigDecimal saltDiffPercentage;

    // Energy
    private Long avgEnergyValueKj;
    private BigDecimal energyDiffPercentage;
}
