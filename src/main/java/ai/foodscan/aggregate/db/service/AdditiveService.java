package ai.foodscan.aggregate.db.service;

import ai.foodscan.aggregate.db.model.api.Additive;
import ai.foodscan.aggregate.db.model.api.AdditiveRecord;
import ai.foodscan.aggregate.db.model.api.Language;
import ai.foodscan.aggregate.db.model.db.entity.AdditiveEntity;
import ai.foodscan.aggregate.db.repository.AdditivesRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class AdditiveService {

    private final AdditivesRepository additivesRepository;

    public AdditiveService(AdditivesRepository additivesRepository) {
        this.additivesRepository = additivesRepository;
    }

    /**
     * Retrieves an additive by its code and maps its entity to an API model containing all fields.
     *
     * @param code the additive's code (primary key)
     * @return a Mono containing the API Additive; empty if not found.
     */
    public Mono<AdditiveRecord> getAdditiveByCode(String code) {
        return additivesRepository.findById(code)
            .map(this::mapToApi)
            .doOnNext(additive -> log.info("Fetched additive with code {}", code))
            .switchIfEmpty(Mono.empty());
    }

    public Mono<List<Additive>> processAdditives(List<Additive> additives, Language language) {
        log.info("Processing additives additives: {} for language: {}", additives, language);
        if (additives == null || additives.isEmpty()) {
            log.warn("Received null or empty additives list for language: {}", language);
            return Mono.just(Collections.emptyList());
        }
        return Flux.fromIterable(additives)
                .filter(Objects::nonNull)
                .filter(additive -> additive.getShortcode() != null)
                .flatMap(additive -> {
                    if (additive.isPreservative()) {
                        return getAdditiveByCode(additive.getShortcode())
                                .map(additiveDetails -> {
                                    String name = (language == Language.LT) ? additiveDetails.getNameLt() : additiveDetails.getNameEn();
                                    Additive enrichedAdditive = additive.toBuilder()
                                            .name(name)
                                            .build();
                                    log.debug("Enriched additive: {}", enrichedAdditive);
                                    return enrichedAdditive;
                                })
                                .switchIfEmpty(Mono.defer(() -> {
                                    log.warn("No details found for additive code: {}", additive.getShortcode());
                                    return Mono.just(additive);
                                }));
                    } else {
                        return Mono.just(additive);
                    }
                })
                .collectList()
                .doOnNext(list -> log.info("Processed additives list for language {}: {}", language, list));
    }

    /**
     * Maps an {@link AdditiveEntity} to an API {@link Additive}.
     * The entity's code is mapped to shortCode in the API model.
     *
     * @param entity the entity from the database
     * @return the mapped Additive API model containing both Lithuanian and English fields.
     */
    private AdditiveRecord mapToApi(AdditiveEntity entity) {
        return AdditiveRecord.builder()
            .code(entity.getCode())
            .url(entity.getUrl())
            .isDangerous(entity.getIsDangerous())
            
            // Lithuanian fields
            .nameLt(entity.getNameLt())
            .generalUsageInFoodIndustryLt(entity.getGeneralUsageInFoodIndustryLt())
            .usageInFoodsLt(entity.getUsageInFoodsLt())
            .otherInformationLt(entity.getOtherInformationLt())
            .damageLt(entity.getDamageLt())
            
            // English fields
            .nameEn(entity.getNameEn())
            .generalUsageInFoodIndustryEn(entity.getGeneralUsageInFoodIndustryEn())
            .usageInFoodsEn(entity.getUsageInFoodsEn())
            .otherInformationEn(entity.getOtherInformationEn())
            .damageEn(entity.getDamageEn())
            .build();
    }
}
