package com.ericsson.cifwk.tdm.adapter;

import com.ericsson.cifwk.taf.ServiceRegistry;
import com.ericsson.cifwk.taf.api.Nullable;
import com.ericsson.cifwk.taf.configuration.Configuration;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataSourceContext;
import com.ericsson.cifwk.taf.datasource.DataSourceNotFoundException;
import com.ericsson.cifwk.taf.datasource.TafDataSourceProvider;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.datasource.UnknownDataSourceTypeException;
import com.ericsson.cifwk.taf.spi.DataSourceAdapter;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

import static com.ericsson.cifwk.tdm.adapter.TdmDataSourceConfiguration.DATA_SOURCE_TYPE;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 23/05/2016
 */
public class TdmDataSourceAdapter implements DataSourceAdapter<DataRecord> {

    private static final Logger LOG = LoggerFactory.getLogger(TdmDataSourceAdapter.class);

    @Override
    public Optional<TestDataSource<DataRecord>> provide(String name,
                                                        @Nullable Method method,
                                                        @Nullable DataSourceContext dataSourceContext,
                                                        @Nullable Predicate<? super DataRecord> predicate,
                                                        @Nullable Class<DataRecord> dataRecordType) {

        Configuration configuration = ServiceRegistry.getConfigurationProvider().get();
        TdmDataSourceConfiguration configurationSource = new TdmDataSourceConfiguration(name, configuration);

        String type = configurationSource.getProperty(DATA_SOURCE_TYPE);

        if (type == null || !SupportedTypes.contains(type)) {
            return Optional.absent();
        }

        TdmDataSourceFactory tdmDataSourceFactory = new TdmDataSourceFactory(configurationSource, method);
        TestDataSource<DataRecord> dataSource;
        try {
            dataSource = TafDataSourceProvider.provide(tdmDataSourceFactory, name, method,
                    dataSourceContext, predicate, dataRecordType);
        } catch (UnknownDataSourceTypeException e) { //NOSONAR
            return Optional.absent();
        } catch (DataSourceNotFoundException e) {
            LOG.debug("Data source not found", e);
            return Optional.absent();
        }

        return Optional.of(dataSource);
    }
}
