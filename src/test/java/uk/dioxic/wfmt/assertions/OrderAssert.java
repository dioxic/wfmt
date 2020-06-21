package uk.dioxic.wfmt.assertions;

import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.InstanceOfAssertFactories;
import uk.dioxic.wfmt.model.Activity;
import uk.dioxic.wfmt.model.ActivitySummary;
import uk.dioxic.wfmt.model.Order;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderAssert extends AbstractObjectAssert<OrderAssert, Order> {

    public OrderAssert(Order order) {
        super(order, OrderAssert.class);
    }

    public OrderAssert containsSummary(Activity activity) {
        isNotNull();
        assertThat(actual.getActivities())
                .containsExactly(new ActivitySummary(activity));

        return this;
    }

    public OrderAssert containsSummaries(List<Activity> activities) {
        isNotNull();

        assertThat(actual.getActivities())
                .containsExactlyInAnyOrder(activities.stream()
                        .map(ActivitySummary::new)
                        .toArray(ActivitySummary[]::new)
                );

        return this;
    }

    public OrderAssert hasZeroSummaries() {
        isNotNull();
        extracting(Order::getActivities, InstanceOfAssertFactories.LIST)
                .hasSize(0);

        return this;
    }

}