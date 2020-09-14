Note that offline compaction requires a long duration of downtime from 30 minutes to 7 hours (and in extreme cases more time).

Offline Oak compaction with debug logging:

1. Download the oak-run version matching your oak version: https://repo1.maven.org/maven2/org/apache/jackrabbit/oak-run/
  * AEM6.0 - use oak-run 1.0.x - if you have Oak 1.0.11 or earlier installed in AEM then use oak-run 1.0.11 otherwise use the version that matches the installed on in AEM.
  * AEM6.1 - use latest oak-run 1.2.x version
  * AEM6.2 - use latest oak-run 1.4.x version
  * AEM6.3 - use latest oak-run 1.6.x version
  * AEM6.4 - use latest oak-run 1.8.x version
  * AEM6.5 - use latest oak-run 1.10.x version
2. Download the countNodes-offline-oakRun\*.groovy  script corresponding to your AEM version of AEM / Oak.
3. Upload the oak-run jar file to your server that has AEM on it.
4. If you are on AEM 6.3 or older then stop AEM.
5. Run this command from the same directory as the AEM oak-run jar file (change the paths and jar file name to match your system’s paths, and tune -Xmx10g to not exceed your server's memory while leaving room for the OS memory): 
  
  Linux, Unix, Mac:
  ```
    nohup sudo -u crx nohup /usr/java/latest/bin/java -server -Xmx30g -jar oak-run-*.jar console /path/to/segmentstore ":load >> countNodes.log 2>> countNodes-error.log &
  ```
  Windows (note that memoryMapped setting is removed):
  ```
    java -Dtar.memoryMapped=false -server -Xmx30g -jar oak-run-*.jar console /path/to/segmentstore >> countNodes.log 2>> countNodes-error.log &
  ```
6. Once the process completes then you will need to review the paths and use rmNode to remove corrupt nodes https://gist.github.com/stillalex/43c49af065e3dd1fd5bf
