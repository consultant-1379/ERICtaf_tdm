const dataSourceEditConstants = {
    recordKeyDelete: 'RECORD_KEY_DELETE',
    recordKeyAdd: 'RECORD_KEY_ADD',
    recordKeyRename: 'RECORD_KEY_RENAME',
    recordAdd: 'RECORD_ADD',
    recordDelete: 'RECORD_DELETE',
    recordValueEdit: 'RECORD_VALUE_EDIT',
    columnOrderChange: 'COLUMN_ORDER_CHANGE',

    identityNameEdit: 'IDENTITY_NAME_EDIT',
    identityApprovalStatus: 'IDENTITY_APPROVAL_STATUS',
    identityGroupEdit: 'IDENTITY_GROUP_EDIT',
    identityVersionEdit: 'IDENTITY_VERSION_EDIT',
    identityKeyAdd: 'IDENTITY_KEY_ADD',
    identityKeyDelete: 'IDENTITY_KEY_DELETE',
    identityValueEdit: 'IDENTITY_VALUE_EDIT',
    htmlTagRegex: /(<([^>]+)>)/ig,

    unapproved: 'UNAPPROVED',
    errorInVersionMessage: 'Your version does not match the semantic versioning format: X.X.X-SNAPSHOT'
};
export default dataSourceEditConstants;
