package ai.foodscan.aggregate.db.router;

import ai.foodscan.aggregate.db.handler.AdditiveHandler;
import ai.foodscan.aggregate.db.model.api.AdditiveRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class AdditiveRouter {

    @RouterOperations({
            @RouterOperation(
                    path = "/v1/additives/{code}",
                    method = RequestMethod.GET,
                    beanClass = AdditiveHandler.class,
                    beanMethod = "getAdditiveByShortCode",
                    operation = @Operation(
                            method = "GET",
                            operationId = "getAdditiveByShortCode",
                            tags = {"additives"},
                            description = "Get an additive by its short code. Returns all additive fields (both Lithuanian and English).",
                            parameters = {
                                    @Parameter(
                                            name = "code",
                                            in = ParameterIn.PATH,
                                            required = true,
                                            schema = @Schema(implementation = String.class),
                                            description = "The additive short code"
                                    )
                            },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Additive retrieved successfully",
                                            content = @Content(schema = @Schema(implementation = AdditiveRecord.class))
                                    ),
                                    @ApiResponse(responseCode = "404", description = "Additive not found")
                            }
                    )
            )
    })
    @Bean
    public RouterFunction<ServerResponse> additiveRoutes(AdditiveHandler additiveHandler) {
        return route(GET("/v1/additives/{code}"), additiveHandler::getAdditiveByShortCode);
    }
}
