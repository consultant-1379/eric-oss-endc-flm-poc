#
# COPYRIGHT Ericsson 2024
#
#
#
# The copyright to the computer program(s) herein is the property of
#
# Ericsson Inc. The programs may be used and/or copied only with written
#
# permission from Ericsson Inc. or in accordance with the terms and
#
# conditions stipulated in the agreement/contract under which the
#
# program(s) have been supplied.
#

ARG CBOS_IMAGE_TAG=6.15.0-9
ARG CBOS_IMAGE_REPO=armdocker.rnd.ericsson.se/proj-ldc/common_base_os_release
ARG CBOS_IMAGE_NAME=sles

FROM ${CBOS_IMAGE_REPO}/${CBOS_IMAGE_NAME}:${CBOS_IMAGE_TAG}
ARG CBOS_IMAGE_TAG
ARG CBOS_REPO_URL=https://arm.sero.gic.ericsson.se/artifactory/proj-ldc-repo-rpm-local/common_base_os/sles/${CBOS_IMAGE_TAG}

#Install Apache server
RUN zypper ar -C -G -f ${CBOS_REPO_URL}?ssl_verify=no \
    COMMON_BASE_OS_SLES_REPO \
    && zypper install -l -y apache2 catatonit shadow \
    && zypper clean --all \
    && zypper rr COMMON_BASE_OS_SLES_REPO

ARG USER_ID=40514
ARG USER_NAME="eric-oss-endc-flm-poc"

RUN echo "$USER_ID:!::0:::::" >>/etc/shadow \
    && echo "$USER_ID:x:$USER_ID:0:An Identity for $USER_NAME:/nonexistent:/bin/false" >>/etc/passwd \
    && chown -R ${USER_ID}:0 /srv/www/htdocs/ /etc/apache2/ /usr/sbin/*apache* /var/log/apache2/ \
    && chmod -R g=u /srv/www/htdocs/ /etc/apache2/ /usr/sbin/*apache* /var/log/apache2/

COPY --chown=${USER_ID}:0 build/ /srv/www/htdocs/eric-oss-endc-flm-poc-ui
COPY --chown=${USER_ID}:0 docker/httpd.conf /tmp/apache2/httpd.conf
COPY --chown=${USER_ID}:0 docker/entrypoint.sh /usr/sbin/entrypoint.sh

WORKDIR /app

RUN chmod +x /usr/sbin/entrypoint.sh \
    && chown -R ${USER_ID}:0 /app

USER $USER_ID

ENTRYPOINT ["/usr/bin/catatonit", "--"]
CMD ["/usr/sbin/entrypoint.sh"]
EXPOSE 8080

ARG COMMIT
ARG BUILD_DATE
ARG APP_VERSION
ARG RSTATE
ARG IMAGE_PRODUCT_NUMBER
LABEL \
    org.opencontainers.image.title=eric-oss-endc-flm-poc-ui-jsb \
    org.opencontainers.image.created=$BUILD_DATE \
    org.opencontainers.image.revision=$COMMIT \
    org.opencontainers.image.vendor=Ericsson \
    org.opencontainers.image.version=$APP_VERSION \
    com.ericsson.product-revision="${RSTATE}" \
    com.ericsson.product-number="$IMAGE_PRODUCT_NUMBER"