# Reindex Oak indexes using pre-extracted text on binary files
On large AEM installations, reindexing (a pre-existing index) can be very slow.  Reindexing is slow due to the text-extraction that occurs on binaries such as PDF files, MS Office docs, images and movie files.  To speed up reindex, in the steps below, we pre-extract the text from the old index copy.

Example fulltext enabled lucene indexes in AEM - /oak:index/ntBaseLucene, /oak:index/damAssetLucene, and /oak:index/lucene.

# Steps

## 1. (Optional optimization) Disable text extraction for some or all binary files

See [here for instructions](reindex-disable-tika.md).

Disabling text extraction on some or all binary files not only greatly speeds up reindexing but reduces the overall size of the index.  However, there is a trade-off, for each file type that is excluded from text-extraction, the contents of those files would not be searchable.  For example, if you exclude "application/pdf" (PDF files), you wouldn't be able to search on words contained in the PDF.

Note that if you completely disable all text-extraction then step 2 below isn't necessary.

## 2. Pre-extract text from old-lucene indexes for reindex

See [here for instructions](pre-extract-text-lucene.md).

## Perform Reindexing
### 3A. Run online reindexing
1. (Only applicable if, step 2, pre-extraction was done) Go to http://host/system/console/configMgr/org.apache.jackrabbit.oak.plugins.blob.datastore.DataStoreTextProviderService and set the *Path* configuration value to /mnt/preExtraction/store.
2. Go to http://host/crx/de/index.jsp (enable CRXDE if not enabled) and log in as admin
3. Browse to each of the index nodes that you want to reindex and set property reindex=true.  Here's one example index:
        /oak:index/lucene
4. Click *Save All* on the top left
5. Monitor reindexing via the error.log file.  See [here](https://helpx.adobe.com/experience-manager/kb/Analyzing-AEM-Indexing-Issues.html) for how to monitor indexing.

### OR 3.B. - Run offline (or out-of-band online) reindexing
#### 1. Create a new checkpoint
A. Create the checkpoint using the JMX console (if AEM is running)
   1. Go to this URL on the host:
      http://host/system/console/jmx/org.apache.jackrabbit.oak%3Aname%3DSegment+node+store+checkpoint+management%2Ctype%3DCheckpointManager
   2. Click "createCheckpoint(long p1)"
   3. Enter 864000000 and click "Invoke"
   4. Copy the checkpoint id to a text file

B. *OR*, stop AEM and create a checkpoint using oak-run console
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
   
#### 2. Run the out-of-band or offline reindex:
* S3 DataStore systems (include S3 DS jars in the classpath):
```bash
nohup java -Xmx2g -classpath ./oak-run-1.22.4.jar:/mnt/preExtraction/jackson-core-2.9.5.jar:/mnt/preExtraction/jackson-annotations-2.9.5.jar:/mnt/preExtraction/jackson-databind-2.9.5.jar:/mnt/crx/author/crx-quickstart/install/15/aws-java-sdk-osgi-1.10.27.jar \
org.apache.jackrabbit.oak.run.Main index -\
-reindex --read-write \
--pre-extracted-text-dir /mnt/preExtraction/store \
--index-paths=/oak:index/socialLucene,/oak:index/authorizables,/oak:index/commerceLucene,/oak:index/cqProjectLucene,/oak:index/cqPageLucene,/oak:index/damAssetLucene,/oak:index/ntBaseLucene,/oak:index/slingeventJob,/oak:index/workflowDataLucene,/oak:index/versionStoreIndex \
--checkpoint=890f552c-d7b5-459f-8097-8964b3905efd \
--s3ds=/mnt/crx/author/crx-quickstart/install/org.apache.jackrabbit.oak.plugins.blob.datastore.SharedS3DataStore.config \
/mnt/crx/author/crx-quickstart/repository/segmentstore &
```

* Azure DS systems (include S3 DS jars in the classpath):
```bash
nohup java -Xmx2g -classpath ./oak-run-1.22.4.jar:/mnt/preExtraction/jackson-core-2.9.5.jar:/mnt/preExtraction/jackson-annotations-2.9.5.jar:/mnt/preExtraction/jackson-databind-2.9.5.jar:/mnt/crx/author/crx-quickstart/install/15/aws-java-sdk-osgi-1.10.27.jar \
org.apache.jackrabbit.oak.run.Main index \
--reindex --read-write \
--pre-extracted-text-dir /mnt/preExtraction/store \
--index-paths=/oak:index/socialLucene,/oak:index/authorizables,/oak:index/commerceLucene,/oak:index/cqProjectLucene,/oak:index/cqPageLucene,/oak:index/damAssetLucene,/oak:index/ntBaseLucene,/oak:index/slingeventJob,/oak:index/workflowDataLucene,/oak:index/versionStoreIndex \
--checkpoint=890f552c-d7b5-459f-8097-8964b3905efd \
--azureblobds=/mnt/crx/author/crx-quickstart/install/org.apache.jackrabbit.oak.plugins.blob.datastore.AzureDataStore.config \
/mnt/crx/author/crx-quickstart/repository/segmentstore &
```

3. The offline indexing cycle should automatically import the new index to the source repository at the end.  However, if it fails with an error or starts reindexing when you start AEM, then follow the steps documented [here](https://github.com/chetanmeh/oak-console-scripts/blob/master/src/main/groovy/felixconsole/indexImport/README.md)
