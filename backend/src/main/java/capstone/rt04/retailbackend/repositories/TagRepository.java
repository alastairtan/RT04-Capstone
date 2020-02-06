package capstone.rt04.retailbackend.repositories;

import capstone.rt04.retailbackend.entities.Customer;
import capstone.rt04.retailbackend.entities.Tag;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends CrudRepository<Tag, Long> {

}
