package ai.foodscan.aggregate.db.mapper;

import ai.foodscan.aggregate.db.model.api.MinimalProduct;
import ai.foodscan.aggregate.db.model.db.entity.ProductEntity;

public class MinimalProductMapper {

    private MinimalProductMapper() {
    }

    // Map ProductEntity to Product (Database to API/Service Layer)
    public static MinimalProduct toProduct(ProductEntity entity) {
        return MinimalProduct.builder()
                .internalProductId(entity.getInternalProductId())
                .barcode(entity.getBarcode())
                .nameEn(entity.getNameEn())
                .nameLt(entity.getNameLt())
                .imageUrl(entity.getImageUrl())
                .price(entity.getPrice()[entity.getPrice().length - 1])
                .mainCategoryEn(entity.getMainCategoryEn())
                .subCategoryEn(entity.getSubCategoryEn())
                .subSubCategoryEn(entity.getSubSubCategoryEn())
                .mainCategoryLt(entity.getMainCategoryLt())
                .subCategoryLt(entity.getSubCategoryLt())
                .subSubCategoryLt(entity.getSubSubCategoryLt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
