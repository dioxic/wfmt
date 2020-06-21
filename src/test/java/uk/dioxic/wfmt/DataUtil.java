package uk.dioxic.wfmt;

import uk.dioxic.wfmt.model.Activity;
import uk.dioxic.wfmt.model.Activity.ActivityBuilder;
import uk.dioxic.wfmt.model.OrderSummary;
import uk.dioxic.wfmt.model.Order;
import uk.dioxic.wfmt.model.ActivitySummary;
import uk.dioxic.wfmt.model.Order.OrderBuilder;
import uk.dioxic.wfmt.model.Region;
import uk.dioxic.wfmt.model.Region.RegionBuilder;
import uk.dioxic.wfmt.model.User;
import uk.dioxic.wfmt.model.User.UserBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class DataUtil {

    @Autowired
    private MongoOperations mongoOps;

    public void clearData() {
        mongoOps.dropCollection(Activity.class);
        mongoOps.dropCollection(Order.class);
        mongoOps.dropCollection(User.class);
        mongoOps.dropCollection(Region.class);

        mongoOps.createCollection(Activity.class);
        mongoOps.createCollection(Order.class);
        mongoOps.createCollection(User.class);
        mongoOps.createCollection(Region.class);
    }

    public UserBuilder defaultUser() {
        return User.builder()
                .firstName("Bob")
                .lastName("TheDuck")
                .userId(111);
    }

    public RegionBuilder defaultRegion() {
        return Region.builder()
                .regionId(1)
                .name("London");
    }

    public ActivityBuilder defaultActivity() {
        return Activity.builder()
                .activityId("A1")
                .actualEcc("ecc")
//                .order(new OrderSummary(defaultOrder().build()))
                .regionId(1)
                .state(Activity.ActivityState.ALLOCATED);
    }

    public OrderBuilder defaultOrder() {
        return Order.builder()
                .orderPk(new Order.OrderPk(111, 111))
                .name("Order1")
                .priority(1)
                .state(Order.OrderState.NEW);
    }

    public List<Region> getRegions(int number) {
        return IntStream.range(0, number)
                .mapToObj(id -> defaultRegion().regionId(id).build())
                .collect(Collectors.toList());
    }

    public List<User> getUsers(int number) {
        return IntStream.range(0, number)
                .mapToObj(id -> defaultUser().userId(id).build())
                .collect(Collectors.toList());
    }

    public List<User> getUsersWithRegions(int number, List<Region> regions) {
        return getUsersWithRegionIds(number, regions.stream().map(Region::getRegionId).collect(Collectors.toList()));
    }

    public List<User> getUsersWithRegionIds(int number, List<Integer> regionIds) {
        return IntStream.range(0, number)
                .mapToObj(id -> defaultUser()
                        .userId(id)
                        .regions(regionIds)
                        .build())
                .collect(Collectors.toList());
    }

    public List<Order> getOrdersWithActivities(int number, List<Activity> activities) {
        List<ActivitySummary> activitySummaries = activities.stream()
                .map(ActivitySummary::new)
                .collect(Collectors.toList());

        return getOrdersWithActivitySummaries(number, activitySummaries);
    }

    public List<Order> getOrdersWithActivitySummaries(int number, List<ActivitySummary> activitySummaries) {
        return IntStream.range(0, number)
                .mapToObj(id -> defaultOrder()
                        .orderPk(new Order.OrderPk(id, id))
                        .name("Order" + id)
                        .activities(activitySummaries)
                        .build())
                .collect(Collectors.toList());
    }

    public List<Order> getOrders(int number) {
        return getOrdersWithActivities(number, Collections.emptyList());
    }

    public List<Activity> getActivitiesWithOrder(int number, Order order) {
        return IntStream.range(0, number)
                .mapToObj(id -> defaultActivity()
                        .activityId("A" + id)
                        .order(new OrderSummary(order))
                        .build())
                .collect(Collectors.toList());
    }

    public List<Activity> getActivities(int number) {
        return IntStream.range(0, number)
                .mapToObj(id -> defaultActivity()
                        .activityId("A" + id)
                        .build())
                .collect(Collectors.toList());
    }

}
