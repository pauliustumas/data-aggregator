package ai.foodscan.aggregate.db.model.db.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Table(name = "product_availability", schema = "aggregate")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class ProductAvailabilityEntity implements Serializable, Persistable<UUID> {

    @Id
    @Column("id")
    private UUID id;

    @Column("internal_product_id")
    private UUID internalProductId;

    @Column("source")
    private String source;

    @Column("url")
    private String url;

    @Column("price")
    private BigDecimal price;

    @Column("original_price")
    private BigDecimal originalPrice;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Override
    public boolean isNew() {
        return id == null;
    }
}
