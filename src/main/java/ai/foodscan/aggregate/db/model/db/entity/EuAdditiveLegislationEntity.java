package ai.foodscan.aggregate.db.model.db.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;
import java.time.LocalDate;

@Table(name = "eu_additive_legislations", schema = "aggregate")
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Data
public class EuAdditiveLegislationEntity implements Serializable, Persistable<Long> {

    @Id
    @Column("id")
    private Long id;

    @Column("additive_id")
    private Long additiveId;

    @Column("eu_legislation_id")
    private Long euLegislationId;

    @Column("title_en")
    private String titleEn;

    @Column("title_lt")
    private String titleLt;

    @Column("text")
    private String text;

    @Column("eurlex_link")
    private String eurlexLink;

    @Column("publication_date")
    private LocalDate publicationDate;

    @Column("date_entry_into_force")
    private LocalDate dateEntryIntoForce;

    @Column("oj_number")
    private String ojNumber;

    @Column("oj_page")
    private String ojPage;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return id == null;
    }
}
