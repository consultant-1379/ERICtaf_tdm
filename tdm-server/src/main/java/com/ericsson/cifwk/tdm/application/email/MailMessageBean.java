package com.ericsson.cifwk.tdm.application.email;

import static org.springframework.util.CollectionUtils.isEmpty;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;
import java.util.Set;

public final class MailMessageBean {

    /**
     * Multiple recipients email addresses
     */
    private final Set<String> to;

    /**
     * CC Review Requester
     */
    private String cc;

    /**
     * Email template name to be used to generate the email subject
     */
    private final MsgTemplate subjTemplate;

    /**
     * The Map containing the parameters map for subject message which
     * will be useful in FreeMarker email.
     */
    private final Map<String, Object> subjectParams;

    /**
     * Email template name to be used to generate the email body
     */
    private final MsgTemplate bodyTemplate;

    /**
     * The Map containing the parameters map for body message which
     * will be useful in FreeMarker email.
     */
    private final Map<String, Object> bodyParams;

    private MailMessageBean(Set<String> to, final String cc, MsgTemplate subjTemplate,
            Map<String, Object> subjectParams, MsgTemplate bodyTemplate, Map<String, Object> bodyParams) {
        this.to = to;
        this.cc = cc;
        this.subjTemplate = subjTemplate;
        this.subjectParams = subjectParams;
        this.bodyTemplate = bodyTemplate;
        this.bodyParams = bodyParams;
    }

    public Set<String> getTo() {
        return to;
    }

    public String getCc() {
        return cc;
    }

    public MsgTemplate getSubjTemplate() {
        return subjTemplate;
    }

    public Map<String, Object> getSubjectParams() {
        return subjectParams;
    }

    public MsgTemplate getBodyTemplate() {
        return bodyTemplate;
    }

    public Map<String, Object> getBodyParams() {
        return bodyParams;
    }


    public static final class MailMessageBuilder {
        private Set<String> to;
        private String cc;
        private MsgTemplate subjTemplate;
        private Map<String, Object> subjectParams;
        private MsgTemplate bodyTemplate;
        private Map<String, Object> bodyParams;

        private MailMessageBuilder() {
        }

        public static MailMessageBuilder aMailMessageBean() {
            return new MailMessageBuilder();
        }

        public MailMessageBuilder withTo(Set<String> to) {
            this.to = to;
            return this;
        }

        public MailMessageBuilder withCc(final String email) {
            this.cc = email;
            return this;
        }

        public MailMessageBuilder withSubjTemplate(MsgTemplate subjTemplate) {
            this.subjTemplate = subjTemplate;
            return this;
        }

        public MailMessageBuilder withSubjectParams(Map<String, Object> subjectParams) {
            this.subjectParams = subjectParams;
            return this;
        }

        public MailMessageBuilder withSubjectParam(String key, Object value) {
            if (this.subjectParams == null) {
                this.subjectParams = newHashMap();
            }
            this.subjectParams.put(key, value);
            return this;
        }

        public MailMessageBuilder withBodyTemplate(MsgTemplate bodyTemplate) {
            this.bodyTemplate = bodyTemplate;
            return this;
        }

        public MailMessageBuilder withBodyParams(Map<String, Object> bodyParams) {
            this.bodyParams = bodyParams;
            return this;
        }

        public MailMessageBuilder withBodyParam(String key, Object value) {
            if (this.bodyParams == null) {
                this.bodyParams = newHashMap();
            }
            this.bodyParams.put(key, value);
            return this;
        }

        public MailMessageBean build() {
            checkArgument(!isEmpty(to), "At least one receiver email must be provided");
            checkArgument(subjTemplate != null, "Mail subject template must be defined");
            checkArgument(bodyTemplate != null, " Mail body template must be defined");
            return new MailMessageBean(to, cc, subjTemplate, subjectParams, bodyTemplate, bodyParams);
        }
    }
}
