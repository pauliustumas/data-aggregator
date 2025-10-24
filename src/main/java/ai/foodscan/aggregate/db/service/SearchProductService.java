package ai.foodscan.aggregate.db.service;

import ai.foodscan.aggregate.db.enhancer.ProductFormatter;
import ai.foodscan.aggregate.db.mapper.MinimalProductMapper;
import ai.foodscan.aggregate.db.model.api.MinimalProduct;
import ai.foodscan.aggregate.db.model.api.Product;
import ai.foodscan.aggregate.db.model.db.entity.ProductEntity;
import ai.foodscan.aggregate.db.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
@Slf4j
@Service
public class SearchProductService {

    private final ProductRepository productRepository;
    private final ProductFormatter productFormatter;

    public SearchProductService(ProductRepository productRepository, ProductFormatter productFormatter) {
        this.productRepository = productRepository;
        this.productFormatter = productFormatter;
    }

    /**
     * Searches products by either barcode, name (LT or EN), or both, up to the given limit.
     *
     * @param barcode  the partial/complete barcode to search
     * @param name   the partial/complete name to search
     * @param limit    maximum number of results to return
     * @return a {@link Flux} of enriched {@link Product} objects
     * @throws IllegalArgumentException if neither barcode nor name is provided
     */
    public Flux<MinimalProduct> searchProducts(String barcode, String name, int limit) {
        boolean hasBarcode = barcode != null && !barcode.trim().isEmpty();
        boolean hasName = name != null && !name.trim().isEmpty();

        if (!hasBarcode && !hasName) {
            log.warn("No search parameters provided (both barcode and name are blank).");
            return Flux.error(new IllegalArgumentException(
                    "At least one query parameter (barcode or name) must be provided."));
        }

        Flux<ProductEntity> productEntityFlux;
        if (hasBarcode) {
            log.debug("Searching by barcode: '{}'", barcode);
            productEntityFlux = productRepository.findByBarcodeLike(barcode, limit);
        } else {
            log.debug("Searching by name: '{}'", name);
            productEntityFlux = productRepository.findByNameFullText(name, limit);
        }

        return productEntityFlux
                .map(MinimalProductMapper::toProduct)
                .map(productFormatter::format)
                .doOnComplete(() -> log.info("Completed search."))
                .doOnError(e -> log.error("Error during product search: {}", e.getMessage(), e))
                .switchIfEmpty(Flux.empty());  // Return empty flux if no matches
    }
}
