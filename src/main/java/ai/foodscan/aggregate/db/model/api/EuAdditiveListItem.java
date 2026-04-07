package ai.foodscan.aggregate.db.model.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Minimal projection for the EU additives list endpoint.
 * Used to render the index page with all 412 items in a single small response.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class EuAdditiveListItem {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("e_number")
    private String eNumber;

    @JsonProperty("e_number_display")
    private String eNumberDisplay;

    @JsonProperty("display_name_en")
    private String displayNameEn;

    @JsonProperty("display_name_lt")
    private String displayNameLt;

    @JsonProperty("is_group")
    private Boolean isGroup;
}
