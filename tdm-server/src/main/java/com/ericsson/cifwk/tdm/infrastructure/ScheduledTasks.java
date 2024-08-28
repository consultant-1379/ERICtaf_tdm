package com.ericsson.cifwk.tdm.infrastructure;

import com.ericsson.cifwk.tdm.application.ciportal.testware.CIPortalTestwareRepository;
import com.ericsson.cifwk.tdm.application.contexts.ContextService;
import com.ericsson.cifwk.tdm.application.contexts.TceContextRepository;
import com.ericsson.cifwk.tdm.application.locks.LockDataService;
import com.ericsson.cifwk.tdm.infrastructure.lock.LockingService;
import com.ericsson.gic.tms.presentation.dto.ContextBean;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@EnableScheduling
public class ScheduledTasks implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledTasks.class);

    @Autowired
    private TceContextRepository tceContextRepo;

    @Autowired
    private ContextService contextService;

    @Autowired
    private CIPortalTestwareRepository ciPortalTestwareRepository;

    @Autowired
    private LockingService lockingService;

    @Autowired
    private LockDataService lockDataService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        runJobLoadContexts();
        runJobLoadTestwareGroups();
    }

    @Scheduled(cron = "${remote.tce.cron}")
    public void runJobLoadContexts() {
        lockingService.lock("loadContexts", this::loadContexts);
    }

    @VisibleForTesting
    void loadContexts() {
        List<ContextBean> contexts = tceContextRepo.getContexts();

        for (ContextBean context : contexts) {
            contextService.createOrUpdate(context);
            LOGGER.info("context {} loaded", context.getId());
        }
    }

    @Scheduled(cron = "${remote.ci.testware.cron}")
    public void runJobLoadTestwareGroups() {
        lockingService.lock("loadTestwareGroups", this::loadTestwareGroups);
    }

    @VisibleForTesting
    void loadTestwareGroups() {
        ciPortalTestwareRepository.getTestwareArtifacts();
    }

    @Scheduled(fixedRate = 60_000L, initialDelay = 30_000L) //run every 60 seconds
    public void expireLocks() {
        lockDataService.expireLocks();
    }
}
