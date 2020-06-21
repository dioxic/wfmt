package uk.dioxic.wfmt.model;

import uk.dioxic.wfmt.repository.OrderRepository;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.With;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.query.UpdateDefinition;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Data
@Builder
@Document
public class Order {

    public enum OrderState {
        NEW,
        TRIAGE,
        JEOPARDY,
        MISSED,
        MONITOR,
        CLOSED
    }

    @With
    @MongoId
    private final OrderPk orderPk;
    private final String name;
    private final String field1;
    private final String field2;
    private final String field3;
    private final LocalDateTime updateDate;
    private final Integer priority;

    // fields not present in source system
    private final OrderState state;
    private final List<ActivitySummary> activities;

    @Data
    public static class OrderPk {
        private final Integer orderId;
        private final Integer circuitId;
    }

    public Integer getOrderId() {
        return orderPk.getOrderId();
    }

    public Integer getCircuitId() {
        return orderPk.getCircuitId();
    }

    /**
     * This creates an update definition for the {@link OrderRepository}.
     * We exclude the Activity summary and state fields as these are derived
     * @return update definition
     */
    public UpdateDefinition getUpdateDefinition() {
        Update update = new Update();

        update.set("name", name);
        update.set("field1", field1);
        update.set("field1", field2);
        update.set("field1", field3);
        update.set("updateDate", updateDate);
        update.set("priority", priority);

        // if this is a new order (i.e. an insert), set defaults
        update.setOnInsert("state", OrderState.NEW);
        update.setOnInsert("activities", Collections.emptyList());

        return update;
    }

    /**
     * This applies a projection to the query so only the summary fields are returned from Mongo.
     * @param query input query
     * @return query
     */
    public static Query includeSummaryFieldsOnly(Query query) {
        query.fields()
                .exclude("_id")
                .include("name");

        return query;
    }

    /**
     * This compares the summary fields of 2 Order. If they are not equal, related {@link Activity}
     * records need to be updated.
     * @param order1 first order
     * @param order2 second order
     * @return true if summary fields are equal
     */
    public static boolean summaryFieldsEqual(@NonNull Order order1, @NonNull Order order2) {
        return Objects.equals(order1.getName(), order2.getName());
    }

}
