package uk.dioxic.wfmt.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.dioxic.wfmt.model.Activity;
import uk.dioxic.wfmt.model.Order.OrderPk;
import uk.dioxic.wfmt.repository.fragments.ActivityCustomizedRepository;

import java.util.List;

@Repository
public interface ActivityRepository extends MongoRepository<Activity, String>, ActivityCustomizedRepository<Activity, String> {

    List<Activity> findByRegionId(Integer regionId);
    List<Activity> findByOrderPk(OrderPk orderPk);

}