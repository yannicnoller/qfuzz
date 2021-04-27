FROM ubuntu:18.04

MAINTAINER Yannic Noller <yannic.noller@acm.org>

# Dependencies
ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get -y update
RUN apt-get -y install git build-essential openjdk-8-jdk wget unzip ant python3 python3-numpy python3-scipy python3-matplotlib vim nano texlive-latex-base texlive-latex-recommended texlive-pictures screen
RUN update-java-alternatives --set /usr/lib/jvm/java-1.8.0-openjdk-amd64
RUN wget https://services.gradle.org/distributions/gradle-4.4.1-bin.zip -P /tmp
RUN unzip -d /opt/gradle /tmp/gradle-*.zip
ENV GRADLE_HOME=/opt/gradle/gradle-4.4.1
ENV PATH=${GRADLE_HOME}/bin:${PATH}

# Installing QFuzz
WORKDIR /root
ADD evaluation /root/qfuzz/evaluation
ADD tool /root/qfuzz/tool
ADD README.md /root/qfuzz/
WORKDIR /root/qfuzz/tool
RUN ./setup.sh
WORKDIR /root/qfuzz
