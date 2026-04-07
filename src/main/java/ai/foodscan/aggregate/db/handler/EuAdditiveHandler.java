package ai.foodscan.aggregate.db.handler;

import ai.foodscan.aggregate.db.service.EuAdditiveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class EuAdditiveHandler {

    private final EuAdditiveService euAdditiveService;

    public EuAdditiveHandler(EuAdditiveService euAdditiveService) {
        this.euAdditiveService = euAdditiveService;
    }

    /**
     * GET /v1/eu-additives
     * Returns the full ordered list of all EU additives as minimal list items
     * (id, e_number, display names, is_group). ~412 entries.
     */
    public Mono<ServerResponse> getAllAsList(ServerRequest request) {
        log.info("Fetching all EU additives list");
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(euAdditiveService.getAllAsList(), Object.class);
    }

    /**
     * GET /v1/eu-additives/{eNumber}
     * Returns the full details of a single EU additive by E-number,
     * including all restrictions and legislations.
     */
    public Mono<ServerResponse> getByENumber(ServerRequest request) {
        String eNumber = request.pathVariable("eNumber");
        log.info("Fetching EU additive by e_number: {}", eNumber);

        return euAdditiveService.getByENumber(eNumber)
                .flatMap(additive ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(additive)
                )
                .switchIfEmpty(ServerResponse.notFound().build());
    }
}
