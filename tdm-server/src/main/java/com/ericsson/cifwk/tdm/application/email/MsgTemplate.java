package com.ericsson.cifwk.tdm.application.email;

public enum MsgTemplate {

    NOTIFY_DS_CHANGE_SUBJ_EN
        ("notify_data_source_change_subj_en.ftl"),

    NOTIFY_DS_APPROVED_BODY_EN
        ("notify_ds_approved_body_en.ftl"),

    NOTIFY_DS_PENDING_BODY_EN
        ("notify_ds_pending_body_en.ftl"),

    NOTIFY_DS_CANCELLED_BODY_EN
            ("notify_ds_cancelled_body_en.ftl"),

    NOTIFY_DS_DELETED_BODY_EN
            ("notify_ds_deleted_body_en.ftl"),

    NOTIFY_DS_REJECTED_BODY_EN
        ("notify_ds_rejected_body_en.ftl");

    private String name;

    MsgTemplate(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
