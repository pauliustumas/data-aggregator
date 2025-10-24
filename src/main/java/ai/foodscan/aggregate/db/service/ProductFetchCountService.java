package ai.foodscan.aggregate.db.service;

import ai.foodscan.aggregate.db.model.db.entity.ProductFetchCountEntity;
import ai.foodscan.aggregate.db.repository.ProductFetchCountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductFetchCountService {

    private final ProductFetchCountRepository fetchCountRepository;

    /**
     * Increments the fetch count for the specified product.
     */
    public Mono<ProductFetchCountEntity> incrementFetchCount(UUID internalProductId) {
        return fetchCountRepository.findById(internalProductId)
                .flatMap(existingRecord -> {
                    existingRecord.setFetchCount(existingRecord.getFetchCount() + 1);
                    existingRecord.setLastFetchedAt(LocalDateTime.now());
                    return fetchCountRepository.save(existingRecord);
                })
                .switchIfEmpty(
                        Mono.defer(() -> {
                            ProductFetchCountEntity newRecord = ProductFetchCountEntity.builder()
                                    .internalProductId(internalProductId)
                                    .fetchCount(1L)
                                    .lastFetchedAt(LocalDateTime.now())
                                    .build();
                            log.info("Creating new fetch count record for product ID: {}", internalProductId);
                            return fetchCountRepository.save(newRecord);
                        })
                )
                .doOnSuccess(record -> log.debug("Fetch count updated: {}", record))
                .doOnError(e -> log.error("Failed to update fetch count for product ID: {}", internalProductId, e));
    }

    /**
     * Retrieves the top searched product IDs (sorted by fetch_count DESC).
     */
    public Flux<UUID> findTopSearchedProductIds(int page, int size) {
        int offset = page * size;
        return fetchCountRepository.findTopSearchedProductIds(size, offset);
    }

    /**
     * Retrieves the most recently searched product IDs (sorted by last_fetched_at DESC).
     */
    public Flux<UUID> findRecentSearchedProductIds(int page, int size) {
        int offset = page * size;
        return fetchCountRepository.findRecentSearchedProductIds(size, offset);
    }

    /**
     * Counts total records in product_fetch_counts.
     */
    public Mono<Long> countAllFetchRecords() {
        return fetchCountRepository.countAllFetchRecords();
    }
}
