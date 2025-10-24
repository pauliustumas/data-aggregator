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
public class StorageConditions {

    @JsonProperty("min_temperature")
    private Integer minTemperature;

    @JsonProperty("max_temperature")
    private Integer maxTemperature;

    @JsonProperty("post_opening_lt")
    private String postOpeningLt;

    @JsonProperty("post_opening_en")
    private String postOpeningEn;
}
