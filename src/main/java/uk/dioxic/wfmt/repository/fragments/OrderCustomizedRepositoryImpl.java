package uk.dioxic.wfmt.repository.fragments;

import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.query.UpdateDefinition;
import uk.dioxic.wfmt.annotation.TransactionalWithRetry;
import uk.dioxic.wfmt.model.Activity;
import uk.dioxic.wfmt.model.ActivitySummary;
import uk.dioxic.wfmt.model.Order;
import uk.dioxic.wfmt.model.Order.OrderPk;
import uk.dioxic.wfmt.repository.ActivityRepository;
import uk.dioxic.wfmt.repository.OrderRepository;

import java.util.Collections;
import java.util.Objects;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static uk.dioxic.wfmt.util.MongoUtil.queryById;

public class OrderCustomizedRepositoryImpl implements OrderCustomizedRepository<Order, OrderPk> {

    private final Logger LOG = LogManager.getLogger();

    @Autowired
    private MongoOperations mongoOps;

    @Autowired
    private ActivityRepository activityRepository;

    @TransactionalWithRetry
    public <S extends Order> S save(@NonNull S order) {

        // save the order and return the previous version of the order
        Order prevOrder = mongoOps.findAndModify(
                includeOrderSummaryFields(queryById(order.getOrderPk())),
                getSaveUpdateDefinition(order),
                FindAndModifyOptions.options().upsert(true),
                Order.class
        );

        // update activities if summary fields have changed
        if (prevOrder != null && !summaryFieldsEqual(prevOrder, order)) {
            activityRepository.updateOrderSummary(order);
        }

        return order;
    }

    public Order addActivity(Activity activity) {
        if (activity != null && activity.getOrderPk() != null) {
            LOG.debug("adding activity {} to order {}", activity.getActivityId(), activity.getOrderPk());
            Query query = includeOrderSummaryFields(queryById(activity.getOrderPk()));
            Update addToSet = new Update().push("activities").value(new ActivitySummary(activity));

            Order order = mongoOps.findAndModify(query, addToSet, Order.class);

            if (order == null) {
                LOG.warn("order {} not found! this will lead to data inconsistencies", activity.getOrderPk());
            }

            return order;
        }
        return null;
    }

    public void removeActivity(Activity activity) {
        if (activity != null && activity.getOrderPk() != null) {
            removeActivity(activity, activity.getOrderPk());
        }
    }

    public void removeActivity(@NonNull Activity activity, @NonNull OrderPk orderPk) {
        LOG.debug("removing activity {} from order {}", activity.getActivityId(), orderPk);
        Query query = queryById(orderPk);
        Update pull = new Update().pull("activities", query(where("activityId").is(activity.getActivityId())));

        mongoOps.updateFirst(query, pull, Order.class);
    }

    public void removeAllActivities() {
        LOG.debug("removing all activities from order collection");
        mongoOps.updateMulti(new Query(), new Update().unset("activities"), Order.class);
    }

    public void updateActivity(@NonNull Activity activity) {
        if (activity.getOrderPk() != null) {
            LOG.debug("modifying activity {} on order {}", activity.getActivityId(), activity.getOrderPk());
            Query query = queryById(activity.getOrderPk());
            Update set = new Update()
                    .set("activities.$[activity].regionId", activity.getRegionId())
                    .set("activities.$[activity].state", activity.getState())
                    .filterArray(where("activity.activityId").is(activity.getActivityId()));

            mongoOps.updateFirst(query, set, Order.class);
        }
    }

    /**
     * This creates an update definition for the {@link OrderRepository}.
     * We exclude the Activity summary and state fields as these are derived
     * @return update definition
     */
    public UpdateDefinition getSaveUpdateDefinition(Order order) {
        Update update = new Update();

        update.set("name", order.getName());
        update.set("field1", order.getField1());
        update.set("field1", order.getField2());
        update.set("field1", order.getField3());
        update.set("updateDate", order.getUpdateDate());
        update.set("priority", order.getPriority());

        // if this is a new order (i.e. an insert), set defaults
        update.setOnInsert("state", Order.OrderState.NEW);
        update.setOnInsert("activities", Collections.emptyList());

        return update;
    }

    public Activity getSummary(Activity activity) {
        return Activity.builder()
                .activityId(activity.getActivityId())
                .regionId(activity.getRegionId())
                .state(activity.getState())
                .build();
    }

    /**
     * This applies a projection to the query so only the summary fields are returned from Mongo.
     * @param query input query
     * @return query
     */
    private Query includeOrderSummaryFields(Query query) {
        query.fields()
                .include("name");

        return query;
    }

    /**
     * This compares the summary fields of 2 Order. If they are not equal, related {@link Activity}
     * records need to be updated.
     * @param order1 first order
     * @param order2 second order
     * @return true if summary fields are equal
     */
    private boolean summaryFieldsEqual(@NonNull Order order1, @NonNull Order order2) {
        return Objects.equals(order1.getName(), order2.getName());
    }
}
