package uk.dioxic.wfmt.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.dioxic.wfmt.model.Region;

@Repository
public interface RegionRepository extends MongoRepository<Region, Integer> {

}