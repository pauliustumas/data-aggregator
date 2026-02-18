package ai.foodscan.aggregate.db.filter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ai.foodscan.aggregate.db.utils.TestLogAppender;
import org.junit.jupiter.api.*;
import org.slf4j.LoggerFactory;
import org.springframework.core.codec.CharSequenceEncoder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.http.codec.EncoderHttpMessageWriter;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.mock.http.client.reactive.MockClientHttpRequest;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClientRequestLoggingInterceptorTest {

    private static final URI DEFAULT_URL = URI.create("https://test");
    private static final String TEST_BODY = "test body";
    private final List<String> headersToMask = Collections.emptyList();
    private final List<String> bodyJsonFieldsToMask = Collections.emptyList();
    private final String expectedLoggMessageWithoutBody =
            "\n Request:\n" +
                    " method=GET\n" +
                    " uri=https://test\n" +
                    " headers=[Content-Length:\"0\"]";
    private final String expectedLoggMessageWithBody =
            "\n Request:\n" +
                    " method=POST\n" +
                    " uri=https://test\n" +
                    " headers=[]\n" +
                    " payload=*body of type null is not displayed*";
    private ExchangeStrategies strategies;
    private TestLogAppender testLogAppender;


    @BeforeAll
    void setUp() {
        Logger logger = (Logger) LoggerFactory.getLogger("com.example.VariousSpringWebFluxTestings.filter");
        testLogAppender = new TestLogAppender();
        testLogAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        logger.setLevel(Level.INFO);
        logger.addAppender(testLogAppender);
        testLogAppender.start();
    }

    @BeforeEach
    void reset() {
        List<HttpMessageWriter<?>> messageWriters = new ArrayList<>();
        messageWriters.add(new EncoderHttpMessageWriter<>(CharSequenceEncoder.allMimeTypes()));

        strategies = mock(ExchangeStrategies.class);
        when(strategies.messageWriters()).thenReturn(messageWriters);

        testLogAppender.reset();
    }

    @AfterAll
    void cleanUp() {
        testLogAppender.reset();
        testLogAppender.stop();
    }

    @Test
    void writeWith_withBody_getAndLogsExpectedBody() {
        BodyInserter<String, ClientHttpRequest> inserter = (response, strategies) -> {
            byte[] bodyBytes = TEST_BODY.getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = DefaultDataBufferFactory.sharedInstance.wrap(bodyBytes);
            return response.writeWith(Mono.just(buffer));
        };

        ClientRequest initialRequest = ClientRequest.create(HttpMethod.POST, DEFAULT_URL).body(inserter).build();

        MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.POST, DEFAULT_URL);
        ClientRequestLoggingInterceptor interceptedRequest = new ClientRequestLoggingInterceptor(request, headersToMask, bodyJsonFieldsToMask);
        initialRequest.writeTo(interceptedRequest, strategies).block();

        StepVerifier.create(request.getBodyAsString())
                .expectNext(TEST_BODY)
                .verifyComplete();

        testLogAppender.list.forEach(action -> assertEquals(expectedLoggMessageWithBody, action.getFormattedMessage()));
    }

    @Test
    void writeWith_withNoBody_getAndLogsNoBody() {
        ClientRequest initialRequest = ClientRequest.create(HttpMethod.GET, DEFAULT_URL).build();

        MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.GET, DEFAULT_URL);
        request.getHeaders().setContentLength(0L);
        ClientRequestLoggingInterceptor interceptedRequest = new ClientRequestLoggingInterceptor(request, headersToMask, bodyJsonFieldsToMask);
        initialRequest.writeTo(interceptedRequest, strategies).block();

        assertEquals(0, request.getHeaders().getContentLength());

        testLogAppender.list.forEach(action -> assertEquals(expectedLoggMessageWithoutBody, action.getFormattedMessage()));
    }
}
