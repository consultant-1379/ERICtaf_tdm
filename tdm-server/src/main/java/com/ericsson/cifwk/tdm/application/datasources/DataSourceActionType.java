package com.ericsson.cifwk.tdm.application.datasources;

import static com.ericsson.cifwk.tdm.application.datasources.DataSourceChangeType.DATA;
import static com.ericsson.cifwk.tdm.application.datasources.DataSourceChangeType.SCHEMA;
import static com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity.Attributes.APPROVAL_STATUS;
import static com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity.Attributes.APPROVER;
import static com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity.Attributes.COMMENT;
import static com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity.Attributes.REVIEWERS;
import static com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity.Attributes.REVIEW_REQUESTER;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.getLast;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;

import com.ericsson.cifwk.tdm.api.model.ApprovalStatus;
import com.ericsson.cifwk.tdm.api.model.DataSourceAction;
import com.ericsson.cifwk.tdm.model.DataRecordEntity;
import com.ericsson.cifwk.tdm.model.DataSourceActionEntity;
import com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity;
import com.ericsson.cifwk.tdm.model.Version;


public enum DataSourceActionType {
    RECORD_KEY_DELETE(SCHEMA) {
        @Override
        public void apply(DataSourceIdentityEntity identity,
                          SortedMap<String, DataRecordEntity> records,
                          DataSourceActionEntity action,
                          AppliedDataSourceActionAccumulator actions) {
            for (Map.Entry<String, DataRecordEntity> entity : records.entrySet()) {
                for (String key : action.getValues().keySet()) {
                    if (entity.getValue().getValues().remove(key) != null) {
                        actions.addAction(action, entity.getKey());
                    }
                }
            }
        }
    },

    RECORD_KEY_ADD(SCHEMA) {
        @Override
        public void apply(DataSourceIdentityEntity identity,
                          SortedMap<String, DataRecordEntity> records,
                          DataSourceActionEntity action,
                          AppliedDataSourceActionAccumulator actions) {
            for (Map.Entry<String, DataRecordEntity> entity : records.entrySet()) {
                actions.addAction(action, entity.getKey());
            }
        }
    },

    RECORD_KEY_RENAME(SCHEMA) {
        @Override
        public void apply(DataSourceIdentityEntity identity,
                          SortedMap<String, DataRecordEntity> records,
                          DataSourceActionEntity action,
                          AppliedDataSourceActionAccumulator actions) {
            for (Map.Entry<String, DataRecordEntity> entity : records.entrySet()) {
                Map<String, Object> values = entity.getValue().getValues();
                for (Map.Entry<String, Object> keyValue : action.getValues().entrySet()) {
                    if (values.containsKey(keyValue.getKey())) {
                        Object value = values.remove(keyValue.getKey());
                        values.put(keyValue.getValue().toString(), value);
                        actions.addAction(action, entity.getKey());
                    }
                }
            }
        }
    },

    RECORD_ADD(DATA) {
        @Override
        public void apply(DataSourceIdentityEntity identity,
                          SortedMap<String, DataRecordEntity> records,
                          DataSourceActionEntity action,
                          AppliedDataSourceActionAccumulator actions) {
            DataRecordEntity entity = new DataRecordEntity();
            entity.setDataSourceId(identity.getId());

            entity.getValues().putAll(action.getValues());
            if (!action.getParentId().startsWith(DataSourceAction.NEW_PREFIX)) {
                entity.setId(action.getParentId());
            }
            actions.addAction(action, action.getParentId());

            records.put(action.getParentId(), entity);
        }
    },

    RECORD_DELETE(DATA) {
        @Override
        public void apply(DataSourceIdentityEntity identity,
                          SortedMap<String, DataRecordEntity> records,
                          DataSourceActionEntity action,
                          AppliedDataSourceActionAccumulator actions) {
            DataRecordEntity entity = records.get(action.getParentId());
            if (entity != null && entity.isPersisted()) {
                final DataRecordEntity dataRecordEntity = records.get(entity.getId());
                dataRecordEntity.setDeleted(true);
                dataRecordEntity.deletedIn(identity.getVersion());
                actions.addAction(action, action.getParentId());
            } else {
                actions.removePreviousActionIfExists(RECORD_ADD, action.getParentId(), null);
                actions.removePreviousActionIfExists(RECORD_VALUE_EDIT, action.getParentId(), null);
                records.remove(action.getParentId());
            }
        }
    },

    RECORD_VALUE_EDIT(DATA) {
        @Override
        public void apply(DataSourceIdentityEntity identity,
                          SortedMap<String, DataRecordEntity> records,
                          DataSourceActionEntity action,
                          AppliedDataSourceActionAccumulator actions) {
            DataRecordEntity entity = records.get(action.getParentId());
            entity.getValues().putAll(action.getValues());
            actions.addAction(action, action.getParentId());
        }
    },

    IDENTITY_NAME_EDIT(SCHEMA) {
        @Override
        public void apply(DataSourceIdentityEntity identity,
                          SortedMap<String, DataRecordEntity> records,
                          DataSourceActionEntity action,
                          AppliedDataSourceActionAccumulator actions) {
            Collection<Object> values = action.getValues().values();
            checkArgument(values.size() == 1);
            identity.setName(getLast(values).toString().trim());
            actions.addAction(action, identity.getId());
        }
    },

    IDENTITY_GROUP_EDIT(SCHEMA) {
        @Override
        public void apply(DataSourceIdentityEntity identity,
                          SortedMap<String, DataRecordEntity> records,
                          DataSourceActionEntity action,
                          AppliedDataSourceActionAccumulator actions) {
            Collection<Object> values = action.getValues().values();
            checkArgument(values.size() == 1);
            identity.setGroup(getLast(values).toString());
            actions.addAction(action, identity.getId());
        }
    },

    IDENTITY_VERSION_EDIT(SCHEMA) {
        @Override
        public void apply(DataSourceIdentityEntity identity,
                          SortedMap<String, DataRecordEntity> records,
                          DataSourceActionEntity action,
                          AppliedDataSourceActionAccumulator actions) {
            Collection<Object> values = action.getValues().values();
            checkArgument(values.size() == 1);
            identity.setVersion(new Version(getLast(values).toString()));
            actions.addAction(action, identity.getId());
        }
    },

    IDENTITY_KEY_DELETE(SCHEMA) {
        @Override
        public void apply(DataSourceIdentityEntity identity,
                          SortedMap<String, DataRecordEntity> records,
                          DataSourceActionEntity action,
                          AppliedDataSourceActionAccumulator actions) {
            for (String key : action.getValues().keySet()) {
                identity.getProperties().remove(key);
                if (!actions.removePreviousActionIfExists(IDENTITY_KEY_ADD, identity.getId(), key)) {
                    actions.addAction(action, identity.getId());
                }
            }
        }
    },

    IDENTITY_KEY_ADD(SCHEMA) {
        @Override
        public void apply(DataSourceIdentityEntity identity,
                          SortedMap<String, DataRecordEntity> records,
                          DataSourceActionEntity action,
                          AppliedDataSourceActionAccumulator actions) {
            identity.getProperties().putAll(action.getValues());
            actions.addAction(action, identity.getId());
        }
    },

    IDENTITY_KEY_RENAME(SCHEMA) {
        @Override
        public void apply(DataSourceIdentityEntity identity,
                          SortedMap<String, DataRecordEntity> records,
                          DataSourceActionEntity action,
                          AppliedDataSourceActionAccumulator actions) {
            Map<String, Object> properties = identity.getProperties();
            for (Map.Entry<String, Object> keyValue : action.getValues().entrySet()) {
                checkArgument(properties.containsKey(keyValue.getKey()), "TODO erorr message here"); //TODO
                Object value = properties.remove(keyValue.getKey());
                properties.put(keyValue.getValue().toString(), value);
                actions.addAction(action, identity.getId());
            }
        }
    },

    IDENTITY_VALUE_EDIT(SCHEMA) {
        @Override
        public void apply(DataSourceIdentityEntity identity,
                          SortedMap<String, DataRecordEntity> records,
                          DataSourceActionEntity action,
                          AppliedDataSourceActionAccumulator actions) {
            for (String key : action.getValues().keySet()) {
                checkArgument(identity.getProperties().containsKey(key), "TODO erorr message here"); //TODO
            }
            identity.getProperties().putAll(action.getValues());
            actions.addAction(action, identity.getId());
        }
    },

    IDENTITY_APPROVAL_STATUS(SCHEMA) {
        @Override
        public void apply(DataSourceIdentityEntity identity,
                          SortedMap<String, DataRecordEntity> records,
                          DataSourceActionEntity action,
                          AppliedDataSourceActionAccumulator actions) {
            Map<String, Object> values = action.getValues();
            String approvalStatus = values.get(APPROVAL_STATUS).toString();

            identity.setApprovalStatus(ApprovalStatus.valueOf(approvalStatus));
            @SuppressWarnings("unchecked")
            final List<String> reviewers = (List<String>) values.get(REVIEWERS);
            identity.setReviewers(reviewers);
            identity.setComment(getValue(values, COMMENT));
            identity.setApprover(getValue(values, APPROVER));
            identity.setReviewRequester(getValue(values, REVIEW_REQUESTER));
            actions.addAction(action, identity.getId());
        }

        private String getValue(Map<String, Object> values, final String value) {
            return Optional.ofNullable(values.getOrDefault(value, "")).orElse("").toString();
        }
    },

    IDENTITY_DROP_EDIT(SCHEMA) {
        @Override
        public void apply(DataSourceIdentityEntity identity,
                          SortedMap<String, DataRecordEntity> records,
                DataSourceActionEntity action,
                AppliedDataSourceActionAccumulator actions) {
            //NO SONAR
        }
    },

    IDENTITY_INITIAL_CREATE(SCHEMA) {
        @Override
        public void apply(DataSourceIdentityEntity identity,
                          SortedMap<String, DataRecordEntity> records,
                          DataSourceActionEntity action,
                          AppliedDataSourceActionAccumulator actions) {
            identity.setId(action.getParentId());
            identity.populateFromValueMap(action.getValues());
        }
    },

    COLUMN_ORDER_CHANGE(SCHEMA) {
        @Override
        public void apply(DataSourceIdentityEntity identity,
                          SortedMap<String, DataRecordEntity> records,
                DataSourceActionEntity action,
                AppliedDataSourceActionAccumulator actions) {

            actions.addAction(action, identity.getId());
        }
    };

    private DataSourceChangeType type;

    DataSourceActionType(DataSourceChangeType type) {
        this.type = type;
    }

    public DataSourceChangeType getType() {
        return type;
    }

    public abstract void apply(DataSourceIdentityEntity identity,
                               SortedMap<String, DataRecordEntity> records,
                               DataSourceActionEntity action,
                               AppliedDataSourceActionAccumulator actions);

}
