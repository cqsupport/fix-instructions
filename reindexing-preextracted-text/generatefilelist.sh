#!/bin/bash
echo "GENERATING CSV BLOB LIST FROM NODESTORE"
echo "-----------------------------------\n"

set -e

QS_PATH=${QS_PATH-/mnt/crx/author/crx-quickstart}

JAR_ROOT=${JAR_ROOT-/mnt/crx/author/crx-quickstart/opt/helpers/preExtraction}
OAK_RUN_JAR_PATH="$JAR_ROOT/oak-run-1.10.8.jar"

TIKA_STATS_PATH="oak-binary-stats.csv"

STORE='/mnt/crx/author/crx-quickstart/repository/segmentstore'

echo "Nodestore: $STORE"

echo "Using oak run from $OAK_RUN_JAR_PATH"

JAVA_VERSION=`java -version`
JAVA_LOCATION=`which java`
echo "Using java $JAVA_VERSION from $JAVA_LOCATION"

time java -jar $OAK_RUN_JAR_PATH \
        tika --fake-ds-path=temp \
        --data-file $TIKA_STATS_PATH\
        --generate \
        $STORE
