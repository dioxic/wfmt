package uk.dioxic.wfmt.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.List;

@Data
@Builder
@Document
public class User {

    @MongoId
    private final Integer userId;
    private final String firstName;
    private final String lastName;
    private final List<Integer> regions;

}
