# Issue
The Datastore Garbage Collection is failing with the following error in AEM 6.x:
```
org.apache.jackrabbit.oak.plugins.blob.MarkSweepGarbageCollector Not all repositories have marked references available : [12a6bd36-e502-4ba0-aa9c-060bb4fddd05]
```
# **WARNING**
If you have a shared datastore then take care to only delete references for instances (or oak-upgrade / crx2oak migration runs) that are no longer active or using the datastore.

# Cause
This is caused by having more than one repository files in the datastore. Most probably the extra repository file is from another server that has been moved to the datastore for example after a shared datastore has been broken down into 2 independent ones.

# Resolution
1. First, check the invalid repository ID
2. Go to http://host:port/system/console/jmx/org.apache.jackrabbit.oak%3Aname%3DSegment+node+store+blob+garbage+collection%2Ctype%3DBlobGarbageCollection or go to http://host:port/system/console/jmx and open ```org.apache.jackrabbit.oak: Segment node store blob garbage collection (BlobGarbageCollection)```
3. Find the repositoryID with a ```*``` to the right of it, for example ```ff822b5e-778b-474c-bb09-92e6ab0cb279 *```.  This indicates the repositoryId of the local instance where the operation runs.
4. Stop AEM
5. Log into the server OS
6. Go to ```crx-quickstart/repository/blobids``` (or the datastore directory you have configured) and delete the unnecessary repository files from the datastore. You can identify the faulty ones using the creation date. The older ones are the problematic ones.
7. Start AEM


# Specified Message
```
org.apache.jackrabbit.oak.plugins.blob.MarkSweepGarbageCollector Not all repositories have marked references available
```
