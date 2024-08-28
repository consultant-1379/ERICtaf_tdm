package com.ericsson.cifwk.tdm.application.datasources;

import com.ericsson.cifwk.tdm.db.MongoBee;
import com.ericsson.cifwk.tdm.model.Version;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static com.ericsson.cifwk.tdm.infrastructure.Profiles.INTEGRATION_TEST;
import static com.ericsson.cifwk.tdm.infrastructure.changelogs.InitialChangelog.PREVIOUSLY_APPROVED_DATA_SOURCE;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(INTEGRATION_TEST)
@MongoBee(location = "com.ericsson.cifwk.tdm.infrastructure.changelogs")
public class DataSourceActionServiceITest {

    @Autowired
    private DataSourceActionsService dataSourceActionsService;

    @Test
    public void findLatestApprovedVersion() throws Exception {
        Optional<Version> latestApprovedVersion = dataSourceActionsService.findLatestApprovedVersion(PREVIOUSLY_APPROVED_DATA_SOURCE);
        Version version = new Version(0, 0, 2);
        version.setSnapshot(false);
        assertThat(latestApprovedVersion).isPresent().hasValue(version);
    }
}
