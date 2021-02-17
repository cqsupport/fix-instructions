# How to entirely disable Lucene text extraction in Apache Jackrabbit Oak
## Overview
The steps below completely disable text extraction.
This will optimize lucene indexes in AEM / Oak, but at the cost of making it so users cannot search the contents of documents.
They would still be able to search document titles and metadata, but not the text in those documents.

# Steps
1. Find all tika-config.xml files in the jar
  ```
  $ jar -tvf oak-lucene-1.10.8-OAK-8978.jar  | grep tika
  1938 Sat Mar 28 07:27:56 PDT 2020 org/apache/jackrabbit/oak/plugins/index/lucene/tika-config.xml
  1938 Sat Mar 28 07:28:04 PDT 2020 org/apache/jackrabbit/oak/plugins/index/search/spi/editor/tika-config.xml
  ```
2. Extract those configs:
 ```
 $ jar -xvf oak-lucene-1.10.8-OAK-8978.jar org/apache/jackrabbit/oak/plugins/index/lucene/tika-config.xml org/apache/jackrabbit/oak/plugins/index/search/spi/editor/tika-config.xml
 inflated: org/apache/jackrabbit/oak/plugins/index/lucene/tika-config.xml
 inflated: org/apache/jackrabbit/oak/plugins/index/search/spi/editor/tika-config.xml
 ```
3. Edit the configs:
   ```
   $ vi org/apache/jackrabbit/oak/plugins/index/search/spi/editor/tika-config.xml
   $ vi org/apache/jackrabbit/oak/plugins/index/lucene/tika-config.xml 
   ```
   
Replaced contents with this:
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
4. Update the jar
  ```
  $ jar -uvf oak-lucene-1.10.8-OAK-8978.jar org/apache/jackrabbit/oak/plugins/index/lucene/tika-config.xml org/apache/jackrabbit/oak/plugins/index/search/spi/editor/tika-config.xml
  adding: org/apache/jackrabbit/oak/plugins/index/lucene/tika-config.xml(in = 1139) (out= 616)(deflated 45%)
  adding: org/apache/jackrabbit/oak/plugins/index/search/spi/editor/tika-config.xml(in = 1140) (out= 616)(deflated 45%)
  ```
5. Rename the jar
  ```
  $ mv oak-lucene-1.10.8-OAK-8978.jar oak-lucene-1.10.8-OAK-8978-notika.jar
  ```
