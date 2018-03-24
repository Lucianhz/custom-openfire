#在完成各种构建之后 比如
FROM openjdk:8-jdk as builder
COPY . /usr/src
RUN apt-get update -qq \
    && apt-get install -qqy ant \
    && cd /usr/src \
    && ant -Dhalt.on.plugin.error=true -Dno.package=true -f build/build.xml dist.bin

FROM registry.cn-shanghai.aliyuncs.com/mingshz/openjdk-utf8:8-jre

COPY --from=builder /usr/src/target/openfire /opt/openfire
#COPY --from=builder /usr/src/build/docker/entrypoint.sh /sbin/entrypoint.sh
WORKDIR /opt/openfire

RUN rm /opt/openfire/plugins/broadcast.jar
RUN rm /opt/openfire/plugins/callbackOnOffline.jar
RUN rm /opt/openfire/plugins/chatLogs.jar
RUN rm /opt/openfire/plugins/contentFilter.jar
RUN rm /opt/openfire/plugins/dbaccess.jar
RUN rm /opt/openfire/plugins/emailListener.jar
RUN rm /opt/openfire/plugins/emailOnAway.jar
RUN rm /opt/openfire/plugins/fastpath.jar
RUN rm /opt/openfire/plugins/gojara.jar
RUN rm /opt/openfire/plugins/hazelcast.jar
RUN rm /opt/openfire/plugins/jingleNodes.jar
RUN rm /opt/openfire/plugins/jmxweb.jar
RUN rm /opt/openfire/plugins/justmarried.jar
RUN rm /opt/openfire/plugins/kraken.jar
RUN rm /opt/openfire/plugins/loadStats.jar
RUN rm /opt/openfire/plugins/monitoring.jar
RUN rm /opt/openfire/plugins/motd.jar
RUN rm /opt/openfire/plugins/mucservice.jar
RUN rm /opt/openfire/plugins/nodejs.jar
RUN rm /opt/openfire/plugins/packetFilter.jar
RUN rm /opt/openfire/plugins/rayo.jar
RUN rm /opt/openfire/plugins/registration.jar
RUN rm /opt/openfire/plugins/search.jar
RUN rm /opt/openfire/plugins/sip.jar
RUN rm /opt/openfire/plugins/stunserver.jar
RUN rm /opt/openfire/plugins/subscription.jar
RUN rm /opt/openfire/plugins/userCreation.jar
RUN rm /opt/openfire/plugins/userImportExport.jar
RUN rm /opt/openfire/plugins/userMucList.jar
RUN rm /opt/openfire/plugins/userservice.jar
RUN rm /opt/openfire/plugins/xmldebugger.jar

ENV OPENFIRE_HOME=/opt/openfire

CMD ["/opt/openfire/bin/openfire.sh"]
