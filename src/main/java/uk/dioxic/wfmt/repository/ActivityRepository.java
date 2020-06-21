package uk.dioxic.wfmt.repository;

import uk.dioxic.wfmt.model.Activity;
import uk.dioxic.wfmt.repository.fragments.ActivityCustomizedRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityRepository extends MongoRepository<Activity, String>, ActivityCustomizedRepository<Activity, String> {

    List<Activity> findByRegionId(Integer regionId);
    List<Activity> findByOrderOrderId(Integer orderId);
    List<Activity> findByOrderOrderIdAndOrderCircuitId(Integer orderId, Integer circuitId);

}