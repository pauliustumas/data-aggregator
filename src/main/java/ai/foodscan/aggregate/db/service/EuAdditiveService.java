package ai.foodscan.aggregate.db.service;

import ai.foodscan.aggregate.db.model.api.EuAdditive;
import ai.foodscan.aggregate.db.model.api.EuAdditiveLegislation;
import ai.foodscan.aggregate.db.model.api.EuAdditiveListItem;
import ai.foodscan.aggregate.db.model.api.EuAdditiveRestriction;
import ai.foodscan.aggregate.db.model.db.entity.EuAdditiveEntity;
import ai.foodscan.aggregate.db.model.db.entity.EuAdditiveLegislationEntity;
import ai.foodscan.aggregate.db.model.db.entity.EuAdditiveRestrictionEntity;
import ai.foodscan.aggregate.db.repository.EuAdditiveLegislationRepository;
import ai.foodscan.aggregate.db.repository.EuAdditiveRepository;
import ai.foodscan.aggregate.db.repository.EuAdditiveRestrictionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class EuAdditiveService {

    private final EuAdditiveRepository euAdditiveRepository;
    private final EuAdditiveRestrictionRepository restrictionRepository;
    private final EuAdditiveLegislationRepository legislationRepository;

    public EuAdditiveService(EuAdditiveRepository euAdditiveRepository,
                             EuAdditiveRestrictionRepository restrictionRepository,
                             EuAdditiveLegislationRepository legislationRepository) {
        this.euAdditiveRepository = euAdditiveRepository;
        this.restrictionRepository = restrictionRepository;
        this.legislationRepository = legislationRepository;
    }

    /**
     * Get the full ordered list of EU additives as minimal list items.
     * Used by the index page to render all 412 additives in a single response.
     */
    public Flux<EuAdditiveListItem> getAllAsList() {
        log.info("Fetching ordered list of all EU additives");
        return euAdditiveRepository.findAllOrdered()
                .map(this::mapToListItem);
    }

    /**
     * Get a single EU additive by E-number with all restrictions and legislations.
     * Lookup is case-insensitive (e.g. "E586", "e586", "E 586" all work).
     */
    public Mono<EuAdditive> getByENumber(String eNumber) {
        if (eNumber == null || eNumber.isBlank()) {
            return Mono.empty();
        }
        String normalized = eNumber.replaceAll("\\s+", "").toUpperCase();
        log.info("Fetching EU additive by e_number: {}", normalized);

        return euAdditiveRepository.findByENumberCaseInsensitive(normalized)
                .flatMap(this::loadFullAdditive);
    }

    /**
     * Get a single EU additive by internal ID with all restrictions and legislations.
     */
    public Mono<EuAdditive> getById(Long id) {
        log.info("Fetching EU additive by id: {}", id);
        return euAdditiveRepository.findById(id)
                .flatMap(this::loadFullAdditive);
    }

    private Mono<EuAdditive> loadFullAdditive(EuAdditiveEntity entity) {
        Mono<List<EuAdditiveRestriction>> restrictions = restrictionRepository.findByAdditiveId(entity.getId())
                .map(this::mapRestriction)
                .collectList()
                .defaultIfEmpty(Collections.emptyList());

        Mono<List<EuAdditiveLegislation>> legislations = legislationRepository.findByAdditiveId(entity.getId())
                .map(this::mapLegislation)
                .collectList()
                .defaultIfEmpty(Collections.emptyList());

        return Mono.zip(restrictions, legislations)
                .map(tuple -> mapToApi(entity, tuple.getT1(), tuple.getT2()));
    }

    private EuAdditiveListItem mapToListItem(EuAdditiveEntity e) {
        return EuAdditiveListItem.builder()
                .id(e.getId())
                .eNumber(e.getENumber())
                .eNumberDisplay(e.getENumberDisplay())
                .displayNameEn(e.getDisplayNameEn())
                .displayNameLt(e.getDisplayNameLt())
                .isGroup(e.getIsGroup())
                .build();
    }

    private EuAdditive mapToApi(EuAdditiveEntity e,
                                List<EuAdditiveRestriction> restrictions,
                                List<EuAdditiveLegislation> legislations) {
        return EuAdditive.builder()
                .id(e.getId())
                .eNumber(e.getENumber())
                .eNumberDisplay(e.getENumberDisplay())
                .displayNameEn(e.getDisplayNameEn())
                .displayNameLt(e.getDisplayNameLt())
                .identifyingNameEn(e.getIdentifyingNameEn())
                .identifyingNameLt(e.getIdentifyingNameLt())
                .synonymsEn(e.getSynonymsEn() != null ? Arrays.asList(e.getSynonymsEn()) : Collections.emptyList())
                .synonymsLt(e.getSynonymsLt() != null ? Arrays.asList(e.getSynonymsLt()) : Collections.emptyList())
                .insNumber(e.getInsNumber())
                .isGroup(e.getIsGroup())
                .memberOfGroup(e.getMemberOfGroup())
                .policyItemCode(e.getPolicyItemCode())
                .sourceUrl(e.getSourceUrl())
                .restrictions(restrictions)
                .legislations(legislations)
                .build();
    }

    private EuAdditiveRestriction mapRestriction(EuAdditiveRestrictionEntity r) {
        return EuAdditiveRestriction.builder()
                .foodCategoryId(r.getFoodCategoryId())
                .foodCategoryNumber(r.getFoodCategoryNumber())
                .foodCategoryNameEn(r.getFoodCategoryNameEn())
                .foodCategoryNameLt(r.getFoodCategoryNameLt())
                .restrictionType(r.getRestrictionType())
                .restrictionValue(r.getRestrictionValue())
                .restrictionUnit(r.getRestrictionUnit())
                .restrictionCommentEn(r.getRestrictionCommentEn())
                .restrictionCommentLt(r.getRestrictionCommentLt())
                .noteNumber(r.getNoteNumber())
                .noteTextEn(r.getNoteTextEn())
                .noteTextLt(r.getNoteTextLt())
                .legislationId(r.getLegislationId())
                .build();
    }

    private EuAdditiveLegislation mapLegislation(EuAdditiveLegislationEntity l) {
        return EuAdditiveLegislation.builder()
                .euLegislationId(l.getEuLegislationId())
                .titleEn(l.getTitleEn())
                .titleLt(l.getTitleLt())
                .text(l.getText())
                .eurlexLink(l.getEurlexLink())
                .publicationDate(l.getPublicationDate())
                .dateEntryIntoForce(l.getDateEntryIntoForce())
                .ojNumber(l.getOjNumber())
                .ojPage(l.getOjPage())
                .build();
    }
}
