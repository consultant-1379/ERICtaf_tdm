package com.ericsson.cifwk.tdm.presentation.validation.datasources;

import com.ericsson.cifwk.tdm.api.model.DataSourceCopyRequest;
import com.ericsson.cifwk.tdm.api.model.DataSourceIdentity;
import com.ericsson.cifwk.tdm.application.datasources.DataSourceService;
import com.google.common.base.Strings;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 09/05/2016
 */
public class DataSourceIdValidator implements ConstraintValidator<DataSourceIdExists, DataSourceCopyRequest> {

    @Autowired
    DataSourceService dataSourceService;
    private boolean allowNull;

    @Override
    public void initialize(DataSourceIdExists constraintAnnotation) {
        allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(DataSourceCopyRequest value, ConstraintValidatorContext context) {
        if (allowNull && Strings.isNullOrEmpty(value.getDataSourceId())) {
            return true;
        }

        if (ObjectId.isValid(value.getDataSourceId())) {
            Optional<DataSourceIdentity> dataSourceIdentity = dataSourceService.findById(value.getDataSourceId());
            return dataSourceIdentity.isPresent();
        }
        return false;
    }
}
