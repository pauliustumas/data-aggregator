package ai.foodscan.aggregate.db.service;

import ai.foodscan.aggregate.db.model.api.ActivityType;
import ai.foodscan.aggregate.db.model.api.CalorieBurnEstimate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalorieBurnService {

    private static final double KJ_TO_KCAL_CONVERSION_FACTOR = 0.239;

    /**
     * Calculates the time in minutes required to burn off the given energy (in kJ) through various activities.
     *
     * @param energyValueKj the energy value in kilojoules.
     * @return a Flux emitting CalorieBurnEstimate instances for each activity.
     */
    public Flux<CalorieBurnEstimate> calculateBurnEstimates(long energyValueKj) {
        if (energyValueKj <= 0) {
            log.warn("Invalid energy valueKj: {}", energyValueKj);
            return Flux.empty();
        }

        double energyKcal = energyValueKj * KJ_TO_KCAL_CONVERSION_FACTOR;
        log.debug("Converted energy: {} kJ = {} kcal", energyValueKj, energyKcal);

        return Flux.fromArray(ActivityType.values())
                .map(activityType -> {
                    int timeMinutes = (int) Math.ceil(energyKcal / activityType.getKcalPerMinute());
                    return CalorieBurnEstimate.builder()
                            .activityType(activityType.getDisplayName())
                            .timeMinutes(timeMinutes)
                            .build();
                });
    }
}
