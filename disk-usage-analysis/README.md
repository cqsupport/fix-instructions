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
8. Modify the very last line with the path you would like to run it
countNodes("/")

# Stopping a running script
If you start the script and want to stop it while it's running you can open a new browser window for /system/console/sc and set this system property:
```
System.setProperty("calculateDiskUsage.stop", "true")
```
The script would detect the system property, remove the property and stop the script.


# Sample Output
```
07.03.2021 08:52:18.105 *INFO* [qtp1785043953-1310] calculateDiskUsage.groovy Running node count and size estimate: WARNING - this isn't a perfectly accurate calculation of repository size, it is just an estimation
07.03.2021 08:52:18.105 *INFO* [qtp1785043953-1310] calculateDiskUsage.groovy Counting nodes in tree /
07.03.2021 08:52:18.109 *INFO* [qtp1785043953-1310] calculateDiskUsage.groovy /:async, bytes: 449 B
07.03.2021 08:52:18.110 *INFO* [qtp1785043953-1310] calculateDiskUsage.groovy /tmp/social-enablement/rep:policy, bytes: 754 B
07.03.2021 08:52:18.111 *INFO* [qtp1785043953-1310] calculateDiskUsage.groovy /tmp/social-enablement, bytes: 829 B
07.03.2021 08:52:18.111 *INFO* [qtp1785043953-1310] calculateDiskUsage.groovy /tmp/versionhistory/rep:policy, bytes: 222 B
07.03.2021 08:52:18.111 *INFO* [qtp1785043953-1310] calculateDiskUsage.groovy /tmp/versionhistory, bytes: 297 B
07.03.2021 08:52:18.111 *INFO* [qtp1785043953-1310] calculateDiskUsage.groovy /tmp/fd/af, bytes: 249 B
...
07.03.2021 08:52:18.151 *INFO* [qtp1785043953-1310] calculateDiskUsage.groovy /oak:index/socialLucene/:data, bytes: 259 B
07.03.2021 08:52:18.151 *INFO* [qtp1785043953-1310] calculateDiskUsage.groovy /oak:index/socialLucene/:status, bytes: 43 B
07.03.2021 08:52:18.152 *INFO* [qtp1785043953-1310] calculateDiskUsage.groovy /oak:index/socialLucene/:index-definition, bytes: 1.4 KB
07.03.2021 08:52:18.153 *INFO* [qtp1785043953-1310] calculateDiskUsage.groovy /oak:index/socialLucene/indexRules, bytes: 1.3 KB
07.03.2021 08:52:18.153 *INFO* [qtp1785043953-1310] calculateDiskUsage.groovy /oak:index/socialLucene, bytes: 3.1 KB
07.03.2021 08:52:18.154 *INFO* [qtp1785043953-1310] calculateDiskUsage.groovy /oak:index/workflowDataLucene/:data, bytes: 4.5 KB
07.03.2021 08:52:18.154 *INFO* [qtp1785043953-1310] calculateDiskUsage.groovy /oak:index/workflowDataLucene/aggregates, bytes: 111 B
07.03.2021 08:52:18.154 *INFO* [qtp1785043953-1310] calculateDiskUsage.groovy /oak:index/workflowDataLucene/:status, bytes: 43 B
07.03.2021 08:52:18.155 *INFO* [qtp1785043953-1310] calculateDiskUsage.groovy /oak:index/workflowDataLucene/:index-definition, bytes: 1.8 KB
07.03.2021 08:52:18.156 *INFO* [qtp1785043953-1310] calculateDiskUsage.groovy /oak:index/workflowDataLucene/indexRules, bytes: 1.6 KB
07.03.2021 08:52:18.156 *INFO* [qtp1785043953-1310] calculateDiskUsage.groovy /oak:index/workflowDataLucene, bytes: 8.2 KB
07.03.2021 08:52:18.156 *INFO* [qtp1785043953-1310] calculateDiskUsage.groovy /oak:index/slingeventJob/:data, bytes: 259 B
07.03.2021 08:52:18.156 *INFO* [qtp1785043953-1310] calculateDiskUsage.groovy /oak:index/slingeventJob/:status, bytes: 43 B
07.03.2021 08:52:18.156 *INFO* [qtp1785043953-1310] calculateDiskUsage.groovy /oak:index/slingeventJob/:index-definition, bytes: 543 B
07.03.2021 08:52:18.156 *INFO* [qtp1785043953-1310] calculateDiskUsage.groovy /oak:index/slingeventJob/indexRules, bytes: 398 B
07.03.2021 08:52:18.157 *INFO* [qtp1785043953-1310] calculateDiskUsage.groovy /oak:index/slingeventJob, bytes: 1.4 KB
07.03.2021 08:52:18.158 *INFO* [qtp1785043953-1310] calculateDiskUsage.groovy /oak:index/jcrLanguage/:index, bytes: 159 B
07.03.2021 08:52:18.158 *INFO* [qtp1785043953-1310] calculateDiskUsage.groovy /oak:index/jcrLanguage, bytes: 255 B
07.03.2021 08:52:18.158 *INFO* [qtp1785043953-1310] calculateDiskUsage.groovy /oak:index/versionStoreIndex/:data, bytes: 142.1 KB
...

07.03.2021 08:53:29.816 *INFO* [qtp1785043953-1307] calculateDiskUsage.groovy /apps/we-retail-communities/config, bytes: 1.0 KB
07.03.2021 08:53:29.816 *INFO* [qtp1785043953-1307] calculateDiskUsage.groovy /apps/we-retail-communities, bytes: 1.1 KB
07.03.2021 08:53:29.816 *INFO* [qtp1785043953-1307] calculateDiskUsage.groovy /apps, bytes: 7.5 MB
07.03.2021 08:53:29.816 *INFO* [qtp1785043953-1307] calculateDiskUsage.groovy Total nodes in tree /: 288558
07.03.2021 08:53:29.816 *INFO* [qtp1785043953-1307] calculateDiskUsage.groovy Total binaries in tree /: 25334
07.03.2021 08:53:29.816 *INFO* [qtp1785043953-1307] calculateDiskUsage.groovy Total bytes in tree /: 2.3 GB
07.03.2021 08:53:29.816 *INFO* [qtp1785043953-1307] calculateDiskUsage.groovy Done: [bytes:2485397161, binaryCount:25334, nodeCount:288558]
```
