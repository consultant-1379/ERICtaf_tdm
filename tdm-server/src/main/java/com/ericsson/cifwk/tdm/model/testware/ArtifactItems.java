package com.ericsson.cifwk.tdm.model.testware;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "latest-testware")
public class ArtifactItems implements Serializable {

    private static final long serialVersionUID = 9071995669230190659L;

    @XmlElement(name = "testwareArtifact")
    private List<GAV> artifacts = newArrayList();

    public List<GAV> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(List<GAV> artifacts) {
        this.artifacts = artifacts;
    }

    public void addArtifact(GAV artifact) {
        artifacts.add(artifact);
    }
}
