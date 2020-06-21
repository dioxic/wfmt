package uk.dioxic.wfmt.projections;

import uk.dioxic.wfmt.model.Activity;
import uk.dioxic.wfmt.model.OrderSummary;

public interface ActivitySummaryProjection {
    OrderSummary getOrder();
    Activity.ActivityState getState();
    Integer getRegionId();
}
