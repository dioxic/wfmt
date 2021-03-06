package uk.dioxic.wfmt.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.dioxic.wfmt.model.Order;
import uk.dioxic.wfmt.repository.fragments.OrderCustomizedRepository;

import java.util.List;

@Repository
public interface OrderRepository extends MongoRepository<Order, Order.OrderPk>, OrderCustomizedRepository<Order, Order.OrderPk> {

    List<Order> findByActivitiesRegionId(Integer regionId);
}