package ai.foodscan.aggregate.db.router;

import ai.foodscan.aggregate.db.handler.ProductHandler;
import ai.foodscan.aggregate.db.model.api.Product;
import ai.foodscan.aggregate.db.model.api.ProductsByCategoryResponse;
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

import java.util.UUID;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class ProductRouter {

    @RouterOperations({
            // Internal ID Based Endpoints
            @RouterOperation(
                    path = "/v1/products/internal/{id}",
                    method = RequestMethod.GET,
                    beanClass = ProductHandler.class,
                    beanMethod = "getProductByInternalId",
                    operation = @Operation(
                            method = "GET",
                            operationId = "getProductByInternalId",
                            tags = {"products"},
                            description = "Get a product by internal_product_id.",
                            parameters = @Parameter(name = "id", in = ParameterIn.PATH, required = true, schema = @Schema(implementation = UUID.class)),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Product retrieved successfully.",
                                            content = @Content(schema = @Schema(implementation = Product.class))),
                                    @ApiResponse(responseCode = "404", description = "Product not found.")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/v1/products/barcode/{id}",
                    method = RequestMethod.GET,
                    beanClass = ProductHandler.class,
                    beanMethod = "getByBarcode",
                    operation = @Operation(
                            method = "GET",
                            operationId = "getProductByBarcode",
                            tags = {"products"},
                            description = "Get a product by its barcode.",
                            parameters = {
                                    @Parameter(name = "main", in = ParameterIn.QUERY, required = false, schema = @Schema(implementation = String.class)),
                                    @Parameter(name = "lang", in = ParameterIn.QUERY, required = false, schema = @Schema(implementation = String.class)),
                                    @Parameter(name = "id", in = ParameterIn.PATH, required = true, description = "Barcode value", schema = @Schema(implementation = String.class)),
                            },
                             responses = {
                                    @ApiResponse(responseCode = "200", description = "Product retrieved successfully.",
                                            content = @Content(schema = @Schema(implementation = Product.class))),
                                    @ApiResponse(responseCode = "404", description = "Product not found.")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/v1/products",
                    method = RequestMethod.PUT,
                    beanClass = ProductHandler.class,
                    beanMethod = "saveOrUpdateProduct",
                    operation = @Operation(
                            method = "PUT",
                            operationId = "saveOrUpdateProduct",
                            tags = {"products"},
                            description = "Insert a new product or update an existing product based on the presence of 'id' query parameter.",
                            parameters = {
                                    @Parameter(name = "id", in = ParameterIn.QUERY, required = false, schema = @Schema(implementation = UUID.class))
                            },
                            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                    content = @Content(schema = @Schema(implementation = Product.class))),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Product saved or updated successfully."),
                                    @ApiResponse(responseCode = "400", description = "Invalid product data.")
                            }
                    )
            ),
            // Search and Barcodes Routes
            @RouterOperation(
                    path = "/v1/products/search",
                    method = RequestMethod.GET,
                    beanClass = ProductHandler.class,
                    beanMethod = "searchProducts",
                    operation = @Operation(
                            method = "GET",
                            operationId = "searchProducts",
                            tags = {"products"},
                            description = "Search products by barcode or name with a maximum of 5 results.",
                            parameters = {
                                    @Parameter(name = "barcode", in = ParameterIn.QUERY, required = false, schema = @Schema(implementation = String.class)),
                                    @Parameter(name = "name_en", in = ParameterIn.QUERY, required = false, schema = @Schema(implementation = String.class)),
                                    @Parameter(name = "name_lt", in = ParameterIn.QUERY, required = false, schema = @Schema(implementation = String.class)),
                                    @Parameter(name = "limit", in = ParameterIn.QUERY, required = false, description = "Maximum number of products to return. Defaults to 5.", schema = @Schema(type = "integer", defaultValue = "5", maximum = "5"))
                            },
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Products retrieved successfully.",
                                            content = @Content(schema = @Schema(implementation = Product.class, type = "array"))),
                                    @ApiResponse(responseCode = "400", description = "Invalid search parameters.")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/v1/products/barcodes",
                    method = RequestMethod.GET,
                    beanClass = ProductHandler.class,
                    beanMethod = "getAllUniqueBarcodes",
                    operation = @Operation(
                            method = "GET",
                            operationId = "getAllUniqueBarcodes",
                            tags = {"products"},
                            description = "Retrieve all unique product barcodes.",
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Barcodes retrieved successfully.",
                                            content = @Content(schema = @Schema(implementation = String.class, type = "array"))),
                                    @ApiResponse(responseCode = "500", description = "Internal server error.")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/v1/products/barcode-scan",
                    method = RequestMethod.POST,
                    beanClass = ProductHandler.class,
                    beanMethod = "scanBarcodeAndReturnProduct",
                    operation = @Operation(
                            method = "POST",
                            operationId = "scanBarcodeAndReturnProduct",
                            tags = {"products"},
                            description = "Scan an image for a barcode and return the associated product.",
                            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                    content = @Content(mediaType = "multipart/form-data",
                                            schema = @Schema(type = "object",
                                                    description = "Image file containing the barcode")))
                            ,
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Product retrieved successfully.",
                                            content = @Content(schema = @Schema(implementation = Product.class))),
                                    @ApiResponse(responseCode = "400", description = "Invalid image or barcode not found.")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/v1/products/by-category",
                    method = org.springframework.web.bind.annotation.RequestMethod.GET,
                    beanClass = ProductHandler.class,
                    beanMethod = "getProductsByCategory",
                    operation = @Operation(
                            method = "GET",
                            operationId = "getProductsByCategory",
                            tags = {"products"},
                            description = "Retrieve paged products by category. Provide mainCategory and optionally subCategory and subSubCategory along with language.",
                            parameters = {
                                    @Parameter(
                                            name = "lang",
                                            in = ParameterIn.QUERY,
                                            required = true,
                                            description = "Language code: 'en' for English or 'lt' for Lithuanian.",
                                            schema = @Schema(implementation = String.class)
                                    ),
                                    @Parameter(
                                            name = "mainCategory",
                                            in = ParameterIn.QUERY,
                                            required = true,
                                            description = "Main category name.",
                                            schema = @Schema(implementation = String.class)
                                    ),
                                    @Parameter(
                                            name = "subCategory",
                                            in = ParameterIn.QUERY,
                                            required = false,
                                            description = "Sub-category name. Optional when mainCategory is provided.",
                                            schema = @Schema(implementation = String.class)
                                    ),
                                    @Parameter(
                                            name = "subSubCategory",
                                            in = ParameterIn.QUERY,
                                            required = false,
                                            description = "Sub-sub-category name. Optional when subCategory is provided.",
                                            schema = @Schema(implementation = String.class)
                                    ),
                                    @Parameter(
                                            name = "page",
                                            in = ParameterIn.QUERY,
                                            required = false,
                                            description = "Page number (0-based). Defaults to 0.",
                                            schema = @Schema(implementation = Integer.class, defaultValue = "0")
                                    ),
                                    @Parameter(
                                            name = "size",
                                            in = ParameterIn.QUERY,
                                            required = false,
                                            description = "Page size. Defaults to 20.",
                                            schema = @Schema(implementation = Integer.class, defaultValue = "20")
                                    )
                            },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Products retrieved successfully.",
                                            content = @Content(schema = @Schema(implementation = ProductsByCategoryResponse.class))
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "Invalid parameters.",
                                            content = @Content
                                    ),
                                    @ApiResponse(
                                            responseCode = "404",
                                            description = "No products found for the specified category.",
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
            @RouterOperation(
                    path = "/v1/products/top-searched",
                    method = RequestMethod.GET,
                    beanClass = ProductHandler.class,
                    beanMethod = "getTopSearchedProducts",
                    operation = @Operation(
                            operationId = "getTopSearchedProducts",
                            description = "Get a paginated list of top searched products.",
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Top searched products returned successfully."),
                                    @ApiResponse(responseCode = "400", description = "Invalid pagination parameters.")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/v1/products/recent-searched",
                    method = RequestMethod.GET,
                    beanClass = ProductHandler.class,
                    beanMethod = "getRecentSearchedProducts",
                    operation = @Operation(
                            operationId = "getRecentSearchedProducts",
                            description = "Get a paginated list of recently searched products.",
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Recently searched products returned successfully."),
                                    @ApiResponse(responseCode = "400", description = "Invalid pagination parameters.")
                            }
                    )
            )
    })
    @Bean
    public RouterFunction<ServerResponse> productRoutes(ProductHandler productHandler) {
        return route(POST("/v1/products/internal/"), productHandler::saveOrUpdateProductByOptionalInternalId)
                .andRoute(GET("/v1/products/internal/{id}"), productHandler::getProductByInternalId)
                .andRoute(GET("/v1/products/barcode/{id}"), productHandler::getProductByBarcode)
                .andRoute(GET("/v1/products/search"), productHandler::searchProducts)
                .andRoute(GET("/v1/products/barcodes"), productHandler::getAllUniqueBarcodes)
                .andRoute(POST("/v1/products/barcode-scan"), productHandler::scanBarcodeAndReturnProduct)
                .andRoute(GET("/v1/products/by-category"), productHandler::getProductsByCategory)
                .andRoute(GET("/v1/products/top-searched"), productHandler::getTopSearchedProducts)
                .andRoute(GET("/v1/products/recent-searched"), productHandler::getRecentSearchedProducts);
    }
}
