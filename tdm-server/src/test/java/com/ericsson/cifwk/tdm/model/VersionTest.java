package com.ericsson.cifwk.tdm.model;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.jongo.Mapper;
import org.jongo.bson.Bson;
import org.jongo.bson.BsonDocument;
import org.jongo.marshall.jackson.JacksonMapper;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class VersionTest {

    @Test
    public void incrementMinor_shouldCreateNewVersion() throws Exception {
        Version version = Version.INITIAL_VERSION;

        Version result = version.incrementMinor();

        assertThat(result).isNotSameAs(version);
    }

    @Test
    public void incrementMinor_shouldKeepMajorVersion() throws Exception {
        Version version = new Version(1, 1, 0);

        Version result = version.incrementMinor();

        assertThat(result).isEqualTo(new Version(1, 1, 1));
    }

    @Test
    public void comparison() throws Exception {
        Version v1_0_0 = new Version(1, 0, 0);
        Version v1_1_0 = new Version(1, 1, 0);
        Version v2_0_0 = new Version(2, 0, 0);

        assertThat(v1_0_0).isEqualTo(v1_0_0);
        assertThat(v1_0_0).isLessThan(v1_1_0);
        assertThat(v1_0_0).isLessThan(v2_0_0);

        assertThat(v1_1_0).isGreaterThan(v1_0_0);
        assertThat(v1_1_0).isEqualTo(v1_1_0);
        assertThat(v1_1_0).isLessThan(v2_0_0);

        assertThat(v2_0_0).isGreaterThan(v1_0_0);
        assertThat(v2_0_0).isGreaterThan(v1_1_0);
        assertThat(v2_0_0).isEqualTo(v2_0_0);
    }

    @Test
    public void jongo_shouldMarshall_version() throws Exception {
        Version version = new Version(1, 0, 0);

        Mapper mapper = new JacksonMapper.Builder().build();
        BsonDocument bsonDocument = mapper.getMarshaller().marshall(version);

        assertThat(bsonDocument.toString()).isEqualTo("{ \"major\" : 1 ," +
                " \"minor\" : 0 ," +
                " \"build\" : 0 ," +
                " \"snapshot\" : true}");
    }

    @Test
    public void jongo_shouldUnmarshall_version() throws Exception {
        String version = "{ \"major\" : 1 , \"minor\" : 0, \"build\" : 0}";
        DBObject jsonDocument = (DBObject) JSON.parse(version);
        BsonDocument bsonDocument = Bson.createDocument(jsonDocument);

        Mapper mapper = new JacksonMapper.Builder().build();
        Version result = mapper.getUnmarshaller().unmarshall(bsonDocument, Version.class);

        assertThat(result).isEqualTo(new Version(1, 0, 0));
    }

    @Test
    public void version_check_equals() throws Exception {
        Version version = new Version(0, 0, 1);
        Version compareVersion = new Version(0, 0, 1);
        Version badVersion = new Version(1, 1, 1);

        assertThat(version.isLessThanOrEqual(compareVersion)).isTrue();
        assertThat(version.isLessThanOrEqual(badVersion)).isTrue();
    }

    @Test
    public void version_check_greaterThan() throws Exception {
        Version version = new Version(0, 0, 1);
        Version buildVersion = new Version(0, 0, 2);
        Version minorVersion = new Version(0, 1, 1);
        Version majorVersion = new Version(1, 0, 1);

        assertThat(version.isLessThanOrEqual(buildVersion)).isTrue();
        assertThat(version.isLessThanOrEqual(minorVersion)).isTrue();
        assertThat(version.isLessThanOrEqual(majorVersion)).isTrue();
    }

    @Test
    public void version_check_lessThan() throws Exception {
        Version version = new Version(2, 0, 0);
        Version buildVersion = new Version(0, 0, 2);
        Version minorVersion = new Version(0, 1, 1);
        Version majorVersion = new Version(1, 0, 1);

        assertThat(version.isLessThanOrEqual(buildVersion)).isFalse();
        assertThat(version.isLessThanOrEqual(minorVersion)).isFalse();
        assertThat(version.isLessThanOrEqual(majorVersion)).isFalse();
    }

    @Test
    public void isTheSameNumericVersion_compare_same_numeric_versions() {
        Version version1 = new Version(1, 0, 1);
        Version version2 = new Version(1, 0, 1);
        Version version3 = new Version(1, 1, 1);
        Version version4 = new Version(2, 1, 1);

        assertThat(version1.isTheSameNumericVersion(version2)).isTrue();
        assertThat(version3.isTheSameNumericVersion(version2)).isFalse();
        assertThat(version3.isTheSameNumericVersion(version4)).isFalse();
    }

    @Test
    public void isTheSameNumericVersion_compare_different_numeric_versions() {
        Version version0 = new Version (1, 0, 1);
        Version version1 = new Version(1, 0, 1);
        Version version2 = new Version(1, 0, 2);
        Version version3 = new Version(0, 0, 9);
        Version version4 = new Version(2, 0, 9);

        assertThat(version0.isTheSameNumericVersion(version1)).isTrue();
        assertThat(version1.isTheSameNumericVersion(version2)).isFalse();
        assertThat(version1.isTheSameNumericVersion(version3)).isFalse();
        assertThat(version3.isTheSameNumericVersion(version4)).isFalse();
    }


}