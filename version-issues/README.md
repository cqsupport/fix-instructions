# Issue
When deleting versions from Apache Jackrabbit Oak you observe errors in the logs similar to the one below:
```
java.lang.NullPointerException: null
        at org.apache.jackrabbit.oak.plugins.version.ReadWriteVersionManager.removeVersion(ReadWriteVersionManager.java:210)
```
For the full stack see [error.txt](#file-error-txt) below.

# Solution
There are a number of scripts available for validating and fixing Apache Oak version histories.  The steps for running these are below.  
### A. Run the repair scripts
1. Install the groovy script console for apache felix: http://felix.apache.org/documentation/subprojects/apache-felix-script-console-plugin.html
2. Download these two jars and install them to the /system/console/bundles UI
https://repo1.maven.org/maven2/org/codehaus/groovy/groovy-all/2.4.6/groovy-all-2.4.6.jar
https://repo1.maven.org/maven2/org/apache/felix/org.apache.felix.webconsole.plugins.scriptconsole/1.0.2/org.apache.felix.webconsole.plugins.scriptconsole-1.0.2.jar
3. Go to  http://host/system/console/configMgr/org.apache.sling.jcr.base.internal.LoginAdminWhitelist
4. Add org.apache.felix.webconsole.plugins.scriptconsole to "Whitelist regexp" and save
5. Go to http://host/system/console/sc
6. Select "Groovy" as the language
7. Copy / paste the contents of script [FixCorruptVersions-OAK-5193.groovy](#file-fixcorruptversions-oak-5193-groovy) to the console and run it - output goes to the error.log by default
8. Copy / paste the contents of script [FixOrphanedVersions-GRANITE-25586.groovy](#file-fixorphanedversions-granite-25586-groovy) to the console and run it - output goes to the error.log by default

### B. Fix the cost calculation and optimize the /oak:index/versionStoreIndex
If you run version purge and see this [error](#file-purge-query-error-txt) then you need to fix the cost calculation of the /oak:index/versionStoreIndex.
1. Go to /crx/de/index.jsp and log in as admin
2. Browse to /oak:index/versionStoreIndex and set these two properties:
    ```
    refresh (Boolean) = true
    entryCount (Long) = 2000
    evaluatePathRestrictions (Boolean) = true
    reindex (Boolean) = true
    ```
3. Save - now monitor the logs for org.apache.jackrabbit.oak.plugins.index to see the progress of reindexing
4. Now you should be able to run version purge without it failing.

### C. (If it still fails) Remove mixins on nodes which are missing their version histories
If version purge still fails, it might be due to versionable nodes that have missing version histories.  To fix this, perform the steps below:
1. Download and install this package to AEM: https://documentcloud.adobe.com/link/track?uri=urn%3Aaaid%3Ascds%3AUS%3A07e61ad8-7fad-4682-b7e0-6ef75781ce9f
2. Go to http://host:port/system/console/slinglog and add a log for org.apache.jsp.apps.tools.components.checkversions
3. Go to http://host:port/apps/tools/components/checkversions/run.html and click "Start"
4. In the log file from step 2 it will report if there are any version corruptions.
5. Nodes that are missing version histories will output an error like this:
   ```
   11.06.2019 13:08:13.006 *ERROR* [Thread-1136] org.apache.jsp.apps.tools.components.checkversions.POST_jsp$VersionCheckThread VERSION ERROR: Node /content/dam/geometrixx-outdoors/activities/hiking/PDP_2_c05.jpg contains a jcr:versionHistory property that points to non-existing node with uuid e98b4045-b145-47d1-8832-3df194ef6e4a
   ```
6. Copy / paste the contents of script [remove-version-props-from-node.groovy](#file-remove-version-props-from-node-groovy)
7. For each node missing a version history, add a line like this one:
    ```
    removeVersionProps("/content/dam/path/to/item/missing/versionhistory/example.pdf")
    ```
8. Run the script and it will remove mix:versionable and related properties with version history references from the nodes.

#### D. Corrupt /oak:index/reference index
If version purge succeeds but throws errors like the one below then it is likely that the /oak:index/reference index has some inconsistencies.

To fix this we need to reindex it:
Follow the steps in [this article](https://helpx.adobe.com/experience-manager/kb/how-to-reindex-a-synchronous-AEM-index-AEM-Oak.html) to reindex it with minimal impact on system performance.
```
16.07.2019 03:19:57.446 *ERROR* [sling-threadpool-576514bd-c94a-41c0-ae83-fe58543ae1b3-(apache-sling-job-thread-pool)-80-Maintenance Queue(com/adobe/granite/maintenance/job/VersionPurgeTask)] com.day.cq.wcm.core.impl.VersionManagerImpl Unable to purge version 1.1 for /content/dam/geometrixx-outdoors/activities/hiking/PDP_2_c05.jpg : OakIntegrity0001: Unable to delete referenced node
javax.jcr.ReferentialIntegrityException: OakIntegrity0001: Unable to delete referenced node
at org.apache.jackrabbit.oak.api.CommitFailedException.asRepositoryException(CommitFailedException.java:235)
at org.apache.jackrabbit.oak.api.CommitFailedException.asRepositoryException(CommitFailedException.java:212)
at org.apache.jackrabbit.oak.jcr.version.ReadWriteVersionManager.removeVersion(ReadWriteVersionManager.java:243)
at org.apache.jackrabbit.oak.jcr.delegate.VersionManagerDelegate.removeVersion(VersionManagerDelegate.java:226)
at org.apache.jackrabbit.oak.jcr.delegate.VersionHistoryDelegate.removeVersion(VersionHistoryDelegate.java:209)
at org.apache.jackrabbit.oak.jcr.version.VersionHistoryImpl$11.performVoid(VersionHistoryImpl.java:240)
at org.apache.jackrabbit.oak.jcr.delegate.SessionDelegate.performVoid(SessionDelegate.java:274)
at org.apache.jackrabbit.oak.jcr.version.VersionHistoryImpl.removeVersion(VersionHistoryImpl.java:236)
at com.day.cq.wcm.core.impl.VersionManagerImpl.purgeVersions(VersionManagerImpl.java:504)
at com.day.cq.wcm.core.impl.VersionPurgeTask.process(VersionPurgeTask.java:121)
at org.apache.sling.event.impl.jobs.queues.JobQueueImpl.startJob(JobQueueImpl.java:293)
at org.apache.sling.event.impl.jobs.queues.JobQueueImpl.access$100(JobQueueImpl.java:60)
at org.apache.sling.event.impl.jobs.queues.JobQueueImpl$1.run(JobQueueImpl.java:229)
at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
at java.lang.Thread.run(Thread.java:745)
Caused by: org.apache.jackrabbit.oak.api.CommitFailedException: OakIntegrity0001: Unable to delete referenced node
at org.apache.jackrabbit.oak.plugins.index.reference.ReferenceEditor.checkReferentialIntegrity(ReferenceEditor.java:340)
at org.apache.jackrabbit.oak.plugins.index.reference.ReferenceEditor.leave(ReferenceEditor.java:187)
at org.apache.jackrabbit.oak.plugins.index.IndexUpdate.leave(IndexUpdate.java:329)
at org.apache.jackrabbit.oak.spi.commit.VisibleEditor.leave(VisibleEditor.java:63)
at org.apache.jackrabbit.oak.spi.commit.CompositeEditor.leave(CompositeEditor.java:74)
at org.apache.jackrabbit.oak.spi.commit.EditorDiff.process(EditorDiff.java:56)
at org.apache.jackrabbit.oak.spi.commit.EditorHook.processCommit(EditorHook.java:55)
at org.apache.jackrabbit.oak.spi.commit.CompositeHook.processCommit(CompositeHook.java:61)
at org.apache.jackrabbit.oak.spi.commit.CompositeHook.processCommit(CompositeHook.java:61)
at org.apache.jackrabbit.oak.segment.SegmentNodeStore$Commit.prepare(SegmentNodeStore.java:604)
at org.apache.jackrabbit.oak.segment.SegmentNodeStore$Commit.optimisticMerge(SegmentNodeStore.java:634)
at org.apache.jackrabbit.oak.segment.SegmentNodeStore$Commit.execute(SegmentNodeStore.java:690)
at org.apache.jackrabbit.oak.segment.SegmentNodeStore.merge(SegmentNodeStore.java:334)
at org.apache.jackrabbit.oak.core.MutableRoot.commit(MutableRoot.java:249)
at org.apache.jackrabbit.oak.jcr.delegate.SessionDelegate.commit(SessionDelegate.java:347)
at org.apache.jackrabbit.oak.jcr.delegate.SessionDelegate.commit(SessionDelegate.java:372)
at org.apache.jackrabbit.oak.jcr.version.ReadWriteVersionManager.removeVersion(ReadWriteVersionManager.java:239)
... 13 common frames omitted
```
