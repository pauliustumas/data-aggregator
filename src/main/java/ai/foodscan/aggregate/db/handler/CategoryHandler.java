package ai.foodscan.aggregate.db.handler;

import ai.foodscan.aggregate.db.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * Handler for Category-related HTTP requests.
 */
@Component
public class CategoryHandler {

    private static final Logger logger = LoggerFactory.getLogger(CategoryHandler.class);

    private final CategoryService categoryService;
    private final Function<Throwable, Mono<ServerResponse>> errorHandler;

    public CategoryHandler(CategoryService categoryService,
                           Function<Throwable, Mono<ServerResponse>> errorHandler) {
        this.categoryService = categoryService;
        this.errorHandler = errorHandler;
    }

    /**
     * Retrieves distinct categories based on the specified language.
     *
     * @param request the ServerRequest containing the 'lang' query parameter.
     * @return ServerResponse with the list of distinct categories or an error message.
     */
    public Mono<ServerResponse> getDistinctCategories(ServerRequest request) {
        return categoryService.getDistinctCategories()
                .collectList()
                .flatMap(categories -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(categories))
                .onErrorResume(this.errorHandler)
                .doOnError(e -> logger.error("Error fetching distinct categories: {}", e.getMessage()));
    }

    /**
     * Retrieves distinct main categories based on the specified language.
     *
     * @param request the ServerRequest containing the 'lang' query parameter.
     * @return ServerResponse with the list of main categories or an error message.
     */
    public Mono<ServerResponse> getMainCategories(ServerRequest request) {
        return categoryService.getMainCategories()
                .collectList()
                .flatMap(mainCategories -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(mainCategories))
                .onErrorResume(this.errorHandler)
                .doOnError(e -> logger.error("Error fetching main categories: {}", e.getMessage()));
    }

    /**
     * Retrieves sub-categories based on the main category slug and specified language.
     *
     * @param request the ServerRequest containing the 'mainCategorySlug' and 'lang' query parameters.
     * @return ServerResponse with the list of sub-categories or an error message.
     */
    public Mono<ServerResponse> getSubCategories(ServerRequest request) {
        String mainCategorySlug = request.queryParam("mainCategorySlug").orElse(null);

        logger.info("Received request to fetch sub-categories with mainCategorySlug: {}}", mainCategorySlug);

        return categoryService.getSubCategories(mainCategorySlug)
                .collectList()
                .flatMap(subCategories -> {
                    if (subCategories.isEmpty()) {
                        logger.warn("No sub-categories found for mainCategorySlug: {}", mainCategorySlug);
                        return ServerResponse.notFound().build();
                    }
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(subCategories);
                })
                .onErrorResume(this.errorHandler)
                .doOnError(e -> logger.error("Error fetching sub-categories: {}", e.getMessage()));
    }

    /**
     * Retrieves sub-sub-categories based on the main and sub-category slugs and specified language.
     *
     * @param request the ServerRequest containing the 'mainCategorySlug', 'subCategorySlug', and 'lang' query parameters.
     * @return ServerResponse with the list of sub-sub-categories or an error message.
     */
    public Mono<ServerResponse> getSubSubCategories(ServerRequest request) {
        String mainCategorySlug = request.queryParam("mainCategorySlug").orElse(null);
        String subCategorySlug = request.queryParam("subCategorySlug").orElse(null);

        logger.info("Received request to fetch sub-sub-categories with mainCategorySlug: {}, subCategorySlug: {}", mainCategorySlug, subCategorySlug);

        return categoryService.getSubSubCategories(mainCategorySlug, subCategorySlug)
                .collectList()
                .flatMap(subSubCategories -> {
                    if (subSubCategories.isEmpty()) {
                        logger.warn("No sub-sub-categories found for mainCategorySlug: {}, subCategorySlug: {}", mainCategorySlug, subCategorySlug);
                        return ServerResponse.notFound().build();
                    }
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(subSubCategories);
                })
                .onErrorResume(this.errorHandler)
                .doOnError(e -> logger.error("Error fetching sub-sub-categories: {}", e.getMessage()));
    }
}
