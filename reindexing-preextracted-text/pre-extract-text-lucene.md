
## Pre-extract binary-text from old indexes
1. Log into the server and create the folder ```preExtraction``` under ```crx-quickstart/opt/helpers```, for example ```crx-quickstart/opt/helpers/preExtraction```
2. Upload [generatefilelist.sh](generatefilelist.sh?raw=true), [preextraction.sh](preextraction.sh?raw=true), [pre-text-extract-lucene-1.0.jar](pre-text-extract-lucene-1.0.jar?raw=true) and [oak-run-1.10.8.jar](https://repo1.maven.org/maven2/org/apache/jackrabbit/oak-run/1.10.8/oak-run-1.10.8.jar) to the AEM Linux server under ```crx-quickstart/opt/helpers/preExtraction```.  Create the ```preExtraction``` folder.
3. Log into the server via ssh and run the following commands as root (instead of crx as the user id replace with your own environment's aem process user id):
    
        export AEM_PROCESS_USER_ID=crx
        export AEM_INSTALL_DIRECTORY=/mnt/crx/author
        cd $AEM_INSTALL_DIRECTORY/crx-quickstart
        mkdir -p repository/preExtraction/store
        chown -R ${AEM_PROCESS_USER_ID}: repository/preExtraction
        cd repository/preExtraction
        sudo -u $AEM_PROCESS_USER_ID bash -c "nohup bash generatefilelist.sh" &

4. Go to http://host/system/console/jmx/org.apache.jackrabbit.oak%3Aname%3DIndexCopier+support+statistics%2Ctype%3DIndexCopierStats and find the ```fsPath``` of the ```/oak:index/lucene``` index.  For example: ```/mnt/crx/author/crx-quickstart/repository/index/lucene-1576519499912```.  Get the timestamp from the fsPath path (e.g. ```1576519499912```) and edit this line in the ```preextraction.sh``` script:

    * Before:

            LUCENE_INDEX_PATH_ROOT="$QS_PATH/repository/index/lucene-1579303574763/data"

    * After:
 
            LUCENE_INDEX_PATH_ROOT="$QS_PATH/repository/index/lucene-1576519499912/data"

5. Now that the preextraction.sh script points to the right index path, now run this command:

        sudo -u crx bash -c "nohup bash preextraction.sh" &

To use the pre-extraction store in a running AEM instance during reindexing then...

6. Go to http://host/system/console/configMgr/org.apache.jackrabbit.oak.plugins.blob.datastore.DataStoreTextProviderService and set the Path configuration value to /mnt/preExtraction/store.

Now when an index that has fulltext indexing enabled (such as damAssetLucene) gets reindexed, then it would read from the pre-extraction files instead of reading the binaries from the datastore.  This greatly optimizes reindexing performance.
