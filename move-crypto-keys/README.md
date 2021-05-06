# Overview
In AEM 6.4 and 6.5 the crypto keys that are included in an AEM instance were moved from the Oak repository (under /etc/key) to the file system under `crx-quickstart/launchpad/felix/bundle${BUNDLE_ID}/data` on the file system (where `${BUNDLE_ID}` is the id of the `com.adobe.granite.crypto` bundle).  If you do not move the keys from the repository to the file system then certain features of AEM will not work properly.

For example when using any features that leverage IMS authentication, when the token expires you would get an error like the one below when the system tries to get a refresh token:
```
com.adobe.granite.auth.oauth.AccessTokenProvider failed to get access token from authorization server status: 400 response:
{"error":"invalid_client","error_description":"invalid client_secret parameter"}
```

# Steps
To move the crypto keys in AEM from the JCR to the file system follow the steps below.  This applies to AEM installations that were upgraded from 6.3 and older versions to 6.4 or 6.5.

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
8. There is a file named ```storage``` in the ```data``` directory, download this [storage file](https://raw.githubusercontent.com/cqsupport/fix-instructions/master/move-crypto-keys/storage) and upload and replace the one one the server.  You must replace the file instead of just changing the value from ```JCR``` to ```Bundle``` because the file must not have a newline char at the end, [most editors like vi add a newline char](https://vi.stackexchange.com/questions/3434/dont-add-new-line-at-the-end-of-a-file).

9. Go to to the directory ```crx-quickstart/launchpad/felix/bundle${FILE_BUNDLE_ID}/data```, where ```${FILE_BUNDLE_ID}``` is replaced by the "Bundle Id" value from step 7 (the crypto.file bundle ID).
10. Copy the ```master``` and ```hmac``` files from step 3 to this bundle's ```data``` folder.
11. Start AEM
12. Remove the keys from the repository via CRXDE by removing the two properties ```hmac``` and ```master``` from the node ```/etc/key```.
13. Click "Save All" to save
