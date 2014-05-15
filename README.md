# LinkRest

LinkRest is a server-side Java REST framework for easy access of backend data stores. Its goal is to simplify backend interaction of client applications, such as JavaScript apps or a native mobile apps. It is closely integrated with [JAX-RS 2.0](http://en.wikipedia.org/wiki/Java_API_for_RESTful_Web_Services) REST containers and minimizes the amount of the server-side code needed to expose your data.

LinkRest was originally written with Sencha/ExtJS clients in mind, but it is not specific to a any single client technology. It can be used from iOS and Android applications, jQuery, etc.

LinkRest is written on top of [Apache Cayenne ORM](http://cayenne.apache.org/) and supports any relational data store, such as MySQL or Oracle. Alternative data stores can be provided with relative ease for NoSQL databases, flat files, etc. Users can also use LinkRest as "protocol adapter" and provide their own data.

LinkRest is open source and is distributed under the terms of [BSD license](https://github.com/nhl/link-rest/blob/master/LICENSE.txt).

## LinkRest Protocol

LinkRest defines a simple communication protocol between client and server based on HTTP and JSON. Client applications use it to read or modify data entities. The main feature of LinkRest Protocol is the ability of the client to customize fine details of the response JSON. The clients can explicitly request inclusion in response of any given set of attributes (including related entities and their attributes), specify filtering criteria, sort ordering, pagination, etc. This allows to implement general purpose REST API with very little to no effort.

TODO: protocol details

## What's Needed

* Java 1.7 or newer
* A JAX-RS 2.0 container, such as Jersey 2.x.
* A Maven Java "war" project that will serve your REST requests. You don't have to use Maven. But the docs here are assuming you are.
* Cayenne 3.2M2 or newer. Mapping your database and starting Cayenne ServerRuntime is outside the scope of this document. Please refer to the [corresponding Cayenne docs](http://cayenne.apache.org/docs/3.1/cayenne-guide/index.html).

_TODO: Since M2 is not officially released as of this writing, LinkRest itself references an unofficial build from ObjectStyle repo. If you configure custom Maven repository per instructions below, you should be able to get it._

## Bootstrap LinkRest

Declare LinkRest Maven repository in your pom.xml (unless you have your own repo proxy, in which 
case add this repo to the proxy):

    <repositories>
        <repository>
            <id>lr-repo</id>
            <name>ObjectStyle LinkRest Repo</name>
            <url>http://maven.objectstyle.org/nexus/content/repositories/linkrestreleases</url>
        </repository>
    </repositories>
    
_TODO: eventually we'll publish LinkRest in Central so the step above will not be needed_
	
Add LinkRest dependency:

    <dependency>
        <groupId>com.nhl.link.rest</groupId>
        <artifactId>link-rest</artifactId>
        <version>1.0</version>
    </dependency>

On application startup assemble LinkRest stack and bootstrap LinkRest JAX RS "feature". A good place to do that is inside your JAX RS Application class. E.g. if your are using Jersey 2 JAX container:

    import org.glassfish.jersey.server.ResourceConfig;

    public class MyApp extends ResourceConfig {
    
        public MyApp() {
            // bootstrap Cayenne
            ServerRuntime cayenneRuntime  = new ServerRuntime("my-cayenne.xml");
    
            // bootstrap LinkRest with the minimal set of options
            LinkRestRuntime lrRuntime = new LinkRestBuilder().cayenneRuntime(cayenneRuntime).build();
            
            // register LinkRest as a JAX RS "feature"
            register(lrRuntime.getFeature());
        }
    }
    
## Use LinkRest

In a resource class you need to get a hold of ILinkRestService to perform any operations. It is stored in injectable JAX RS Configuration class:

    @Path("address")
    public class AddressResource {
        
        @Context
        private Configuration config;
        
        private ILinkRestService getLinkRest() {
            // find ILinkRestService in Configuration properties
            return LinkRestRuntime.service(ILinkRestService.class, config);
        }
        
        @GET
        public DataResponse<Address> getAddresses(@Context UriInfo uriInfo) {
            // execute a generic select. All details are passed by the client in UriInfo
            return getLinkRest().forSelect(Address.class).with(uriInfo).select();
        }
        
    }
    
A note on injection. If your container supports @Inject annotation, you may inject ILinkRestService singleton in resources directly, instead of looking it up in Configuration. The service is accessible via LinkRestRuntime:

    ILinkRestService linkRest = lrRuntime.service(ILinkRestService.class);
    
Consult your JAX RS provider documentation on how to make this object available for injection.
