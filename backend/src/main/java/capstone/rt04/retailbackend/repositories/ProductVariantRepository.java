package capstone.rt04.retailbackend.repositories;

import capstone.rt04.retailbackend.entities.ProductVariant;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductVariantRepository extends CrudRepository<ProductVariant, Long> {

    ProductVariant findBySKU(String sku);

    List<ProductVariant> findAllByProduct_ProductId(Long productId);

    List<ProductVariant> findAllByColourIn(List<String> colour);

}
