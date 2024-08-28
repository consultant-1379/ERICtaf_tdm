package com.ericsson.cifwk.tdm.infrastructure;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import org.springframework.context.annotation.Bean;

import java.io.IOException;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 15/03/2016
 */
abstract class BaseInMemoryDbConfiguration extends BaseDbConfiguration {

    private static final MongodStarter STARTER = MongodStarter.getDefaultInstance();

    @Bean(destroyMethod = "stop")
    public MongodProcess mongod() throws IOException {
        return mongodExe().start();
    }

    @Bean(destroyMethod = "stop")
    public MongodExecutable mongodExe() throws IOException {
        return STARTER.prepare(mongodConfig());
    }

}
