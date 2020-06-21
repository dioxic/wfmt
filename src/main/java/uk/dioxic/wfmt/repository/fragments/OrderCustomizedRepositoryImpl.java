package uk.dioxic.wfmt.repository.fragments;

import uk.dioxic.wfmt.model.Activity;
import uk.dioxic.wfmt.model.ActivitySummary;
import uk.dioxic.wfmt.model.Order;
import uk.dioxic.wfmt.repository.ActivityRepository;
import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.transaction.annotation.Transactional;

import static uk.dioxic.wfmt.util.MongoUtil.queryById;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

public class OrderCustomizedRepositoryImpl implements OrderCustomizedRepository<Order, Order.OrderPk> {

    private final Logger LOG = LogManager.getLogger();

    @Autowired
    private MongoOperations mongoOps;

    @Autowired
    private ActivityRepository activityRepository;

    @Transactional
    public <S extends Order> S save(@NonNull S order) {

        // save the order and return the previous version of the order
        Order prevOrder = mongoOps.findAndModify(
                Order.includeSummaryFieldsOnly(queryById(order.getOrderPk())),
                order.getUpdateDefinition(),
                FindAndModifyOptions.options().upsert(true),
                Order.class
        );

        // update activities if summary fields have changed
        if (prevOrder != null && !Order.summaryFieldsEqual(prevOrder, order)) {
            activityRepository.updateOrderSummary(order);
        }

        return order;
    }

    public void addActivity(@NonNull Activity activity) {
        if (activity.getOrder() != null) {
            LOG.debug("adding activity {} to order {}", activity.getActivityId(), activity.getOrder());
            Query query = query(where("_id").is(activity.getOrder().getOrderPk()));
            Update addToSet = new Update().addToSet("activities").value(new ActivitySummary(activity));

            mongoOps.updateFirst(query, addToSet, Order.class);
        }
    }

    public void removeActivity(@NonNull Activity activity) {
        if (activity.getOrder() != null && activity.getOrder().getOrderPk() != null) {
            removeActivity(activity, activity.getOrder().getOrderPk());
        }
    }

    public void removeActivity(@NonNull Activity activity, @NonNull Order.OrderPk orderPk) {
        LOG.debug("removing activity {} from order {}", activity.getActivityId(), orderPk);
        Query query = query(where("_id").is(orderPk));
        Update pull = new Update().pull("activities", query(where("activityId").is(activity.getActivityId())));

        mongoOps.updateFirst(query, pull, Order.class);
    }

    public void removeAllActivities() {
        LOG.debug("removing all activities from order collection");
        mongoOps.updateMulti(new Query(), new Update().unset("activities"), Order.class);
    }

    public void updateActivity(@NonNull Activity activity) {
        if (activity.getOrder() != null) {
            LOG.debug("modifying activity {} on order {}", activity.getActivityId(), activity.getOrder());
            Query query = query(where("_id").is(activity.getOrder().getOrderPk()));
            Update set = new Update()
                    .set("activities.$[activity]", new ActivitySummary(activity))
                    .filterArray(where("activity.activityId").is(activity.getActivityId()));

            mongoOps.updateFirst(query, set, Order.class);
        }
    }
}
