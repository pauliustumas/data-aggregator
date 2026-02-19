package ai.foodscan.aggregate.db.repository;

import ai.foodscan.aggregate.db.model.api.ProductFilterRequest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class ProductFilterRepository {

    private final DatabaseClient databaseClient;

    public ProductFilterRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    public Flux<UUID> findFilteredProductIds(ProductFilterRequest filter) {
        Map<String, Object> params = new HashMap<>();
        String where = buildWhereClause(filter, params);
        String orderBy = resolveOrderBy(filter.getSortBy(), filter.getLang());

        int limit = filter.getSize();
        int offset = filter.getPage() * limit;
        params.put("limit", limit);
        params.put("offset", offset);

        String sql = "SELECT internal_product_id FROM aggregate.products"
                + where + orderBy + " LIMIT :limit OFFSET :offset";

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            spec = spec.bind(entry.getKey(), entry.getValue());
        }

        return spec.map(row -> row.get("internal_product_id", UUID.class)).all();
    }

    public Mono<Long> countFilteredProducts(ProductFilterRequest filter) {
        Map<String, Object> params = new HashMap<>();
        String where = buildWhereClause(filter, params);

        String sql = "SELECT COUNT(*) FROM aggregate.products" + where;

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            spec = spec.bind(entry.getKey(), entry.getValue());
        }

        return spec.map(row -> row.get(0, Long.class)).one();
    }

    private String buildWhereClause(ProductFilterRequest filter, Map<String, Object> params) {
        List<String> conditions = new ArrayList<>();
        boolean isLt = "lt".equalsIgnoreCase(filter.getLang());

        // Category filters — multi-value takes precedence over single-value
        String mainCatCol = isLt ? "main_category_lt" : "main_category_en";
        if (filter.getCategories() != null && !filter.getCategories().isEmpty()) {
            conditions.add(mainCatCol + " = ANY(CAST(:categories AS text[]))");
            params.put("categories", filter.getCategories().toArray(new String[0]));
        } else if (filter.getCategory() != null && !filter.getCategory().isBlank()) {
            conditions.add(mainCatCol + " = :category");
            params.put("category", filter.getCategory());
        }

        String subCatCol = isLt ? "sub_category_lt" : "sub_category_en";
        if (filter.getSubCategories() != null && !filter.getSubCategories().isEmpty()) {
            conditions.add(subCatCol + " = ANY(CAST(:subCategories AS text[]))");
            params.put("subCategories", filter.getSubCategories().toArray(new String[0]));
        } else if (filter.getSubCategory() != null && !filter.getSubCategory().isBlank()) {
            conditions.add(subCatCol + " = :subCategory");
            params.put("subCategory", filter.getSubCategory());
        }

        String subSubCatCol = isLt ? "sub_sub_category_lt" : "sub_sub_category_en";
        if (filter.getSubSubCategories() != null && !filter.getSubSubCategories().isEmpty()) {
            conditions.add(subSubCatCol + " = ANY(CAST(:subSubCategories AS text[]))");
            params.put("subSubCategories", filter.getSubSubCategories().toArray(new String[0]));
        } else if (filter.getSubSubCategory() != null && !filter.getSubSubCategory().isBlank()) {
            conditions.add(subSubCatCol + " = :subSubCategory");
            params.put("subSubCategory", filter.getSubSubCategory());
        }

        // Allergens exclusion
        if (filter.getExcludeAllergens() != null && !filter.getExcludeAllergens().isEmpty()) {
            String col = isLt ? "allergens_lt" : "allergens_en";
            conditions.add("(" + col + " IS NULL OR NOT (" + col + " && CAST(:excludeAllergens AS text[])))");
            params.put("excludeAllergens", filter.getExcludeAllergens().toArray(new String[0]));
        }

        // Additives exclusion
        if (filter.getExcludeAdditives() != null && !filter.getExcludeAdditives().isEmpty()) {
            String col = isLt ? "additives_lt" : "additives_en";
            conditions.add("(" + col + " IS NULL OR NOT EXISTS ("
                    + "SELECT 1 FROM jsonb_array_elements(" + col + ") AS elem "
                    + "WHERE elem->>'shortcode' = ANY(CAST(:excludeAdditives AS text[]))))");
            params.put("excludeAdditives", filter.getExcludeAdditives().toArray(new String[0]));
        }

        // Price filters — use denormalized latest_price column
        if (filter.getPriceMin() != null) {
            conditions.add("latest_price >= :priceMin");
            params.put("priceMin", filter.getPriceMin());
        }
        if (filter.getPriceMax() != null) {
            conditions.add("latest_price <= :priceMax");
            params.put("priceMax", filter.getPriceMax());
        }

        // Name full-text search — multi-name takes precedence over single name
        List<String> nameValues = filter.getNames() != null && !filter.getNames().isEmpty()
                ? filter.getNames()
                : (filter.getName() != null && !filter.getName().isBlank()
                        ? List.of(filter.getName()) : List.of());

        if (!nameValues.isEmpty() && nameValues.size() == 1) {
            // Single name — keep original parameter style
            conditions.add(
                    "((to_tsvector('simple', immutable_unaccent(LOWER(name_lt)))"
                    + " @@ to_tsquery('simple', regexp_replace(immutable_unaccent(LOWER(:name)), '\\s+', ':* & ', 'g') || ':*'))"
                    + " OR (to_tsvector('simple', immutable_unaccent(LOWER(name_en)))"
                    + " @@ to_tsquery('simple', regexp_replace(immutable_unaccent(LOWER(:name)), '\\s+', ':* & ', 'g') || ':*')))"
            );
            params.put("name", nameValues.get(0));
        } else if (nameValues.size() > 1) {
            // Multiple names — each gets its own bind parameter, combined with OR
            List<String> nameConditions = new ArrayList<>();
            for (int i = 0; i < nameValues.size(); i++) {
                String p = "name_" + i;
                nameConditions.add(
                        "((to_tsvector('simple', immutable_unaccent(LOWER(name_lt)))"
                        + " @@ to_tsquery('simple', regexp_replace(immutable_unaccent(LOWER(:" + p + ")), '\\s+', ':* & ', 'g') || ':*'))"
                        + " OR (to_tsvector('simple', immutable_unaccent(LOWER(name_en)))"
                        + " @@ to_tsquery('simple', regexp_replace(immutable_unaccent(LOWER(:" + p + ")), '\\s+', ':* & ', 'g') || ':*')))"
                );
                params.put(p, nameValues.get(i));
            }
            conditions.add("(" + String.join(" OR ", nameConditions) + ")");
        }

        if (conditions.isEmpty()) {
            return "";
        }
        return " WHERE " + String.join(" AND ", conditions);
    }

    private String resolveOrderBy(String sortBy, String lang) {
        if (sortBy == null || sortBy.isBlank()) {
            return " ORDER BY created_at DESC";
        }
        boolean isLt = "lt".equalsIgnoreCase(lang);
        return switch (sortBy) {
            case "price_asc" -> " ORDER BY latest_price ASC NULLS LAST";
            case "price_desc" -> " ORDER BY latest_price DESC NULLS LAST";
            case "name_asc" -> " ORDER BY " + (isLt ? "name_lt" : "name_en") + " ASC NULLS LAST";
            case "name_desc" -> " ORDER BY " + (isLt ? "name_lt" : "name_en") + " DESC NULLS LAST";
            default -> " ORDER BY created_at DESC";
        };
    }
}
