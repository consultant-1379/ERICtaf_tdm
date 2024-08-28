package com.ericsson.cifwk.tdm.infrastructure;

import static com.ericsson.cifwk.tdm.infrastructure.Profiles.INTEGRATION_TEST;
import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import org.jongo.Jongo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import com.github.mongobee.Mongobee;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

public abstract class BaseDbConfiguration {

    @Value("${spring.data.mongodb.host}")
    private String mongoHost;

    @Value("${spring.data.mongodb.backup_host}")
    private String backUpHost;

    @Value("${spring.data.mongodb.database}")
    protected String mongoDatabase;

    private static  String changeLogsPackage  = "com.ericsson.cifwk.tdm.infrastructure.changelogs";

    @Bean
    public IMongodConfig mongodConfig() throws IOException {
        return new MongodConfigBuilder()
                .version(Version.Main.PRODUCTION)
                .net(new Net(determineMongoPort(), Network.localhostIsIPv6()))
                .build();
    }

    @Bean
    public Jongo jongoProvider() throws UnknownHostException {
        MongoClient mongo = createMongoClient();
        // Jongo doesn't support the new MongoDatabase API currently, will be released in 1.5.0
        // https://github.com/bguerout/jongo/issues/254
        @SuppressWarnings("deprecation")
        DB db = mongo.getDB(mongoDatabase);
        return new Jongo(db);
    }

    @Bean
    @Profile("!" + INTEGRATION_TEST)
    public Mongobee mongobee(Environment environment) throws UnknownHostException {
        MongoClient mongo = createMongoClient();
        Mongobee runner = new Mongobee(mongo);
        runner.setDbName(mongoDatabase);
        runner.setSpringEnvironment(environment);
        runner.setChangeLogsScanPackage(changeLogsPackage);
        return runner;
    }

    protected abstract List<MongoCredential> determineMongoCredential();

    protected abstract int determineMongoPort();

    protected abstract int determineBackupMongoPort();

    private MongoClient createMongoClient() throws UnknownHostException {
        ServerAddress server = new ServerAddress(mongoHost, determineMongoPort());
        List<ServerAddress> servers = newArrayList(server);
        List<MongoCredential> credentials = determineMongoCredential();
        MongoClientOptions options = new MongoClientOptions.Builder().build();

        if (!backUpHost.isEmpty()) {
            ServerAddress server2 = new ServerAddress(backUpHost, determineBackupMongoPort());
            servers.add(server2);
        }


        return new MongoClient(servers, credentials, options);
    }
}
