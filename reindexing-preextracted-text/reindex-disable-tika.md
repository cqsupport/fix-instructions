To disable the default Apache Tika configs in AEM:

1. Find the oak-lucene bundle
   1. In the AEM Web Console (http://host:port/system/console/bundles) search for 'oak-lucene'.  Note the bundle id in the far left column.  
   2. Or run this command at the file system level to find the bundle's path:
      ```
      find . -name "bundle.info" -exec grep oak-lucene {} \; -print
      ```
      
2. Shutdown the AEM instance.

3. On the server file system, go to crx-quickstart/launchpad/felix/bundlexxx directory where `xxx` is the bundle id of oak-lucene from step 1.

4. cd to the subdirectory with versionX.Y in the name (e.g. felix/bundle102/version0.2):
   ```
   cd version*
   ```

5. Extract the contents of the tika-config.xml file from the jar file:
   ```
   jar -xvf bundle.jar org/apache/jackrabbit/oak/plugins/index/lucene/tika-config.xml
   ```

6. Edit file tika-config.xml
   ```
   vi org/apache/jackrabbit/oak/plugins/index/lucene/tika-config.xml
   ```

   For example, add the file mime type that needs to be disabled: 
   ```
   <mime>application/zip</mime>
   ```
   
7. Save the changes to the bundle.jar. 
   ```
   jar -uvf bundle.jar org/apache/jackrabbit/oak/plugins/index/lucene/tika-config.xml
   ```
   
8. Restart AEM instance and test by searching for assets of the mime type added.
