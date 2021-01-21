# Reindex Oak indexes using pre-extracted text on binary files
On large AEM installations, reindexing (a pre-existing index) can be very slow.  Reindexing is slow due to the text-extraction that occurs on binaries such as PDF files, MS Office docs, images and movie files.  To speed up reindex, in the steps below, we pre-extract the text from the old index copy.

Example fulltext enabled lucene indexes in AEM - /oak:index/ntBaseLucene, /oak:index/damAssetLucene, and /oak:index/lucene.

# Steps

## Pre-extract binary-text from old indexes
1. Log into the server and create the folder ```preExtraction``` under ```crx-quickstart/opt/helpers```, for example ```crx-quickstart/opt/helpers/preExtraction```
2. Upload [generatefilelist.sh](generatefilelist.sh), [preextraction.sh](preextraction.sh), [pre-text-extract-lucene-1.0.jar](pre-text-extract-lucene-1.0.jar?raw=true) and [oak-run-1.10.8.jar](https://repo1.maven.org/maven2/org/apache/jackrabbit/oak-run/1.10.8/oak-run-1.10.8.jar) to the AEM Linux server under ```crx-quickstart/opt/helpers/preExtraction```.  Create the ```preExtraction``` folder.
3. Log into the server via ssh and run the following commands as root (instead of crx as the user id replace with your own environment's aem process user id):
    
        cd /mnt/crx/author/crx-quickstart/opt/helpers
        mkdir -p preExtraction/store
        chown -R crx: preExtraction
        cd preExtraction
        sudo -u crx bash -c "nohup bash generatefilelist.sh" &
4. Go to http://host/system/console/jmx/org.apache.jackrabbit.oak%3Aname%3DIndexCopier+support+statistics%2Ctype%3DIndexCopierStats and find the ```fsPath``` of the ```/oak:index/lucene``` index.  For example: ```/mnt/crx/author/crx-quickstart/repository/index/lucene-1576519499912```.  Get the timestamp from the fsPath path (e.g. ```1576519499912```) and edit this line in the ```preextraction.sh``` script:

    * Before:

            LUCENE_INDEX_PATH_ROOT="$QS_PATH/repository/index/lucene-1579303574763/data"

    * After:
 
            LUCENE_INDEX_PATH_ROOT="$QS_PATH/repository/index/lucene-1576519499912/data"

5. Now that the preextraction.sh script points to the right index path, now run this command:

        sudo -u crx bash -c "nohup bash preextraction.sh" &


## Run an online reindex
1. Go to http://host/system/console/configMgr/org.apache.jackrabbit.oak.plugins.blob.datastore.DataStoreTextProviderService and set the *Path* configuration value to /mnt/preExtraction/store
2. Go to http://host/crx/de/index.jsp (enable CRXDE if not enabled) and log in as admin
3. Browse to each of the index nodes that you want to reindex and set property reindex=true.  Here's one example index:
        /oak:index/lucene
4. Click *Save All* on the top left
5. Monitor reindexing via the error.log file.  See [here](https://helpx.adobe.com/experience-manager/kb/Analyzing-AEM-Indexing-Issues.html) for how to monitor indexing.

## Out-of-band or offline reindexing
1A. Create a checkpoint using the JMX console if AEM is running
   1. Go to this URL on the host:
      /system/console/jmx/org.apache.jackrabbit.oak%3Aname%3DSegment+node+store+checkpoint+management%2Ctype%3DCheckpointManager
   2. Click "createCheckpoint(long p1)"
   3. Enter 864000000 and click "Invoke"
   4. Copy the checkpoint id to a text file

1B. OR, stop AEM and create a checkpoint using oak-run console
   1. Run the command below to open an oak-run console shell
      ```
      java -Xmx2g -jar -oak-run-1.22.4.jar console /mnt/crx/author/crx-quickstart/repository/segmentstore
      ```
   2. When the oak-run console shell opens then run this command to create a checkpoint
      ```
      checkpoint 864000000
      ```
   3. Copy the checkpoint id to a text file
   4. Enter command ```:exit``` or hit _\[Ctrl]+c_ to close the shell
   
2. Run the out-of-band or offline reindex:
For S3 DataStore systems (include S3 DS jars in the classpath):
```
nohup java -Xmx2g -classpath ./oak-run-1.22.4.jar:/mnt/preExtraction/jackson-core-2.9.5.jar:/mnt/preExtraction/jackson-annotations-2.9.5.jar:/mnt/preExtraction/jackson-databind-2.9.5.jar:/mnt/crx/author/crx-quickstart/install/15/aws-java-sdk-osgi-1.10.27.jar \
org.apache.jackrabbit.oak.run.Main index -\
-reindex --read-write \
--pre-extracted-text-dir /mnt/preExtraction/store \
--index-paths=/oak:index/socialLucene,/oak:index/authorizables,/oak:index/commerceLucene,/oak:index/cqProjectLucene,/oak:index/cqPageLucene,/oak:index/damAssetLucene,/oak:index/ntBaseLucene,/oak:index/slingeventJob,/oak:index/workflowDataLucene,/oak:index/versionStoreIndex \
--checkpoint=890f552c-d7b5-459f-8097-8964b3905efd \
--s3ds=/mnt/crx/author/crx-quickstart/install/org.apache.jackrabbit.oak.plugins.blob.datastore.SharedS3DataStore.config \
/mnt/crx/author/crx-quickstart/repository/segmentstore &
```

For Azure DS systems (include S3 DS jars in the classpath):
```
nohup java -Xmx2g -classpath ./oak-run-1.22.4.jar:/mnt/preExtraction/jackson-core-2.9.5.jar:/mnt/preExtraction/jackson-annotations-2.9.5.jar:/mnt/preExtraction/jackson-databind-2.9.5.jar:/mnt/crx/author/crx-quickstart/install/15/aws-java-sdk-osgi-1.10.27.jar \
org.apache.jackrabbit.oak.run.Main index -\
-reindex --read-write \
--pre-extracted-text-dir /mnt/preExtraction/store \
--index-paths=/oak:index/socialLucene,/oak:index/authorizables,/oak:index/commerceLucene,/oak:index/cqProjectLucene,/oak:index/cqPageLucene,/oak:index/damAssetLucene,/oak:index/ntBaseLucene,/oak:index/slingeventJob,/oak:index/workflowDataLucene,/oak:index/versionStoreIndex \
--checkpoint=890f552c-d7b5-459f-8097-8964b3905efd \
--azureblobds=/mnt/crx/author/crx-quickstart/install/org.apache.jackrabbit.oak.plugins.blob.datastore.AzureDataStore.config \
/mnt/crx/author/crx-quickstart/repository/segmentstore &
```

3. The offline indexing cycle should automatically import the new index to the source repository at the end.  However, if it fails with an error or starts reindexing when you start AEM, then follow the steps documented [here](https://github.com/chetanmeh/oak-console-scripts/blob/master/src/main/groovy/felixconsole/indexImport/README.md)
