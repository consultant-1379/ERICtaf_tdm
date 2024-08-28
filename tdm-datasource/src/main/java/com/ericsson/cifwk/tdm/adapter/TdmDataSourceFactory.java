package com.ericsson.cifwk.tdm.adapter;


import com.ericsson.cifwk.taf.datasource.ConfigurationSource;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataSourceFactory;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.datasource.UnknownDataSourceTypeException;
import com.ericsson.cifwk.taf.management.TafRunnerContext;
import com.ericsson.cifwk.tdm.api.model.Context;
import com.ericsson.cifwk.tdm.api.model.DataSourceIdentity;
import com.ericsson.cifwk.tdm.client.TDMClient;
import com.ericsson.cifwk.tdm.client.services.ContextService;
import com.ericsson.cifwk.tdm.client.services.DataSourceService;
import com.ericsson.cifwk.tdm.datasource.TdmDataSource;
import com.ericsson.cifwk.tdm.datasource.TdmDataSourceBuilder;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import retrofit2.Call;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import static com.ericsson.cifwk.tdm.adapter.TdmDataSourceConfiguration.TDM_APPROVED;
import static com.ericsson.cifwk.tdm.adapter.TdmDataSourceConfiguration.TDM_DATASOURCE_COLUMNS;
import static com.ericsson.cifwk.tdm.adapter.TdmDataSourceConfiguration.TDM_DATASOURCE_CONTEXT;
import static com.ericsson.cifwk.tdm.adapter.TdmDataSourceConfiguration.TDM_DATASOURCE_FILTER;
import static com.ericsson.cifwk.tdm.adapter.TdmDataSourceConfiguration.TDM_DATASOURCE_ID;
import static com.ericsson.cifwk.tdm.adapter.TdmDataSourceConfiguration.TDM_DATASOURCE_LABEL;
import static com.ericsson.cifwk.tdm.adapter.TdmDataSourceConfiguration.TDM_DATASOURCE_NAME;
import static com.ericsson.cifwk.tdm.adapter.TdmDataSourceConfiguration.TDM_DATASOURCE_QUANTITY;
import static com.ericsson.cifwk.tdm.adapter.TdmDataSourceConfiguration.TDM_DATASOURCE_VERSION;
import static com.ericsson.cifwk.tdm.adapter.TdmDataSourceConfiguration.TDM_ENABLE_LOCK;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static org.slf4j.LoggerFactory.getLogger;

public final class TdmDataSourceFactory implements DataSourceFactory {

    static final String ERROR_PROPERTIES = "TDM DataSource properties are incorrectly defined. "
            + "Either label, ID or name and context should be defined";
    static final String ERROR_NOT_FOUND = "Specified DataSource could not be found: %s";
    static final String ERROR_NOT_APPROVED = "Specified DataSource is not approved";

    private static TestwareResolver testwareResolver = new TestwareResolver();

    private static final Logger LOGGER = getLogger(TdmDataSourceFactory.class);

    private TDMClient tdmClient;

    private String testwarePackage;

    private TdmDataSourceConfiguration tdmConfigurationSource;

    public TdmDataSourceFactory(TdmDataSourceConfiguration configurationSource, Method method) {
        this.tdmConfigurationSource = configurationSource;
        this.tdmClient = new TDMClient(configurationSource.getTdmHost());
        this.testwarePackage = testwareResolver.getTestwarePackage(method);
    }

    @Override
    public TestDataSource<DataRecord> createDataSource(String type, ConfigurationSource configurationSource)
            throws UnknownDataSourceTypeException {
        if (type != null && SupportedTypes.contains(type)) {
            DataSourceProperties properties = new DataSourceProperties(tdmConfigurationSource);
            try {
                DataSourceIdentity dataSource = findDataSource(properties, tdmClient);

                TdmDataSource tdmDataSource = new TdmDataSourceBuilder(dataSource, tdmClient)
                        .withTestwarePackage(testwarePackage)
                        .withColumns(properties.columns)
                        .withFilter(properties.filter)
                        .withLimit(properties.limit)
                        .withVersion(dataSource.getVersion())
                        .build();

                tdmDataSource.init(this.tdmConfigurationSource);

                TafRunnerContext context = TafRunnerContext.getContext();
                context.addCloseable(tdmDataSource);

                return tdmDataSource;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new UnknownDataSourceTypeException(type);
        }
    }

    private static DataSourceIdentity findDataSource(DataSourceProperties properties, TDMClient tdmClient)
    throws IOException {
        if (properties.hasLabelAndContext()) {
            return getByLabelAndContext(properties, tdmClient.getDataSourceService(), tdmClient.getContextService());
        }
        if (properties.hasId()) {
            return getById(properties, tdmClient.getDataSourceService());
        }
        return getByContextAndName(properties, tdmClient.getContextService(), tdmClient.getDataSourceService());
    }

    @VisibleForTesting
    static DataSourceIdentity getByLabelAndContext(DataSourceProperties properties, DataSourceService service,
            ContextService contextService)
    throws IOException {
        final Context context = getContext(properties.context, contextService);
        Call<DataSourceIdentity> call = service.getDataSourceByLabel(properties.label, context.getId());
        DataSourceIdentity dataSource = call.execute().body();
        checkNotNull(dataSource, String.format(ERROR_NOT_FOUND, properties));
        checkState(!properties.approved || dataSource.isApproved(), ERROR_NOT_APPROVED);
        return dataSource;
    }

    private static Context getContext(final String context, final ContextService contextService) throws IOException {
        final Call<Context> call = contextIsFullPath(context) ?
                contextService.getContextByPath(context) : getContextByName(context, contextService);
        final Context ctx = call.execute().body();
        checkNotNull(ctx, String.format("Specified context could not be found: %s", context));
        return ctx;
    }

    private static Call<Context> getContextByName(final String context, final ContextService contextService) {
        LOGGER.warn("Context name [{}] may not be unique on the system. You may get the wrong or no datasource. If "
                + "you run into issues use the full context path to ensure you get the correct context. Please read "
                + "the documentation for more information", context);
        return contextService.getContextByName(context);
    }

    private static boolean contextIsFullPath(final String context) {
        return context.contains("/");
    }

    private static DataSourceIdentity getById(DataSourceProperties properties, DataSourceService service)
    throws IOException {
        Call<DataSourceIdentity> call;
        if (properties.hasVersion()) {
            call = service.getDataSourceByIdAndVersion(properties.id, properties.version);
        } else {
            call = service.getDataSourceById(properties.id, properties.approved);
        }
        DataSourceIdentity dataSource = call.execute().body();
        checkNotNull(dataSource, String.format(ERROR_NOT_FOUND, properties));
        checkState(!properties.approved || dataSource.isApproved(), ERROR_NOT_APPROVED);
        return dataSource;
    }

    @VisibleForTesting
    static DataSourceIdentity getByContextAndName(DataSourceProperties properties, ContextService contextService,
            DataSourceService dataSourceService) throws IOException {
        final Context context = getContext(properties.context, contextService);
        DataSourceIdentity dataSource = dataSourceService
                .getLatestIdentityByContextAndName(context.getId(), properties.name, properties.approved).execute()
                .body();
        checkNotNull(dataSource, String.format(ERROR_NOT_FOUND, properties));
        String dataSourceId = dataSource.getId();
        if (properties.hasVersion()) {
            dataSource = dataSourceService.getDataSourceByIdAndVersion(dataSourceId, properties.version).execute()
                    .body();
            checkNotNull(dataSource, String.format(ERROR_NOT_FOUND, properties));
        }
        checkState(!properties.approved || dataSource.isApproved(), ERROR_NOT_APPROVED);
        return dataSource;
    }

    @VisibleForTesting
    static class DataSourceProperties {

        private final String id;
        private final String version;
        private final String context;
        private final String name;
        private final String label;
        private final boolean approved;
        private final List<String> filter;
        private final String columns;
        private final List<Integer> limit;
        private final boolean lockEnabled;

        DataSourceProperties(TdmDataSourceConfiguration config) {
            lockEnabled = Boolean.parseBoolean(System.getProperty(TDM_ENABLE_LOCK, "false"));
            id = config.getProperty(TDM_DATASOURCE_ID);
            version = config.getProperty(TDM_DATASOURCE_VERSION);
            context = config.getProperty(TDM_DATASOURCE_CONTEXT);
            name = config.getProperty(TDM_DATASOURCE_NAME);
            label = config.getProperty(TDM_DATASOURCE_LABEL);
            approved = Boolean.parseBoolean(config.getProperty(TDM_APPROVED, "true"));
            filter = config.getListProperty(TDM_DATASOURCE_FILTER);
            columns = config.getProperty(TDM_DATASOURCE_COLUMNS);
            limit = convertToIntList(config.getListProperty(TDM_DATASOURCE_QUANTITY));
            checkArgument(hasId() || hasContextAndName() || hasLabelAndContext(), ERROR_PROPERTIES);
        }

        private boolean hasId() {
            return id != null;
        }

        private boolean hasVersion() {
            return version != null;
        }

        private boolean hasContextAndName() {
            return context != null && name != null;
        }

        private boolean hasLabelAndContext() {
            return label != null && context != null;
        }

        private List<Integer> convertToIntList(List<String> items) {
            List<Integer> result = newArrayList();
            if (items.isEmpty()) {
                if (!lockEnabled) {
                    result.add(-1);
                }
                return result;
            }
            for (String item :items) {
                if (!lockEnabled) {
                    result.add(-1);
                } else {
                    result.add(Integer.parseInt(item));
                }
            }
            return result;
        }

        @Override
        public String toString() {
            StringBuilder details = new StringBuilder("[");
            if (hasId()) {
                details.append(" ID: ").append(id);
            }
            if (hasContextAndName()) {
                details.append(" Context: ").append(context).append(" Name: ").append(name);
            }
            if (hasLabelAndContext()) {
                details.append(" Label: ").append(label).append(" Context: ").append(context);
            }
            if (hasVersion()) {
                details.append(" Version: ").append(version);
            }
            return details.append(" ]").toString();
        }
    }
}
