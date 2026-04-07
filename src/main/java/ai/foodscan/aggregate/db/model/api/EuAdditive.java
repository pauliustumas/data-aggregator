package ai.foodscan.aggregate.db.model.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class EuAdditive {

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

    @JsonProperty("identifying_name_en")
    private String identifyingNameEn;

    @JsonProperty("identifying_name_lt")
    private String identifyingNameLt;

    @JsonProperty("synonyms_en")
    private List<String> synonymsEn;

    @JsonProperty("synonyms_lt")
    private List<String> synonymsLt;

    @JsonProperty("ins_number")
    private String insNumber;

    @JsonProperty("is_group")
    private Boolean isGroup;

    @JsonProperty("member_of_group")
    private String memberOfGroup;

    @JsonProperty("policy_item_code")
    private String policyItemCode;

    @JsonProperty("source_url")
    private String sourceUrl;

    @JsonProperty("restrictions")
    private List<EuAdditiveRestriction> restrictions;

    @JsonProperty("legislations")
    private List<EuAdditiveLegislation> legislations;
}
