
In certain rare scenarios, the Jackrabbit Oak repository could be in a state where an IndexUpdate cycle requires more memory than the system has available.
This can happen if massive amounts of nodes are deleted using oak-run console.  When AEM is started, it requires too much memory to calculate the diff of the old checkpoint to the HEAD revision of the data on the system.  To deal with these situations we can either reindex entirely or skip indexing of the changes that are causing the issue.  The steps below explain how to skip the changes that are causing the memory issues.

## Process to skip the index update instead of reindexing
The steps below show how to skip index updates specifically for the "async" indexing lane.
1. Stop AEM
2. Download oak-run console 1.22.11 (from [here](https://repo1.maven.org/maven2/org/apache/jackrabbit/oak-run/1.22.11/oak-run-1.22.11.jar) to the server
3. Open oak-run console and run the checkpoint command in the console to create a new checkpoint
4. Download setProperty.groovy script to the server under the same directory:
    ```
    wget https://gist.githubusercontent.com/andrewmkhoury/b2599fa59079828bea83/raw/setProperty.groovy
    ```
5. Run oak-run console
    ```
    java -Xmx20g -jar oak-run-1.22.11.jar console --read-write crx-quickstart/repository/segmentstore
    ```
6. Once the oak-run console has loaded and it provides you with a prompt, then run the `checkpoint`
    command to create a new checkpoint.  Copy and save the resultant checkpoint id.
    ```
    checkpoint
    ```
    Example output:
    ```
    09b6d428-0bbb-4a5c-a4a5-079d86fb51f4
    ```
7. Run these commands with the new checkpoint id (replace the id `09b6d428-0bbb-4a5c-a4a5-079d86fb51f4` with the one from your system).  Modify the setProperty parameters as needed to fast-forward indexing of other index lanes.
    ```
    :load setProperty.groovy
    setProperty(session, "/:async", "async", "09b6d428-0bbb-4a5c-a4a5-079d86fb51f4", false)
    setProperty(session, "/:async", "async-temp", "09b6d428-0bbb-4a5c-a4a5-079d86fb51f4", false)
    ```
8. Use command `:exit` to exit the oak-run console shell:
    ```
    :exit
    ```
9. Once the shell exits thene start it again to reinitialize the shell
    ```
    java -Xmx20g -jar oak-run-1.22.11.jar console --read-write crx-quickstart/repository/segmentstore
    ```
10. When the shell opens then run these commands to confirm the properties were set:
    ```
    cd :async
    print-node
    ```
11. If the `async` and `async-temp` properties have the new value you set in the steps above then it is all ready to start AEM.
12. Exit the oak-run console shell by running `:exit` command.
    ```
    :exit
    ```
13. Start AEM
14. Monitor the log files, grepping for "Reindexing" to make sure that the system doesn't start reindexing.
15. Go to the /system/console/jmx UI and run listCheckpoints() on the CheckpointManager MBean to verify that the checkpoints are now up to date.  You can visit this URL below to go directly to the MBean:
    ```
    /system/console/jmx/org.apache.jackrabbit.oak%3Aname%3DSegment+node+store+checkpoint+management%2Ctype%3DCheckpointManager
    ```
