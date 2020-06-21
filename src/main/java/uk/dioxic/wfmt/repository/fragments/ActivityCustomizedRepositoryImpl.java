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
import org.springframework.transaction.annotation.Transactional;
import uk.dioxic.wfmt.model.Activity;
import uk.dioxic.wfmt.model.Order;
import uk.dioxic.wfmt.repository.OrderRepository;

import java.util.Objects;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static uk.dioxic.wfmt.util.MongoUtil.queryById;

public class ActivityCustomizedRepositoryImpl implements ActivityCustomizedRepository<Activity, String> {

    private final Logger LOG = LogManager.getLogger();

    @Autowired
    private MongoOperations mongoOps;

    @Autowired
    private OrderRepository orderRepository;

    @Transactional
    public <S extends Activity> S save(@NonNull S activity) {

        Query query = queryById(activity.getActivityId());
        includeActivitySummaryFields(query);
        includeOrderFkFields(query);

        // save the activity - only update fields which are not null
        Activity prevActivity = mongoOps.findAndModify(
                query,
                getSaveUpdateDefinition(activity),
                FindAndModifyOptions.options().upsert(true),
                Activity.class);

        // check if order collection needs updating
        if (prevActivity == null) {
            // add activity summary to the order
            addActivityToOrderAndUpdateSummary(activity);
        } else {
            if (!Objects.equals(activity.getOrderPk(), prevActivity.getOrderPk())) {
                orderRepository.removeActivity(prevActivity);
                addActivityToOrderAndUpdateSummary(activity);
            }

            // check if activity summary needs changing on the related order
            if (!summaryFieldsEqual(activity, prevActivity)) {
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

    public void updateOrderSummary(Order orderSummary, Activity activity) {
        if (orderSummary != null && activity != null) {
            LOG.debug("Updating order summary for {} on activity {}", orderSummary.getOrderPk(), activity.getActivityId());
            mongoOps.updateFirst(
                    queryById(activity.getActivityId()),
                    getOrderSummaryUpdateDefinition(orderSummary),
                    Activity.class
            );
        }
    }

    public void updateOrderSummary(Order orderSummary) {
        if (orderSummary != null) {
            LOG.debug("Updating order summary for {} on all related activities", orderSummary.getOrderPk());
            mongoOps.updateMulti(
                    query(where("orderPk").is(orderSummary.getOrderPk())),
                    getOrderSummaryUpdateDefinition(orderSummary),
                    Activity.class
            );
        }
    }

    private void addActivityToOrderAndUpdateSummary(Activity activity) {
        Order order = orderRepository.addActivity(activity);
        updateOrderSummary(order, activity);
    }

    /**
     * The update definition for saving an {@link Activity}.
     * We save everything apart from the order summary fields which are set retrospectively.
     * @return update definition
     */
    private UpdateDefinition getSaveUpdateDefinition(Activity activity) {

        // we set all fields except for the primary key and the Order summary fields (i.e. order.name)
        return new Update()
                .set("regionId", activity.getRegionId())
                .set("orderPk", activity.getOrderPk())
                .set("state", activity.getState())
                .set("field1", activity.getField1())
                .set("field2", activity.getField2())
                .set("field3", activity.getField3())
                .set("priority", activity.getPriority());
    }

    /**
     * The update definition for saving the {@link Order} summary fields.
     * @param order order
     * @return update definition
     */
    private static UpdateDefinition getOrderSummaryUpdateDefinition(Order order) {
        return new Update()
                .set("orderName", order.getName());
    }

    /**
     * Applies a projection to the query to inlude the {@link Activity} summary fields.
     * @param query input query
     */
    private static void includeActivitySummaryFields(Query query) {
        query.fields()
                .include("state")
                .include("regionId");
    }

    /**
     * Applies a projection to the query to inlude the {@link Order} foreign key fields.
     * @param query input query
     */
    private static void includeOrderFkFields(Query query) {
        query.fields()
                .include("orderPk");
    }

    private static boolean summaryFieldsEqual(@NonNull Activity activity1, @NonNull Activity activity2) {
        return Objects.equals(activity1.getRegionId(), activity2.getRegionId()) &&
                Objects.equals(activity1.getState(), activity2.getState());
    }

}
