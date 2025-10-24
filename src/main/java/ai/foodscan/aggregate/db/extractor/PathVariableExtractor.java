package ai.foodscan.aggregate.db.extractor;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.Optional;

@Component
public class PathVariableExtractor {

    public PathVariableExtractor() {
    }

    public Optional<String> getPathValue(ServerRequest serverRequest, String paramName) {
        try {
            return Optional.of(serverRequest.pathVariable(paramName));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}