package com.ericsson.cifwk.tdm.infrastructure.lock;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static com.ericsson.cifwk.tdm.infrastructure.Profiles.INTEGRATION_TEST;
import static com.hazelcast.core.Hazelcast.newHazelcastInstance;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles(INTEGRATION_TEST)
public class HazelcastLockingServiceITest {

    @Autowired
    private ApplicationContext context;

    private Stack<Integer> stack = new Stack<>();

    @Test
    public void lock() throws Exception {
        HazelcastLockingService member1 = context.getBean(HazelcastLockingService.class);
        HazelcastLockingService member2 = context.getBean(HazelcastLockingService.class);
        ExecutorService service = newFixedThreadPool(2);

        String jobName = "test";
        service.submit(() -> member1.lock(jobName, this::job));
        service.submit(() -> member2.lock(jobName, this::job));
        service.awaitTermination(5, TimeUnit.SECONDS);

        assertThat(stack).containsExactly(42);
    }

    private void job() {
        try {
            Thread.sleep(1000);
            stack.push(42);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Configuration
    static class HazelcastITestConfiguration {

        @Bean
        @Scope(SCOPE_PROTOTYPE)
        public HazelcastLockingService hazelcastLockingService() {
            return new HazelcastLockingService(hazelcastInstance());
        }

        @Bean
        @Scope(SCOPE_PROTOTYPE)
        public HazelcastInstance hazelcastInstance() {
            return newHazelcastInstance(new Config());
        }
    }
}
