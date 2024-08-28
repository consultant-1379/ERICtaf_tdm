package com.ericsson.cifwk.tdm.application.ciportal.testware;

import com.ericsson.cifwk.tdm.model.testware.ArtifactItems;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static com.ericsson.cifwk.tdm.application.util.XmlParser.parseObjectFile;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CIPortalTestwareServiceTest {

    @InjectMocks
    private CIPortalTestwareService ciPortalTestwareService;

    @Mock
    private CIPortalTestwareRepository ciPortalTestwareRepository;

    @Before
    public void setup() throws Exception {
        ArtifactItems artifactItems = parseObjectFile(
                "testware-groups/artifact-items.xml", ArtifactItems.class);
        when(ciPortalTestwareRepository.getTestwareArtifacts()).thenReturn(artifactItems);
    }

    @Test
    public void get_shouldRetrieveTestwareArtifacts_fromCIPortal() throws Exception {
        List<String> result = ciPortalTestwareService.getTestwareGroups();
        assertThat(result).containsExactly("group1", "group2", "group3").inOrder();
    }

    @Test
    public void test_artifactGroupMapper() throws Exception {
        ArtifactItems artifactItems = ciPortalTestwareRepository.getTestwareArtifacts();
        List<String> testwareGroups = ciPortalTestwareService.mapTestwareGroups(artifactItems.getArtifacts());
        assertThat(testwareGroups).containsExactly("group1", "group2", "group3").inOrder();
    }
}
