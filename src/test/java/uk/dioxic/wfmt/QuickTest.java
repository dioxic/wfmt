package uk.dioxic.wfmt;

import uk.dioxic.wfmt.config.MongoConfiguration;
import uk.dioxic.wfmt.repository.ActivityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoOperations;

@DataMongoTest
@Import({DataUtil.class, MongoConfiguration.class})
public class QuickTest {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private MongoOperations mongoOps;

    @Autowired
    private DataUtil dataUtil;

    @BeforeEach
    void setup() {
        dataUtil.clearData();
    }

}
