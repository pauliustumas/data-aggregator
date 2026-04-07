package ai.foodscan.aggregate.db.repository;

import ai.foodscan.aggregate.db.model.db.entity.EuAdditiveLegislationEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface EuAdditiveLegislationRepository extends ReactiveCrudRepository<EuAdditiveLegislationEntity, Long> {

    Flux<EuAdditiveLegislationEntity> findByAdditiveId(Long additiveId);
}
