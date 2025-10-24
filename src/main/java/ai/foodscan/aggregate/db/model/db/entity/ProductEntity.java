package ai.foodscan.aggregate.db.model.db.entity;

import io.r2dbc.postgresql.codec.Json;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Table(name = "products", schema = "aggregate")
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Data
public class ProductEntity implements Serializable, Persistable<UUID> {

    @Id
    @Column("internal_product_id")
    private UUID internalProductId;                       // Internal unique identifier

    @Column("barcode")
    private String barcode;                               // Unique barcode identifier

    @Column("name_lt")
    private String nameLt;                                  // Product name LT

    @Column("name_en")
    private String nameEn;                                  // Product name EN

    @Column("raw_description")
    private String rawDescription;                          // Raw product description

    @Column("image_url")
    private String imageUrl;                              // Image URL

    @Column("price")
    private BigDecimal[] price;                           // Product price history

    @Column("original_price")
    private BigDecimal[] originalPrice;                   // Original price history before discount

    @Column("description_en")
    private String descriptionEn;                         // Product description in English

    @Column("description_lt")
    private String descriptionLt;                         // Product description in Lithuanian

    @Column("country_of_origin_lt")
    private String countryOfOriginLt;

    @Column("country_of_origin_en")
    private String countryOfOriginEn;

    @Column("ai_opinion_en")
    private String aiOpinionEn;

    @Column("ai_opinion_lt")
    private String aiOpinionLt;

    @Column("ai_datasource")
    private String aiDatasource;

    @Column("net_weight_g")
    private Integer netWeightG;

    @Column("ingredients_lt")
    private String ingredientsLt;

    @Column("ingredients_en")
    private String ingredientsEn;

    @Column("additives_lt")
    private Json additivesLt;

    @Column("additives_en")
    private Json additivesEn;

    @Column("nutrition_per_100g")
    private Json nutritionPer100g;

    @Column("storage_conditions")
    private Json storageConditions;

    @Column("packaging_lt")
    private String packagingLt;

    @Column("packaging_en")
    private String packagingEn;

    @Column("brand")
    private String brand;                                 // Brand name

    @Column("manufacturer_description_lt")
    private String manufacturerDescriptionLt;             // Manufacturer description in Lithuanian

    @Column("manufacturer_description_en")
    private String manufacturerDescriptionEn;             // Manufacturer description in English

    @Column("manufacturer_name")
    private String manufacturerName;

    @Column("main_category_en")
    private String mainCategoryEn;                        // Main category in English

    @Column("sub_category_en")
    private String subCategoryEn;                         // Sub-category in English

    @Column("sub_sub_category_en")
    private String subSubCategoryEn;                      // Sub-sub-category in English

    @Column("main_category_lt")
    private String mainCategoryLt;                        // Main category in Lithuanian

    @Column("sub_category_lt")
    private String subCategoryLt;                         // Sub-category in Lithuanian

    @Column("sub_sub_category_lt")
    private String subSubCategoryLt;                      // Sub-sub-category in Lithuanian

    @Column("original_url_en")
    private String originalUrlEn;                         // Original URL in English

    @Column("original_url_lt")
    private String originalUrlLt;                         // Original URL in Lithuanian

    @Column("allergens_en")
    private List<String> allergensEn;                      // Allergens in English

    @Column("allergens_lt")
    private List<String> allergensLt;                      // Allergens in Lithuanian

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;                      // Timestamp when the record was created

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;                      // Timestamp when the record was last updated

    @Column("recommended")
    @Builder.Default
    private boolean recommended = false;

    @Override
    public UUID getId() {
        return internalProductId;
    }

    @Override
    public boolean isNew() {
        return internalProductId == null;
    }
}
