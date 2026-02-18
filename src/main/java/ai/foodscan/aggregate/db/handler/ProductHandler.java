package ai.foodscan.aggregate.db.handler;

import ai.foodscan.aggregate.db.exception.BarcodeDecodingException;
import ai.foodscan.aggregate.db.exception.MissingParameterException;
import ai.foodscan.aggregate.db.exception.NoProductFoundException;
import ai.foodscan.aggregate.db.extractor.PathVariableExtractor;
import ai.foodscan.aggregate.db.integration.openfood.OpenFoodClient;
import ai.foodscan.aggregate.db.model.api.Product;
import ai.foodscan.aggregate.db.service.CategoryService;
import ai.foodscan.aggregate.db.service.ProductService;
import ai.foodscan.aggregate.db.service.SearchProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.function.Function;

@Component
public class ProductHandler {

    private static final Logger logger = LoggerFactory.getLogger(ProductHandler.class);

    private final ProductService productService;
    private final CategoryService categoryService;
    private final PathVariableExtractor pathVariableExtractor;
    private final SearchProductService searchProductService;
    private final OpenFoodClient openFoodClient;
    private final Function<Throwable, Mono<ServerResponse>> errorHandler;

    public ProductHandler(ProductService productService,
                          CategoryService categoryService,
                          PathVariableExtractor pathVariableExtractor,
                          SearchProductService searchProductService,
                          OpenFoodClient openFoodClient,
                          Function<Throwable, Mono<ServerResponse>> errorHandler) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.pathVariableExtractor = pathVariableExtractor;
        this.searchProductService = searchProductService;
        this.openFoodClient = openFoodClient;
        this.errorHandler = errorHandler;
    }

    /**
     * Retrieves a product by its internal_product_id.
     *
     * @param request the ServerRequest containing the internal_product_id path variable.
     * @return ServerResponse with the product details or an error message.
     */
    public Mono<ServerResponse> getProductByInternalId(ServerRequest request) {
        return pathVariableExtractor.getPathValue(request, "id")
                .map(UUID::fromString)
                .map(productService::getProductByInternalId)
                .orElseGet(() -> Mono.error(new IllegalArgumentException("Missing or invalid internal_product_id parameter")))
                .flatMap(product -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(product))
                .onErrorResume(this.errorHandler)
                .doOnError(e -> logger.error("Error fetching product by internal_product_id: {}", e.getMessage()));
    }

    /**
     * Inserts a new product or updates an existing product based on the presence of 'id' request parameter.
     *
     * @param request the ServerRequest containing product details and optional 'id' request parameter.
     * @return ServerResponse indicating success or failure.
     */
    public Mono<ServerResponse> saveOrUpdateProductByOptionalInternalId(ServerRequest request) {
        Mono<Product> productMono = request.bodyToMono(Product.class);
        return productMono.flatMap(productService::saveOrUpdateProduct)
                          .flatMap(savedProduct -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(savedProduct))
                .onErrorResume(this.errorHandler)
                .doOnError(e -> logger.error("Error saving or updating product: {}", e.getMessage()));
    }

    /**
     * Retrieves a product by its barcode.
     *
     * @param request the ServerRequest containing the barcode in the path variable "id".
     * @return ServerResponse with the product details or a 404 response if not found.
     */
    public Mono<ServerResponse> getProductByBarcode(ServerRequest request) {
        String barcode = request.pathVariable("id");
        boolean onlyMainSearch = Boolean.parseBoolean(request.queryParam("main")
                .orElse("True"));
        logger.info("Fetching product by barcode: {}", barcode);
        return productService.getProductByBarcode(barcode)
                .flatMap(product -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(product))
                .switchIfEmpty(
                        onlyMainSearch ? ServerResponse.notFound().build() :
                                openFoodClient.findProductByBarcode(barcode)
                                .flatMap(product -> ServerResponse.ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(product))
                                .switchIfEmpty(ServerResponse.notFound().build())
                )
                .onErrorResume(e -> {
                    logger.error("Error fetching product by barcode: {}", e.getMessage());
                    return errorHandler.apply(e);
                });
    }

    /**
     * Searches for products by barcode (partial match) or name (partial match).
     *
     * @param request the ServerRequest containing query parameters.
     * @return ServerResponse with the list of matching products or an error message.
     */
    public Mono<ServerResponse> searchProducts(ServerRequest request) {
        String input = request.queryParam("name_lt")
                .orElse(request.queryParam("name_en")
                        .orElse(request.queryParam("input")
                                .orElse(request.queryParam("barcode")
                                        .orElse(request.queryParam("name")
                                                .orElse(null)))));
        // Extract 'limit' query parameter with default value 5
        int limit = request.queryParam("limit")
                .map(Integer::parseInt)
                .orElse(5);

        // Regex to check if input is number
        String barcode = null;
        String name = null;
        if (input != null) {
            if (input.matches("\\d+")) {
                barcode = input;
            } else {
                name = input;
            }
        }

        logger.info("Searching products with barcode: {}, name: {}, limit: {}", barcode, name, limit);
        return searchProductService.searchProducts(barcode, name, limit)
                .collectList()
                .flatMap(products -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(products))
                .onErrorResume(this.errorHandler)
                .doOnError(e -> logger.error("Error searching products: {}", e.getMessage()));
    }


    /**
     * Retrieves all unique barcodes from the products.
     *
     * @param request the ServerRequest.
     * @return ServerResponse with the list of unique barcodes or an error message.
     */
    public Mono<ServerResponse> getAllUniqueBarcodes(ServerRequest request) {
        return productService.getAllUniqueBarcodes()
                .collectList()
                .flatMap(barcodes -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(barcodes))
                .onErrorResume(this.errorHandler)
                .doOnError(e -> logger.error("Error retrieving barcodes: {}", e.getMessage()));
    }

    /**
     * Scans an uploaded image for a barcode and returns the associated product.
     *
     * @param request the ServerRequest containing the image file.
     * @return ServerResponse with the product details or an error message.
     */
    public Mono<ServerResponse> scanBarcodeAndReturnProduct(ServerRequest request) {
        return request.multipartData()
                .flatMap(parts -> {
                    // Expecting the image to be sent in a part named "image"
                    Part part = parts.toSingleValueMap().get("image");
                    if (part instanceof FilePart) {
                        FilePart filePart = (FilePart) part;
                        // Read all the file's DataBuffer(s) and join them
                        return DataBufferUtils.join(filePart.content())
                                .flatMap(dataBuffer -> {
                                    byte[] imageBytes = new byte[dataBuffer.readableByteCount()];
                                    dataBuffer.read(imageBytes);
                                    DataBufferUtils.release(dataBuffer);
                                    // Delegate to ProductService to process the image and retrieve the product
                                    return productService.getProductFromBarcodeImage(imageBytes);
                                });
                    } else {
                        return Mono.error(new MissingParameterException("Image file is required."));
                    }
                })
                .flatMap(product -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(product))
                .onErrorResume(this.errorHandler)
                .doOnError(e -> {
                    if (e instanceof BarcodeDecodingException) {
                        logger.error("Barcode decoding error: {}", e.getMessage());
                    } else if (e instanceof NoProductFoundException) {
                        logger.error("Product not found: {}", e.getMessage());
                    } else {
                        logger.error("Error scanning barcode: {}", e.getMessage());
                    }
                });
    }

    /**
     * Retrieves paged products by category.
     *
     * @param request the ServerRequest containing query parameters.
     * @return ServerResponse with the paged list of product IDs or an error message.
     */
    public Mono<ServerResponse> getProductsByCategory(ServerRequest request) {
        String language = request.queryParam("lang").orElse(null);
        String mainCategory = request.queryParam("mainCategory").orElse(null);
        String subCategory = request.queryParam("subCategory").orElse(null);
        String subSubCategory = request.queryParam("subSubCategory").orElse(null);

        String pageParam = request.queryParam("page").orElse("0");
        String sizeParam = request.queryParam("size").orElse("24");

        int page;
        int size;

        try {
            page = Integer.parseInt(pageParam);
            size = Integer.parseInt(sizeParam);
            if (page < 0 || size <= 0) {
                throw new NumberFormatException("Page must be non-negative and size must be positive.");
            }
        } catch (NumberFormatException e) {
            logger.warn("Invalid pagination parameters: page={}, size={}", pageParam, sizeParam);
            return ServerResponse.badRequest().bodyValue("Invalid pagination parameters. 'page' must be non-negative and 'size' must be positive integers (max 100).");
        }

        return categoryService.getProductsByCategory(mainCategory, subCategory, subSubCategory, language, page, size)
                .flatMap(pagedResponse -> {
                    if (pagedResponse.getProducts().isEmpty()) {
                        logger.warn("No products found for the specified category.");
                        return ServerResponse.notFound().build();
                    }
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(pagedResponse);
                })
                .onErrorResume(e -> {
                    if (e instanceof IllegalArgumentException) {
                        logger.error("Bad request: {}", e.getMessage());
                        return ServerResponse.badRequest().bodyValue(e.getMessage());
                    }
                    return errorHandler.apply(e);
                })
                .doOnError(e -> logger.error("Error fetching products by category: {}", e.getMessage()));
    }

    /**
     * Get Top Searched Products (paged).
     */
    public Mono<ServerResponse> getTopSearchedProducts(ServerRequest request) {
        String pageParam = request.queryParam("page").orElse("0");
        String sizeParam = request.queryParam("size").orElse("24");
        int page;
        int size;
        try {
            page = Integer.parseInt(pageParam);
            size = Integer.parseInt(sizeParam);
            if (page < 0 || size <= 0) {
                throw new NumberFormatException("Page must be non-negative and size must be positive.");
            }
        } catch (NumberFormatException e) {
            logger.warn("Invalid pagination parameters: page={}, size={}", pageParam, sizeParam);
            return ServerResponse.badRequest().bodyValue("Invalid pagination parameters. 'page' >= 0 and 'size' > 0 required.");
        }

        logger.info("Request for top searched products: page={}, size={}", page, size);

        return categoryService.getTopSearchedProducts(page, size)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(errorHandler)
                .doOnError(e -> logger.error("Error retrieving top searched products: {}", e.getMessage()));
    }

    /**
     * Get Recently Searched Products (paged).
     */
    public Mono<ServerResponse> getRecentSearchedProducts(ServerRequest request) {
        String pageParam = request.queryParam("page").orElse("0");
        String sizeParam = request.queryParam("size").orElse("24");
        int page;
        int size;
        try {
            page = Integer.parseInt(pageParam);
            size = Integer.parseInt(sizeParam);
            if (page < 0 || size <= 0) {
                throw new NumberFormatException("Page must be non-negative and size must be positive.");
            }
        } catch (NumberFormatException e) {
            logger.warn("Invalid pagination parameters: page={}, size={}", pageParam, sizeParam);
            return ServerResponse.badRequest().bodyValue("Invalid pagination parameters. 'page' >= 0 and 'size' > 0 required.");
        }

        logger.info("Request for recent searched products: page={}, size={}", page, size);

        return categoryService.getRecentSearchedProducts(page, size)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(errorHandler)
                .doOnError(e -> logger.error("Error retrieving recently searched products: {}", e.getMessage()));
    }
}
