package ai.foodscan.aggregate.db.handler;

import ai.foodscan.aggregate.db.model.api.ProductFilterRequest;
import ai.foodscan.aggregate.db.service.ProductFilterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Component
public class ProductFilterHandler {

    private static final Logger logger = LoggerFactory.getLogger(ProductFilterHandler.class);

    private final ProductFilterService productFilterService;
    private final Function<Throwable, Mono<ServerResponse>> errorHandler;

    public ProductFilterHandler(ProductFilterService productFilterService,
                                Function<Throwable, Mono<ServerResponse>> errorHandler) {
        this.productFilterService = productFilterService;
        this.errorHandler = errorHandler;
    }

    public Mono<ServerResponse> filterProducts(ServerRequest request) {
        return request.bodyToMono(ProductFilterRequest.class)
                .flatMap(productFilterService::filterProducts)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(errorHandler)
                .doOnError(e -> logger.error("Error filtering products: {}", e.getMessage()));
    }
}
