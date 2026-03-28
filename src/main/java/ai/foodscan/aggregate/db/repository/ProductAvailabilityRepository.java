package ai.foodscan.aggregate.db.repository;

import ai.foodscan.aggregate.db.model.db.entity.ProductAvailabilityEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface ProductAvailabilityRepository extends ReactiveCrudRepository<ProductAvailabilityEntity, UUID> {

    Flux<ProductAvailabilityEntity> findByInternalProductId(UUID internalProductId);

    Flux<ProductAvailabilityEntity> findBySource(String source);

    @Query("""
        SELECT * FROM aggregate.product_availability
        WHERE internal_product_id = :productId AND source = :source
        """)
    Mono<ProductAvailabilityEntity> findByInternalProductIdAndSource(UUID productId, String source);

    Mono<Void> deleteByInternalProductId(UUID internalProductId);
}
