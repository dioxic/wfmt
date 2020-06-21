package uk.dioxic.wfmt.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoOperations;
import uk.dioxic.wfmt.DataUtil;
import uk.dioxic.wfmt.config.MongoConfiguration;
import uk.dioxic.wfmt.model.Region;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Import({DataUtil.class, MongoConfiguration.class})
public class RegionRepositoryTest {

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private MongoOperations mongoOps;

    @Autowired
    private DataUtil dataUtil;

    @BeforeEach
    void setup() {
        dataUtil.clearData();
    }

    @Test
    void insertRegion() {
        Region expected = dataUtil.defaultRegion().build();

        regionRepository.save(expected);

        Region actual = mongoOps.findById(expected.getRegionId(), Region.class);

        assertThat(actual).as("check Region was inserted").isEqualTo(expected);
    }

    @Test
    void updateRegion() {
        Region initial = dataUtil.defaultRegion()
                .name("London")
                .build();

        mongoOps.insert(initial);
        assertThat(regionRepository.count()).as("check Region was inserted").isEqualTo(1);

        Region expected = dataUtil.defaultRegion()
                .name("Mumbai")
                .build();

        regionRepository.save(expected);

        Region actual = mongoOps.findById(expected.getRegionId(), Region.class);
        assertThat(actual).as("check Region was modified").isEqualTo(expected);
    }

    @Test
    void deleteRegion() {
        Region region = dataUtil.defaultRegion().build();

        mongoOps.insert(region);

        assertThat(regionRepository.count()).as("check Region was inserted").isEqualTo(1);

        regionRepository.delete(region);

        assertThat(regionRepository.count()).as("check Region was deleted").isEqualTo(0);
    }

}
