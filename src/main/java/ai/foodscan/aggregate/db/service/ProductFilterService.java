package ai.foodscan.aggregate.db.service;

import ai.foodscan.aggregate.db.enhancer.ProductFormatter;
import ai.foodscan.aggregate.db.mapper.MinimalProductMapper;
import ai.foodscan.aggregate.db.model.api.MinimalProduct;
import ai.foodscan.aggregate.db.model.api.ProductFilterRequest;
import ai.foodscan.aggregate.db.model.api.ProductsByCategoryResponse;
import ai.foodscan.aggregate.db.model.db.entity.ProductEntity;
import ai.foodscan.aggregate.db.repository.ProductFilterRepository;
import ai.foodscan.aggregate.db.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductFilterService {

    private final ProductFilterRepository productFilterRepository;
    private final ProductRepository productRepository;
    private final ProductFormatter productFormatter;

    public ProductFilterService(ProductFilterRepository productFilterRepository,
                                ProductRepository productRepository,
                                ProductFormatter productFormatter) {
        this.productFilterRepository = productFilterRepository;
        this.productRepository = productRepository;
        this.productFormatter = productFormatter;
    }

    public Mono<ProductsByCategoryResponse> filterProducts(ProductFilterRequest filter) {
        String lang = filter.getLang() != null ? filter.getLang() : "en";
        if (!lang.equalsIgnoreCase("en") && !lang.equalsIgnoreCase("lt")) {
            return Mono.error(new IllegalArgumentException("Invalid language parameter. Use 'en' or 'lt'."));
        }

        int size = filter.getSize() > 0 ? Math.min(filter.getSize(), 100) : 20;
        int page = Math.max(filter.getPage(), 0);

        ProductFilterRequest validated = filter.toBuilder()
                .lang(lang)
                .size(size)
                .page(page)
                .build();

        log.info("Filtering products: lang={}, page={}, size={}, category={}, name={}, sortBy={}, excludeCat={}, excludeSubCat={}",
                lang, page, size, validated.getCategory(), validated.getName(), validated.getSortBy(),
                validated.getExcludeCategories(), validated.getExcludeSubCategories());

        return Mono.zip(
                productFilterRepository.countFilteredProducts(validated),
                productFilterRepository.findFilteredProductIds(validated).collectList()
        ).flatMap(tuple -> {
            long totalCount = tuple.getT1();
            List<UUID> productIds = tuple.getT2();
            log.debug("Filter returned {} IDs, totalCount={}", productIds.size(), totalCount);

            return productRepository.findAllById(productIds)
                    .collectList()
                    .map(entities -> reorderAndMap(entities, productIds))
                    .map(products -> ProductsByCategoryResponse.builder()
                            .products(products)
                            .totalCount(totalCount)
                            .currentPage(page)
                            .productsPerPage(size)
                            .build());
        });
    }

    private List<MinimalProduct> reorderAndMap(List<ProductEntity> entities, List<UUID> orderedIds) {
        Map<UUID, ProductEntity> entityMap = entities.stream()
                .collect(Collectors.toMap(ProductEntity::getInternalProductId, Function.identity()));
        return orderedIds.stream()
                .map(entityMap::get)
                .filter(Objects::nonNull)
                .map(MinimalProductMapper::toEnrichedProduct)
                .map(productFormatter::format)
                .toList();
    }
}
