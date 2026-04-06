package ai.foodscan.aggregate.db.model.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class ProductFilterRequest {

    @JsonProperty("exclude_allergens")
    private List<String> excludeAllergens;

    @JsonProperty("exclude_additives")
    private List<String> excludeAdditives;

    @JsonProperty("category")
    private String category;

    @JsonProperty("sub_category")
    private String subCategory;

    @JsonProperty("sub_sub_category")
    private String subSubCategory;

    @JsonProperty("price_min")
    private BigDecimal priceMin;

    @JsonProperty("price_max")
    private BigDecimal priceMax;

    @JsonProperty("name")
    private String name;

    @JsonProperty("names")
    private List<String> names;

    @JsonProperty("categories")
    private List<String> categories;

    @JsonProperty("sub_categories")
    private List<String> subCategories;

    @JsonProperty("sub_sub_categories")
    private List<String> subSubCategories;

    @JsonProperty("exclude_categories")
    private List<String> excludeCategories;

    @JsonProperty("exclude_sub_categories")
    private List<String> excludeSubCategories;

    @JsonProperty("sort_by")
    private String sortBy;

    @JsonProperty("lang")
    @Builder.Default
    private String lang = "en";

    @JsonProperty("page")
    @Builder.Default
    private int page = 0;

    @JsonProperty("size")
    @Builder.Default
    private int size = 20;
}
