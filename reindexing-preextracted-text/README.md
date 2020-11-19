# Reindex Oak indexes using pre-extracted text on binary files
On large AEM installations, reindexing can be very slow.  To speed up reindexing of fulltext indexes on AEM such as the /oak:index/ntBaseLucene, /oak:index/damAssetLucene, and /oak:index/lucene indexes, follow the steps below.

# Steps
1. Log into the server and create the folder /mnt/crx/author/crx-quickstart/opt/helpers/preExtraction
2. Upload [generatefilelist.sh](generatefilelist.sh), [preextraction.sh](preextraction.sh), [pre-text-extract-lucene-1.0.jar](pre-text-extract-lucene-1.0.jar?raw=true) and [oak-run-1.10.8.jar](https://repo1.maven.org/maven2/org/apache/jackrabbit/oak-run/1.10.8/oak-run-1.10.8.jar) to the AEM Linux server under /mnt/crx/author/crx-quickstart/opt/helpers/preExtraction.  Create the preExtraction folder.
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
    
6. Go to http://host/system/console/configMgr/org.apache.jackrabbit.oak.plugins.blob.datastore.DataStoreTextProviderService and set the *Path* configuration value to /mnt/crx/author/crx-quickstart/opt/helpers/preExtraction/store
7. Go to http://host/crx/de/index.jsp (enable CRXDE if not enabled) and log in as admin
8. Browse to each of the index nodes that you want to reindex and set property reindex=true.  Here's one example index:
        /oak:index/lucene
9. Click *Save All* on the top left
10. Monitor reindexing via the error.log file

See here for how to monitor indexing:
https://helpx.adobe.com/experience-manager/kb/Analyzing-AEM-Indexing-Issues.html
