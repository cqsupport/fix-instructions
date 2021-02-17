# How to disable Apache Jackrabbit Oak Luecene Index text extraction for some files

## Overview
To reduce the Lucene index sizes in Apache Jackrabbit Oak and Adobe AEM you can follow the steps below.  Disabling text extraction means the text within those files would no longer be searchable after reindexing.  For example you would no longer be able to seearch the contents of PDF documents if you excluded mimetype application/pdf.

### Note
To disable the text extraction entirely, then replace the whole tika-config.xml file with this:

  ```
  <properties>
    <detectors>
      <detector class="org.apache.tika.detect.EmptyDetector"/>
    </detectors>
    <parsers>
      <parser class="org.apache.tika.parser.EmptyParser"/>
    </parsers>
    <service-loader initializableProblemHandler="ignore" dynamic="true"/>
  </properties>
  ```


## Steps

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

5. Find all tika-config.xml files in the jar file:
   ```
   jar -tvf bundle.jar | grep tika-config.xml
   ```
   
6. Extract all tika configs that were in the output of the previous step:
   ```
   jar -xvf bundle.jar org/apache/jackrabbit/oak/plugins/index/lucene/tika-config.xml
   ```

6. Edit the tika-config.xml files:
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
