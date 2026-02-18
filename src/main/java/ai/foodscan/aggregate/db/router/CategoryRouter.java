package ai.foodscan.aggregate.db.router;

import ai.foodscan.aggregate.db.handler.CategoryHandler;
import ai.foodscan.aggregate.db.model.api.CategoryDto;
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
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

/**
 * Router configuration for Category-related endpoints.
 */
@Configuration
public class CategoryRouter {

    @RouterOperations({
            // Existing getDistinctCategories operation
            @RouterOperation(
                    path = "/v1/categories",
                    method = org.springframework.web.bind.annotation.RequestMethod.GET,
                    beanClass = CategoryHandler.class,
                    beanMethod = "getDistinctCategories",
                    operation = @Operation(
                            method = "GET",
                            operationId = "getDistinctCategories",
                            tags = {"categories"},
                            description = "Retrieve distinct main categories, sub-categories, and sub-sub-categories based on the specified language.",
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Categories retrieved successfully.",
                                            content = @Content(schema = @Schema(type = "array", implementation = CategoryDto.class))
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "Invalid language parameter. Must be 'en' or 'lt'.",
                                            content = @Content
                                    ),
                                    @ApiResponse(
                                            responseCode = "404",
                                            description = "No categories found for the specified language.",
                                            content = @Content
                                    ),
                                    @ApiResponse(
                                            responseCode = "500",
                                            description = "Internal server error.",
                                            content = @Content
                                    )
                            }
                    )
            ),
            // New getMainCategories operation
            @RouterOperation(
                    path = "/v1/categories/main",
                    method = org.springframework.web.bind.annotation.RequestMethod.GET,
                    beanClass = CategoryHandler.class,
                    beanMethod = "getMainCategories",
                    operation = @Operation(
                            method = "GET",
                            operationId = "getMainCategories",
                            tags = {"categories"},
                            description = "Retrieve distinct main categories with names and slugs based on the specified language.",
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Main categories retrieved successfully.",
                                            content = @Content(schema = @Schema(type = "array", implementation = CategoryDto.class))
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "Invalid language parameter. Must be 'en' or 'lt'.",
                                            content = @Content
                                    ),
                                    @ApiResponse(
                                            responseCode = "404",
                                            description = "No main categories found for the specified language.",
                                            content = @Content
                                    ),
                                    @ApiResponse(
                                            responseCode = "500",
                                            description = "Internal server error.",
                                            content = @Content
                                    )
                            }
                    )
            ),
            // New getSubCategories operation
            @RouterOperation(
                    path = "/v1/categories/sub",
                    method = org.springframework.web.bind.annotation.RequestMethod.GET,
                    beanClass = CategoryHandler.class,
                    beanMethod = "getSubCategories",
                    operation = @Operation(
                            method = "GET",
                            operationId = "getSubCategories",
                            tags = {"categories"},
                            description = "Retrieve sub-categories with names and slugs based on the main category slug and specified language.",
                            parameters = {
                                    @Parameter(
                                            name = "mainCategorySlug",
                                            in = ParameterIn.QUERY,
                                            required = true,
                                            description = "Slug of the main category.",
                                            schema = @Schema(implementation = String.class)
                                    )
                            },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Sub-categories retrieved successfully.",
                                            content = @Content(schema = @Schema(type = "array", implementation = CategoryDto.class))
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "Invalid parameters.",
                                            content = @Content
                                    ),
                                    @ApiResponse(
                                            responseCode = "404",
                                            description = "No sub-categories found for the specified main category and language.",
                                            content = @Content
                                    ),
                                    @ApiResponse(
                                            responseCode = "500",
                                            description = "Internal server error.",
                                            content = @Content
                                    )
                            }
                    )
            ),
            // New getSubSubCategories operation
            @RouterOperation(
                    path = "/v1/categories/subsub",
                    method = org.springframework.web.bind.annotation.RequestMethod.GET,
                    beanClass = CategoryHandler.class,
                    beanMethod = "getSubSubCategories",
                    operation = @Operation(
                            method = "GET",
                            operationId = "getSubSubCategories",
                            tags = {"categories"},
                            description = "Retrieve sub-sub-categories with names and slugs based on the main and sub-category slugs and specified language.",
                            parameters = {
                                    @Parameter(
                                            name = "mainCategorySlug",
                                            in = ParameterIn.QUERY,
                                            required = true,
                                            description = "Slug of the main category.",
                                            schema = @Schema(implementation = String.class)
                                    ),
                                    @Parameter(
                                            name = "subCategorySlug",
                                            in = ParameterIn.QUERY,
                                            required = true,
                                            description = "Slug of the sub-category.",
                                            schema = @Schema(implementation = String.class)
                                    )
                            },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Sub-sub-categories retrieved successfully.",
                                            content = @Content(schema = @Schema(type = "array", implementation = CategoryDto.class))
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "Invalid parameters.",
                                            content = @Content
                                    ),
                                    @ApiResponse(
                                            responseCode = "404",
                                            description = "No sub-sub-categories found for the specified main and sub-category slugs and language.",
                                            content = @Content
                                    ),
                                    @ApiResponse(
                                            responseCode = "500",
                                            description = "Internal server error.",
                                            content = @Content
                                    )
                            }
                    )
            )
    })
    @Bean
    public RouterFunction<ServerResponse> categoryRoutes(CategoryHandler categoryHandler) {
        return route(GET("/v1/categories"), categoryHandler::getDistinctCategories)
                .andRoute(GET("/v1/categories/main"), categoryHandler::getMainCategories)
                .andRoute(GET("/v1/categories/sub"), categoryHandler::getSubCategories)
                .andRoute(GET("/v1/categories/subsub"), categoryHandler::getSubSubCategories);
    }
}
