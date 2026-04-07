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
import java.time.LocalDateTime;

@Table(name = "eu_additives", schema = "aggregate")
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Data
public class EuAdditiveEntity implements Serializable, Persistable<Long> {

    @Id
    @Column("id")
    private Long id;

    @Column("e_number")
    private String eNumber;

    @Column("e_number_display")
    private String eNumberDisplay;

    @Column("e_number_sort")
    private Integer eNumberSort;

    @Column("display_name_en")
    private String displayNameEn;

    @Column("display_name_lt")
    private String displayNameLt;

    @Column("identifying_name_en")
    private String identifyingNameEn;

    @Column("identifying_name_lt")
    private String identifyingNameLt;

    @Column("synonyms_en")
    private String[] synonymsEn;

    @Column("synonyms_lt")
    private String[] synonymsLt;

    @Column("ins_number")
    private String insNumber;

    @Column("is_group")
    private Boolean isGroup;

    @Column("member_of_group")
    private String memberOfGroup;

    @Column("policy_item_code")
    private String policyItemCode;

    @Column("source_url")
    private String sourceUrl;

    @Column("scraped_at")
    private LocalDateTime scrapedAt;

    @Column("translated_at")
    private LocalDateTime translatedAt;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return id == null;
    }
}
