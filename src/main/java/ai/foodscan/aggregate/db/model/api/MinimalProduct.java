package ai.foodscan.aggregate.db.model.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import ai.foodscan.aggregate.db.model.api.NutritionPer100g;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class MinimalProduct {

    @JsonProperty("internal_product_id")
    private UUID internalProductId;                 // Internal unique identifier

    @JsonProperty("barcode")
    private String barcode;                         // Unique barcode identifier

    @JsonProperty("name_lt")
    private String nameLt;                            // Product name

    @JsonProperty("name_en")
    private String nameEn;

    @JsonProperty("image_url")
    private String imageUrl;                        // Image URL

    @JsonProperty("price")
    private BigDecimal price;                       // Current product price

    @JsonProperty("main_category_en")
    private String mainCategoryEn;                  // Main category in English

    @JsonProperty("sub_category_en")
    private String subCategoryEn;                   // Sub-category in English

    @JsonProperty("sub_sub_category_en")
    private String subSubCategoryEn;                // Sub-sub-category in English

    @JsonProperty("main_category_lt")
    private String mainCategoryLt;                  // Main category in Lithuanian

    @JsonProperty("sub_category_lt")
    private String subCategoryLt;                   // Sub-category in Lithuanian

    @JsonProperty("sub_sub_category_lt")
    private String subSubCategoryLt;                // Sub-sub-category in Lithuanian

    @JsonProperty("nutrition_per_100g")
    private NutritionPer100g nutritionPer100g;

    @JsonProperty("net_weight_g")
    private Integer netWeightG;

    // Timestamp Fields
    @JsonProperty("created_at")
    private LocalDateTime createdAt;                // Timestamp when the product was created

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;                // Timestamp when the product was last updated
}
