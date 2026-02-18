package ai.foodscan.aggregate.db.handler;

import ai.foodscan.aggregate.db.exception.BarcodeDecodingException;
import ai.foodscan.aggregate.db.exception.InvalidValueException;
import ai.foodscan.aggregate.db.exception.MissingParameterException;

import java.util.function.Function;

import ai.foodscan.aggregate.db.exception.NoProductFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.api.ErrorMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

@Configuration
public class WebErrorHandler {
    private static final Logger logger = LoggerFactory.getLogger(WebErrorHandler.class);

    // Common logging method for exceptions.
    private void logError(Exception exception, ErrorMessage errorMessage) {
        logger.error("Exception occurred with error message id: {}. Details: {}",
                errorMessage.getId(), exception.getMessage(), exception);
    }

    private final Function<Exception, Mono<ServerResponse>> internalErrorHandler = (exception) ->
            Mono.just(new ErrorMessage("General error"))
                    .doOnNext(errorMessage -> logError(exception, errorMessage))
                    .flatMap(errorMessage -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(BodyInserters.fromValue(errorMessage)));

    private final Function<Exception, Mono<ServerResponse>> badRequestHandler = (exception) ->
            Mono.just(new ErrorMessage("Bad request"))
                    .doOnNext(errorMessage -> logError(exception, errorMessage))
                    .flatMap(errorMessage -> ServerResponse.status(HttpStatus.BAD_REQUEST)
                            .body(BodyInserters.fromValue(errorMessage)));

    private final Function<Exception, Mono<ServerResponse>> notFoundHandler = (exception) ->
            Mono.just(new ErrorMessage("Not found"))
                    .doOnNext(errorMessage -> logError(exception, errorMessage))
                    .flatMap(errorMessage -> ServerResponse.status(HttpStatus.NOT_FOUND)
                            .body(BodyInserters.fromValue(errorMessage)));

    public WebErrorHandler() {
    }

    @Bean({"errorHandler"})
    public Function<Throwable, Mono<ServerResponse>> errorHandler() {
        return (throwable) -> Mono.error(throwable)
                .flatMap((error) -> ServerResponse.ok().build())
                .onErrorResume(InvalidValueException.class, this.badRequestHandler)
                .onErrorResume(MissingParameterException.class, this.badRequestHandler)
                .onErrorResume(IllegalArgumentException.class, this.badRequestHandler)  // Maps IllegalArgumentException to BAD_REQUEST
                .onErrorResume(ServerWebInputException.class, this.badRequestHandler)
                .onErrorResume(NoProductFoundException.class, this.notFoundHandler)
                .onErrorResume(BarcodeDecodingException.class, this.badRequestHandler)
                .onErrorResume(Exception.class, this.internalErrorHandler);
    }
}
