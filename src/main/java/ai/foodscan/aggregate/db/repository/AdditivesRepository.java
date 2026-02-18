package ai.foodscan.aggregate.db.repository;

import ai.foodscan.aggregate.db.model.db.entity.AdditiveEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdditivesRepository extends ReactiveCrudRepository<AdditiveEntity, String> {
    // You can define custom query methods here if needed.
}
