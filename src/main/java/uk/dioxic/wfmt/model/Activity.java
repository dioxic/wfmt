package uk.dioxic.wfmt.model;

import lombok.Builder;
import lombok.Data;
import lombok.With;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;
import uk.dioxic.wfmt.model.Order.OrderPk;

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

    // order PK
    private OrderPk orderPk;

    // order summary fields
    private String orderName;



}

