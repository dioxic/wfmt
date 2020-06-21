package uk.dioxic.wfmt.repository.fragments;

import uk.dioxic.wfmt.model.Activity;
import uk.dioxic.wfmt.model.OrderSummary;
import uk.dioxic.wfmt.model.Order;
import uk.dioxic.wfmt.repository.OrderRepository;
import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.transaction.annotation.Transactional;
import uk.dioxic.wfmt.model.ActivitySummary;
import uk.dioxic.wfmt.util.MongoUtil;

import java.util.Objects;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

public class ActivityCustomizedRepositoryImpl implements ActivityCustomizedRepository<Activity, String> {

    private final Logger LOG = LogManager.getLogger();

    @Autowired
    private MongoOperations mongoOps;

    @Autowired
    private OrderRepository orderRepository;

    @Transactional
    public <S extends Activity> S save(@NonNull S activity) {

        // save the activity - only update fields which are not null
        Activity prevActivity = mongoOps.findAndModify(
                Activity.includeSummaryFields(MongoUtil.queryById(activity.getActivityId())),
                activity.getSaveUpdateDefinition(),
                FindAndModifyOptions.options().upsert(true),
                Activity.class);

        Query query = new Query();
        query.fields().include("");

        // check if order collection needs updating
        if (prevActivity == null) {
            if (activity.getOrder() != null) {
                // add activity summary to the order
                orderRepository.addActivity(activity);
            }
        } else {
            OrderSummary currOrder = activity.getOrder();
            OrderSummary prevOrder = prevActivity.getOrder();

            if (!Objects.equals(currOrder, prevOrder)) {
                if (prevOrder != null) {
                    // remove activity from previous order
                    orderRepository.removeActivity(activity, prevOrder.getOrderPk());
                }
                if (currOrder != null) {
                    orderRepository.addActivity(activity);
                }
            }

            // check if activity summary needs changing on the related order
            if (!ActivitySummary.summaryFieldsEqual(activity, prevActivity)) {
                orderRepository.updateActivity(activity);
            }
        }

        return activity;
    }

    @Transactional
    public void deleteById(@NonNull String id) {
        /*
             could be done by searching for Orders with the embedded activityId
             would need order.activities.activityId to be indexed to be efficient
         */
        throw new UnsupportedOperationException("Not implemented");
    }

    @Transactional
    public void delete(@NonNull Activity activity) {
        mongoOps.remove(activity);
        orderRepository.removeActivity(activity);
    }

    public void deleteAll(@NonNull Iterable<? extends Activity> entities) {
        entities.forEach(this::delete);
    }

    public void deleteAll() {
        /*
            not wrapped in a transaction because this is potentially a lot of data
            there are limits to how much data can be processed in a single transaction
         */
        orderRepository.removeAllActivities();
        mongoOps.remove(new Query(), Activity.class);
    }

    public void updateOrderSummary(Order order) {
        LOG.debug("Updating order summary for {} on related activities", order.getOrderPk());
        mongoOps.updateMulti(query(where("order.orderId")
                        .is(order.getOrderPk().getOrderId())
                        .and("order.circuitId")
                        .is(order.getOrderPk().getCircuitId())),
                new Update().set("order.name", order.getName()),
                Activity.class
        );
    }

}
