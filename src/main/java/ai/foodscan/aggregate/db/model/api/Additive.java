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
public class Additive {

    @JsonProperty("ai_analysis")
    private String aiAnalysis;

    @JsonProperty("shortcode")
    private String shortcode;

    @JsonProperty("is_preservative")
    private boolean isPreservative;

    @JsonProperty("name")
    private String name;

}
