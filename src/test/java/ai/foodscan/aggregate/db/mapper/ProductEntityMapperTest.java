package ai.foodscan.aggregate.db.mapper;

import ai.foodscan.aggregate.db.model.api.Product;
import ai.foodscan.aggregate.db.model.db.entity.ProductEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductEntityMapperTest {

    @Test
    void testUpdateWithNonNullAndNullFields() {
        // Create an existing entity with initial values.
        ProductEntity existing = new ProductEntity();
        existing.setNameEn("Old English Name");
        existing.setNameLt("Old Lithuanian Name");
        existing.setRawDescription("Old Raw Description");
        // Assume price is stored as an array of BigDecimal.
        existing.setPrice(new BigDecimal[]{new BigDecimal("10.00")});

        // Create a product update where:
        // - nameEn is provided,
        // - nameLt is null (so existing value should be kept),
        // - rawDescription is provided,
        // - price is provided.
        Product product = Product.builder().nameEn("New English Name").nameLt(null)  // missing value: keep the existing "Old Lithuanian Name"
                .rawDescription("New Raw Description").price(new BigDecimal("15.00")).build();

        ProductEntity updated = ProductEntityMapper.updateExistingProduct(existing, product);

        assertEquals("New English Name", updated.getNameEn());
        assertEquals("Old Lithuanian Name", updated.getNameLt());
        assertEquals("New Raw Description", updated.getRawDescription());

        BigDecimal[] expectedPrices = new BigDecimal[]{new BigDecimal("10.00"), new BigDecimal("15.00")};
        assertArrayEquals(expectedPrices, updated.getPrice());
    }

    @Test
    void testUpdateWithBlankString() {
        // Create an existing entity with a name.
        ProductEntity existing = new ProductEntity();
        existing.setNameEn("Existing Name");

        // Create a product update with a blank (only spaces) name.
        Product product = Product.builder().nameEn("   ")  // blank value should be ignored
                .build();

        ProductEntity updated = ProductEntityMapper.updateExistingProduct(existing, product);

        // Since the new name is blank, the existing name should remain.
        assertEquals("Existing Name", updated.getNameEn());
    }

    @Test
    void testUpdateAllergensNotOverwrittenIfEmpty() {
        // Existing entity has some allergens.
        ProductEntity existing = new ProductEntity();
        existing.setAllergensEn(Arrays.asList("allergen1", "allergen2"));

        // Incoming product has an empty allergens list.
        Product product = Product.builder().allergensEn(Collections.emptyList()).build();

        ProductEntity updated = ProductEntityMapper.updateExistingProduct(existing, product);

        // Since the incoming allergens list is empty, the existing allergens should be preserved.
        assertEquals(Arrays.asList("allergen1", "allergen2"), updated.getAllergensEn());
    }

    @Test
    void testNoUpdateWhenProductIsEmpty() {
        // Create an existing entity with data.
        ProductEntity existing = new ProductEntity();
        existing.setNameEn("Existing Name");
        existing.setNameLt("Existing LT");
        existing.setRawDescription("Existing Description");
        existing.setPrice(new BigDecimal[]{new BigDecimal("50.00")});
        existing.setAllergensEn(List.of("allergen1"));

        // Create an empty product update (all fields null).
        Product product = Product.builder().build();

        ProductEntity updated = ProductEntityMapper.updateExistingProduct(existing, product);

        // Verify that the existing data remains unchanged.
        assertEquals("Existing Name", updated.getNameEn());
        assertEquals("Existing LT", updated.getNameLt());
        assertEquals("Existing Description", updated.getRawDescription());
        BigDecimal[] expectedPrice = new BigDecimal[]{new BigDecimal("50.00")};
        assertArrayEquals(expectedPrice, updated.getPrice());
        assertEquals(List.of("allergen1"), updated.getAllergensEn());
    }
}
