# How to allow CORS requests to AEM instances
For details on what CORS (Cross-Origin Resource Sharing) is then see [here](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS).

1. Follow [this documentation](https://experienceleague.adobe.com/docs/experience-manager-learn/foundation/security/develop-for-cross-origin-resource-sharing.html?lang=en) on the initial configuration.
2. Follow [this document](https://experienceleague.adobe.com/docs/experience-manager-learn/foundation/security/understand-cross-origin-resource-sharing.html?lang=en) as well
3. Test the configurations directly against a local AEM instance (`SITEHOST` variable should point directly to the AEM publish instance you configured and `ORIGIN` should be the base URL of the site where the AJAX/CORS request will originate from).  You can test it with the curl script below:
    ```   
    #!/bin/bash
    SITEHOST=http://aemserverhostname:4503
    ORIGIN=https://sitehost.makingcorsrequest.com
    curl -X OPTIONS -k -v \
    -H "Access-Control-Request-Method: POST" \
    -H "Access-Control-Request-Headers: X-Requested-With" \
    -H "Origin: $ORIGIN" \
    -H "X-Forwarded-Proto: https" \
    $SITEHOST
    ```
   If the request works then in the response you should see `access-control-allow-*` headers:
    ```
    > OPTIONS / HTTP/2
    > Host: http://aemserverhostname:4503
    > User-Agent: curl/7.64.1
    > Accept: */*
    > Access-Control-Request-Method: POST
    > Access-Control-Request-Headers: X-Requested-With
    > Origin: https://401518-contentsymphonydev-shelly.adobeio-static.net
    > X-Forwarded-Proto: https
    > 
    * Connection state changed (MAX_CONCURRENT_STREAMS == 100)!
    < HTTP/2 204 
    < access-control-allow-origin: https://sitehost.makingcorsrequest.com
    < access-control-allow-credentials: true
    < access-control-allow-methods: GET, HEAD, POST, PUT, DELETE, OPTIONS
    < access-control-allow-headers: X-Requested-With
    < access-control-max-age: 1800
    < set-cookie: affinity="60941634c1fb3b2a"; Path=/; HttpOnly
    < accept-ranges: bytes
    < date: Fri, 27 Aug 2021 16:41:15 GMT
    < via: 1.1 varnish
    < strict-transport-security: max-age=31557600
    < x-served-by: cache-bur17576-BUR
    < x-cache: MISS
    < x-cache-hits: 0
    < x-timer: S1630082475.528734,VS0,VS0,VE611
    ```
    
4. Now test with a dispatcher added.  Testing with the [dispatcher docker container](https://experienceleague.adobe.com/docs/experience-manager-cloud-service/implementing/content-delivery/disp-overview.html?lang=en) provided in the AEM SDK would work great for this test.

    These items should be added to the farm config for the allowed request headers `/clientheaders` config - this is in the `/clientheaders` configuration:
    ```
    Access-Control-Request-Method
    Access-Control-Request-Headers
    X-Requested-With
    ```
    
    These items should be added to the farm config for response header caching - this is in the `/cache` -> `/headers` configurations:
    ```
    "Cache-Control"
    "Content-Disposition"
    "Content-Type"
    "Expires"
    "Last-Modified"
    "X-Content-Type-Options"
    "Access-Control-Allow-Origin"
    "Access-Control-Expose-Headers"
    "Access-Control-Max-Age"
    "Access-Control-Allow-Credentials"
    "Access-Control-Allow-Methods"
    "Access-Control-Allow-Headers"
    ```
    To allow OPTIONS requests at dispatcher we add this in the vhost config under <Directory /> (under /etc/conf.d/enabled_vhosts):
    ```
    <Limit OPTIONS>
      Require all granted
    </Limit>
    ```
5. Once again, test with the curl script from step 3, but this time change the `SITEHOST` variable to point to your dispatcher instead of AEM directly.
    ``` 
    #!/bin/bash
    SITEHOST=http://dispatcherserverhostname
    ORIGIN=https://sitehost.makingcorsrequest.com
    curl -X OPTIONS -k -v \
    -H "Access-Control-Request-Method: POST" \
    -H "Access-Control-Request-Headers: X-Requested-With" \
    -H "Origin: $ORIGIN" \
    -H "X-Forwarded-Proto: https" \
    $SITEHOST
    ```
    Output should be similar to step 3.
6. Repeat the same curl script against the Load Balancer, WAF, CDN, etc.  Basically repeat until you finally get the CORS request working against the actual domain / base URL that the AJAX call would be sent to. 
