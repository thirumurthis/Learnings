##Dockerfile content
FROM <Image repo from ACR or Docker for centos/v7.6+>

LABEL org.label-schema.schema-version="1.0" \
  org.label-schema.name="spark/v3.1.2" \
  org.label-schema.version="1.1.0" \
  org.label-schema.description="Apache Spark 3+ Docker image Python build runtime environment." \
  org.label-schema.vendor="PySpark image" \
  org.label-schema.vcs-url="<git-repository-of-custom-spark-build>"


# Install addtional source repos that contains the Azul JDK
# 
#  File content of azure zulu repo
# [zulu-azure]
# name=zulu-azure Azul Systems packages.
# baseurl=http://repos.azul.com/azure-only/zulu/yum
# gpgkey=http://repos.azul.com/azure-only/azul.asc
# enabled=1
# gpgcheck=1
# Note the above content 

COPY zulu-azure.repo /etc/yum.repos.d

#
# MZ Azul JDK
# 
RUN yum install -y zulu-8-azure-jdk

#
# update Java JRE certs to the system ones so we can connect to internal services
#
RUN update-ca-trust && rm /usr/lib/jvm/zulu-8-azure/jre/lib/security/cacerts && ln -s /etc/pki/ca-trust/extracted/java/cacerts /usr/lib/jvm/zulu-8-azure/jre/lib/security/cacerts

# 
# udpate GIT to work with Microsoft Code, Also need Docker
#
RUN yum -y remove git && \
#    yum -y install https://centos7.iuscommunity.org/ius-release.rpm && \
    yum -y install https://repo.ius.io/ius-release-el7.rpm && \
    yum -y install git2u-all docker


#
# Install miniconda + python 3 + pip(3)
#
RUN yum -y update \
    && yum -y install bzip2 \
    && curl -sSL https://repo.continuum.io/miniconda/Miniconda3-latest-Linux-x86_64.sh -o /tmp/miniconda.sh \
    && bash /tmp/miniconda.sh -bfp /usr/local/ \
    && rm -rf /tmp/miniconda.sh \
    && conda install -y python=3 \
    && conda update conda --yes \
    && conda clean --all --yes \
    && rpm -e --nodeps bzip2 \
    && yum clean all

#
# Install Spark and Hadoop client.This can be updated to the latest version as well
#
RUN curl -L -o /tmp/spark.tgz "https://downloads.apache.org/spark/spark-3.1.2/spark-3.1.2-bin-hadoop3.2.tgz" && \
    tar -C /opt -xzf /tmp/spark.tgz && \
    ln -s /opt/spark-3.1.2-bin-hadoop3.2 /opt/spark


ENV SPARK_HOME=/opt/spark
ENV PATH="$PATH:${SPARK_HOME}/bin"
ENV PYTHONPATH="${SPARK_HOME}/python:${SPARK_HOME}/python/lib/py4j-0.10.7-src.zip:${SPARK_HOME}/python/lib/pyspark.zip"


#
# Install jq to process parsing json results 
#
RUN yum -y install jq


#
# Install python modules needed for building AHM analytics libraries
#
RUN pip install pytest findspark azure-storage-blob azure-storage-queue

#
# Install Databricks CLI for deployment
#
RUN pip install databricks-cli

# 
# Add the azure cli so we can access key vault to get Databricks credentials for deployment
# Refrence link: https://docs.microsoft.com/en-us/cli/azure/install-azure-cli-yum?view=azure-cli-latest

RUN rpm --import https://packages.microsoft.com/keys/microsoft.asc && \
  echo -e "[azure-cli]\nname=Azure CLI\nbaseurl=https://packages.microsoft.com/yumrepos/azure-cli\nenabled=1\ngpgcheck=1\ngpgkey=https://packages.microsoft.com/keys/microsoft.asc" > /etc/yum.repos.d/azure-cli.repo && \
  yumdownloader azure-cli && \
  rpm -ivh --nodeps azure-cli-*.rpm && \
  ln -s /usr/local/bin/python3.9 /usr/bin/python3


#
# Fire up a bash shell to run an builds
#
CMD ["/bin/bash"]
