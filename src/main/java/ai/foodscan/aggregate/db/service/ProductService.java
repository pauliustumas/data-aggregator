package ai.foodscan.aggregate.db.service;

import ai.foodscan.aggregate.db.calculators.NutritionCalculator;
import ai.foodscan.aggregate.db.enhancer.ProductFormatter;
import ai.foodscan.aggregate.db.exception.NoProductFoundException;
import ai.foodscan.aggregate.db.mapper.ProductEntityMapper;
import ai.foodscan.aggregate.db.mapper.ProductMapper;
import ai.foodscan.aggregate.db.model.api.Language;
import ai.foodscan.aggregate.db.model.api.Product;
import ai.foodscan.aggregate.db.model.db.entity.ProductEntity;
import ai.foodscan.aggregate.db.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

/**
 * Service class responsible for managing products, including creating,
 * updating, and retrieving product data. It also performs enrichment
 * (e.g., nutrition comparisons, fetch count increments, additive processing).
 */
@Slf4j
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final BarcodeService barcodeService;
    private final NutritionCalculator nutritionCalculator;
    private final AdditiveService additiveService;
    private final ProductFetchCountService fetchCountService;
    private final CalorieBurnService calorieBurnService;
    private final CategoryService categoryService;
    private final EanCheckService eanCheckService;
    private final ProductFormatter productFormatter;

    /**
     * Constructs the ProductService.
     *
     * @param productRepository    the repository for product data
     * @param barcodeService       the service used to decode barcodes from images
     * @param nutritionCalculator  the calculator for comparing nutrition data
     * @param additiveService      the service for processing product additives
     * @param fetchCountService    the service for incrementing fetch counts
     * @param calorieBurnService   the service for calculating calorie burn estimates
     */
    public ProductService(ProductRepository productRepository,
                          BarcodeService barcodeService,
                          NutritionCalculator nutritionCalculator,
                          AdditiveService additiveService,
                          ProductFetchCountService fetchCountService,
                          CalorieBurnService calorieBurnService,
                          CategoryService categoryService,
                          EanCheckService eanCheckService,
                          ProductFormatter productFormatter) {
        this.productRepository = productRepository;
        this.barcodeService = barcodeService;
        this.nutritionCalculator = nutritionCalculator;
        this.additiveService = additiveService;
        this.fetchCountService = fetchCountService;
        this.calorieBurnService = calorieBurnService;
        this.categoryService = categoryService;
        this.eanCheckService = eanCheckService;
        this.productFormatter = productFormatter;
    }

    /**
     * Saves or updates a product in the database and returns the enriched product.
     * <p>
     * If an existing product is found (based on either internalProductId or barcode),
     * that product is updated. Otherwise, a new entry is inserted.
     *
     * @param scrappedProduct the product to save or update
     * @return a {@link Mono} emitting the saved or updated (enriched) {@link Product}
     */
    public Mono<Product> saveOrUpdateProduct(Product scrappedProduct) {
        log.info("Attempting to save or update product with barcode: {}", scrappedProduct.getBarcode());
        return initialiseProduct(scrappedProduct)
                .flatMap(product -> {
                    if (product.getInternalProductId() != null) {
                        UUID internalProductId = product.getInternalProductId();
                        log.debug("Product has an internalProductId: {}", internalProductId);
                        return productRepository.findById(internalProductId)
                                .flatMap(existingEntity -> updateExistingProduct(existingEntity, product, internalProductId))
                                .switchIfEmpty(Mono.defer(() -> insertNewProduct(product, internalProductId)));
                    } else {
                        String barcode = product.getBarcode();
                        log.debug("No internalProductId provided; checking by barcode: {}", barcode);
                        return productRepository.findByBarcode(barcode, 1)
                                .flatMap(existingEntity -> updateExistingProductByBarcode(existingEntity, product, barcode))
                                .switchIfEmpty(Mono.defer(() -> insertNewProductByBarcode(product, barcode)));
                    }
                });
    }

    private Mono<Product> initialiseProduct(Product scrappedProduct){
        String barcode = scrappedProduct.getBarcode();
        if (barcode == null || barcode.trim().isEmpty()) {
            String generatedBarcode = "MISSING_BARCODE_" + UUID.randomUUID();
            log.warn("Barcode is null or empty. Generating temporary barcode: {}", generatedBarcode);
            Product productWithTempBarcode = scrappedProduct.toBuilder()
                    .barcode(generatedBarcode)
                    .build();
            return Mono.just(productWithTempBarcode);
        }

        if (!eanCheckService.isValidEan(barcode)) {
            log.warn("Invalid EAN barcode: {}", barcode);
            return Mono.error(new IllegalArgumentException("Invalid EAN barcode: " + barcode));
        }
        return Mono.just(scrappedProduct);
    }

    private Mono<Product> updateExistingProduct(ProductEntity existingEntity, Product product, UUID internalProductId) {
        log.info("Existing product found with internalProductId: {}. Updating.", internalProductId);
        ProductEntity updatedEntity = ProductEntityMapper.updateExistingProduct(existingEntity, product);
        return productRepository.save(updatedEntity)
                .map(ProductMapper::toProduct)
                .flatMap(this::enrichProduct)
                .doOnSuccess(updatedProduct -> log.info("Successfully updated product with internalProductId: {} and barcode: {}",
                        internalProductId, updatedProduct.getBarcode()))
                .doOnError(e -> log.error("Error updating product with internalProductId: {}. Message: {}",
                        internalProductId, e.getMessage(), e));
    }

    private Mono<Product> insertNewProduct(Product product, UUID internalProductId) {
        log.info("No existing product found with internalProductId: {}. Inserting new product.", internalProductId);
        ProductEntity newEntity = ProductMapper.toEntity(product);
        return productRepository.save(newEntity)
                .map(ProductMapper::toProduct)
                .flatMap(this::enrichProduct)
                .doOnSuccess(savedProduct -> log.info("New product inserted with internalProductId: {} and barcode: {}",
                        internalProductId, savedProduct.getBarcode()))
                .doOnError(e -> log.error("Error inserting product with internalProductId: {}. Message: {}",
                        internalProductId, e.getMessage(), e));
    }

    private Mono<Product> updateExistingProductByBarcode(ProductEntity existingEntity, Product product, String barcode) {
        log.info("Existing product found with barcode: {}. Updating.", barcode);
        ProductEntity updatedEntity = ProductEntityMapper.updateExistingProduct(existingEntity, product);
        return productRepository.save(updatedEntity)
                .map(ProductMapper::toProduct)
                .flatMap(this::enrichProduct)
                .doOnSuccess(updatedProduct -> log.info("Successfully updated product with barcode: {}", updatedProduct.getBarcode()))
                .doOnError(e -> log.error("Error updating product with barcode: {}. Message: {}", barcode, e.getMessage(), e));
    }

    private Mono<Product> insertNewProductByBarcode(Product product, String barcode) {
        log.info("No existing product found with barcode: {}. Inserting new product.", barcode);
        ProductEntity newEntity = ProductMapper.toEntity(product);
        return productRepository.save(newEntity)
                .map(ProductMapper::toProduct)
                .flatMap(this::enrichProduct)
                .doOnSuccess(savedProduct -> log.info("New product inserted with barcode: {}", savedProduct.getBarcode()))
                .doOnError(e -> log.error("Error inserting product with barcode: {}. Message: {}", barcode, e.getMessage(), e));
    }

    /**
     * Retrieves a product by its {@code internalProductId}, then enriches it.
     *
     * @param internalProductId the internal product ID
     * @return a {@link Mono} emitting the enriched {@link Product}
     * @throws NoProductFoundException if no product is found for the provided ID
     */
    public Mono<Product> getProductByInternalId(UUID internalProductId) {
        log.info("Fetching product with internalProductId: {}", internalProductId);
        return productRepository.findById(internalProductId)
                .map(ProductMapper::toProduct)
                .flatMap(this::enrichProduct)
                .switchIfEmpty(Mono.error(new NoProductFoundException(
                        "No product found with internalProductId: " + internalProductId)))
                .doOnSuccess(product ->
                        log.info("Successfully retrieved product with internalProductId: {} and barcode: {}",
                                internalProductId, product.getBarcode()))
                .doOnError(e ->
                        log.error("Failed to retrieve product with internalProductId: {}. Error: {}",
                                internalProductId, e.getMessage(), e));
    }

    /**
     * Decodes a barcode from an image and attempts to fetch the associated product,
     * then enrich it.
     *
     * @param imageBytes the image data as a byte array
     * @return a {@link Mono} emitting the enriched {@link Product}
     */
    public Mono<Product> getProductFromBarcodeImage(byte[] imageBytes) {
        log.info("Decoding barcode from provided image ({} bytes)", imageBytes.length);
        return barcodeService.decodeBarcodeFromImage(imageBytes)
                .flatMap(this::getProductByBarcode)
                .doOnError(e -> log.error("Failed to decode or retrieve product from image. Error: {}", e.getMessage(), e));
    }

    /**
     * Retrieves all unique barcodes from products in the repository.
     *
     * @return a {@link Flux} of barcodes as {@link String}
     */
    public Flux<String> getAllUniqueBarcodes() {
        log.info("Retrieving all unique barcodes from repository.");
        return productRepository.findAllBarcodes()
                .distinct()
                .doOnNext(barcode -> log.debug("Discovered barcode: {}", barcode))
                .switchIfEmpty(Flux.empty());
    }

    /**
     * Retrieves a product by barcode, then enriches it.
     *
     * @param barcode the product barcode
     * @return a {@link Mono} emitting the enriched {@link Product}
     * @throws NoProductFoundException if no product exists with that barcode
     */
    public Mono<Product> getProductByBarcode(String barcode) {
        log.debug("Fetching product by barcode: {}", barcode);
        return productRepository.findByBarcode(barcode, 1)
                .map(ProductMapper::toProduct)
                .flatMap(this::enrichProduct)
                // Return Mono.empty() if no product is found
                .switchIfEmpty(Mono.empty())
                .doOnSuccess(product -> {
                    if (product != null) {
                        log.info("Successfully fetched product by barcode: {} (internalProductId: {})",
                                barcode, product.getInternalProductId());
                    } else {
                        log.info("No product found with barcode: {}", barcode);
                    }
                })
                .doOnError(e ->
                        log.error("Error fetching product by barcode: {}. Message: {}",
                                barcode, e.getMessage(), e));
    }

    /**
     * Enriches a {@link Product} by applying:
     * <ul>
     *   <li>Nutrition comparisons (against category averages)</li>
     *   <li>Fetch count increments</li>
     *   <li>Calorie burn estimates</li>
     *   <li>Additive processing (both LT and EN)</li>
     * </ul>
     *
     * @param product the product to enrich
     * @return a {@link Mono} emitting the enriched {@link Product}
     */
    private Mono<Product> enrichProduct(Product product) {
        log.debug("Enriching product with internalProductId: {} and barcode: {}",
                product.getInternalProductId(), product.getBarcode());

        return Mono.just(product)
                .map(this.productFormatter::format)
                .flatMap(this::applyNutritionComparison)
                .flatMap(this::applyFetchCount)
                .flatMap(this::applyCalorieBurnEstimates)
                .flatMap(this::applyAdditives)
                .flatMap(enhancedProduct -> fetchSimilarOrRecommendedProducts(enhancedProduct, false))
                .flatMap(enhancedProduct -> fetchSimilarOrRecommendedProducts(enhancedProduct, true))
                .doOnSuccess(enriched ->
                        log.info("Successfully enriched product (internalProductId: {}, barcode: {})",
                                enriched.getInternalProductId(), enriched.getBarcode()))
                .doOnError(e ->
                        log.error("Error while enriching product (internalProductId: {}, barcode: {}). Message: {}",
                                product.getInternalProductId(), product.getBarcode(), e.getMessage(), e));
    }

    /**
     * Retrieves the average nutrition values for the product's category and
     * calculates how the product compares to those averages.
     *
     * @param product the product to compare
     * @return a {@link Mono} emitting the product with its nutrition comparison set
     */
    private Mono<Product> applyNutritionComparison(Product product) {
        log.debug("Applying nutrition comparison for product (internalProductId: {}, barcode: {})",
                product.getInternalProductId(), product.getBarcode());

        return productRepository
                .findNutritionalAveragesByCategory(
                        product.getMainCategoryEn(),
                        product.getSubCategoryEn(),
                        product.getSubSubCategoryEn())
                .map(avgNutrition -> {
                    log.trace("Found average nutrition data for product categories. Calculating comparison.");
                    return nutritionCalculator.calculateNutritionComparison(
                            product.getNutritionPer100g(), avgNutrition);
                })
                .map(nutritionComparison -> product.toBuilder()
                        .nutritionComparison(nutritionComparison)
                        .build())
                .defaultIfEmpty(product)
                .doOnError(e -> log.error("Error applying nutrition comparison: {}", e.getMessage(), e));
    }

    /**
     * Increments and retrieves the fetch count for the product, then sets it on the product.
     *
     * @param product the product to update
     * @return a {@link Mono} emitting the product with updated fetch count
     */
    private Mono<Product> applyFetchCount(Product product) {
        log.debug("Incrementing fetch count for product (internalProductId: {}, barcode: {})",
                product.getInternalProductId(), product.getBarcode());

        return fetchCountService.incrementFetchCount(product.getInternalProductId())
                .map(fetchCountEntity -> {
                    log.trace("Current fetch count from DB: {}", fetchCountEntity.getFetchCount());
                    return product.toBuilder()
                            .counter(fetchCountEntity.getFetchCount())
                            .build();
                })
                .defaultIfEmpty(product)
                .doOnError(e -> log.error("Error incrementing fetch count: {}", e.getMessage(), e));
    }

    /**
     * Calculates calorie burn estimates for the product, if {@code energyValueKj} is present.
     * Otherwise, sets an empty list of estimates.
     *
     * @param product the product to enrich with calorie burn estimates
     * @return a {@link Mono} emitting the updated product
     */
    private Mono<Product> applyCalorieBurnEstimates(Product product) {
        log.debug("Applying calorie burn estimates for product (internalProductId: {}, barcode: {})",
                product.getInternalProductId(), product.getBarcode());

        Long energyValueKj = (product.getNutritionPer100g() != null)
                ? product.getNutritionPer100g().getEnergyValueKj()
                : null;

        if (energyValueKj == null) {
            log.info("Skipping calorie burn estimates for product (internalProductId: {}) due to null energyValueKj",
                    product.getInternalProductId());
            return Mono.just(product.toBuilder().calorieBurnEstimates(List.of()).build());
        }

        log.trace("Calculating calorie burn estimates using energyValueKj: {}", energyValueKj);
        return calorieBurnService.calculateBurnEstimates(energyValueKj)
                .collectList()
                .map(burnEstimates -> {
                    log.trace("Calorie burn estimates calculated. Total estimates: {}", burnEstimates.size());
                    return product.toBuilder()
                            .calorieBurnEstimates(burnEstimates)
                            .build();
                })
                .defaultIfEmpty(product)
                .doOnError(e -> log.error("Error calculating calorie burn estimates: {}", e.getMessage(), e));
    }

    /**
     * Processes additives for both Lithuanian (LT) and English (EN) versions
     * by leveraging the {@link AdditiveService}.
     *
     * @param product the product whose additives will be processed
     * @return a {@link Mono} emitting the updated product
     */
    private Mono<Product> applyAdditives(Product product) {
        log.debug("Applying additives for product (internalProductId: {}, barcode: {})",
                product.getInternalProductId(), product.getBarcode());

        return additiveService.processAdditives(product.getAdditivesLt(), Language.LT)
                .zipWith(additiveService.processAdditives(product.getAdditivesEn(), Language.EN))
                .map(tuple -> {
                    log.trace("Additive processing complete for LT and EN versions");
                    return product.toBuilder()
                            .additivesLt(tuple.getT1())
                            .additivesEn(tuple.getT2())
                            .build();
                })
                .doOnError(e -> log.error("Error applying additives: {}", e.getMessage(), e));
    }

    /**
     * Fetches similar or recommended products and sets them on the given product.
     *
     * @param product the product to set similar products on
     * @return a {@link Mono} emitting the updated product with similar products
     */
    private Mono<Product> fetchSimilarOrRecommendedProducts(Product product, boolean recommended) {
        String mainCategory = product.getMainCategoryEn();
        String subCategory = product.getSubCategoryEn();
        String subSubCategory = product.getSubSubCategoryEn();

        return categoryService.getSimilarOrRecommendedProductsByEnCategories(mainCategory, subCategory, subSubCategory, recommended)
                .collectList()
                .map(products -> {
                    if (recommended) {
                        return product.toBuilder()
                                .recommendedProducts(products)
                                .build();
                    } else {
                        return product.toBuilder()
                                .similarProducts(products)
                                .build();
                    }
                })
                .doOnSuccess(updatedProduct -> log.debug("Setting {} products for product (internalProductId: {}, barcode: {})",
                        recommended ? "recommended" : "similar",
                        product.getInternalProductId(),
                        product.getBarcode()))
                .doOnError(e -> log.error("Error fetching {} products for product (internalProductId: {}, barcode: {}). Message: {}",
                        recommended ? "recommended" : "similar",
                        product.getInternalProductId(),
                        product.getBarcode(),
                        e.getMessage(), e));
    }

}
