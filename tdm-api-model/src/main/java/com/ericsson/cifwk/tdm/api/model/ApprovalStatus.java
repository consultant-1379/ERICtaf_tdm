package com.ericsson.cifwk.tdm.api.model;

/**
 * Approval status workflow:
 *
 * UNAPPROVED <-> PENDING -> REJECTED or APPROVED -> UNAPPROVED
 */
public enum ApprovalStatus {

    UNAPPROVED {
        @Override
        public boolean canTransitionTo(ApprovalStatus status) {
            return PENDING.equals(status);
        }
    },

    PENDING {
        @Override
        public boolean canTransitionTo(ApprovalStatus status) {
            return !PENDING.equals(status);
        }
    },

    REJECTED {
        @Override
        public boolean canTransitionTo(ApprovalStatus status) {
            return UNAPPROVED.equals(status);
        }
    },

    APPROVED {
        @Override
        public boolean canTransitionTo(ApprovalStatus status) {
            return UNAPPROVED.equals(status);
        }
    },

    CANCELLED {
        @Override
        public boolean canTransitionTo(ApprovalStatus status) {
            return PENDING.equals(status);
        }
    };

    public abstract boolean canTransitionTo(ApprovalStatus status);
}
