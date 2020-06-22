package uk.dioxic.wfmt.config;

import com.mongodb.MongoException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.stereotype.Component;

/**
 * Determines whether a transaction exception can be retried.
 */
public class MongoTransactionExceptionChecker {

    private final Logger LOG = LogManager.getLogger();

    public boolean canRetry(Throwable exception) {
        if (exception == null) {
            return false;
        }
        else if (exception instanceof MongoException) {
            MongoException ex = (MongoException) exception;
            if (ex.hasErrorLabel(MongoException.UNKNOWN_TRANSACTION_COMMIT_RESULT_LABEL)
                    || ex.hasErrorLabel(MongoException.TRANSIENT_TRANSACTION_ERROR_LABEL)) {
                LOG.trace("transient transaction exception detected");
                return true;
            }
            return false;
        }
        else {
            return canRetry(exception.getCause());
        }
    }
}
