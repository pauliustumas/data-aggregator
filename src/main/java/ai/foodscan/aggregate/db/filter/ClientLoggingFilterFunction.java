package ai.foodscan.aggregate.db.filter;

import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.http.client.reactive.ClientHttpResponse;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

import java.util.List;

public class ClientLoggingFilterFunction {

    private final List<String> headersToMask;
    private final List<String> bodyJsonFieldsToMask;

    public ClientLoggingFilterFunction(List<String> headersToMask,
                                       List<String> bodyJsonFieldsToMask) {
        this.headersToMask = headersToMask;
        this.bodyJsonFieldsToMask = bodyJsonFieldsToMask;
    }

    public ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            BodyInserter<?, ? super ClientHttpRequest> requestBodyInserter = clientRequest.body();
            return Mono.just(ClientRequest.from(clientRequest)
                    .body((clientHttpRequest, context) -> {
                        ClientHttpRequest interceptedRequest =
                                new ClientRequestLoggingInterceptor(clientHttpRequest, headersToMask, bodyJsonFieldsToMask);
                        return requestBodyInserter.insert(interceptedRequest, context);
                    }).build());
        });
    }

    public ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse ->
                clientResponse.body((clientHttpResponse, context) -> {
                    ClientHttpResponse interceptedResponse =
                            new ClientResponseLoggingInterceptor(clientHttpResponse, headersToMask, bodyJsonFieldsToMask);
                    return Mono.just(clientResponse.mutate()
                            .body(body -> interceptedResponse.getBody())
                            .build());
                }));
    }
}
