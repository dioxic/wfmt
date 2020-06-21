package uk.dioxic.wfmt.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.dioxic.wfmt.model.User;

import java.util.List;

@Repository
public interface UserRepository extends MongoRepository<User, Integer> {

  List<User> findByFirstName(String firstName);
  List<User> findByLastName(String lastName);

//  interface RegionsOnly {
//    List<ObjectId> getRegions();
//  }

}