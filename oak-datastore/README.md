# Issue
The Datastore Garbage Collection is failing with the following error in AEM 6.x:
```
org.apache.jackrabbit.oak.plugins.blob.MarkSweepGarbageCollector Not all repositories have marked references available : [12a6bd36-e502-4ba0-aa9c-060bb4fddd05]
```

# Cause
This is caused by having more than one repository files in the datastore. Most probably the extra repository file is from another server that has been moved to the datastore for example after a shared datastore has been broken down into 2 independent ones.

# Resolution
1. First, check the invalid repository ID
2. Go to http://host:port/system/console/jmx/org.apache.jackrabbit.oak%3Aname%3DSegment+node+store+blob+garbage+collection%2Ctype%3DBlobGarbageCollection
3. Find and open ```org.apache.jackrabbit.oak: Segment node store blob garbage collection (BlobGarbageCollection)```
4. Find the repositoryID, for example ```ff822b5e-778b-474c-bb09-92e6ab0cb279```.  This indicates the repositoryId of the local instance where the operation runs.
5. Remove the unnecessary repository files from the datastore. You can identify the faulty one using the creation date. The older ones are the problematic ones.


# Specified Message
```
org.apache.jackrabbit.oak.plugins.blob.MarkSweepGarbageCollector Not all repositories have marked references available
```
