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
public class CategoryDto {

    @JsonProperty("id") // Ensure this column exists in your table or adjust accordingly.
    private String id;

    @JsonProperty("main_category_slug_en")
    private String mainCategorySlugEn;
    @JsonProperty("sub_category_slug_en")
    private String subCategorySlugEn;
    @JsonProperty("sub_sub_category_slug_en")
    private String subSubCategorySlugEn;
    @JsonProperty("main_category_slug_lt")
    private String mainCategorySlugLt;
    @JsonProperty("sub_category_slug_lt")
    private String subCategorySlugLt;
    @JsonProperty("sub_sub_category_slug_lt")
    private String subSubCategorySlugLt;
    @JsonProperty("main_category_en")
    private String mainCategoryEn;
    @JsonProperty("sub_category_en")
    private String subCategoryEn;
    @JsonProperty("sub_sub_category_en")
    private String subSubCategoryEn;
    @JsonProperty("main_category_lt")
    private String mainCategoryLt;
    @JsonProperty("sub_category_lt")
    private String subCategoryLt;
    @JsonProperty("sub_sub_category_lt")
    private String subSubCategoryLt;
}
