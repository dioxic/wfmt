package uk.dioxic.wfmt.assertions;

import org.assertj.core.api.Assertions;
import uk.dioxic.wfmt.model.Activity;
import uk.dioxic.wfmt.model.Order;

public class CustomAssertions extends Assertions {

    public static OrderAssert assertThat(Order actual) {
        return new OrderAssert(actual);
    }

    public static ActivityAssert assertThat(Activity actual) {
        return new ActivityAssert(actual);
    }
}
