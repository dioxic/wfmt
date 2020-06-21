package uk.dioxic.wfmt.repository;

import uk.dioxic.wfmt.DataUtil;
import uk.dioxic.wfmt.config.MongoConfiguration;
import uk.dioxic.wfmt.model.Activity;
import uk.dioxic.wfmt.model.OrderSummary;
import uk.dioxic.wfmt.model.Order;
import uk.dioxic.wfmt.model.ActivitySummary;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoOperations;

import java.util.List;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Import({DataUtil.class, MongoConfiguration.class})
public class ActivityRepositoryTest {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private MongoOperations mongoOps;

    @Autowired
    private DataUtil dataUtil;

    @BeforeEach
    void setup() {
        dataUtil.clearData();
    }

    @Test
    @DisplayName("Find by regionId")
    void findByRegionId() {
        Activity activity = dataUtil.defaultActivity()
                .regionId(1)
                .build();

        mongoOps.insert(activity);

        List<Activity> results = activityRepository.findByRegionId(1);

        assertThat(results).as("check query by orderId").containsExactly(activity);
    }

    @Test
    @DisplayName("Find by orderId")
    void findByOrderId() {
        Activity activity = dataUtil.defaultActivity()
                .order(new OrderSummary(111,111,"Order1"))
                .build();

        mongoOps.insert(activity);

        List<Activity> results = activityRepository.findByOrderOrderId(111);

        assertThat(results).as("check query by orderId").containsExactly(activity);
    }

    @Test
    @DisplayName("Find by orderId and circuitId")
    void findByOrderIdAndCircuitId() {
        Activity expected = dataUtil.defaultActivity()
                .activityId("A1")
                .order(new OrderSummary(111,111,"Order1"))
                .build();

        Activity notExpected = dataUtil.defaultActivity()
                .activityId("A2")
                .order(new OrderSummary(111,222,"Order2"))
                .build();

        mongoOps.insert(expected);
        mongoOps.insert(notExpected);

        List<Activity> results = activityRepository.findByOrderOrderIdAndOrderCircuitId(111, 111);

        assertThat(results).as("check query by orderId and circuitId").containsExactly(expected);
    }

    @Test
    @DisplayName("New activity without related order")
    void saveNewActivityWithNoOrder() {
        Activity expected = dataUtil.defaultActivity().build();

        activityRepository.save(expected);

        Activity actual = mongoOps.findById(expected.getActivityId(), Activity.class);

        assertThat(actual).as("check Activity was inserted").isEqualTo(expected);
    }

    @Test
    @DisplayName("New activity with related order")
    void saveNewActivityWithOrder() {
        Order order = dataUtil.defaultOrder().build();
        mongoOps.insert(order);

        Activity activity = dataUtil.defaultActivity()
                .order(new OrderSummary(order))
                .build();

        activityRepository.save(activity);

        Activity actual = mongoOps.findById(activity.getActivityId(), Activity.class);

        assertThat(actual).as("check Activity was inserted").isEqualTo(activity);

        order = mongoOps.findById(order.getOrderPk(), Order.class);

        assertThat(order)
                .as("check Activity summary has been added to related order")
                .isNotNull()
                .extracting(Order::getActivities, as(InstanceOfAssertFactories.LIST))
                .containsExactly(new ActivitySummary(activity));

    }

    @Test
    @DisplayName("Change to order relationship")
    void saveExistingActivityWithOrderChange() {
        List<Order> orders = dataUtil.getOrders(2);

        mongoOps.insertAll(orders);

        Activity activity = dataUtil.defaultActivity()
                .order(new OrderSummary(orders.get(0)))
                .build();
        activityRepository.save(activity);

        activity = dataUtil.defaultActivity()
                .order(new OrderSummary(orders.get(1)))
                .build();
        activityRepository.save(activity);

        Order expectedOrder1 = mongoOps.findById(orders.get(0).getOrderPk(), Order.class);
        Order expectedOrder2 = mongoOps.findById(orders.get(1).getOrderPk(), Order.class);

        assertThat(expectedOrder1)
                .as("check Activity summary has been removed from previously-related order")
                .isNotNull()
                .extracting(Order::getActivities, as(InstanceOfAssertFactories.LIST))
                .hasSize(0);

        assertThat(expectedOrder2)
                .as("check Activity summary has been added to related order")
                .isNotNull()
                .extracting(Order::getActivities, as(InstanceOfAssertFactories.LIST))
                .containsExactly(new ActivitySummary(activity));

    }

    @Test
    @DisplayName("Change to summary fields")
    void saveExistingActivityWithChangeToSummaryFields() {
        List<Order> orders = dataUtil.getOrders(1);

        mongoOps.insertAll(orders);

        Activity activity = dataUtil.defaultActivity()
                .order(new OrderSummary(orders.get(0)))
                .regionId(1)
                .build();
        activityRepository.save(activity);

        Activity modifiedActivity = dataUtil.defaultActivity()
                .order(new OrderSummary(orders.get(0)))
                .regionId(2)
                .state(Activity.ActivityState.JEOPARDY)
                .build();
        activityRepository.save(activity);

        Order expectedOrder = mongoOps.findById(orders.get(0).getOrderPk(), Order.class);

        assertThat(expectedOrder)
                .as("check Activity summary is has been modified on the related order")
                .isNotNull()
                .extracting(Order::getActivities, as(InstanceOfAssertFactories.LIST))
                .containsExactly(new ActivitySummary(modifiedActivity));

    }

    @Test
    @DisplayName("Delete activity with related order")
    void deleteActivity() {
        Order order = dataUtil.defaultOrder().build();
        mongoOps.insert(order);

        Activity activity = dataUtil.defaultActivity()
                .order(new OrderSummary(order))
                .build();

        activityRepository.save(activity);
        assertThat(activityRepository.count()).as("check Activity was inserted").isEqualTo(1);

        activityRepository.delete(activity);
        assertThat(activityRepository.count()).as("check Activity was deleted").isEqualTo(0);

        Order modifiedOrder = mongoOps.findById(order.getOrderPk(), Order.class);

        assertThat(modifiedOrder)
                .as("check Activity has been removed from Order")
                .isNotNull()
                .extracting(Order::getActivities, as(InstanceOfAssertFactories.LIST))
                .hasSize(0);
    }

}
