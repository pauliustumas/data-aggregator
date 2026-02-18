package ai.foodscan.aggregate.db.utils;

import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;

import java.util.List;

public class DummyServerResponseContext implements ServerResponse.Context {

    @Override
    public List<HttpMessageWriter<?>> messageWriters() {
        return HandlerStrategies.withDefaults().messageWriters();
    }

    @Override
    public List<ViewResolver> viewResolvers() {
        return HandlerStrategies.withDefaults().viewResolvers();
    }

}