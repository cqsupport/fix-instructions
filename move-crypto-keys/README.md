How to move the crypto keys in AEM from the JCR to the file system.

1. Log into AEM as admin
2. Go to http://host/crx/de/index.jsp
3. Download the content of the ```master``` and ```hmac``` properties in the ```/etc/key``` node.
4. Go to http://host/system/console/bundles/com.adobe.granite.crypto 
5. Copy the "Bundle Id" number to a text file for reference
6. Go to http://host/system/console/bundles/com.adobe.granite.crypto.file
7. Copy the "Bundle Id" number of this file to the text file as well referencing it as "crypto.file"
8. Stop AEM
9. Go to ```crx-quickstart/launchpad/felix``` and change directores to the directory ```crx-quickstart/launchpad/felix/bundle${BUNDLE_ID}/data```, where ```${BUNDLE_ID}``` is replaced by the "Bundle Id" value from step 5.
For example, on my system this folder is ```crx-quickstart/launchpad/felix/bundle29/data```.
8. Edit the file named ```storage``` in the ```data``` directory, change the value in the file from ```JCR``` to ```Bundle```
9. Go to to the directory ```crx-quickstart/launchpad/felix/bundle${FILE_BUNDLE_ID}/data```, where ```${FILE_BUNDLE_ID}``` is replaced by the "Bundle Id" value from step 7 (the crypto.file bundle ID).
10. Copy the ```master``` and ```hmac``` files from step 3 to this bundle's ```data``` folder.
11. Edit the AEM startup script so that this additional JVM parameter is set during startup:
    ```
    -Dcom.adobe.granite.crypto.file.disable=false
    ```
12. Start AEM
13. Remove the keys from the repository via CRXDE by removing the two properties ```hmac``` and ```master``` from the node ```/etc/key```.
14. Click "Save All" to save
