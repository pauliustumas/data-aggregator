package ai.foodscan.aggregate.db.enhancer;

import ai.foodscan.aggregate.db.model.api.MinimalProduct;
import ai.foodscan.aggregate.db.model.api.Product;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProductFormatter {

    private final String imageBasePath;

    public ProductFormatter(@Value("${image.base.url}") String imageBasePath) {
        this.imageBasePath = imageBasePath;
    }

    public MinimalProduct format(MinimalProduct product) {
        String imageUrl = hasOriginalImage(product.getImageUrl())
                ? product.getImageUrl()
                : imageBasePath + product.getInternalProductId() + ".png";
        return product.toBuilder()
                .imageUrl(imageUrl)
                .build();
    }

    public Product format(Product product) {
        String imageUrl = hasOriginalImage(product.getImageUrl())
                ? product.getImageUrl()
                : imageBasePath + product.getInternalProductId() + ".png";
        return product.toBuilder()
                .imageUrl(imageUrl)
                .build();
    }

    private boolean hasOriginalImage(String url) {
        return url != null && !url.isBlank() && url.startsWith("http") && !url.contains("foodscan.ai");
    }
}
