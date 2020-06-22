package uk.dioxic.wfmt;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.UncategorizedMongoDbException;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.dioxic.wfmt.config.MongoConfiguration;
import uk.dioxic.wfmt.model.User;

import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.data.mongodb.core.query.Update.update;
import static uk.dioxic.wfmt.util.MongoUtil.queryById;

@DataMongoTest
@Import({MongoConfiguration.class, DataUtil.class, RetryTest.MockRepository.class})
public class RetryTest {

    private static final Logger LOG = LogManager.getLogger();

    @Autowired
    private MockRepository repository;

    @Autowired
    private MongoOperations mongoOps;

    @Autowired
    private DataUtil dataUtil;

    @BeforeEach
    void setup() {
        dataUtil.clearData();

    }

    @Test
    void testConcurrentOperation() throws InterruptedException {
        Callable<String> updateTask1 = () -> {
            repository.updateUsers(1, 2, 200, "Bob");
            return "Success";
        };

        Callable<String> updateTask2 = () -> {
            repository.updateUsers(1, 2, 200, "Ravi");
            return "Success";
        };

        repository.createUsers();

        List<Throwable> exceptions = invokeAndGetExceptions(List.of(updateTask1, updateTask2));

        exceptions.forEach(LOG::info);

        assertThat(exceptions)
                .as("check exception thrown")
                .hasSize(1)
                .allMatch(t -> {
                    LOG.error(t);
                    return t instanceof UncategorizedMongoDbException;
                }, "check correct exception type");
    }

    @Test
    void testRetrableConcurrentOperation() throws InterruptedException {
        Callable<String> updateTask1 = () -> {
            repository.updateUsersWithRetry(1, 2, 200, "Bob");
            return "Success";
        };

        Callable<String> updateTask2 = () -> {
            repository.updateUsersWithRetry(1, 2, 200, "Ravi");
            return "Success";
        };

        repository.createUsers();

        List<Throwable> exceptions = invokeAndGetExceptions(List.of(updateTask1, updateTask2));

        assertThat(exceptions)
                .as("check exception NOT thrown")
                .hasSize(0);
    }

    @Test
    void testNonTransientException() {
        User user = User.builder()
                .userId(1)
                .build();

        repository.insertUserWithRetry(user);

        Throwable thrown = catchThrowable(() -> repository.insertUserWithRetry(user));

        assertThat(thrown)
                .isInstanceOf(DuplicateKeyException.class);
    }

    private List<Throwable> invokeAndGetExceptions(List<Callable<String>> callables) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        return executor.invokeAll(callables).stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (Exception e) {
                        return e.getCause();
                    }
                })
                .filter(res -> res instanceof Throwable)
                .map(Throwable.class::cast)
                .collect(Collectors.toList());
    }

    @Component
    static class MockRepository {

        @Autowired
        private MongoOperations mongoOps;

        @Transactional
        public void updateUsers(int id1, int id2, long delay, String firstName) throws InterruptedException {
            mongoOps.updateFirst(queryById(id1), update("firstName", firstName), User.class);
            TimeUnit.MILLISECONDS.sleep(delay);
            mongoOps.updateFirst(queryById(id2), update("firstName", firstName), User.class);
        }

        @Retryable(
                value = {UncategorizedMongoDbException.class},
                maxAttempts = 2,
                backoff = @Backoff(delay = 500))
        @Transactional
        public void updateUsersWithRetry(int id1, int id2, long delay, String firstName) throws InterruptedException {
            mongoOps.updateFirst(queryById(id1), update("firstName", firstName), User.class);
            TimeUnit.MILLISECONDS.sleep(delay);
            mongoOps.updateFirst(queryById(id2), update("firstName", firstName), User.class);
        }

        @Retryable(
                value = {UncategorizedMongoDbException.class},
                maxAttempts = 2,
                backoff = @Backoff(delay = 500))
        @Transactional
        public void insertUserWithRetry(User user) {
            mongoOps.insert(user);
        }

        public void createUsers() {
            User user1 = User.builder()
                    .userId(1)
                    .build();
            User user2 = User.builder()
                    .userId(2)
                    .build();

            mongoOps.insertAll(List.of(user1, user2));
        }
    }
}
