package com.ericsson.cifwk.tdm.model.testware;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Objects.equal;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Objects;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "testwareArtifact")
public class GAV implements Serializable {

    private static final long serialVersionUID = -4557816590439289591L;

    @XmlElement
    private String groupId;
    @XmlElement
    private String artifactId;
    @XmlElement
    private String version;

    @XmlElement
    @XmlJavaTypeAdapter(DateAdapter.class)
    private Date dateCreated;
    @XmlElement
    private String testPom;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getTestPom() {
        return testPom;
    }

    public void setTestPom(String testPom) {
        this.testPom = testPom;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("groupId", groupId)
                .add("artifactId", artifactId)
                .add("version", version)
                .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(groupId, artifactId, version);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        GAV gav = (GAV) other;
        return equal(groupId, gav.groupId) &&
                equal(artifactId, gav.artifactId) &&
                equal(version, gav.version);
    }

    private static class DateAdapter extends XmlAdapter<String, Date> {

        private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        @Override
        public String marshal(Date v) throws Exception {
            return dateFormat.format(v);
        }

        @Override
        public Date unmarshal(String v) throws Exception {
            return dateFormat.parse(v);
        }
    }
}
