package ai.foodscan.aggregate.db.repository;

import ai.foodscan.aggregate.db.model.db.entity.CategoryEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

/**
 * Reactive repository for CategoryEntity.
 */
public interface CategoryRepository extends ReactiveCrudRepository<CategoryEntity, String> {

    /**
     * Retrieves distinct main categories, sub-categories, and sub-sub-categories.
     *
     * @return a Flux emitting CategoryEntity instances.
     */
    @Query("""
            SELECT DISTINCT *
            FROM aggregate.category_slugs 
            WHERE main_category_lt IS NOT NULL 
              AND sub_category_lt IS NOT NULL 
              AND main_category_en IS NOT NULL 
              AND sub_category_en IS NOT NULL 
            ORDER BY main_category_lt ASC, sub_category_lt ASC, sub_sub_category_lt ASC
            """)
    Flux<CategoryEntity> findDistinctCategories();

    /**
     * Retrieves distinct main categories with names and slugs in English.
     *
     * @return a Flux emitting CategoryEntity instances with only mainCategoryEn and mainCategorySlugEn populated.
     */
    @Query("""
            SELECT DISTINCT main_category_en, main_category_slug_en, main_category_lt, main_category_slug_lt
            FROM aggregate.category_slugs
            WHERE main_category_en IS NOT NULL
            AND main_category_lt IS NOT NULL
            ORDER BY main_category_en ASC
            """)
    Flux<CategoryEntity> findDistinctMainCategories();

    /**
     * Retrieves distinct sub-categories with names and slugs in Lithuanian based on main category slug.
     *
     * @param mainCategorySlug the slug of the main category.
     * @return a Flux emitting CategoryEntity instances with only subCategoryLt and subCategorySlugLt populated.
     */
    @Query("""
            SELECT DISTINCT 
                CONCAT(main_category_slug_en, '-', sub_category_slug_en) AS id,
                main_category_lt, 
                main_category_slug_lt,
                sub_category_lt, 
                sub_category_slug_lt,
                main_category_en, 
                main_category_slug_en,
                sub_category_en, 
                sub_category_slug_en
            FROM aggregate.category_slugs
            WHERE main_category_slug_lt = :mainCategorySlug 
                OR main_category_slug_en = :mainCategorySlug
                AND sub_category_lt IS NOT NULL 
                AND sub_category_en IS NOT NULL
            ORDER BY sub_category_en ASC
            """)
    Flux<CategoryEntity> findDistinctSubCategories(String mainCategorySlug);

    /**
     * Retrieves distinct sub-sub-categories with names and slugs in English based on main and sub-category slugs.
     *
     * @param mainCategorySlug the slug of the main category.
     * @param subCategorySlug  the slug of the sub-category.
     * @return a Flux emitting CategoryEntity instances with only subSubCategoryEn and subSubCategorySlugEn populated.
     */
    @Query("""
            SELECT DISTINCT 
                CONCAT(main_category_slug_en, '-', sub_category_slug_en, '-', sub_sub_category_slug_en) AS id,
                main_category_en, 
                main_category_slug_en,
                sub_category_en, 
                sub_category_slug_en,
                sub_sub_category_en, 
                sub_sub_category_slug_en,
                main_category_lt, 
                main_category_slug_lt,
                sub_category_lt, 
                sub_category_slug_lt,
                sub_sub_category_lt, 
                sub_sub_category_slug_lt
            FROM aggregate.category_slugs
            WHERE main_category_slug_en = :mainCategorySlug
              AND sub_category_slug_en = :subCategorySlug
              AND sub_sub_category_en IS NOT NULL
              OR main_category_slug_lt = :mainCategorySlug
              AND sub_category_slug_lt = :subCategorySlug
              AND sub_sub_category_lt IS NOT NULL
              
            ORDER BY sub_sub_category_en ASC
            """)
    Flux<CategoryEntity> findDistinctSubSubCategories(String mainCategorySlug, String subCategorySlug);
}
