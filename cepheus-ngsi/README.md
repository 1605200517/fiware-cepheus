# FIWARE NGSI API

This project is a java implementation of the [NGSI v1 API](http://telefonicaid.github.io/fiware-orion/api/v1/)
and part of the Fiware-Cepheus project under the GPLv2 License.

This implementation supports java 7 and java 8.

## Note

This library will move to [Orange-OpenSource/fiware-ngsi-api](https://github.com/Orange-OpenSource/fiware-ngsi-api)
under the Apache Licence Version 2 to ease its reuse by other FIWARE projects.

## Usage

### From Maven

```xml
<dependency>
    <groupId>com.orange.cepheus</groupId>
    <artifactId>cepheus-ngsi</artifactId>
    <version>0.1.5</version>
</dependency>
```

For java 7, you must add the classifier:

```xml
<dependency>
   <groupId>com.orange.cepheus</groupId>
   <artifactId>cepheus-ngsi</artifactId>
   <classifier>java7</classifier>
   <version>0.1.5</version>
</dependency>
```

### Download the jar from [Sonatype Central repository](http://central.sonatype.org/) using wget

If you don't have `maven` installed on your machine, you can still download the standalone JAR using `wget` or any browser:

    wget -O cepheus-ngsi.jar "https://oss.sonatype.org/service/local/artifact/maven/redirect?r=releases&g=com.orange.cepheus&a=cepheus-ngsi&v=LATEST"

## Client
The implementation provides the NgsiClient class for the standard operations and NgsiRestClient for the convenient operations.
This class implements the ngsi-10 standard and convenient operations following:

    updateContext
    subscribeContext
    updateContextSubscription
    unsubscribeContext
    notifyContext
    queryContext
    
This class implements one ngsi-9 standard and convenient operation following:

    registerContext
    
The client use an synchronous or asynchronous mechanism.

Example: to send a synchronous updateContext

    NgsiClient ngsiClient;
    ngsiClient.updateContext(providerUrl, httpHeaders, update).get();

Example: to send an asynchronous updateContext

    NgsiClient ngsiClient;
    ngsiClient.updateContext(brokerUrl, httpHeaders, update)
              .addCallback(updateContextResponse -> logUpdateContextResponse(updateContextResponse, brokerUrl),
                       throwable -> logger.warn("UpdateContext failed for {}: {}", brokerUrl, throwable.toString()));
       

## Server
The implementation provides the NgsiBaseController class that is a controller class for the standard operations and
the NgsiRestBaseController class used for the convenient operations.
The two classes validate the specification rules and return errors if an exception is thrown.

Your controller class must override the methods you want implement. By default the methods return an error "not implemented operation".

    public class NgsiController extends NgsiBaseController {
    @Override
        public UpdateContextResponse updateContext(final UpdateContext update) throws Exception {

or

    public class NgsiRestController extends NgsiRestBaseController {
    @Override
        protected AppendContextElementResponse appendContextElement(String entityID, AppendContextElement appendContextElement) throws Exception {
        

## License

This project is under the GPL.
