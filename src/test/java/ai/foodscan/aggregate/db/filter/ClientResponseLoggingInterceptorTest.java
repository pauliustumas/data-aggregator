package ai.foodscan.aggregate.db.filter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ai.foodscan.aggregate.db.utils.TestLogAppender;
import org.junit.jupiter.api.*;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.client.reactive.MockClientHttpResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClientResponseLoggingInterceptorTest {

    private static final String TEST_BODY = "test body";
    private final List<String> headersToMask = Collections.emptyList();
    private final List<String> bodyJsonFieldsToMask = Collections.emptyList();
    private final String expectedLoggMessageWithJsonBody =
            "\n Response:\n" +
                    " status=200 OK\n" +
                    " headers=[Content-Length:\"9\", Content-Type:\"application/json\"]\n" +
                    " payload=test body";
    private final String expectedLoggMessageWithTextBody =
            "\n Response:\n" +
                    " status=200 OK\n" +
                    " headers=[Content-Length:\"9\", Content-Type:\"text/plain\"]\n" +
                    " payload=*body of type text/plain is not displayed*";
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
        testLogAppender.reset();
    }

    @AfterAll
    void cleanUp() {
        testLogAppender.reset();
        testLogAppender.stop();
    }

    @Test
    void getBody_withJsonBodyInResponse_logsAndReturnsExpectedBody() {
        MockClientHttpResponse clientHttpResponse = new MockClientHttpResponse(HttpStatus.OK);
        clientHttpResponse.getHeaders().setContentLength(TEST_BODY.length());
        clientHttpResponse.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        clientHttpResponse.setBody(TEST_BODY);
        ClientResponseLoggingInterceptor interceptedResponse = new ClientResponseLoggingInterceptor(clientHttpResponse, headersToMask, bodyJsonFieldsToMask);

        StepVerifier.create(interceptedResponse.getBody()
                        .flatMap(dataBuffer -> Mono.just(dataBuffer.toString(Charset.defaultCharset()))))
                .expectNext(TEST_BODY)
                .verifyComplete();
        testLogAppender.list.forEach(action -> assertEquals(expectedLoggMessageWithJsonBody, action.getFormattedMessage()));
    }

    @Test
    void getBody_withTextBodyInResponse_logsAndReturnsExpectedBody() {
        MockClientHttpResponse clientHttpResponse = new MockClientHttpResponse(HttpStatus.OK);
        clientHttpResponse.getHeaders().setContentLength(TEST_BODY.length());
        clientHttpResponse.getHeaders().setContentType(MediaType.TEXT_PLAIN);
        clientHttpResponse.setBody(TEST_BODY);
        ClientResponseLoggingInterceptor interceptedResponse = new ClientResponseLoggingInterceptor(clientHttpResponse, headersToMask, bodyJsonFieldsToMask);

        StepVerifier.create(interceptedResponse.getBody()
                        .flatMap(dataBuffer -> Mono.just(dataBuffer.toString(Charset.defaultCharset()))))
                .expectNext(TEST_BODY)
                .verifyComplete();
        testLogAppender.list.forEach(action -> assertEquals(expectedLoggMessageWithTextBody, action.getFormattedMessage()));
    }
}