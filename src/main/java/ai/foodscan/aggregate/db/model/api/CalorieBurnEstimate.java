package ai.foodscan.aggregate.db.model.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CalorieBurnEstimate {
    private String activityType;
    private Integer timeMinutes;
}
