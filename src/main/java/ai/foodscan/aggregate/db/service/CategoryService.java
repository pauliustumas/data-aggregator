package ai.foodscan.aggregate.db.service;

import ai.foodscan.aggregate.db.enhancer.ProductFormatter;
import ai.foodscan.aggregate.db.exception.NoProductFoundException;
import ai.foodscan.aggregate.db.mapper.MinimalProductMapper;
import ai.foodscan.aggregate.db.model.api.CategoryDto;
import ai.foodscan.aggregate.db.model.api.MinimalProduct;
import ai.foodscan.aggregate.db.model.api.ProductsByCategoryResponse;
import ai.foodscan.aggregate.db.model.db.entity.CategoryEntity;
import ai.foodscan.aggregate.db.repository.CategoryRepository;
import ai.foodscan.aggregate.db.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * Service layer for Category operations. Provides methods to retrieve:
 * <ul>
 *   <li>Products filtered by categories</li>
 *   <li>Top searched products</li>
 *   <li>Recently searched products</li>
 *   <li>Distinct category hierarchy (main, sub, sub-sub) in specified language</li>
 * </ul>
 */
@Slf4j
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductFetchCountService fetchCountService;
    private final ProductRepository productRepository;
    private final ProductFormatter productFormatter;

    /**
     * Maps {@link CategoryEntity} to {@link CategoryDto}.
     * Language-specific fields are selected based on non-null values in the entity.
     */
    private final Function<CategoryEntity, CategoryDto> entityToDtoMapper = entity -> CategoryDto.builder()
            .id(entity.getId())
            .mainCategorySlugEn(entity.getMainCategorySlugEn())
            .mainCategorySlugLt(entity.getMainCategorySlugLt())
            .mainCategoryEn(entity.getMainCategoryEn())
            .mainCategoryLt(entity.getMainCategoryLt())
            .subCategorySlugEn(entity.getSubCategorySlugEn())
            .subCategorySlugLt(entity.getSubCategorySlugLt())
            .subCategoryEn(entity.getSubCategoryEn())
            .subCategoryLt(entity.getSubCategoryLt())
            .subSubCategorySlugEn(entity.getSubSubCategorySlugEn())
            .subSubCategorySlugLt(entity.getSubSubCategorySlugLt())
            .subSubCategoryEn(entity.getSubSubCategoryEn())
            .subSubCategoryLt(entity.getSubSubCategoryLt())
            .build();

    public CategoryService(CategoryRepository categoryRepository,
                           ProductFetchCountService fetchCountService,
                           ProductRepository productRepository,
                           ProductFormatter productFormatter) {
        this.categoryRepository = categoryRepository;
        this.fetchCountService = fetchCountService;
        this.productRepository = productRepository;
        this.productFormatter = productFormatter;
    }

    /**
     * Retrieves paged products filtered by the specified category hierarchy.
     * If no categories are provided, all products are returned paginated.
     *
     * @param mainCategory   The main category name (optional).
     * @param subCategory    The sub-category name (optional).
     * @param subSubCategory The sub-sub-category name (optional).
     * @param language       The language code. Must be "en" or "lt".
     * @param page           The page number (0-based).
     * @param size           The page size.
     * @return A Mono that emits a {@link ProductsByCategoryResponse}.
     */
    public Mono<ProductsByCategoryResponse> getProductsByCategory(String mainCategory,
                                                                  String subCategory,
                                                                  String subSubCategory,
                                                                  String language,
                                                                  int page,
                                                                  int size) {
        log.info("Entering getProductsByCategory with mainCategory={}, subCategory={}, subSubCategory={}, language={}, page={}, size={}",
                mainCategory, subCategory, subSubCategory, language, page, size);

        // Validate language
        if (language == null || (!language.equalsIgnoreCase("en") && !language.equalsIgnoreCase("lt"))) {
            log.error("Invalid language parameter. Provided: {}", language);
            return Mono.error(new IllegalArgumentException("Invalid language parameter. Use 'en' or 'lt'."));
        }

        // Calculate limit and offset
        int limit = size > 0 ? size : 20;
        int currentPage = Math.max(page, 0);
        int offset = currentPage * limit;

        // Determine which repository methods to call based on provided parameters
        Mono<Long> countMono;
        Flux<UUID> productIdsFlux;

        boolean hasMain = mainCategory != null && !mainCategory.trim().isEmpty();
        boolean hasSub = subCategory != null && !subCategory.trim().isEmpty();
        boolean hasSubSub = subSubCategory != null && !subSubCategory.trim().isEmpty();

        if (!hasMain && !hasSub && !hasSubSub) {
            // No category filters - return all products
            log.debug("No category filters provided. Fetching all products.");
            countMono = productRepository.countAllProducts();
            productIdsFlux = productRepository.findAllProductIds(limit, offset);
        } else {
            // Category filters provided
            if (!hasMain) {
                log.error("mainCategory must be provided when subCategory or subSubCategory is specified.");
                return Mono.error(new IllegalArgumentException(
                        "mainCategory must be provided when filtering by subCategory or subSubCategory."));
            }

            if (hasSubSub && !hasSub) {
                log.error("subCategory must be provided when subSubCategory is specified.");
                return Mono.error(new IllegalArgumentException(
                        "subCategory must be provided when subSubCategory is specified."));
            }

            if (language.equalsIgnoreCase("en")) {
                if (hasSubSub) {
                    countMono = productRepository.countProductsByMainSubSubCategoryEn(mainCategory, subCategory, subSubCategory);
                    productIdsFlux = productRepository.findProductIdsByMainSubSubCategoryEn(mainCategory, subCategory, subSubCategory, limit, offset);
                } else if (hasSub) {
                    countMono = productRepository.countProductsByMainAndSubCategoryEn(mainCategory, subCategory);
                    productIdsFlux = productRepository.findProductIdsByMainAndSubCategoryEn(mainCategory, subCategory, limit, offset);
                } else {
                    countMono = productRepository.countProductsByMainCategoryEn(mainCategory);
                    productIdsFlux = productRepository.findProductIdsByMainCategoryEn(mainCategory, limit, offset);
                }
            } else {
                // Language is "lt"
                if (hasSubSub) {
                    countMono = productRepository.countProductsByMainSubSubCategoryLt(mainCategory, subCategory, subSubCategory);
                    productIdsFlux = productRepository.findProductIdsByMainSubSubCategoryLt(mainCategory, subCategory, subSubCategory, limit, offset);
                } else if (hasSub) {
                    countMono = productRepository.countProductsByMainAndSubCategoryLt(mainCategory, subCategory);
                    productIdsFlux = productRepository.findProductIdsByMainAndSubCategoryLt(mainCategory, subCategory, limit, offset);
                } else {
                    countMono = productRepository.countProductsByMainCategoryLt(mainCategory);
                    productIdsFlux = productRepository.findProductIdsByMainCategoryLt(mainCategory, limit, offset);
                }
            }
        }

        return Mono.zip(countMono, productIdsFlux.collectList())
                .flatMap(tuple -> {
                    long totalCount = tuple.getT1();
                    List<UUID> productIds = tuple.getT2();
                    log.debug("Retrieved {} product IDs. totalCount={}", productIds.size(), totalCount);

                    return Flux.fromIterable(productIds)
                            .flatMap(this::getMinimalProductByInternalId)
                            .collectList()
                            .map(products -> new ProductsByCategoryResponse(products, totalCount, currentPage, limit));
                })
                .doOnSuccess(response -> log.info("Successfully created ProductsByCategoryResponse with {} products.",
                        response.getProducts().size()))
                .doOnError(e -> log.error("Error while creating ProductsByCategoryResponse: {}", e.getMessage(), e));
    }

    /**
     * Retrieves similar or recommended products in English for the given categories.
     * <p>
     * This method first validates that both main and sub categories are provided.
     * It then counts how many products exist matching the given main, sub, and sub-sub categories
     * along with the recommended flag. If more than one product is found, it attempts to fetch
     * product IDs based on main, sub, and sub-sub categories; otherwise, it falls back to fetching
     * based only on main and sub categories.
     * <p>
     * Each product ID is then converted to a {@code MinimalProduct}.
     *
     * @param mainCategory   The main category in English.
     * @param subCategory    The sub-category in English.
     * @param subSubCategory The sub-sub-category in English.
     * @param isRecommended  {@code true} to fetch recommended products, {@code false} for similar products.
     * @return A Flux of {@code MinimalProduct} objects that match the criteria.
     */
    public Flux<MinimalProduct> getSimilarOrRecommendedProductsByEnCategories(
            String mainCategory,
            String subCategory,
            String subSubCategory,
            boolean isRecommended) {

        // Basic validation
        if (isEmpty(mainCategory) || isEmpty(subCategory)) {
            return Flux.error(new IllegalArgumentException("mainCategory and subCategory are required."));
        }

        // Step 1: Count how many products match main+sub+subSub for the given recommended flag
        return productRepository
                .countProductsByMainSubSubCategoryEn(mainCategory, subCategory, subSubCategory, isRecommended)
                .flatMapMany(count -> {
                    // Step 2: If more than 1 product exists, use the subSub filter
                    if (count > 1) {
                        return productRepository.findProductIdsByMainSubSubCategoryEnRandomOrder(
                                mainCategory, subCategory, subSubCategory, isRecommended, 24);
                    } else {
                        // Step 3: Otherwise, fall back to main+sub filtering
                        return productRepository.findProductIdsByMainAndSubCategoryEnRandomOrder(
                                mainCategory, subCategory, isRecommended, 24);
                    }
                })
                // Convert each UUID to a MinimalProduct
                .flatMap(this::getMinimalProductByInternalId)
                .doOnComplete(() -> log.info("Fetched {} products for main={}, sub={}, subSub={}",
                        isRecommended ? "recommended" : "similar", mainCategory, subCategory, subSubCategory))
                .doOnError(err -> log.error("Error fetching {} products in English: {}",
                        isRecommended ? "recommended" : "similar", err.getMessage(), err));
    }

    /**
     * Simple helper for empty-check.
     * (You might already have something like this in your code.)
     */
    private boolean isEmpty(String s) {
        return (s == null || s.trim().isEmpty());
    }

    /**
     * Retrieves top searched products. This method uses {@link ProductFetchCountService} to:
     * <ul>
     *   <li>Get the total number of fetch records</li>
     *   <li>Get product IDs in order of search frequency</li>
     * </ul>
     *
     * @param page The page number (0-based).
     * @param size The page size.
     * @return A Mono that emits a {@link ProductsByCategoryResponse} of the top searched products.
     */
    public Mono<ProductsByCategoryResponse> getTopSearchedProducts(int page, int size) {
        log.info("Entering getTopSearchedProducts with page={}, size={}", page, size);

        return fetchCountService.countAllFetchRecords()
                .flatMap(totalCount ->
                        fetchCountService.findTopSearchedProductIds(page, size)
                                .flatMap(internalProductId ->
                                        getMinimalProductByInternalId(internalProductId)
                                                .onErrorResume(NoProductFoundException.class, ex -> {
                                                    log.warn("Skipping missing product with ID={}", internalProductId);
                                                    return Mono.empty(); // skip
                                                })
                                )
                                .collectList()
                                .map(productList -> ProductsByCategoryResponse.builder()
                                        .products(productList)
                                        .totalCount(totalCount)
                                        .currentPage(page)
                                        .productsPerPage(size)
                                        .build()
                                )
                )
                .doOnSuccess(response -> log.info("Successfully retrieved top searched products (count={}).",
                        response.getProducts().size()))
                .doOnError(e -> log.error("Error while retrieving top searched products: {}", e.getMessage(), e));
    }

    /**
     * Retrieves recently searched products (sorted by last fetched date in descending order).
     *
     * @param page The page number (0-based).
     * @param size The page size.
     * @return A Mono that emits a {@link ProductsByCategoryResponse} of the recently searched products.
     */
    public Mono<ProductsByCategoryResponse> getRecentSearchedProducts(int page, int size) {
        log.info("Entering getRecentSearchedProducts with page={}, size={}", page, size);

        return fetchCountService.countAllFetchRecords()
                .flatMap(totalCount ->
                        fetchCountService.findRecentSearchedProductIds(page, size)
                                .flatMap(internalProductId ->
                                        getMinimalProductByInternalId(internalProductId)
                                                .onErrorResume(NoProductFoundException.class, ex -> {
                                                    log.warn("Skipping missing product with ID={}", internalProductId);
                                                    return Mono.empty(); // skip
                                                })
                                )
                                .collectList()
                                .map(productList -> ProductsByCategoryResponse.builder()
                                        .products(productList)
                                        .totalCount(totalCount)
                                        .currentPage(page)
                                        .productsPerPage(size)
                                        .build()
                                )
                )
                .doOnSuccess(response -> log.info("Successfully retrieved recent searched products (count={}).",
                        response.getProducts().size()))
                .doOnError(e -> log.error("Error while retrieving recent searched products: {}", e.getMessage(), e));
    }

    /**
     * Retrieves all distinct categories (main, sub, and sub-sub) in the specified language.
     *
     * @return A Flux of {@link CategoryDto} representing distinct categories.
     */
    public Flux<CategoryDto> getDistinctCategories() {
        log.info("Entering getDistinctCategories");
        return categoryRepository.findDistinctCategories()
                .map(entityToDtoMapper)
                .doOnComplete(() -> log.info("Completed fetching distinct categories in English."));
    }

    /**
     * Retrieves all distinct main categories in the specified language.
     *
     * @return A Flux of {@link CategoryDto} with only mainCategory and mainCategorySlug populated.
     */
    public Flux<CategoryDto> getMainCategories() {
        log.info("Entering getMainCategories");
        return categoryRepository.findDistinctMainCategories()
                .map(entityToDtoMapper)
                .doOnComplete(() -> log.info("Completed fetching main categories in English."));
    }

    /**
     * Retrieves sub-categories for a given main category (by slug) in the specified language.
     *
     * @param mainCategorySlug The slug of the main category. Must not be null or empty.
     * @return A Flux of {@link CategoryDto} with subCategory and subCategorySlug populated.
     */
    public Flux<CategoryDto> getSubCategories(String mainCategorySlug) {
        log.info("Entering getSubCategories with mainCategorySlug={}", mainCategorySlug);

        if (mainCategorySlug.isEmpty()) {
            log.error("mainCategorySlug parameter is required.");
            return Flux.error(new IllegalArgumentException("mainCategorySlug parameter is required."));
        }

        return categoryRepository.findDistinctSubCategories(mainCategorySlug)
                .map(entityToDtoMapper)
                .doOnComplete(() -> log.info("Completed fetching sub categories in Lithuanian for slug={}.", mainCategorySlug));
    }

    /**
     * Retrieves sub-sub-categories for a given main category (by slug) and sub-category (by slug) in the specified language.
     *
     * @param mainCategorySlug The slug of the main category. Must not be null or empty.
     * @param subCategorySlug  The slug of the sub-category. Must not be null or empty.
     * @return A Flux of {@link CategoryDto} with subSubCategory and subSubCategorySlug populated.
     */
    public Flux<CategoryDto> getSubSubCategories(String mainCategorySlug, String subCategorySlug) {
        log.info("Entering getSubSubCategories with mainCategorySlug={}, subCategorySlug={}",
                mainCategorySlug, subCategorySlug);

        if (mainCategorySlug == null || mainCategorySlug.isEmpty()) {
            log.error("mainCategorySlug parameter is required.");
            return Flux.error(new IllegalArgumentException("mainCategorySlug parameter is required."));
        }
        if (subCategorySlug == null || subCategorySlug.isEmpty()) {
            log.error("subCategorySlug parameter is required.");
            return Flux.error(new IllegalArgumentException("subCategorySlug parameter is required."));
        }

        return categoryRepository.findDistinctSubSubCategories(mainCategorySlug, subCategorySlug)
                .map(entityToDtoMapper)
                .doOnComplete(() -> log.info("Completed fetching sub-sub categories in English for mainSlug={} and subSlug={}.",
                        mainCategorySlug, subCategorySlug));
    }

    /**
     * Retrieves the minimal product data by internal product ID.
     *
     * @param internalProductId The unique ID of the product.
     * @return A Mono that emits {@link MinimalProduct} if found, otherwise errors with {@link NoProductFoundException}.
     */
    private Mono<MinimalProduct> getMinimalProductByInternalId(UUID internalProductId) {
        log.debug("Fetching minimal product with internal_product_id={}", internalProductId);

        return productRepository.findById(internalProductId)
                .map(MinimalProductMapper::toProduct)
                .map(productFormatter::format)
                .switchIfEmpty(Mono.error(new NoProductFoundException(
                        "No product found with internal_product_id: " + internalProductId)))
                .doOnSuccess(product -> log.debug("Product retrieved: {}", product))
                .doOnError(e -> log.error("Failed to fetch product: {}", e.getMessage(), e));
    }

}
