package uk.dioxic.wfmt.assertions;

import org.assertj.core.api.AbstractObjectAssert;
import uk.dioxic.wfmt.model.Activity;
import uk.dioxic.wfmt.model.Order;

import static org.assertj.core.api.Assertions.assertThat;

public class ActivityAssert extends AbstractObjectAssert<ActivityAssert, Activity> {

        public ActivityAssert(Activity activity) {
            super(activity, ActivityAssert.class);
        }

        public ActivityAssert hasSummary(Order order) {
            isNotNull();
            assertThat(actual)
                    .extracting("orderPk", "orderName")
                    .contains(order.getOrderPk(), order.getName());

            return this;
        }

    }