package ai.foodscan.aggregate.db.mapper;

import ai.foodscan.aggregate.db.model.api.Product;
import ai.foodscan.aggregate.db.model.db.entity.ProductEntity;

import java.util.Arrays;

public class ProductEntityMapper {

    public static ProductEntity updateExistingProduct(ProductEntity existingEntity, Product product) {
        existingEntity.setNameEn(chooseString(product.getNameEn(), existingEntity.getNameEn()));
        existingEntity.setNameLt(chooseString(product.getNameLt(), existingEntity.getNameLt()));
        existingEntity.setRawDescription(chooseString(product.getRawDescription(), existingEntity.getRawDescription()));
        existingEntity.setImageUrl(chooseString(product.getImageUrl(), existingEntity.getImageUrl()));
        existingEntity.setDescriptionEn(chooseString(product.getDescriptionEn(), existingEntity.getDescriptionEn()));
        existingEntity.setDescriptionLt(chooseString(product.getDescriptionLt(), existingEntity.getDescriptionLt()));
        existingEntity.setCountryOfOriginLt(chooseString(product.getCountryOfOriginLt(), existingEntity.getCountryOfOriginLt()));
        existingEntity.setCountryOfOriginEn(chooseString(product.getCountryOfOriginEn(), existingEntity.getCountryOfOriginEn()));

        if (product.getNetWeightG() != null) {
            existingEntity.setNetWeightG(product.getNetWeightG());
        }
        existingEntity.setIngredientsLt(chooseString(product.getIngredientsLt(), existingEntity.getIngredientsLt()));
        existingEntity.setIngredientsEn(chooseString(product.getIngredientsEn(), existingEntity.getIngredientsEn()));

        // For serialized fields, update only if the new object is not null.
        if (product.getAdditivesLt() != null) {
            existingEntity.setAdditivesLt(ProductMapper.serializeObject(product.getAdditivesLt()));
        }
        if (product.getAdditivesEn() != null) {
            existingEntity.setAdditivesEn(ProductMapper.serializeObject(product.getAdditivesEn()));
        }
        if (product.getNutritionPer100g() != null) {
            existingEntity.setNutritionPer100g(ProductMapper.serializeObject(product.getNutritionPer100g()));
        }
        if (product.getStorageConditions() != null) {
            existingEntity.setStorageConditions(ProductMapper.serializeObject(product.getStorageConditions()));
        }

        existingEntity.setPackagingLt(chooseString(product.getPackagingLt(), existingEntity.getPackagingLt()));
        existingEntity.setPackagingEn(chooseString(product.getPackagingEn(), existingEntity.getPackagingEn()));

        // Update price history if the new price is present
        if (product.getPrice() != null) {
            existingEntity.setPrice(addElement(existingEntity.getPrice(), product.getPrice()));
        }
        if (product.getOriginalPrice() != null) {
            existingEntity.setOriginalPrice(addElement(existingEntity.getOriginalPrice(), product.getOriginalPrice()));
        }

        existingEntity.setBrand(chooseString(product.getBrand(), existingEntity.getBrand()));
        existingEntity.setManufacturerDescriptionLt(chooseString(product.getManufacturerDescriptionLt(), existingEntity.getManufacturerDescriptionLt()));
        existingEntity.setManufacturerDescriptionEn(chooseString(product.getManufacturerDescriptionEn(), existingEntity.getManufacturerDescriptionEn()));
        existingEntity.setManufacturerName(chooseString(product.getManufacturerName(), existingEntity.getManufacturerName()));
        existingEntity.setMainCategoryEn(chooseString(product.getMainCategoryEn(), existingEntity.getMainCategoryEn()));
        existingEntity.setSubCategoryEn(chooseString(product.getSubCategoryEn(), existingEntity.getSubCategoryEn()));
        existingEntity.setSubSubCategoryEn(chooseString(product.getSubSubCategoryEn(), existingEntity.getSubSubCategoryEn()));
        existingEntity.setMainCategoryLt(chooseString(product.getMainCategoryLt(), existingEntity.getMainCategoryLt()));
        existingEntity.setSubCategoryLt(chooseString(product.getSubCategoryLt(), existingEntity.getSubCategoryLt()));
        existingEntity.setSubSubCategoryLt(chooseString(product.getSubSubCategoryLt(), existingEntity.getSubSubCategoryLt()));
        existingEntity.setOriginalUrlEn(chooseString(product.getOriginalUrlEn(), existingEntity.getOriginalUrlEn()));
        existingEntity.setOriginalUrlLt(chooseString(product.getOriginalUrlLt(), existingEntity.getOriginalUrlLt()));
        existingEntity.setRecommended(chooseBoolean(product.getRecommended(), existingEntity.isRecommended()));

        // For list fields, update only if the incoming list is non-null and not empty.
        if (product.getAllergensEn() != null && !product.getAllergensEn().isEmpty()) {
            existingEntity.setAllergensEn(product.getAllergensEn());
        }
        if (product.getAllergensLt() != null && !product.getAllergensLt().isEmpty()) {
            existingEntity.setAllergensLt(product.getAllergensLt());
        }

        return existingEntity;
    }

    /**
     * Returns newValue if it is non-null and non-blank; otherwise returns the oldValue.
     */
    private static String chooseString(String newValue, String oldValue) {
        return (newValue != null && !newValue.trim().isEmpty()) ? newValue : oldValue;
    }

    /**
     * Returns {@code newValue} if it is non-null; otherwise returns {@code oldValue}.
     *
     * @param newValue the new Boolean value to check; may be {@code null}.
     * @param oldValue the fallback boolean value if {@code newValue} is {@code null}.
     * @return {@code newValue} if non-null, otherwise {@code oldValue}.
     */
    private static boolean chooseBoolean(Boolean newValue, boolean oldValue) {
        return newValue != null ? newValue : oldValue;
    }

    /**
     * Helper method to update a field that is stored as an array by adding a new element.
     * (This method remains unchanged from your implementation.)
     */
    private static <T> T[] addElement(T[] array, T element) {
        T[] newArray = Arrays.copyOf(array, array.length + 1);
        newArray[array.length] = element;
        return newArray;
    }
}
