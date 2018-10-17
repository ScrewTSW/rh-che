# Copyright (c) 2017 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

FROM library/centos:centos7

ENV LANG=en_US.utf8 \
    DISPLAY=:99 \
    FABRIC8_USER_NAME=fabric8

COPY google-chrome.repo /etc/yum.repos.d/google-chrome.repo
RUN groupadd docker -g 1000 && \
    useradd --user-group --create-home --shell /usr/bin/bash -G wheel,docker ${FABRIC8_USER_NAME} && \
    yum update --assumeyes && \
    yum install --assumeyes epel-release && \
    yum install --assumeyes chromedriver && \
    yum install --assumeyes google-chrome-stable && \
    yum install --assumeyes \
        xorg-x11-server-Xvfb \
        java-1.8.0-openjdk \
        java-1.8.0-openjdk-devel \
        git \
        docker \
        centos-release-scl && \
    yum install --assumeyes \
        rh-maven33 \
        rh-nodejs8 && \
    yum clean all --assumeyes && \
    rm -rf /var/cache/yum

USER ${FABRIC8_USER_NAME}
WORKDIR /home/${FABRIC8_USER_NAME}/che/

RUN cd /home/${FABRIC8_USER_NAME}/ && \
    git clone https://github.com/redhat-developer/rh-che.git && \
    cd /home/${FABRIC8_USER_NAME}/rh-che && \
    scl enable rh-nodejs8 rh-maven33 "mvn clean install -B --projects functional-tests -Pnative -DskipTests=true"

RUN echo "$MARK" > /dev/null
COPY docker-entrypoint.sh /home/${FABRIC8_USER_NAME}/
ENTRYPOINT ["/home/fabric8/docker-entrypoint.sh"]
