package com.ericsson.cifwk.tdm.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

public class History implements Comparable<History>  {

    private String approvalStatus;

    private String approver;

    private String version;

    private String createdTime;

    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");

    public History(String version, final Map<String, Object> values, Date createdTime) {
        this.version = version;
        this.approvalStatus = Objects.toString(values.get("approvalStatus"), "");
        this.approver = Objects.toString(values.get("approver"), "");
        this.createdTime =  dateFormatter.format(createdTime);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final Version version) {
        this.version = version.toString();
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(final String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public String getApprover() {
        return approver;
    }

    public void setApprover(final String approver) {
        this.approver = approver;
    }

    public String getCreateTime() {
        return createdTime;
    }

    public void setCreateTime(final Date createTime) {
        this.createdTime = dateFormatter.format(createTime);
    }

    @Override
    public int compareTo(History o) {
        int comparison = getCreateTime().compareTo(o.getCreateTime());

        if (comparison == 0) {
            return getVersion().compareTo(o.getVersion());
        }
        return comparison;
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.
                hashCode(createdTime, version, approver, approvalStatus);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        History history = (History) o;
        return createdTime == history.createdTime &&
                approvalStatus == history.approvalStatus &&
                approver == history.approver &&
                version == history.version;
    }

}
