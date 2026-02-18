package ai.foodscan.aggregate.db.integration.openfood;

import ai.foodscan.aggregate.db.model.api.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
@Slf4j
public class OpenFoodClient {
    private final WebClient webClient;
    private final String httpSchema;
    private final String host;
    private final String port;

    public OpenFoodClient(WebClient webClient, String httpSchema, String host, String port) {
        this.webClient = webClient;
        this.httpSchema = httpSchema;
        this.host = host;
        this.port = port;
    }

    public Mono<Product> findProductByBarcode(String barcode) {
        log.info("Looking for product in open food DB by barcode: {}", barcode);
        URI uri = UriComponentsBuilder.newInstance()
                .scheme(httpSchema)
                .host(host)
                .port(port)
                .path("/api/openfood/v1/openfood/translate/")
                .path(barcode)
                .build()
                .toUri();

        return webClient.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Product.class)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                        log.warn("Product not found for barcode: {}. Returning empty Mono.", barcode);
                        return Mono.empty();
                    }
                    // propagate other errors
                    return Mono.error(ex);
                });
    }
}
