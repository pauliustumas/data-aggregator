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
public class EuAdditiveRestriction {

    @JsonProperty("food_category_id")
    private String foodCategoryId;

    @JsonProperty("food_category_number")
    private String foodCategoryNumber;

    @JsonProperty("food_category_name_en")
    private String foodCategoryNameEn;

    @JsonProperty("food_category_name_lt")
    private String foodCategoryNameLt;

    @JsonProperty("restriction_type")
    private String restrictionType;

    @JsonProperty("restriction_value")
    private String restrictionValue;

    @JsonProperty("restriction_unit")
    private String restrictionUnit;

    @JsonProperty("restriction_comment_en")
    private String restrictionCommentEn;

    @JsonProperty("restriction_comment_lt")
    private String restrictionCommentLt;

    @JsonProperty("note_number")
    private String noteNumber;

    @JsonProperty("note_text_en")
    private String noteTextEn;

    @JsonProperty("note_text_lt")
    private String noteTextLt;

    @JsonProperty("legislation_id")
    private Long legislationId;
}
