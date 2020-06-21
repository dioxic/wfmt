package uk.dioxic.wfmt.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoOperations;
import uk.dioxic.wfmt.DataUtil;
import uk.dioxic.wfmt.config.MongoConfiguration;
import uk.dioxic.wfmt.model.User;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Import({DataUtil.class, MongoConfiguration.class})
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MongoOperations mongoOps;

    @Autowired
    private DataUtil dataUtil;

    @BeforeEach
    void setup() {
        dataUtil.clearData();
    }

    @Test
    void insertUser() {
        User expected = dataUtil.defaultUser().build();

        userRepository.save(expected);

        User actual = mongoOps.findById(expected.getUserId(), User.class);
        assertThat(actual).as("check User was inserted").isEqualTo(expected);
    }

    @Test
    void updateUser() {
        User inital = dataUtil.defaultUser()
                .lastName("TheDuck")
                .build();

        mongoOps.insert(inital);
        assertThat(userRepository.count()).as("check User was inserted").isEqualTo(1);

        User expected = dataUtil.defaultUser()
                .lastName("TheFish")
                .build();

        userRepository.save(expected);

        User actual = mongoOps.findById(expected.getUserId(), User.class);
        assertThat(actual).as("check User was modified").isEqualTo(expected);
    }

    @Test
    void deleteUser() {
        User expected = dataUtil.defaultUser().build();

        mongoOps.insert(expected);
        assertThat(userRepository.count()).as("check User was inserted").isEqualTo(1);

        userRepository.delete(expected);

        assertThat(userRepository.count()).as("check User was deleted").isEqualTo(0);
    }

}
