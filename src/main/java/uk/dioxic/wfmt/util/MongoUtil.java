package uk.dioxic.wfmt.util;

import org.springframework.data.mongodb.core.query.Query;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

public class MongoUtil {
//
//    public static MongoClient createPojoClient() {
//        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
//                fromProviders(PojoCodecProvider.builder().automatic(true).build()));
//
//        MongoClientSettings mcs = MongoClientSettings.builder()
//                .codecRegistry(pojoCodecRegistry)
//                .applyConnectionString(new ConnectionString("mongodb://localhost:27017"))
//                .build();
//
//        return MongoClients.create(mcs);
//    }

    public static Query queryById(Object id) {
        return query(where("_id").is(id));
    }

}
