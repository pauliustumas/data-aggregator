package ai.foodscan.aggregate.db.calculators;

import ai.foodscan.aggregate.db.model.api.NutritionComparison;
import ai.foodscan.aggregate.db.model.api.NutritionPer100g;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class NutritionCalculator {

    /**
     * Calculates a NutritionComparison between the product's nutrition and the average nutrition.
     * For each nutrient, if the product’s nutrient value is missing (null),
     * it is treated as equal to the average (i.e. difference 0).
     *
     * @param productNutrition the nutritional values of the product (object is not null but its fields might be null)
     * @param avgNutrition     the average nutritional values (should not be null)
     * @return a NutritionComparison populated with averages and calculated percentage differences.
     */
    public NutritionComparison calculateNutritionComparison(NutritionPer100g productNutrition, NutritionPer100g avgNutrition) {
        NutritionComparison comparison = new NutritionComparison();

        if (productNutrition == null) {
            productNutrition = new NutritionPer100g();
        }

        // Set average values from avgNutrition into the comparison.
        comparison.setAvgFats(avgNutrition.getFats());
        comparison.setAvgSaturatedFats(avgNutrition.getSaturatedFats());
        comparison.setAvgCarbohydrates(avgNutrition.getCarbohydrates());
        comparison.setAvgSugars(avgNutrition.getSugars());
        comparison.setAvgProteins(avgNutrition.getProteins());
        comparison.setAvgSalt(avgNutrition.getSalt());
        comparison.setAvgEnergyValueKj(avgNutrition.getEnergyValueKj());

        // Calculate difference percentages for each nutrient.
        comparison.setFatsDiffPercentage(calculateDiffPercentage(productNutrition.getFats(), avgNutrition.getFats()));
        comparison.setSaturatedFatsDiffPercentage(calculateDiffPercentage(productNutrition.getSaturatedFats(), avgNutrition.getSaturatedFats()));
        comparison.setCarbohydratesDiffPercentage(calculateDiffPercentage(productNutrition.getCarbohydrates(), avgNutrition.getCarbohydrates()));
        comparison.setSugarsDiffPercentage(calculateDiffPercentage(productNutrition.getSugars(), avgNutrition.getSugars()));
        comparison.setProteinsDiffPercentage(calculateDiffPercentage(productNutrition.getProteins(), avgNutrition.getProteins()));
        comparison.setSaltDiffPercentage(calculateDiffPercentage(productNutrition.getSalt(), avgNutrition.getSalt()));

        // Energy: if avg is null or zero, we cannot calculate a meaningful percentage – return 0.
        if (avgNutrition.getEnergyValueKj() == null || avgNutrition.getEnergyValueKj() == 0) {
            comparison.setEnergyDiffPercentage(BigDecimal.ZERO);
        } else {
            long avgEnergy = avgNutrition.getEnergyValueKj();
            // If product energy is null or 0, fallback to the average energy.
            long productEnergy = (productNutrition.getEnergyValueKj() != null && productNutrition.getEnergyValueKj() != 0)
                    ? productNutrition.getEnergyValueKj()
                    : avgEnergy;
            BigDecimal energyDiff = BigDecimal.valueOf(productEnergy - avgEnergy)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(avgEnergy), 2, RoundingMode.HALF_UP);
            comparison.setEnergyDiffPercentage(energyDiff);
        }

        return comparison;
    }

    /**
     * Calculates the percentage difference between productValue and avgValue.
     * If the productValue is null, it is treated as equal to avgValue (i.e. difference 0%).
     *
     * Formula: ((effectiveProductValue - avgValue) / avgValue) * 100
     *
     * @param productValue the product value (may be null)
     * @param avgValue     the average value (assumed non-null and nonzero for calculation)
     * @return the percentage difference as a BigDecimal.
     */
    private BigDecimal calculateDiffPercentage(BigDecimal productValue, BigDecimal avgValue) {
        if (avgValue == null || avgValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        // If productValue is null, treat it as equal to avgValue.
        BigDecimal effectiveProductValue = (productValue == null) ? avgValue : productValue;
        return effectiveProductValue.subtract(avgValue)
                .multiply(BigDecimal.valueOf(100))
                .divide(avgValue, 2, RoundingMode.HALF_UP);
    }
}
