# Overview
Each folder in this git repository contains a set of instructions to fix issues, analyze problems or perform maintenance tasks on an [Adobe AEM](https://www.adobe.com/marketing/experience-manager.html), [Apache Sling](https://sling.apache.org/), [Apache Jackrabbit Oak](https://jackrabbit.apache.org/oak/), or [Apache Felix](https://felix.apache.org/) instance.

1. [count-nodes](count-nodes) Traverse the nodes at the Oak API level to find any inconsistencies.
2. [disk-usage-analysis](disk-usage-analysis) Traverse the nodes at the Oak API level and calculate approximate disk usage per path.
3. [reindexing-preextracted-text](reindexing-preextracted-text) Optimized reindexing steps.
4. [version-issues](version-issues) Steps to fix most AEM page and asset version corruption and version purge failures.
5. [move-crypto-keys](move-crypt-keys) Steps to convert from JCR crypto key storage to file system storage of AEM's crypto (hmac and master) keys.  This only applies to systems that were in-place upgrades from AEM 6.3 or earlier to 6.4 or 6.5.
6. [missing-jcr-content-nodes.md](missing-jcr-content-nodes.md) See how to find pages and assets that are improperly structured (missing jcr:content subnode)
