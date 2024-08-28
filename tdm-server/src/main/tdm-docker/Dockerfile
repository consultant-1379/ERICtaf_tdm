FROM armdocker.rnd.ericsson.se/proj_taf_te/tdm_base_images/frolvlad/alpine-oraclejre8:slim

# Maintainer
LABEL MAINTAINER "Sathish Chintapalli <sathish.chintapalli@tcs.com>"

# Set the reset cache variable
ENV REFRESHED_AT 2017-08-08

ADD tdm-application.jar tdm-application.jar
RUN sh -c 'touch /tdm-application.jar'

# Avoid root user usage tdmuser
RUN addgroup -S tdmuser \
    && adduser -S -g tdmuser tdmuser

# Create log directory
RUN mkdir -p /var/log/tdm \
    && chown -R tdmuser:tdmuser /var/log/tdm ;\
    mkdir -p /var/log/tdmldap \
    && chown -R tdmuser:tdmuser /var/log/tdmldap

USER root

RUN apk --update add openldap-clients
COPY cert2.p12 /keystore.p12

USER tdmuser

VOLUME ["/tmp", "/var/log/tdm"]

COPY ldapsearch.sh /var/log/tdmldap/ldapsearch.sh

# To reduce Tomcat startup time we added a system property pointing to "/dev/urandom" as a source of entropy.
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/tdm-application.jar"]
