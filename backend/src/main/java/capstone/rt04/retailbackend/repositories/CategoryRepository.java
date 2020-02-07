package capstone.rt04.retailbackend.repositories;

import capstone.rt04.retailbackend.entities.Category;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends CrudRepository<Category, Long> {
}
