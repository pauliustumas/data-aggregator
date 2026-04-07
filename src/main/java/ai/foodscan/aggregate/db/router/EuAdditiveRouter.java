package ai.foodscan.aggregate.db.router;

import ai.foodscan.aggregate.db.handler.EuAdditiveHandler;
import ai.foodscan.aggregate.db.model.api.EuAdditive;
import ai.foodscan.aggregate.db.model.api.EuAdditiveListItem;
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
public class EuAdditiveRouter {

    @RouterOperations({
            @RouterOperation(
                    path = "/v1/eu-additives",
                    method = RequestMethod.GET,
                    beanClass = EuAdditiveHandler.class,
                    beanMethod = "getAllAsList",
                    operation = @Operation(
                            method = "GET",
                            operationId = "getAllEuAdditives",
                            tags = {"eu-additives"},
                            description = "Get the full list of all EU food additives (412 entries) " +
                                    "as minimal list items, ordered by E-number. " +
                                    "Source: European Commission Food and Feed Information Portal.",
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "List of EU additives retrieved successfully",
                                            content = @Content(schema = @Schema(implementation = EuAdditiveListItem.class, type = "array"))
                                    )
                            }
                    )
            ),
            @RouterOperation(
                    path = "/v1/eu-additives/{eNumber}",
                    method = RequestMethod.GET,
                    beanClass = EuAdditiveHandler.class,
                    beanMethod = "getByENumber",
                    operation = @Operation(
                            method = "GET",
                            operationId = "getEuAdditiveByENumber",
                            tags = {"eu-additives"},
                            description = "Get a single EU food additive by its E-number, including all food category " +
                                    "restrictions, legislations, and bilingual names (English + Lithuanian). " +
                                    "Lookup is case-insensitive (E586, e586, E 586 all work).",
                            parameters = {
                                    @Parameter(
                                            name = "eNumber",
                                            in = ParameterIn.PATH,
                                            required = true,
                                            schema = @Schema(implementation = String.class),
                                            description = "The E-number, e.g. E300, E586"
                                    )
                            },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "EU additive retrieved successfully",
                                            content = @Content(schema = @Schema(implementation = EuAdditive.class))
                                    ),
                                    @ApiResponse(responseCode = "404", description = "EU additive not found")
                            }
                    )
            )
    })
    @Bean
    public RouterFunction<ServerResponse> euAdditiveRoutes(EuAdditiveHandler euAdditiveHandler) {
        return route(GET("/v1/eu-additives"), euAdditiveHandler::getAllAsList)
                .andRoute(GET("/v1/eu-additives/{eNumber}"), euAdditiveHandler::getByENumber);
    }
}
