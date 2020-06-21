package uk.dioxic.wfmt.model;

import lombok.Builder;
import lombok.Data;
import lombok.With;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.query.UpdateDefinition;

@Data
@Builder
@Document
public class Activity {

    public enum ActivityState {
        FUTURE_START_DATE,
        TRIAGE,
        JEOPARDY,
        MISSED_START_DATE,
        ALLOCATED,
        CLOSED
    }

    @With
    @MongoId
    private String activityId;
    private Integer regionId;
    private ActivityState state;
    private String field1;
    private String field2;
    private String field3;
    private Integer priority;

    // order PK fields
    private Integer orderId;
    private Integer circuitId;

    // order summary fields
    private String orderName;

    /**
     * The update definition for saving an {@link Activity}.
     * We save everything apart from the order summary fields which are set retrospectively.
     * @return update definition
     */
    public UpdateDefinition getSaveUpdateDefinition() {
        Update update = new Update();

        // we set all fields except for the primary key and the Order summary fields (i.e. order.name)

        update.set("regionId", regionId);
        update.set("orderId", orderId);
        update.set("circuitId", circuitId);
        update.set("state", state);
        update.set("field1", field1);
        update.set("field2", field2);
        update.set("field3", field3);

        return update;
    }

    /**
     * The update definition for saving the {@link Order} summary fields.
     * @param order order
     * @return update definition
     */
    public static UpdateDefinition getOrderSummaryUpdateDefinition(Order order) {
        Update update = new Update();

        update.set("orderName", order.getName());

        return update;
    }

    /**
     * Applies a projection to the query to inlude the {@link Activity} summary fields.
     * @param query input query
     * @return query
     */
    public static Query includeSummaryFields(Query query) {
        query.fields()
                .include("state")
                .include("regionId");

        return query;
    }

    /**
     * Applies a projection to the query to inlude the {@link Order} foreign key fields.
     * @param query input query
     * @return query
     */
    public static Query includeOrderFkFields(Query query) {
        query.fields()
                .include("orderId")
                .include("circuitId");

        return query;
    }

}

