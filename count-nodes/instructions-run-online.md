1. Go to http://host:port/system/console/bundles and install these two bundles
    * http://apache.mirrors.lucidnetworks.net//felix/org.apache.felix.webconsole.plugins.scriptconsole-1.0.2.jar
    * https://repo1.maven.org/maven2/org/codehaus/groovy/groovy-all/2.4.9/groovy-all-2.4.9.jar
2. Go to  http://host/system/console/configMgr/org.apache.sling.jcr.base.internal.LoginAdminWhitelist
3. Add org.apache.felix.webconsole.plugins.scriptconsole to "Whitelist regexp" and save
4. After the two bundles fully install then go to http://host:port/system/console/slinglog
5. Click "Add New Logger" and set log level to "Info", "Log File" to "logs/countnodes.log", and "Logger" to "countNodes.groovy"
5. Save the log file config
6. Go to http://host:port/system/console/sc
7. Select "Groovy" as the language
8. Copy/Paste the contents of [countNodes-*.groovy](#file-countnodes-oak16andlater-groovy) script below which matches your oak version to the script console
9. Click "Execute"
10. The output will go to crx-quicktstart/logs/countnodes.log


Side note: The scripts below are an adaptation of this script updated to work in the Felix Script console: https://gist.github.com/stillalex/06303f8cc1d3780d3eab4c72575883ae
