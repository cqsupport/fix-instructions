# Summary
The calculateDiskUsage script will analyze the disk usage of an Oak repository at a low level.  It scans all nodes and properties including datastore binaries and calculates the size of the nodes.
This is useful when analyzing [rapid disk usage increases](https://helpx.adobe.com/experience-manager/kb/analyze-unusual-repository-growth.html) or when trying to reduce repository size of Apache Jackrabbit Oak instances.

# Instructions
1. Download these two jars and install them to the /system/console/bundles UI
    * https://repo1.maven.org/maven2/org/codehaus/groovy/groovy-all/2.1.6/groovy-all-2.1.6.jar
    * http://apache.mirrors.tds.net/felix/org.apache.felix.webconsole.plugins.scriptconsole-1.0.2.jar

    Note: Those two bundles install the groovy script console for apache felix:
    http://felix.apache.org/documentation/subprojects/apache-felix-script-console-plugin.html
2. Go to http://host/system/console/sc
3. Select "Groovy" as the language
4. Copy / paste the [calculateDiskUsage.groovy](calculateDiskUsage.groovy) script to the console
5. Modify the very last line with the path you would like to run it
countNodes("/")
