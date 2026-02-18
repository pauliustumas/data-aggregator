package ai.foodscan.aggregate.db.handler;

import ai.foodscan.aggregate.db.model.api.Additive;
import ai.foodscan.aggregate.db.service.AdditiveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class AdditiveHandler {

    private static final Logger logger = LoggerFactory.getLogger(AdditiveHandler.class);
    private final AdditiveService additiveService;

    public AdditiveHandler(AdditiveService additiveService) {
        this.additiveService = additiveService;
    }

    /**
     * Retrieves an additive by its short code.
     *
     * Example endpoint:
     * GET /v1/additives/{code}
     *
     * @param request the ServerRequest containing the additive short code path variable.
     * @return ServerResponse with the additive details (both Lithuanian and English fields) or 404 if not found.
     */
    public Mono<ServerResponse> getAdditiveByShortCode(ServerRequest request) {
        String code = request.pathVariable("code");
        logger.info("Fetching additive with short code: {}", code);

        return additiveService.getAdditiveByCode(code)
                .flatMap(additive ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(additive)
                )
                .switchIfEmpty(ServerResponse.notFound().build());
    }
}
