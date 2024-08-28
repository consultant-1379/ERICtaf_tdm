package com.ericsson.cifwk.tdm.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Integer.parseInt;
import static java.util.Comparator.comparingInt;

public class Version implements Comparable<Version> {

    public static final Version INITIAL_VERSION = new Version(0, 0, 1);

    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+).(\\d+)(-SNAPSHOT)?");

    private final int major;
    private final int minor;
    private final int build;

    private boolean snapshot;


    public Version(String version) {
        Matcher matcher = VERSION_PATTERN.matcher(version);
        checkArgument(matcher.find(), "Incorrect version: " + version);
        this.major = parseInt(matcher.group(1));
        this.minor = parseInt(matcher.group(2));
        this.build = parseInt(matcher.group(3));

        if (matcher.group(4) == null) {
            this.snapshot = false;
        } else {
            this.snapshot = true;
        }
    }

    @JsonCreator
    public Version(@JsonProperty("major") int major,
                   @JsonProperty("minor") int minor,
                   @JsonProperty("build") int build) {
        this.major = major;
        this.minor = minor;
        this.build = build;
        this.snapshot = true;
    }

    public Version incrementMinor() {
        return new Version(major, minor, build + 1);
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getBuild() {
        return build;
    }

    @Override
    public int compareTo(Version o) {
        return comparingInt(Version::getMajor)
                .thenComparing(Version::getMinor)
                .thenComparing(Version::getBuild)
                .compare(this, o);
    }

    @Override
    public String toString() {
        if (snapshot) {
            return major + "." + minor + "." + build + "-SNAPSHOT";
        } else {
            return major + "." + minor + "." + build;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(major, minor, build, snapshot);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Version version = (Version) o;
        return major == version.major &&
                minor == version.minor &&
                build == version.build &&
                snapshot == version.snapshot;
    }

    public boolean isLessThanOrEqual(Version compareVersion) {
        boolean isVersionLess = isLessThan(compareVersion);
        return isVersionLess || isTheSameNumericVersion(compareVersion);
    }

    public boolean isLessThan(final Version compareVersion) {
        return compareTo(compareVersion) < 0;
    }

    public boolean isTheSameNumericVersion(Version compareVersion) {
        return compareTo(compareVersion) == 0;
    }

    public boolean isSnapshot() {
        return snapshot;
    }

    public void setSnapshot(boolean snapshot) {
        this.snapshot = snapshot;
    }
}
