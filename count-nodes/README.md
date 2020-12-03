# Traverse and Count All Nodes in an Apache Jackrabbit Oak Repository
The count nodes script for Oak repository will count the number of nodes.  It traverses the whole repository including all hidden nodes under /oak:index.  This is useful to find any data corruptions (e.g. SegmentNotFoundException in the case of Tar SegmentNodeStore (aka TarMK).

This is useful for fixing issues such as these:
1. https://helpx.adobe.com/experience-manager/kb/offline-compaction-fails-with-SegmentNotFoundException-and-IllegalArgumentException.html
2. https://helpx.adobe.com/experience-manager/kb/fix-inconsistencies-in-the-repository-when-segmentnotfound-issue.html
3. https://helpx.adobe.com/experience-manager/kb/oak-blobstore-inconsistency-blobId.html

# Instructions

A. [Run with AEM / Sling / Oak process stopped and use oak-run-*.jar console tool](instructions-run-offline.md)

B. [Run online via the Felix Groovy Script console](instructions-run-online.md)
