package ai.foodscan.aggregate.db.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.core.codec.CharSequenceEncoder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.EncoderHttpMessageWriter;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.mock.http.client.reactive.MockClientHttpRequest;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class ClientLoggingFilterFunctionTest {

    private static final URI DEFAULT_URL = URI.create("https://test");
    private static final String TEST_BODY = "test body";
    private final List<String> headersToMask = Collections.emptyList();
    private final List<String> bodyJsonFieldsToMask = Collections.emptyList();
    private final ClientLoggingFilterFunction clientLoggingFilterFunction = new ClientLoggingFilterFunction(headersToMask, bodyJsonFieldsToMask);
    private ClientRequest request;
    private ClientResponse response;

    @BeforeEach
    void setUp() {
        DataBuffer dataBuffer = DefaultDataBufferFactory.sharedInstance.wrap(TEST_BODY.getBytes(StandardCharsets.UTF_8));
        request = ClientRequest.create(HttpMethod.POST, DEFAULT_URL).body(Flux.just(TEST_BODY), String.class).build();
        response = ClientResponse.create(HttpStatus.OK).body(Flux.just(dataBuffer)).build();
    }

    @Test
    void logRequest_withBody_logsAndPassThruExpectedRequestBody() {
        List<HttpMessageWriter<?>> messageWriters = new ArrayList<>();
        messageWriters.add(new EncoderHttpMessageWriter<>(CharSequenceEncoder.allMimeTypes()));

        ExchangeStrategies strategies = mock(ExchangeStrategies.class);
        when(strategies.messageWriters()).thenReturn(messageWriters);

        ExchangeFunction exchange = r -> {
            MockClientHttpRequest consumingRequest = new MockClientHttpRequest(HttpMethod.POST, DEFAULT_URL);
            StepVerifier.create(r.writeTo(consumingRequest, strategies))
                    .verifyComplete();
            StepVerifier.create(consumingRequest.getBody().flatMap(buffer -> Mono.just(buffer.toString(Charset.defaultCharset()))))
                    .expectNext(TEST_BODY)
                    .verifyComplete();
            return Mono.just(response);
        };

        clientLoggingFilterFunction.logRequest().filter(request, exchange).block();
    }

    @Test
    void logResponse_withBody_logsAndPassThruExpectedResponseBody() {
        ExchangeFunction exchange = r -> Mono.just(response);

        ClientResponse resultResponse = clientLoggingFilterFunction.logResponse().filter(request, exchange).block();

        assert resultResponse != null;
        StepVerifier.create(resultResponse.bodyToMono(String.class))
                .expectNext(TEST_BODY)
                .verifyComplete();
    }
}