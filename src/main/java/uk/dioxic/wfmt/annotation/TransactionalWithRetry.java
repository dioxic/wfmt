package uk.dioxic.wfmt.annotation;

import org.springframework.data.mongodb.UncategorizedMongoDbException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Transactional
@Retryable(
        value = {UncategorizedMongoDbException.class},
        exceptionExpression = "#{@mongoTransactionExceptionChecker.canRetry(#root)}",
        maxAttemptsExpression = "#{${mongodb.transactionRetryCount}}",
        backoff = @Backoff(delayExpression = "#{${mongodb.transactionRetryDelay}}"))
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface TransactionalWithRetry {
}
