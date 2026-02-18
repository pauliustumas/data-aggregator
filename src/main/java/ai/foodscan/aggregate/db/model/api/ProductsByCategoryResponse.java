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
public class ProductsByCategoryResponse {

    @JsonProperty("products")
    private List<MinimalProduct> products;

    @JsonProperty("total_count")
    private long totalCount;

    @JsonProperty("current_page")
    private int currentPage;

    @JsonProperty("products_per_page")
    private int productsPerPage;

}
