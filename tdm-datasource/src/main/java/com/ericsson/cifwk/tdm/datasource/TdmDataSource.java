package com.ericsson.cifwk.tdm.datasource;

import com.ericsson.cifwk.taf.datasource.ConfigurationSource;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordImpl;
import com.ericsson.cifwk.taf.datasource.DataRecordModifier;
import com.ericsson.cifwk.taf.datasource.DataRecordProxyFactory;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.tdm.api.model.DataSourceIdentity;
import com.ericsson.cifwk.tdm.api.model.Lock;
import com.ericsson.cifwk.tdm.api.model.LockType;
import com.ericsson.cifwk.tdm.client.TDMClient;
import com.ericsson.cifwk.tdm.lock.LockAttributes;
import com.ericsson.cifwk.tdm.lock.LockFactory;
import com.ericsson.cifwk.tdm.lock.LockProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 23/05/2016
 */
public class TdmDataSource implements TestDataSource<DataRecord> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TdmDataSource.class);

    private List<DataRecord> dataRecords = newArrayList();

    private DataSourceIdentity dataSourceId;

    private String testwarePackage;

    private TDMClient tdmClient;
    private String columns;
    private List<String> filter;
    private List<Integer> limit;
    private List<Lock> locks = newArrayList();
    private String version;

    private boolean isClosed;

    private boolean isInitialized;


    public TdmDataSource(DataSourceIdentity dataSourceId, String testwarePackage, TDMClient tdmClient,
                         String columns, List<String> filter, List<Integer> limit, String version) {
        this.dataSourceId = dataSourceId;
        this.testwarePackage = testwarePackage;
        this.tdmClient = tdmClient;
        this.columns = columns;
        this.filter = filter;
        this.limit = limit;
        this.version = version;
    }

    @Override
    public void init(ConfigurationSource configurationSource) {
        checkState(!isInitialized, "Data Source already initialized. Data Source ID: %s", dataSourceId);
        isInitialized = true;

        LOGGER.info("Initializing TDM DataSource: " + dataSourceId);

        LockProperties lockProperties = new LockProperties(configurationSource);
        LockFactory lockFactory = new LockFactory(tdmClient);

        List<LockType> lockTypes = lockProperties.getLockTypes();

        limit = normaliseWithFilter(filter, limit, Integer.class, Integer.valueOf(1));
        lockTypes = normaliseWithFilter(filter, lockTypes, LockType.class, LockProperties.TDM_DEFAULT_LOCK_TYPE);

        if (!filter.isEmpty()) {
            for (int i = 0; i < filter.size(); i++) {
                LockAttributes lockAttributes = new LockAttributes(dataSourceId, testwarePackage, limit.get(i),
                    analyseFilter(filter.get(i)), columns, lockTypes.get(i), version);

                Lock lock = lockFactory.create(lockProperties, lockAttributes);
                locks.add(lock);
                findDataRecords(lock);
            }
        } else {
            LockAttributes lockAttributes = new LockAttributes(dataSourceId, testwarePackage, limit.get(0),
                    Collections.<String>emptyList(), columns, lockTypes.get(0), version);

            Lock lock = lockFactory.create(lockProperties, lockAttributes);
            locks.add(lock);
            findDataRecords(lock);
        }

    }

    private void findDataRecords(Lock lock) {
        List<DataRecord> mappedRecords = newArrayList();
        for (com.ericsson.cifwk.tdm.api.model.DataRecord record : lock.getDataSourceExecution().getRecords()) {
            mappedRecords.add(DataRecordProxyFactory
                    .createProxy(new DataRecordImpl(record.getValues()), DataRecord.class));
        }
        this.dataRecords.addAll(mappedRecords);
    }

    private static <T> List<T> normaliseWithFilter(List<String> filter, List<T> listToNormalise, Class<T> clazz,
                                            Object defaultValue) {
        List<T> newList = newArrayList();

        if (filter.isEmpty()) {
            if (listToNormalise.isEmpty()) {
                newList.add(clazz.cast(defaultValue));
            } else {
                newList.add(clazz.cast(listToNormalise.get(0)));
            }
        } else {
            for (int i = 0; i < filter.size(); i++) {
                if (listToNormalise.size() <= i || listToNormalise.isEmpty()) {
                    newList.add(clazz.cast(defaultValue));
                } else {
                    newList.add(clazz.cast(listToNormalise.get(i)));
                }
            }
        }
        return newList;
    }

    private static List<String> analyseFilter(String filter) {
        if (filter.contains(";")) {
            return newArrayList(filter.split(";"));
        }
        return newArrayList(filter);
    }

    @Override
    public Iterator<DataRecord> iterator() {
        checkState(!isClosed, "Data source closed");
        return dataRecords.iterator();
    }

    @Override
    public DataRecordModifier addRecord() {
        throw new UnsupportedOperationException("Adding record to TDM Data Source is not supported.");
    }

    @Override
    public void close() throws IOException {
        if (isClosed) {
            return;
        }
        LOGGER.info("Closing TDM DataSource: " + dataSourceId);
        for (Lock lockItem : locks) {
            tdmClient.getLockService().releaseLock(lockItem.getId()).execute();
            tdmClient.getExecutionService().finishExecution(lockItem.getDataSourceExecution()
                    .getExecutionId()).execute();
        }
        isClosed = true;
    }

    @Override
    public TestDataSource getSource() {
        return null;
    }
}
