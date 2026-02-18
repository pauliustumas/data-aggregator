package ai.foodscan.aggregate.db.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpResponse;
import org.springframework.http.client.reactive.ClientHttpResponseDecorator;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;
import java.util.List;

class ClientResponseLoggingInterceptor extends ClientHttpResponseDecorator implements HeadersAndBodyMasker {

    private static final Logger logger = LoggerFactory.getLogger(ClientResponseLoggingInterceptor.class);
    private final List<String> headersToMask;
    private final List<String> bodyJsonFieldsToMask;

    public ClientResponseLoggingInterceptor(ClientHttpResponse delegate,
                                            List<String> headersToMask,
                                            List<String> bodyJsonFieldsToMask) {
        super(delegate);
        this.headersToMask = headersToMask;
        this.bodyJsonFieldsToMask = bodyJsonFieldsToMask;
        if (delegate.getHeaders().getContentLength() <= 0) {
            logger.info("\n Response:\n status={}\n headers={}",
                    delegate.getStatusCode(),
                    getMaskedHttpHeaders(delegate.getHeaders(), headersToMask));
        }
    }

    @Override
    public Flux<DataBuffer> getBody() {
        return super.getBody().doOnNext(dataBuffer -> {
            String bodyValue;
            if (MediaType.APPLICATION_JSON.equalsTypeAndSubtype(getDelegate().getHeaders().getContentType())) {
                String bodyRes = dataBuffer.toString(StandardCharsets.UTF_8);
                bodyValue = getMaskedJsonBody(bodyRes, bodyJsonFieldsToMask);
            } else {
                bodyValue = "*body of type " + getDelegate().getHeaders().getContentType() + " is not displayed*";
            }
            logger.info("\n Response:\n status={}\n headers={}\n payload={}",
                    getDelegate().getStatusCode(),
                    getMaskedHttpHeaders(getDelegate().getHeaders(), headersToMask),
                    getMaskedJsonBody(bodyValue, bodyJsonFieldsToMask));
        });
    }
}