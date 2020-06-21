package uk.dioxic.wfmt.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoOperations;
import uk.dioxic.wfmt.DataUtil;
import uk.dioxic.wfmt.config.MongoConfiguration;
import uk.dioxic.wfmt.model.Activity;
import uk.dioxic.wfmt.model.Activity.ActivityState;
import uk.dioxic.wfmt.model.Order;
import uk.dioxic.wfmt.model.Order.OrderPk;

import java.util.List;

import static uk.dioxic.wfmt.assertions.CustomAssertions.assertThat;

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
    @DisplayName("Find by orderPk")
    void findByOrderPk() {
        Activity expected = dataUtil.defaultActivity()
                .activityId("A1")
                .orderPk(new OrderPk(111,111))
                .build();

        Activity notExpected = dataUtil.defaultActivity()
                .activityId("A2")
                .orderPk(new OrderPk(111,222))
                .build();

        mongoOps.insert(expected);
        mongoOps.insert(notExpected);

        List<Activity> results = activityRepository.findByOrderPk(new OrderPk(111,111));

        assertThat(results).as("check query by orderPk").containsExactly(expected);
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
        Order order = dataUtil.defaultOrder()
                .name("Order1")
                .build();
        mongoOps.insert(order);

        Activity activity = dataUtil.defaultActivity()
                .activityId("A1")
                .regionId(1)
                .state(ActivityState.CLOSED)
                .orderPk(order.getOrderPk())
                .build();

        activityRepository.save(activity);

        Activity actualActivity = mongoOps.findById(activity.getActivityId(), Activity.class);

        assertThat(actualActivity)
                .as("check Activity")
                .isNotNull()
                .extracting(Activity::getRegionId)
                .isEqualTo(activity.getRegionId());

        assertThat(actualActivity)
                .as("check Activity order name")
                .isNotNull()
                .extracting(Activity::getOrderName)
                .isEqualTo(order.getName());

        Order actualOrder = mongoOps.findById(order.getOrderPk(), Order.class);

        assertThat(actualOrder)
                .as("check Activity summary has been added to related order")
                .containsSummary(activity);

    }

    @Test
    @DisplayName("Change to order relationship")
    void saveExistingActivityWithOrderChange() {
        List<Order> orders = dataUtil.getOrders(2);

        mongoOps.insertAll(orders);

        Activity originalActivity = dataUtil.defaultActivity()
                .orderPk(orders.get(0).getOrderPk())
                .regionId(1)
                .state(ActivityState.CLOSED)
                .build();
        activityRepository.save(originalActivity);

        Activity modifiedActivity = dataUtil.defaultActivity()
                .orderPk(orders.get(1).getOrderPk())
                .regionId(2)
                .state(ActivityState.ALLOCATED)
                .build();
        activityRepository.save(modifiedActivity);

        Order actualOrder1 = mongoOps.findById(orders.get(0).getOrderPk(), Order.class);
        Order actualOrder2 = mongoOps.findById(orders.get(1).getOrderPk(), Order.class);

        assertThat(actualOrder1)
                .as("check Activity summary has been removed from previously-related order")
                .isNotNull()
                .hasZeroSummaries();

        assertThat(actualOrder2)
                .as("check Activity summary has been added to related order")
                .isNotNull()
                .containsSummary(modifiedActivity);

    }

    @Test
    @DisplayName("Change to summary fields")
    void saveExistingActivityWithChangeToSummaryFields() {
        Order order = dataUtil.defaultOrder().build();
        mongoOps.insert(order);

        Activity activity = dataUtil.defaultActivity()
                .orderPk(order.getOrderPk())
                .regionId(1)
                .state(ActivityState.CLOSED)
                .build();
        activityRepository.save(activity);

        Activity modifiedActivity = dataUtil.defaultActivity()
                .orderPk(order.getOrderPk())
                .regionId(2)
                .state(ActivityState.JEOPARDY)
                .build();
        activityRepository.save(modifiedActivity);

        Order expectedOrder = mongoOps.findById(order.getOrderPk(), Order.class);

        assertThat(expectedOrder)
                .as("check Activity summary is has been modified on the related order")
                .isNotNull()
                .containsSummary(modifiedActivity);

    }

    @Test
    @DisplayName("Delete activity with related order")
    void deleteActivity() {
        Order order = dataUtil.defaultOrder().build();
        mongoOps.insert(order);

        Activity activity = dataUtil.defaultActivity()
                .orderPk(order.getOrderPk())
                .build();

        activityRepository.save(activity);
        assertThat(activityRepository.count()).as("check Activity was inserted").isEqualTo(1);

        activityRepository.delete(activity);
        assertThat(activityRepository.count()).as("check Activity was deleted").isEqualTo(0);

        Order modifiedOrder = mongoOps.findById(order.getOrderPk(), Order.class);

        assertThat(modifiedOrder)
                .as("check Activity has been removed from Order")
                .isNotNull()
                .hasZeroSummaries();
    }



}
