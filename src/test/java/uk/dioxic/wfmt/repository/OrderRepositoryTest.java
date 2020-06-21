package uk.dioxic.wfmt.repository;

import uk.dioxic.wfmt.DataUtil;
import uk.dioxic.wfmt.config.MongoConfiguration;
import uk.dioxic.wfmt.model.Activity;
import uk.dioxic.wfmt.model.ActivitySummary;
import uk.dioxic.wfmt.model.Order;
import uk.dioxic.wfmt.model.Order.OrderPk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.Optional;

import static uk.dioxic.wfmt.assertions.CustomAssertions.assertThat;
import static uk.dioxic.wfmt.model.Activity.ActivityState.*;
import static java.util.function.Predicate.isEqual;

@DataMongoTest
@Import({DataUtil.class, MongoConfiguration.class})
public class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

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
        ActivitySummary activity1 = ActivitySummary.builder()
                .activityId("A1")
                .regionId(1)
                .state(ALLOCATED)
                .build();

        ActivitySummary activity2 = ActivitySummary.builder()
                .activityId("A2")
                .regionId(2)
                .state(ALLOCATED)
                .build();

        ActivitySummary activity3 = ActivitySummary.builder()
                .activityId("A3")
                .regionId(3)
                .state(ALLOCATED)
                .build();


        Order order1 = Order.builder()
                .orderPk(new OrderPk(111,111))
                .activities(List.of(activity1,activity2,activity3))
                .build();

        Order order2 = Order.builder()
                .orderPk(new OrderPk(222,222))
                .activities(List.of(activity1))
                .build();

        mongoOps.insert(order1);
        mongoOps.insert(order2);

        assertThat(orderRepository.findByActivitiesRegionId(1)).as("check query by orderId").containsOnly(order1, order2);
        assertThat(orderRepository.findByActivitiesRegionId(3)).as("check query by orderId").containsExactly(order1);
    }

    @Test
    @DisplayName("Find by Id")
    void findById() {
        Order expected = dataUtil.defaultOrder()
                .orderPk(new OrderPk(111,111))
                .build();

        mongoOps.insert(expected);

        Optional<Order> actual = orderRepository.findById(expected.getOrderPk());

        assertThat(actual).get().as("check query by PK").isEqualTo(expected);
    }

    @Test
    @DisplayName("Save new order")
    void saveNewOrder() {
        Order expected = dataUtil.defaultOrder().build();

        orderRepository.save(expected);

        Order actual = mongoOps.findById(expected.getOrderPk(), Order.class);

        assertThat(actual).isEqualToIgnoringGivenFields(expected, "state", "activities");
    }

    @Test
    @DisplayName("Modify order summary fields")
    void saveExistingOrderWithActivities() {
        Order originalOrder = dataUtil.defaultOrder()
                .name("original name")
                .build();
        List<Activity> activities = dataUtil.getActivitiesWithOrder(5, originalOrder);

        orderRepository.save(originalOrder);
        mongoOps.insertAll(activities);


        mongoOps.find(new Query(), Activity.class).forEach(a -> assertThat(a)
                .as("check original order name correct")
                .hasSummary(originalOrder)
        );

        Order modifiedOrder = dataUtil.defaultOrder()
                .name("modified name")
                .build();

        orderRepository.save(modifiedOrder);

        mongoOps.find(new Query(), Activity.class).forEach(a -> assertThat(a)
                .as("check order name has been modified")
                .hasSummary(modifiedOrder)
        );
    }

}
