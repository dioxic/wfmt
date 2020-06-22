package uk.dioxic.wfmt.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ReadConcern;
import com.mongodb.WriteConcern;
import org.bson.UuidRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@EnableRetry
@EnableMongoRepositories(basePackages = "uk.dioxic.wfmt.repository")
public class MongoConfiguration extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.uri}")
    private String connectionUri;

    @Override
    protected String getDatabaseName() {
        return "test";
    }

    @Override
    protected void configureClientSettings(MongoClientSettings.Builder builder) {
        builder
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .applicationName("wfmt-POC")
                .retryWrites(true)
                .retryReads(true)
                .writeConcern(WriteConcern.MAJORITY)
                .readConcern(ReadConcern.MAJORITY)
                .applyConnectionString(new ConnectionString(connectionUri));
    }

    @Bean
    MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }

    @Bean
    MongoTransactionExceptionChecker mongoTransactionExceptionChecker() {
        return new MongoTransactionExceptionChecker();
    }

}