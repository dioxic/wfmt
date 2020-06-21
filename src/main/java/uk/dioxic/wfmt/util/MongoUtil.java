package uk.dioxic.wfmt.util;

import org.springframework.data.mongodb.core.query.Query;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

public class MongoUtil {

    public static Query queryById(Object id) {
        return query(where("_id").is(id));
    }

}
