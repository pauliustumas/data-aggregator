package ai.foodscan.aggregate.db.mapper;

import ai.foodscan.aggregate.db.model.api.NutritionPer100g;
import ai.foodscan.aggregate.db.model.api.Product;
import ai.foodscan.aggregate.db.model.api.StorageConditions;
import ai.foodscan.aggregate.db.model.db.entity.ProductEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.postgresql.codec.Json;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ProductMapper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Map ProductEntity to Product (Database to API/Service Layer)
    public static Product toProduct(ProductEntity entity) {
        return Product.builder()
                .internalProductId(entity.getInternalProductId())
                .barcode(entity.getBarcode())
                .nameEn(entity.getNameEn())
                .nameLt(entity.getNameLt())
                .rawDescription(entity.getRawDescription())
                .imageUrl(entity.getImageUrl())
                .price(entity.getPrice()[entity.getPrice().length - 1])
                .originalPrice(entity.getOriginalPrice()[entity.getOriginalPrice().length - 1])
                .descriptionEn(entity.getDescriptionEn())
                .descriptionLt(entity.getDescriptionLt())
                .countryOfOriginLt(entity.getCountryOfOriginLt())
                .countryOfOriginEn(entity.getCountryOfOriginEn())
                .netWeightG(entity.getNetWeightG())
                .ingredientsLt(entity.getIngredientsLt())
                .ingredientsEn(entity.getIngredientsEn())
                .additivesLt(deserializeList(entity.getAdditivesLt(), new TypeReference<>() {
                }))
                .additivesEn(deserializeList(entity.getAdditivesEn(), new TypeReference<>() {
                }))
                .nutritionPer100g(deserializeObject(entity.getNutritionPer100g(), NutritionPer100g.class))
                .storageConditions(deserializeObject(entity.getStorageConditions(), StorageConditions.class))
                .packagingLt(entity.getPackagingLt())
                .packagingEn(entity.getPackagingEn())
                .brand(entity.getBrand())
                .manufacturerDescriptionEn(entity.getManufacturerDescriptionEn())
                .manufacturerDescriptionLt(entity.getManufacturerDescriptionLt())
                .manufacturerName(entity.getManufacturerName())
                .mainCategoryEn(entity.getMainCategoryEn())
                .subCategoryEn(entity.getSubCategoryEn())
                .subSubCategoryEn(entity.getSubSubCategoryEn())
                .mainCategoryLt(entity.getMainCategoryLt())
                .subCategoryLt(entity.getSubCategoryLt())
                .subSubCategoryLt(entity.getSubSubCategoryLt())
                .originalUrlEn(entity.getOriginalUrlEn())
                .originalUrlLt(entity.getOriginalUrlLt())
                .allergensEn(entity.getAllergensEn())
                .allergensLt(entity.getAllergensLt())
                .aiOpinionLt(entity.getAiOpinionLt())
                .aiOpinionEn(entity.getAiOpinionEn())
                .aiDatasource(entity.getAiDatasource())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .recommended(entity.isRecommended())
                .build();
    }

    // Map Product to ProductEntity (API/Service Layer to Database)
    public static ProductEntity toEntity(Product product) {
        return ProductEntity.builder()
                .internalProductId(product.getInternalProductId())
                .barcode(product.getBarcode())
                .nameEn(product.getNameEn())
                .nameLt(product.getNameLt())
                .rawDescription(product.getRawDescription())
                .imageUrl(product.getImageUrl())
                .price(new BigDecimal[] {product.getPrice()})
                .originalPrice(new BigDecimal[] {product.getOriginalPrice()})
                .descriptionEn(product.getDescriptionEn())
                .descriptionLt(product.getDescriptionLt())
                .countryOfOriginLt(product.getCountryOfOriginLt())
                .countryOfOriginEn(product.getCountryOfOriginEn())
                .netWeightG(product.getNetWeightG())
                .ingredientsLt(product.getIngredientsLt())
                .ingredientsEn(product.getIngredientsEn())
                .additivesLt(serializeObject(product.getAdditivesLt()))
                .additivesEn(serializeObject(product.getAdditivesEn()))
                .nutritionPer100g(serializeObject(product.getNutritionPer100g()))
                .storageConditions(serializeObject(product.getStorageConditions()))
                .packagingLt(product.getPackagingLt())
                .packagingEn(product.getPackagingEn())
                .brand(product.getBrand())
                .manufacturerDescriptionEn(product.getManufacturerDescriptionEn())
                .manufacturerDescriptionLt(product.getManufacturerDescriptionLt())
                .manufacturerName(product.getManufacturerName())
                .mainCategoryEn(product.getMainCategoryEn())
                .subCategoryEn(product.getSubCategoryEn())
                .subSubCategoryEn(product.getSubSubCategoryEn())
                .mainCategoryLt(product.getMainCategoryLt())
                .subCategoryLt(product.getSubCategoryLt())
                .subSubCategoryLt(product.getSubSubCategoryLt())
                .originalUrlEn(product.getOriginalUrlEn())
                .originalUrlLt(product.getOriginalUrlLt())
                .allergensEn(product.getAllergensEn())
                .allergensLt(product.getAllergensLt())
                .aiOpinionLt(product.getAiOpinionLt())
                .aiOpinionEn(product.getAiOpinionEn())
                .aiDatasource(product.getAiDatasource())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .recommended(product.getRecommended())
                .build();
    }

    public static <T> T deserializeObject(Json json, Class<T> clazz) {
        if (json == null) return null;
        try {
            return objectMapper.readValue(json.asString(), clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize JSON to " + clazz.getSimpleName(), e);
        }
    }

    private static <T> List<T> deserializeList(Json json, TypeReference<List<T>> typeReference) {
        if (json == null) {
            return Collections.emptyList();
        }
        try {
            // Deserialize JSON into List<T>
            List<T> list = objectMapper.readValue(json.asString(), typeReference);
            // Filter out null elements
            return list.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize JSON to List", e);
        }
    }

    public static Json serializeObject(Object object) {
        if (object == null) return null;
        try {
            return Json.of(objectMapper.writeValueAsString(object));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize object to JSON", e);
        }
    }
}
