FROM ubuntu:22.04

ARG JDK_VERSION=21
ARG ANDROID_CMDLINE_TOOLS_VERSION=11076708
ARG ANDROID_COMPILE_SDK=36
ARG ANDROID_TARGET_SDK=36.0.0
ARG GRADLE_VERSION=9.1.0
ARG PROFILER_VERSION=0.24.0

RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y \
        curl \
        wget \
        git \
        unzip \
        openjdk-${JDK_VERSION}-jdk-headless \
        && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME=/usr/lib/jvm/java-${JDK_VERSION}-openjdk-amd64
ENV PATH=$PATH:$JAVA_HOME/bin

ENV ANDROID_SDK_ROOT=/opt/android-sdk
ENV ANDROID_HOME=$ANDROID_SDK_ROOT
ENV PATH=$PATH:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools

RUN mkdir -p $ANDROID_SDK_ROOT/cmdline-tools && \
    cd /opt && \
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-${ANDROID_CMDLINE_TOOLS_VERSION}_latest.zip && \
    unzip commandlinetools-linux-${ANDROID_CMDLINE_TOOLS_VERSION}_latest.zip -d temp_cmdline && \
    mv temp_cmdline/cmdline-tools $ANDROID_SDK_ROOT/cmdline-tools/latest && \
    rm -rf temp_cmdline commandlinetools-linux-${ANDROID_CMDLINE_TOOLS_VERSION}_latest.zip

RUN yes | sdkmanager --licenses > /dev/null 2>&1 && \
    sdkmanager "platform-tools" "platforms;android-${ANDROID_COMPILE_SDK}"  \
    "build-tools;${ANDROID_TARGET_SDK}" "extras;google;m2repository" "extras;android;m2repository"

RUN wget -q https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip && \
    unzip gradle-${GRADLE_VERSION}-bin.zip -d /opt && \
    rm gradle-${GRADLE_VERSION}-bin.zip && \
    ln -s /opt/gradle-${GRADLE_VERSION}/bin/gradle /usr/local/bin/gradle

RUN wget -q https://repo1.maven.org/maven2/org/gradle/profiler/gradle-profiler/${PROFILER_VERSION}/gradle-profiler-${PROFILER_VERSION}.zip \
    && unzip gradle-profiler-${PROFILER_VERSION}.zip -d /opt \
    && rm gradle-profiler-${PROFILER_VERSION}.zip

ENV PATH=${PATH}:/opt/gradle-profiler-${PROFILER_VERSION}/bin

RUN apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /workspace