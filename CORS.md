# How to allow CORS requests to AEM instances
For details on what CORS (Cross-Origin Resource Sharing) is then see [here](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS).

1. Follow [this documentation](https://experienceleague.adobe.com/docs/experience-manager-learn/foundation/security/develop-for-cross-origin-resource-sharing.html?lang=en) on the initial configuration.
2. Follow [this document](https://experienceleague.adobe.com/docs/experience-manager-learn/foundation/security/understand-cross-origin-resource-sharing.html?lang=en) as well
3. Test the configurations directly against a local AEM instance.  You can test it with the curl script below:
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
