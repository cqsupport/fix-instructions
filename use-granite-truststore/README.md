In AEM as a Cloud service there is no way to create a custom keystore on the server's file system and add the JVM parameters.

To use a truststore or keystore in Cloud Service, see the Granite Trust Store https://docs.adobe.com/content/help/en/experience-manager-65/forms/administrator-help/manage-certificates-credentials/certificates.html

The UI for the Trust Store can be found here:
/libs/granite/security/content/truststore.html

1. Create the Trust Store and upload the certificates you need to be trusted via the UI

2. Edit your code to use a custom SSL Context so you can pass it the Granite KeystoreService's Trust Store.

https://hc.apache.org/httpcomponents-client-4.3.x/httpclient/examples/org/apache/http/examples/client/ClientCustomSSL.java
https://prasans.info/making-https-call-using-apache-httpclient/

Below is the code from the AEM product that wires in this trust store to Jetty for server connections.  Similar code would be used for setting ssl context in HttpClient library for client calls.


Imports:
-------
    import com.adobe.granite.crypto.CryptoException;
    import com.adobe.granite.crypto.CryptoSupport;
    import com.adobe.granite.keystore.KeyStoreService;

OSGi services needed:
-------
    @Reference(policyOption = ReferencePolicyOption.GREEDY)
    private KeyStoreService keyStoreService;

    @Reference(policyOption = ReferencePolicyOption.GREEDY)
    private ResourceResolverFactory resourceResolverFactory;

    @Reference(policyOption = ReferencePolicyOption.GREEDY)
    private SlingRepository slingRepository;
    
    @Reference(policyOption = ReferencePolicyOption.GREEDY)
    private CryptoSupport cryptoSupport;

Code for setting ssl context:
------
    KeyStore keyStore = null;
    KeyStore trustStore = null;
    Session userSession = null;
    ResourceResolver resolver = null;
    try {
        userSession = getSession();
        resolver = getResourceResolver(userSession);
        keyStore = keyStoreService.getKeyStore(resolver);
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(SslContextFactory.DEFAULT_KEYMANAGERFACTORY_ALGORITHM);
        keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());

        trustStore = keyStoreService.getTrustStore(resolver);
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(SslContextFactory.DEFAULT_TRUSTMANAGERFACTORY_ALGORITHM);
        trustManagerFactory.init(trustStore);

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(keyManagerFactory.getKeyManagers(),  trustManagerFactory.getTrustManagers(), new SecureRandom());
        sslContextFactory.setSslContext(context);

        connector.setPort(port);

        return connector;
    } catch (Exception e) {
        throw new RuntimeException("Exception while creating connector", e);
    } finally {
        if (resolver != null) {
            resolver.close();
        }
        if (userSession != null) {
            userSession.logout();
        }
    }
