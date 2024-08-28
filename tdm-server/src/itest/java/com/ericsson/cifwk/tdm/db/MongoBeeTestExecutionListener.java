package com.ericsson.cifwk.tdm.db;

import com.github.mongobee.Mongobee;
import com.github.mongobee.exception.MongobeeException;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import org.jongo.Jongo;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

import java.lang.annotation.Annotation;
import java.util.Set;

public class MongoBeeTestExecutionListener implements TestExecutionListener {

    @Override
    public void beforeTestClass(TestContext testContext) throws Exception {
    }

    @Override
    public void prepareTestInstance(TestContext testContext) throws Exception {

    }

    @Override
    public void beforeTestMethod(TestContext testContext) throws Exception {
        final Annotation annotation = AnnotationUtils.findAnnotation(testContext.getTestClass(), MongoBee.class);
        MongoBee mongoBeeAnnotation = (MongoBee) annotation;
        if (mongoBeeAnnotation.invokeCleanBeforeMethod()) {
            clean(testContext);
        }
        process(testContext, mongoBeeAnnotation);
    }

    private void process(TestContext testContext, MongoBee annotation) throws MongobeeException {
        Environment environment = testContext.getApplicationContext().getEnvironment();
        String mongoHost = environment.getProperty("spring.data.mongodb.host");
        String mongoDatabase = environment.getProperty("spring.data.mongodb.database");
        int port = testContext.getApplicationContext().getBean(IMongodConfig.class).net().getPort();

        String mongoURI = String.format("mongodb://%s:%d/%s", mongoHost, port, mongoDatabase);

        Mongobee runner = new Mongobee(mongoURI);
        runner.setChangeLogsScanPackage(annotation.location());
        runner.setSpringEnvironment(environment);
        runner.setEnabled(true);
        runner.execute();
    }

    private void clean(TestContext testContext) {
        Jongo jongo = getJongo(testContext);

        Set<String> collectionNames = jongo.getDatabase().getCollectionNames();
        for (String collectionName : collectionNames) {
            jongo.getCollection(collectionName).drop();
        }
    }

    @Override
    public void afterTestMethod(TestContext testContext) throws Exception {
    }

    @Override
    public void afterTestClass(TestContext testContext) throws Exception {
    }

    private Jongo getJongo(TestContext testContext) {
        return testContext.getApplicationContext().getBean(Jongo.class);
    }
}
