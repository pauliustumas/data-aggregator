package ai.foodscan.aggregate.db.filter;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.http.client.reactive.ClientHttpRequestDecorator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class ClientRequestLoggingInterceptor extends ClientHttpRequestDecorator implements HeadersAndBodyMasker {

    private static final Logger logger = LoggerFactory.getLogger(ClientRequestLoggingInterceptor.class);
    private final List<String> headersToMask;
    private final List<String> bodyJsonFieldsToMask;

    public ClientRequestLoggingInterceptor(ClientHttpRequest delegate,
                                           List<String> headersToMask,
                                           List<String> bodyJsonFieldsToMask) {
        super(delegate);
        this.headersToMask = headersToMask;
        this.bodyJsonFieldsToMask = bodyJsonFieldsToMask;
        if (HttpMethod.GET.equals(delegate.getMethod())) {
            logger.info("\n Request:\n method={}\n uri={}\n headers={}",
                    delegate.getMethod(),
                    delegate.getURI(),
                    getMaskedHttpHeaders(delegate.getHeaders(), headersToMask));
        }
    }

    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
        Flux<DataBuffer> buffer = Flux.from(body);
        return super.writeWith(buffer.doOnNext(dataBuffer -> {
            String bodyValue;
            if (MediaType.APPLICATION_JSON.equalsTypeAndSubtype(getDelegate().getHeaders().getContentType())) {
                String bodyReq = dataBuffer.toString(StandardCharsets.UTF_8);
                bodyValue = getMaskedJsonBody(bodyReq, bodyJsonFieldsToMask);
            } else {
                bodyValue = "*body of type " + getDelegate().getHeaders().getContentType() + " is not displayed*";
            }
            logger.info("\n Request:\n method={}\n uri={}\n headers={}\n payload={}",
                    getDelegate().getMethod(),
                    getDelegate().getURI(),
                    getMaskedHttpHeaders(getDelegate().getHeaders(), headersToMask),
                    getMaskedJsonBody(bodyValue, bodyJsonFieldsToMask));
        }));
    }
}
