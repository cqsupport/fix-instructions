#!/bin/bash
echo "GENERATING PRE-EXTRACTION CACHE FROM LUCENE LOCAL COPY"
echo "------------------------------------------------------"
set -e
QS_PATH=${QS_PATH-/mnt/crx/author/crx-quickstart}
JAR_ROOT=${JAR_ROOT-/mnt/crx/author/crx-quickstart/opt/helpers/preExtraction}
OAK_RUN_JAR_PATH="$JAR_ROOT/oak-run-1.10.8.jar"
PREUPGRADE_TASKS_JAR="$JAR_ROOT/pre-text-extract-lucene-1.0.jar"
LUCENE_INDEX_PATH_ROOT="$QS_PATH/repository/index/lucene-1579303574763/data"
LUCENE_INDEX_PATH="$LUCENE_INDEX_PATH_ROOT"
if [ ! -d $LUCENE_INDEX_PATH_ROOT ]
then
    echo "Lucene directory not found at $LUCENE_INDEX_PATH"
    exit 1
fi
TIKA_STATS_PATH="oak-binary-stats.csv"
PRE_EXTRACT_STORE_PATH=${PRE_EXTRACT_STORE_PATH-/mnt/crx/author/crx-quickstart/opt/helpers/preExtraction/store}
echo "Lucene index path: $LUCENE_INDEX_PATH"
echo "Pre extraction store path: $PRE_EXTRACT_STORE_PATH"
echo "Using oak run from $OAK_RUN_JAR_PATH"
echo "Using pre-text-extract-lucene from $PREUPGRADE_TASKS_JAR"
JAVA_VERSION=`java -version`
JAVA_LOCATION=`which java`
echo "Using java $JAVA_VERSION from $JAVA_LOCATION"
time java -cp $OAK_RUN_JAR_PATH:$PREUPGRADE_TASKS_JAR \
    PreExtractLuceneData \
    --index-dir $LUCENE_INDEX_PATH \
    --data-file $TIKA_STATS_PATH \
    --store-path $PRE_EXTRACT_STORE_PATH
