Install the groovy script console for apache felix:
http://felix.apache.org/documentation/subprojects/apache-felix-script-console-plugin.html
1. Download these two jars and install them to the /system/console/bundles UI
    * http://repo1.maven.org/maven2/org/codehaus/groovy/groovy-all/2.1.6/groovy-all-2.1.6.jar
    * http://apache.mirrors.tds.net//felix/org.apache.felix.webconsole.plugins.scriptconsole-1.0.2.jar
2. Go to http://host/system/console/sc
3. Select "Groovy" as the language
4. Copy / paste the [calculateDiskUsage.groovy](calculateDiskUsage.groovy) script to the console
5. Modify the very last line with the path you would like to run it
countNodes("/")