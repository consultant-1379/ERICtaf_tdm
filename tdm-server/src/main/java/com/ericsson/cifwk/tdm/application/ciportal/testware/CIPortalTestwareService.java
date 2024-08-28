package com.ericsson.cifwk.tdm.application.ciportal.testware;

import com.ericsson.cifwk.tdm.model.testware.ArtifactItems;
import com.ericsson.cifwk.tdm.model.testware.GAV;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class CIPortalTestwareService {

    @Autowired
    private CIPortalTestwareRepository ciPortalTestwareRepository;

    public List<String> getTestwareGroups() {
        ArtifactItems artifactItems = ciPortalTestwareRepository.getTestwareArtifacts();
        return mapTestwareGroups(artifactItems.getArtifacts());
    }

    @VisibleForTesting
    List<String> mapTestwareGroups(List<GAV> artifacts) {
        return artifacts.stream()
                .map(GAV::getGroupId)
                .distinct()
                .sorted()
                .collect(toList());
    }
}
