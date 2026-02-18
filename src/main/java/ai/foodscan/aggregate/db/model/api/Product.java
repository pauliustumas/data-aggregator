package ai.foodscan.aggregate.db.model.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class Product {

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

    @JsonProperty("original_price")
    private BigDecimal originalPrice;               // Original price before discount

    @JsonProperty("description_en")
    private String descriptionEn;                   // Product description in English

    @JsonProperty("description_lt")
    private String descriptionLt;                   // Product description in Lithuanian

    @JsonProperty("country_of_origin_lt")
    private String countryOfOriginLt;               // Country of origin in Lithuanian

    @JsonProperty("country_of_origin_en")
    private String countryOfOriginEn;               // Country of origin in English

    @JsonProperty("net_weight_g")
    private Integer netWeightG;                     // Net weight in grams

    @JsonProperty("ingredients_lt")
    private String ingredientsLt;             // Array of ingredients in Lithuanian

    @JsonProperty("ingredients_en")
    private String ingredientsEn;             // Array of ingredients in English

    @JsonProperty("raw_description")
    private String rawDescription;             // Raw product description

    @JsonProperty("additives_lt")
    private List<Additive> additivesLt;             // List of additives in Lithuanian

    @JsonProperty("additives_en")
    private List<Additive> additivesEn;             // List of additives in English

    @JsonProperty("ai_datasource")
    private String aiDatasource;

    @JsonProperty("nutrition_per_100g")
    private NutritionPer100g nutritionPer100g;      // Nutrition details per 100g

    @JsonProperty("storage_conditions")
    private StorageConditions storageConditions;     // Storage conditions

    @JsonProperty("packaging_lt")
    private String packagingLt;                     // Packaging description in Lithuanian

    @JsonProperty("packaging_en")
    private String packagingEn;                     // Packaging description in English

    @JsonProperty("brand")
    private String brand;                           // Brand name

    @JsonProperty("manufacturer_description_lt")
    private String manufacturerDescriptionLt;      // Manufacturer description in Lithuanian

    @JsonProperty("manufacturer_description_en")
    private String manufacturerDescriptionEn;       // Manufacturer description in English

    @JsonProperty("manufacturer_name")
    private String manufacturerName;

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

    @JsonProperty("original_url_en")
    private String originalUrlEn;                   // Original URL in English

    @JsonProperty("original_url_lt")
    private String originalUrlLt;                   // Original URL in Lithuanian

    @JsonProperty("allergens_en")
    private List<String> allergensEn;                // Allergens in English

    @JsonProperty("allergens_lt")
    private List<String> allergensLt;                // Allergens in Lithuanian

    @JsonProperty("ai_opinion_en")
    private String aiOpinionEn;                     // Ai opinion in English

    @JsonProperty("ai_opinion_lt")
    private String aiOpinionLt;                     // Ai opinion in Lithuanian

    // Timestamp Fields
    @JsonProperty("created_at")
    private LocalDateTime createdAt;                // Timestamp when the product was created

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;                // Timestamp when the product was last updated

    @JsonProperty("nutrition_comparison")
    private NutritionComparison nutritionComparison; // Nutrition comparison with other products

    @JsonProperty("counter")
    private Long counter;                           // Counter for the product

    @JsonProperty("calorie_burn_estimates")
    private List<CalorieBurnEstimate> calorieBurnEstimates;

    @JsonProperty("similar_products")
    private List<MinimalProduct> similarProducts;

    @JsonProperty("recommended_products")
    private List<MinimalProduct> recommendedProducts;

    @JsonProperty("open_food_data")
    private Boolean openFoodData;

    @JsonProperty("recommended")
    private Boolean recommended;
}
