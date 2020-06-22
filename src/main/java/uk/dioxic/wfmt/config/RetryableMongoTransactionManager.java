package uk.dioxic.wfmt.config;

import com.mongodb.MongoException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;

@Deprecated
/*
 * This will only capture exception thrown when the transaction is committed.
 * It will not capture TransientTransactionExceptions (e.g. when a document is concurrently updated).
 */
public class RetryableMongoTransactionManager extends MongoTransactionManager {

    private Logger LOG = LogManager.getLogger();

    public RetryableMongoTransactionManager(MongoDatabaseFactory dbFactory) {
        super(dbFactory);
    }

    @Override
    protected void doCommit(MongoTransactionObject transactionObject) throws Exception {
        int retries = 3;
        do {
            try {
                transactionObject.commitTransaction();
                break;
            } catch (MongoException ex) {
                if (!ex.hasErrorLabel(MongoException.UNKNOWN_TRANSACTION_COMMIT_RESULT_LABEL)) {
                    throw ex;
                }
                LOG.debug("Transaction WriteConflict detected - retrying");
            }
            Thread.sleep(500);
        } while (--retries > 0);
    }
}
