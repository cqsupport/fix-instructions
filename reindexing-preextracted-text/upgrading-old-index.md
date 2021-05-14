# Upgrading the /oak:index/lucene index definition
If you have upgraded from an older AEM version (e.g. AEM6.1 to 6.5) and your `/oak:index/lucene` node doesn't have `compatVersion`=`2` property then you need to upgrade the index definition.

To upgrade the AEM `/oak:index/lucene` index with the proper config for AEM 6.5, please do the following:
1. Run text pre-extraction - follow steps [here](pre-extract-text-lucene.md)
2. After all pre-extraction steps are completed, then log into AEM as admin
3. Go to http://host:port/crx/de/index.jsp and rename the node `/oak:index/lucene` to `/oak:index/lucene2` (then save)
4. Go to http://host:port/crx/packmgr/index.jsp
5. Upload and install the attached package [cq-6.5-lucene-index-1.1.zip](cq-6.5-lucene-index-1.1.zip?raw=true)
6. That will install the newer version of `/oak:index/lucene`
7. The new index will start indexing and will use the pre-extracted text as configured in step 1.
8. Once the indexing is running you would monitor it until the `reindex` property of `/oak:index/lucene` gets automatically set to `false`.
   Here are some docs for monitoring reindexing:
    * https://blogs.perficient.com/2017/04/19/rebuild-indexes-in-aem-with-oak-index-manager/
    * https://experienceleague.adobe.com/docs/experience-manager-64/deploying/deploying/troubleshooting-oak-indexes.html?lang=en#slow-re-indexing
9. When the indexing of the new `/oak:index/lucene` is complete then go to http://host:port/crx/de/index.jsp again.
10. Browse to the old index `/oak:index/lucene2` and set these properties:
      a. Set `entryCount` (with type `Long`) = `99999999999`
      b. Set `refresh` (with type `Boolean`) = `true`
      This effectively prevents the index from being used without removing it from the system.
11. Save
12. Now you can test with the new index.  Once you confirm it working then it is safe to delete `/oak:index/lucene2` and its corresponding index copy directory under `crx-quickstart/repository/index`.

Note: Please test this in a lower environment and let me know how it works for you.
