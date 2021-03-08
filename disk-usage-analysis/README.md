# Analyze Disk Usage of Apache Jackrabbit Oak Repositories
The calculateDiskUsage script will analyze the disk usage of an Oak repository at a low level.  It scans all nodes and properties including datastore binaries and calculates the size of the nodes.
This is useful when analyzing [rapid disk usage increases](https://helpx.adobe.com/experience-manager/kb/analyze-unusual-repository-growth.html) or when trying to reduce repository size of Apache Jackrabbit Oak instances.

# Instructions
1. Download these two jars and install them to the /system/console/bundles UI
    * https://repo1.maven.org/maven2/org/codehaus/groovy/groovy-all/2.1.6/groovy-all-2.1.6.jar
    * https://repo1.maven.org/maven2/org/apache/felix/org.apache.felix.webconsole.plugins.scriptconsole/1.0.2/org.apache.felix.webconsole.plugins.scriptconsole-1.0.2.jar

    Note: Those two bundles install the groovy script console for apache felix:
    http://felix.apache.org/documentation/subprojects/apache-felix-script-console-plugin.html
2. Go to http://host/system/console/configMgr/org.apache.sling.jcr.base.internal.LoginAdminWhitelist
and add _org.apache.felix.webconsole.plugins.scriptconsole_ to "Whitelist regexp" and save
3. Go to http://host:port/system/console/slinglog
4. Click "Add New Logger" and set log level to "Info", "Log File" to "logs/calculateDiskUsage.log", and "Logger" to "calculateDiskUsage.groovy"
5. Go to http://host/system/console/sc
6. Select "Groovy" as the language
7. Copy / paste the [calculateDiskUsage.groovy](calculateDiskUsage.groovy) script to the console
8. If you want to throttle the script for running on a live system then modify this line:
   ```
   sleepMillis = 0;
   ```
   Set sleepMillis to the number of milliseconds to sleep between node iterations.
   
9. Modify the very last line with the path you would like to run it
   ```
   countNodes("/")
   ```
   
# Stopping a running script
If you start the script and want to stop it while it's running you can open a new browser window for /system/console/sc and set this system property:
```
System.setProperty("calculateDiskUsage.stop", "true")
```
The script would detect the system property, remove the property and stop the script.


# Sample Output
```
08.03.2021 11:26:04.544 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy Running node count and size estimate: WARNING - this isn't a perfectly accurate calculation of repository size, it is just an estimation
08.03.2021 11:26:04.546 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy Counting nodes in tree /
08.03.2021 11:26:04.548 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /:async, nodes: 1,  binaries: 0, bytes: 710, bytes: 710 B
08.03.2021 11:26:04.549 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /tmp/social-enablement/rep:policy, nodes: 4,  binaries: 0, bytes: 754, bytes: 754 B
08.03.2021 11:26:04.549 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /tmp/social-enablement, nodes: 5,  binaries: 0, bytes: 829, bytes: 829 B
08.03.2021 11:26:04.549 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /tmp/versionhistory/rep:policy, nodes: 2,  binaries: 0, bytes: 222, bytes: 222 B
08.03.2021 11:26:04.549 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /tmp/versionhistory, nodes: 3,  binaries: 0, bytes: 297, bytes: 297 B
08.03.2021 11:26:04.549 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /tmp/fd/af, nodes: 3,  binaries: 0, bytes: 249, bytes: 249 B
08.03.2021 11:26:04.551 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /tmp/fd/fm, nodes: 6,  binaries: 0, bytes: 412, bytes: 412 B
08.03.2021 11:26:04.551 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /tmp/fd/xfaforms, nodes: 3,  binaries: 0, bytes: 249, bytes: 249 B
08.03.2021 11:26:04.551 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /tmp/fd, nodes: 13,  binaries: 0, bytes: 956, bytes: 956 B
08.03.2021 11:26:04.552 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /tmp/commerce/rep:policy, nodes: 2,  binaries: 0, bytes: 121, bytes: 121 B
08.03.2021 11:26:04.552 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /tmp/commerce, nodes: 3,  binaries: 0, bytes: 196, bytes: 196 B
08.03.2021 11:26:04.552 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /tmp/content/dmsearch, nodes: 4,  binaries: 0, bytes: 322, bytes: 322 B
08.03.2021 11:26:04.552 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /tmp/content/dam, nodes: 4,  binaries: 0, bytes: 321, bytes: 321 B
08.03.2021 11:26:04.553 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /tmp/content, nodes: 9,  binaries: 0, bytes: 754, bytes: 754 B
...

08.03.2021 11:26:04.582 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /home/groups/rep:policy, nodes: 13,  binaries: 0, bytes: 1198, bytes: 1.2 KB
08.03.2021 11:26:04.582 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /home/groups/d, nodes: 6,  binaries: 0, bytes: 670, bytes: 670 B
08.03.2021 11:26:04.583 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /home/groups/u, nodes: 5,  binaries: 0, bytes: 401, bytes: 401 B
08.03.2021 11:26:04.583 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /home/groups/forms, nodes: 21,  binaries: 0, bytes: 1799, bytes: 1.8 KB
08.03.2021 11:26:04.584 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /home/groups/e, nodes: 9,  binaries: 0, bytes: 951, bytes: 951 B
08.03.2021 11:26:04.584 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /home/groups/c, nodes: 10,  binaries: 0, bytes: 1217, bytes: 1.2 KB
08.03.2021 11:26:04.584 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /home/groups, nodes: 368,  binaries: 0, bytes: 39591, bytes: 38.7 KB
08.03.2021 11:26:04.584 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /home, nodes: 748,  binaries: 8, bytes: 1166512, bytes: 1.1 MB
08.03.2021 11:26:04.585 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /:clusterConfig, nodes: 1,  binaries: 0, bytes: 36, bytes: 36 B
08.03.2021 11:26:04.585 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /oak:index/socialLucene/:data, nodes: 3,  binaries: 2, bytes: 259, bytes: 259 B
08.03.2021 11:26:04.585 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /oak:index/socialLucene/:status, nodes: 1,  binaries: 0, bytes: 43, bytes: 43 B
08.03.2021 11:26:04.586 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /oak:index/socialLucene/:index-definition, nodes: 23,  binaries: 0, bytes: 1458, bytes: 1.4 KB
08.03.2021 11:26:04.587 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /oak:index/socialLucene/indexRules, nodes: 22,  binaries: 0, bytes: 1329, bytes: 1.3 KB
08.03.2021 11:26:04.587 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /oak:index/socialLucene, nodes: 50,  binaries: 2, bytes: 3221, bytes: 3.1 KB
08.03.2021 11:26:04.588 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /oak:index/workflowDataLucene/:data, nodes: 6,  binaries: 5, bytes: 4607, bytes: 4.5 KB
08.03.2021 11:26:04.588 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /oak:index/workflowDataLucene/aggregates, nodes: 3,  binaries: 0, bytes: 111, bytes: 111 B
...
10.03.2021 11:26:08.384 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /var/audit, nodes: 32,  binaries: 20, bytes: 6264, bytes: 6.1 KB
11.03.2021 11:26:08.385 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /var/mailer/status, nodes: 1,  binaries: 0, bytes: 15, bytes: 15 B
12.03.2021 11:26:08.385 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /var/mailer, nodes: 2,  binaries: 0, bytes: 59, bytes: 59 B
13.03.2021 11:26:08.385 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /var/granite/asyncjobs, nodes: 3,  binaries: 0, bytes: 207, bytes: 207 B
14.03.2021 11:26:08.385 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /var/granite, nodes: 4,  binaries: 0, bytes: 253, bytes: 253 B
15.03.2021 11:26:08.385 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /var/replication/rep:policy, nodes: 2,  binaries: 0, bytes: 96, bytes: 96 B
16.03.2021 11:26:08.385 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /var/replication/outbox, nodes: 1,  binaries: 0, bytes: 60, bytes: 60 B
17.03.2021 11:26:08.385 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /var/replication/data, nodes: 2,  binaries: 0, bytes: 120, bytes: 120 B
...
08.03.2021 11:26:08.468 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /apps/granite, nodes: 9,  binaries: 0, bytes: 597, bytes: 597 B
08.03.2021 11:26:08.469 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /apps/core-components-examples/components, nodes: 98,  binaries: 9, bytes: 20022, bytes: 19.6 KB
08.03.2021 11:26:08.474 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /apps/core-components-examples/clientlibs, nodes: 261,  binaries: 111, bytes: 2333942, bytes: 2.2 MB
08.03.2021 11:26:08.474 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /apps/core-components-examples, nodes: 360,  binaries: 120, bytes: 2354007, bytes: 2.2 MB
08.03.2021 11:26:08.474 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /apps/we-retail-communities/config, nodes: 5,  binaries: 2, bytes: 1041, bytes: 1.0 KB
08.03.2021 11:26:08.474 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /apps/we-retail-communities, nodes: 6,  binaries: 2, bytes: 1084, bytes: 1.1 KB
08.03.2021 11:26:08.474 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy /apps, nodes: 5098,  binaries: 1061, bytes: 7819684, bytes: 7.5 MB
08.03.2021 11:26:08.475 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy Total nodes in tree /: 288558
08.03.2021 11:26:08.475 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy Total binaries in tree /: 25338
08.03.2021 11:26:08.475 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy Total bytes in tree /: 2.3 GB
08.03.2021 11:26:08.475 *INFO* [qtp1785043953-4886] calculateDiskUsage.groovy Done: [bytes:2485401609, binaryCount:25338, nodeCount:288558]
```
