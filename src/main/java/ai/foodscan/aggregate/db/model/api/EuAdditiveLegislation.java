package ai.foodscan.aggregate.db.model.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class EuAdditiveLegislation {

    @JsonProperty("eu_legislation_id")
    private Long euLegislationId;

    @JsonProperty("title_en")
    private String titleEn;

    @JsonProperty("title_lt")
    private String titleLt;

    @JsonProperty("text")
    private String text;

    @JsonProperty("eurlex_link")
    private String eurlexLink;

    @JsonProperty("publication_date")
    private LocalDate publicationDate;

    @JsonProperty("date_entry_into_force")
    private LocalDate dateEntryIntoForce;

    @JsonProperty("oj_number")
    private String ojNumber;

    @JsonProperty("oj_page")
    private String ojPage;
}
