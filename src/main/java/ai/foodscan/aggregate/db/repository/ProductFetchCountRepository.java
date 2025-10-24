package ai.foodscan.aggregate.db.repository;

import ai.foodscan.aggregate.db.model.db.entity.ProductFetchCountEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface ProductFetchCountRepository extends ReactiveCrudRepository<ProductFetchCountEntity, UUID> {

    // Existing methods...

    // --- 1) Top Searched --- //
    @Query("""
        SELECT internal_product_id 
        FROM aggregate.product_fetch_counts
        ORDER BY fetch_count DESC
        LIMIT :limit OFFSET :offset
        """)
    Flux<UUID> findTopSearchedProductIds(int limit, int offset);

    // For counting total fetch records (useful for total_count).
    @Query("""
        SELECT COUNT(*) 
        FROM aggregate.product_fetch_counts
        """)
    Mono<Long> countAllFetchRecords();


    // --- 2) Recently Searched --- //
    @Query("""
        SELECT internal_product_id 
        FROM aggregate.product_fetch_counts
        ORDER BY last_fetched_at DESC
        LIMIT :limit OFFSET :offset
        """)
    Flux<UUID> findRecentSearchedProductIds(int limit, int offset);

}
