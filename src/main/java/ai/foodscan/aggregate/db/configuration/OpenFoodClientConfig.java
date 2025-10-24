package ai.foodscan.aggregate.db.configuration;

import ai.foodscan.aggregate.db.integration.openfood.OpenFoodClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class OpenFoodClientConfig {

    @Value("${openfood.http-schema}")
    private String httpSchema;

    @Value("${openfood.host}")
    private String host;

    @Value("${openfood.port}")
    private String port;

    @Bean
    public OpenFoodClient openFoodClient(WebClient.Builder webClientBuilder) {
        WebClient webClient = webClientBuilder.build();
        return new OpenFoodClient(webClient, httpSchema, host, port);
    }
}
