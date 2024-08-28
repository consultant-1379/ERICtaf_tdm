package com.ericsson.cifwk.tdm.application.datasources;

import com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;

@Component
public class DataSourceHelper {

    static final String ERR_IDENTITY_ALREADY_EXISTS = "Data source already exists in context %s with name %s";
    static final String ERR_IDENTITY_NAME_WAS_DELETED =
            "Data source in context %s with name %s already exists but was deleted and cannot be re-used." +
                    " Please refer Online Help for more info!";

    @Autowired
    private DataSourceRepository dataSourceRepository;

    public void validateIdentityNameAndContext(DataSourceIdentityEntity identity) {
        Optional<DataSourceIdentityEntity> identityComparison =
                ofNullable(dataSourceRepository.findAllByContextIdAndName(identity.getContextId(), identity.getName()));

        if (identityComparison.isPresent()) {
            compareNamesAndContext(identity, identityComparison.get());
        }
    }

    public void validateDuplicateNameAndContextAndFail(DataSourceIdentityEntity identity) {
        Optional<DataSourceIdentityEntity> identityComparison =
                ofNullable(dataSourceRepository.findAllByContextIdAndName(identity.getContextId(), identity.getName()));
        if (identityComparison.isPresent()) {
            String errorMessage = getErrorMessage(identity, identityComparison.get().isDeleted());
            throw new IllegalArgumentException(String.valueOf(errorMessage));
        }
    }

    private void compareNamesAndContext(DataSourceIdentityEntity identity, DataSourceIdentityEntity compare) {
        checkArgument(identity.getId().equals(compare.getId()), getErrorMessage(identity, compare.isDeleted()));
    }

    private static String getErrorMessage(DataSourceIdentityEntity identity, boolean isDeleted) {
        if (isDeleted) {
            return format(ERR_IDENTITY_NAME_WAS_DELETED, identity.getContext(), identity.getName());
        } else {
            return format(ERR_IDENTITY_ALREADY_EXISTS, identity.getContext(), identity.getName());
        }
    }
}
