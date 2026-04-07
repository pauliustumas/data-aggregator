package ai.foodscan.aggregate.db.repository;

import ai.foodscan.aggregate.db.model.db.entity.EuAdditiveEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface EuAdditiveRepository extends ReactiveCrudRepository<EuAdditiveEntity, Long> {

    @Query("SELECT * FROM aggregate.eu_additives ORDER BY e_number_sort ASC NULLS LAST, display_name_en ASC")
    Flux<EuAdditiveEntity> findAllOrdered();

    Mono<EuAdditiveEntity> findByENumber(String eNumber);

    @Query("SELECT * FROM aggregate.eu_additives WHERE LOWER(e_number) = LOWER(:eNumber) LIMIT 1")
    Mono<EuAdditiveEntity> findByENumberCaseInsensitive(String eNumber);
}
