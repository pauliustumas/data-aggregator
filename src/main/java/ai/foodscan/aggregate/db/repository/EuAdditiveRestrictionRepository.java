package ai.foodscan.aggregate.db.repository;

import ai.foodscan.aggregate.db.model.db.entity.EuAdditiveRestrictionEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface EuAdditiveRestrictionRepository extends ReactiveCrudRepository<EuAdditiveRestrictionEntity, Long> {

    Flux<EuAdditiveRestrictionEntity> findByAdditiveId(Long additiveId);
}
