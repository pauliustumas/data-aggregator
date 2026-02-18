package ai.foodscan.aggregate.db.configuration;

import ai.foodscan.aggregate.db.filter.ClientLoggingFilterFunction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ClientLoggingConfig {

    @Value("${foodscan.http-client.log.headers-to-mask:}")
    private List<String> headersToMask;
    @Value("${foodscan.http-client.log.json-fields-to-mask:}")
    private List<String> bodyJsonFieldsToMask;

    @Bean
    public ClientLoggingFilterFunction clientLoggingFilterFunction() {
        return new ClientLoggingFilterFunction(headersToMask, bodyJsonFieldsToMask);
    }
}
