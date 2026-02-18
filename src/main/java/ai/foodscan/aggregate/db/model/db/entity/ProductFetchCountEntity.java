package ai.foodscan.aggregate.db.model.db.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Table(name = "product_fetch_counts", schema = "aggregate")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductFetchCountEntity implements Serializable, Persistable<UUID> {

    @Id
    @Column("internal_product_id")
    private UUID internalProductId; // References ProductEntity.internalProductId

    @Column("fetch_count")
    private Long fetchCount; // Number of times the product has been fetched

    @Column("last_fetched_at")
    private LocalDateTime lastFetchedAt; // Timestamp of the last fetch

    @Override
    public UUID getId() {
        return internalProductId;
    }

    @Override
    public boolean isNew() {
        return fetchCount == null || fetchCount <= 1;
    }
}
