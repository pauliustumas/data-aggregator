package ai.foodscan.aggregate.db.repository;

import ai.foodscan.aggregate.db.model.api.NutritionPer100g;
import ai.foodscan.aggregate.db.model.db.entity.ProductEntity;
import io.r2dbc.postgresql.codec.Json;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ProductRepository extends ReactiveCrudRepository<ProductEntity, UUID> {

    /**
     * Finds products with a barcode that contains the specified substring,
     * ignoring case.
     *
     * @param barcode the substring to search for within the barcode.
     * @return a Flux containing all matching {@link ProductEntity} instances.
     */
    @Query("SELECT * FROM aggregate.products WHERE LOWER(barcode) LIKE LOWER(CONCAT('%', :barcode, '%')) LIMIT :limit")
    Flux<ProductEntity> findByBarcodeLike(String barcode, int limit);

    /**
     * Finds a product with an exact match for the specified barcode.
     *
     * @param barcode the exact barcode to search for.
     * @return a Mono emitting the matching {@link ProductEntity} or empty if not found.
     */
    @Query("SELECT * FROM aggregate.products WHERE barcode = :barcode LIMIT :limit")
    Mono<ProductEntity> findByBarcode(String barcode, int limit);

    /**
     * Retrieves all barcodes from the products table.
     *
     * @return a Flux emitting all barcodes as Strings.
     */
    @Query("SELECT barcode FROM aggregate.products")
    Flux<String> findAllBarcodes();

    /**
     * Finds products where either the Lithuanian name (name_lt) or English name (name_en)
     * contains the specified substring, ignoring case.
     *
     * @param name the substring to search for within the name.
     * @return a Flux containing all matching {@link ProductEntity} instances.
     */
    @Query("SELECT * FROM aggregate.products " +
            "WHERE (to_tsvector('simple', immutable_unaccent(LOWER(name_lt))) " +
            "         @@ to_tsquery('simple', regexp_replace(immutable_unaccent(LOWER(:name)), '\\s+', ':* & ', 'g') || ':*')) " +
            "   OR (to_tsvector('simple', immutable_unaccent(LOWER(name_en))) " +
            "         @@ to_tsquery('simple', regexp_replace(immutable_unaccent(LOWER(:name)), '\\s+', ':* & ', 'g') || ':*')) " +
            "LIMIT :limit")
    Flux<ProductEntity> findByNameFullText(String name, int limit);


    /**
     * Finds products by performing a case-insensitive search on barcode, Lithuanian name (name_lt),
     * or English name (name_en). A match in any of these fields will cause the product to be returned.
     *
     * @param barcode the substring to search for within the barcode.
     * @param nameLt  the substring to search for within the Lithuanian name.
     * @param nameEn  the substring to search for within the English name.
     * @return a Flux containing all matching {@link ProductEntity} instances.
     */
    @Query("SELECT * FROM aggregate.products WHERE LOWER(barcode) LIKE LOWER(CONCAT('%', :barcode, '%')) " +
            "OR LOWER(name_lt) LIKE LOWER(CONCAT('%', :nameLt, '%')) OR LOWER(name_en) LIKE LOWER(CONCAT('%', :nameEn, '%')) LIMIT :limit")
    Flux<ProductEntity> findByBarcodeLikeOrNameLike(String barcode, String nameLt, String nameEn, int limit);


    /**
     * Finds the average nutritional values per 100g for products in the specified category.
     *
     * @param category the main category to search for.
     * @return a Mono emitting the average nutritional values per 100g.
     */
    @Query("SELECT " +
            "  ROUND(AVG((nutrition_per_100g->>'fats')::numeric) FILTER (WHERE nutrition_per_100g->>'fats' IS NOT NULL), 2) AS fats, " +
            "  ROUND(AVG((nutrition_per_100g->>'salt')::numeric) FILTER (WHERE nutrition_per_100g->>'salt' IS NOT NULL), 2) AS salt, " +
            "  ROUND(AVG((nutrition_per_100g->>'sugars')::numeric) FILTER (WHERE nutrition_per_100g->>'sugars' IS NOT NULL), 2) AS sugars, " +
            "  ROUND(AVG((nutrition_per_100g->>'proteins')::numeric) FILTER (WHERE nutrition_per_100g->>'proteins' IS NOT NULL), 2) AS proteins, " +
            "  ROUND(AVG((nutrition_per_100g->>'carbohydrates')::numeric) FILTER (WHERE nutrition_per_100g->>'carbohydrates' IS NOT NULL), 2) AS carbohydrates, " +
            "  ROUND(AVG((nutrition_per_100g->>'saturated_fats')::numeric) FILTER (WHERE nutrition_per_100g->>'saturated_fats' IS NOT NULL), 2) AS saturated_fats, " +
            "  ROUND(AVG((nutrition_per_100g->>'energy_value_kj')::numeric) FILTER (WHERE nutrition_per_100g->>'energy_value_kj' IS NOT NULL))::integer AS energy_value_kj " +
            "FROM aggregate.products " +
            "WHERE main_category_en = :category " +
            "  AND sub_category_en = :sub_category " +
            "  AND sub_sub_category_en = :sub_sub_category")

    Mono<NutritionPer100g> findNutritionalAveragesByCategory(String category,
                                                             String sub_category,
                                                             String sub_sub_category);

    /**
     * Retrieves product IDs by Main Category in English.
     *
     * @param mainCategory the main category name.
     * @param limit        the number of records per page.
     * @param offset       the number of records to skip.
     * @return a Flux of product UUIDs.
     */
    @Query("""
        SELECT internal_product_id FROM aggregate.products 
        WHERE main_category_en = :mainCategory 
        ORDER BY main_category_en ASC, sub_category_en ASC, sub_sub_category_en ASC 
        LIMIT :limit OFFSET :offset
        """)
    Flux<UUID> findProductIdsByMainCategoryEn(String mainCategory, int limit, int offset);

    /**
     * Counts total products by Main Category in English.
     *
     * @param mainCategory the main category name.
     * @return a Mono emitting the total count.
     */
    @Query("""
        SELECT COUNT(*) FROM aggregate.products 
        WHERE main_category_en = :mainCategory
        """)
    Mono<Long> countProductsByMainCategoryEn(String mainCategory);

    /**
     * Retrieves product IDs by Main + Sub-Category in English.
     *
     * @param mainCategory the main category name.
     * @param subCategory  the sub-category name.
     * @param limit        the number of records per page.
     * @param offset       the number of records to skip.
     * @return a Flux of product UUIDs.
     */
    @Query("""
        SELECT internal_product_id FROM aggregate.products 
        WHERE main_category_en = :mainCategory 
          AND sub_category_en = :subCategory 
        ORDER BY main_category_en ASC, sub_category_en ASC, sub_sub_category_en ASC 
        LIMIT :limit OFFSET :offset
        """)
    Flux<UUID> findProductIdsByMainAndSubCategoryEn(String mainCategory, String subCategory, int limit, int offset);

    /**
     * Counts total products by Main + Sub-Category in English.
     *
     * @param mainCategory the main category name.
     * @param subCategory  the sub-category name.
     * @return a Mono emitting the total count.
     */
    @Query("""
        SELECT COUNT(*) FROM aggregate.products 
        WHERE main_category_en = :mainCategory 
          AND sub_category_en = :subCategory
        """)
    Mono<Long> countProductsByMainAndSubCategoryEn(String mainCategory, String subCategory);


    /**
     * Retrieves product IDs by Main + Sub + Sub-Sub Category in English.
     *
     * @param mainCategory    the main category name.
     * @param subCategory     the sub-category name.
     * @param subSubCategory  the sub-sub-category name.
     * @param limit           the number of records per page.
     * @param offset          the number of records to skip.
     * @return a Flux of product UUIDs.
     */
    @Query("""
        SELECT internal_product_id FROM aggregate.products 
        WHERE main_category_en = :mainCategory 
          AND sub_category_en = :subCategory 
          AND sub_sub_category_en = :subSubCategory 
        ORDER BY main_category_en ASC, sub_category_en ASC, sub_sub_category_en ASC 
        LIMIT :limit OFFSET :offset
        """)
    Flux<UUID> findProductIdsByMainSubSubCategoryEn(String mainCategory, String subCategory, String subSubCategory, int limit, int offset);

    /**
     * Counts total products by Main + Sub + Sub-Sub Category in English.
     *
     * @param mainCategory    the main category name.
     * @param subCategory     the sub-category name.
     * @param subSubCategory  the sub-sub-category name.
     * @return a Mono emitting the total count.
     */
    @Query("""
        SELECT COUNT(*) FROM aggregate.products 
        WHERE main_category_en = :mainCategory 
          AND sub_category_en = :subCategory 
          AND sub_sub_category_en = :subSubCategory
        """)
    Mono<Long> countProductsByMainSubSubCategoryEn(String mainCategory, String subCategory, String subSubCategory);

    @Query("""
    SELECT COUNT(*)
    FROM aggregate.products
    WHERE main_category_en = :mainCategory
      AND sub_category_en = :subCategory
      AND sub_sub_category_en = :subSubCategory
      AND recommended = :isRecommended
    """)
    Mono<Long> countProductsByMainSubSubCategoryEn(String mainCategory,
                                                   String subCategory,
                                                   String subSubCategory,
                                                   boolean isRecommended);

    // Repeat the above methods for Lithuanian (lt) language

    @Query("""
        SELECT internal_product_id FROM aggregate.products 
        WHERE main_category_lt = :mainCategory 
        ORDER BY main_category_lt ASC, sub_category_lt ASC, sub_sub_category_lt ASC 
        LIMIT :limit OFFSET :offset
        """)
    Flux<UUID> findProductIdsByMainCategoryLt(String mainCategory, int limit, int offset);

    @Query("""
        SELECT COUNT(*) FROM aggregate.products 
        WHERE main_category_lt = :mainCategory
        """)
    Mono<Long> countProductsByMainCategoryLt(String mainCategory);

    @Query("""
        SELECT internal_product_id FROM aggregate.products 
        WHERE main_category_lt = :mainCategory 
          AND sub_category_lt = :subCategory 
        ORDER BY main_category_lt ASC, sub_category_lt ASC, sub_sub_category_lt ASC 
        LIMIT :limit OFFSET :offset
        """)
    Flux<UUID> findProductIdsByMainAndSubCategoryLt(String mainCategory, String subCategory, int limit, int offset);

    @Query("""
        SELECT COUNT(*) FROM aggregate.products 
        WHERE main_category_lt = :mainCategory 
          AND sub_category_lt = :subCategory
        """)
    Mono<Long> countProductsByMainAndSubCategoryLt(String mainCategory, String subCategory);

    @Query("""
        SELECT internal_product_id FROM aggregate.products 
        WHERE main_category_lt = :mainCategory 
          AND sub_category_lt = :subCategory 
          AND sub_sub_category_lt = :subSubCategory 
        ORDER BY main_category_lt ASC, sub_category_lt ASC, sub_sub_category_lt ASC 
        LIMIT :limit OFFSET :offset
        """)
    Flux<UUID> findProductIdsByMainSubSubCategoryLt(String mainCategory, String subCategory, String subSubCategory, int limit, int offset);

    @Query("""
        SELECT COUNT(*) FROM aggregate.products 
        WHERE main_category_lt = :mainCategory 
          AND sub_category_lt = :subCategory 
          AND sub_sub_category_lt = :subSubCategory
        """)
    Mono<Long> countProductsByMainSubSubCategoryLt(String mainCategory, String subCategory, String subSubCategory);

    /**
     * Retrieves all product IDs with pagination.
     *
     * @param limit  the number of records per page.
     * @param offset the number of records to skip.
     * @return a Flux of product UUIDs.
     */
    @Query("""
        SELECT internal_product_id FROM aggregate.products 
        ORDER BY main_category_en ASC, sub_category_en ASC, sub_sub_category_en ASC 
        LIMIT :limit OFFSET :offset
        """)
    Flux<UUID> findAllProductIds(int limit, int offset);

    /**
     * Counts total products without any category filters.
     *
     * @return a Mono emitting the total count.
     */
    @Query("""
        SELECT COUNT(*) FROM aggregate.products
        """)
    Mono<Long> countAllProducts();

    /**
     * Finds up to :limit product IDs matching the given main, sub, and sub-sub category in English,
     * filtering by the recommended flag. The products are returned in random order.
     *
     * @param mainCategory   The English main category.
     * @param subCategory    The English sub-category.
     * @param subSubCategory The English sub-sub-category.
     * @param isRecommended  {@code true} to only return recommended products, {@code false} for similar products.
     * @param limit          The maximum number of products to return.
     * @return A Flux of product UUIDs in random order.
     */
    @Query("""
    SELECT internal_product_id
    FROM aggregate.products
    WHERE main_category_en = :mainCategory
      AND sub_category_en = :subCategory
      AND sub_sub_category_en = :subSubCategory
      AND recommended = :isRecommended
    ORDER BY random()
    LIMIT :limit
    """)
    Flux<UUID> findProductIdsByMainSubSubCategoryEnRandomOrder(String mainCategory,
                                                               String subCategory,
                                                               String subSubCategory,
                                                               boolean isRecommended,
                                                               int limit);

    /**
     * Finds up to :limit product IDs matching the given main and sub category in English,
     * filtering by the recommended flag. The products are returned in random order.
     *
     * @param mainCategory  The English main category.
     * @param subCategory   The English sub-category.
     * @param isRecommended {@code true} to only return recommended products, {@code false} for similar products.
     * @param limit         The maximum number of products to return.
     * @return A Flux of product UUIDs in random order.
     */
    @Query("""
    SELECT internal_product_id
    FROM aggregate.products
    WHERE main_category_en = :mainCategory
      AND sub_category_en = :subCategory
      AND recommended = :isRecommended
    ORDER BY random()
    LIMIT :limit
    """)
    Flux<UUID> findProductIdsByMainAndSubCategoryEnRandomOrder(String mainCategory,
                                                               String subCategory,
                                                               boolean isRecommended,
                                                               int limit);
}
