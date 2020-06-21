package uk.dioxic.wfmt.model;

import lombok.Builder;
import lombok.Data;
import lombok.With;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDateTime;
import java.util.List;

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

    @Data
    public static class OrderPk {
        private final Integer orderId;
        private final Integer circuitId;
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

}
