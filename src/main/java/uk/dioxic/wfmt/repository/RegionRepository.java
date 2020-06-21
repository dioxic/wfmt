package uk.dioxic.wfmt.repository;

import uk.dioxic.wfmt.model.Region;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegionRepository extends MongoRepository<Region, Integer> {

}