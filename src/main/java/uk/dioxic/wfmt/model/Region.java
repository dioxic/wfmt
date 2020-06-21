package uk.dioxic.wfmt.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
@Builder
@Document
public class Region {

    @MongoId
    private final Integer regionId;

    private final String name;

}
