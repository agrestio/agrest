_This document contains upgrade notes for Agrest 5.x and newer. Older versions are documented in 
[UPGRADE-NOTES-1-4](./UPGRADE-NOTES-1-to-4.md)._

## Upgrading to 5.0

5.0 is a major release with a number of breaking changes. Still the upgrade should be fairly straightforward in most
situations. Check below for individual changes that may require user attention.

### Switch to Java 11 [#515](https://github.com/agrestio/agrest/issues/515)
Java 11 is the minimal required Java version. If you are still on Java 8, please continue using Agrest 4.x.

### "agrest-sencha" was removed [#517](https://github.com/agrestio/agrest/issues/517)
"agrest-sencha" extensions module is no longer supported. If you still care for Sencha extensions, you may either 
downgrade to Agrest 4.x or try to adapt your client code to the standard Agrest protocol. In short: no "filter" URL key
(you should be able to express via "exp" key), and no foreign keys in responses (those can be read from related 
objects instead).

### "agrest-openapi-designfirst" was removed [#518](https://github.com/agrestio/agrest/issues/518)
"agrest-openapi-designfirst" extensions module was removed. It wasn't well-maintained and didn't work that well. 
Instead of endpoint generation from the OpenAPI model, we offer code-first documentation approach via "agrest-jaxrs-openapi"
module.

### Multi-property ID format changed for Cayenne objects [#521](https://github.com/agrestio/agrest/issues/521)
Entities can have single-value and multi-value IDs. Single-value id is represented in JSON as `"id":val`. Multi-value 
- as `"id":{"p1":v1, "p2":v2}`. This representation is used for both request bodies (POST, PUT), and responses 
- (GET, POST, PUT).

The change affects Cayenne entities with multi-value IDs. A subset of id properties that are not mapped as object 
properties was using column names for id keys. From now on any column names will be prefixed with "db:". E.g.: 
`"id":{"db:col1":v1, "db:col2":v2, "prop":v3}`.

On the client this affects the following requests:
* Those that submit updates for entities with such ids
* Those that parse responses with such ids for individual id values (instead of treating the ID as opaque)

On the server this affects the following user-facing APIs:
* `SelectBuilder.byId(Map)`
* `SelectBuilder.parent(Class,Map,String)`
* `UpdateBuilder.parent(Class,Map,String)`
* `DeleteBuilder.byId(Map)`
* `DeleteBuilder.parent(Class,Map,String)`

### "agrest-client" was removed [#527](https://github.com/agrestio/agrest/issues/527)
Java Agrest client ("agrest-client" module) was removed. Straight HTTP requests can be used to query Agrest services 
from Java. 

TODO: we might add a utility for DataResponse deserialization in the Agrest core. Nothing like that is 
available yet. 

### Custom Exception mapping is no longer tied to JAX-RS [#530](https://github.com/agrestio/agrest/issues/530)

Now if you need a mapper for a custom Exception to be rendered in a response in a certain way, instead of 
using JAX-RS API (`binder.bindMap(ExceptionMapper.class)`), you would bind a special AgExceptionMapper 
(`binder.bindMap(AgExceptionMapper.class)`) that converts a custom exception to AgException instead of Response. 

In addition to changing the API above, if your code throws `CayenneRuntimeException`, `ValidationException` or any
custom exception other than `AgException` explicitly outside of Agrest processing chain, you will need to replace 
those exceptions with `AgException` to ensure it is rendered cleanly in the response. Throwing within custom "stages"
works the same way as before and requires no changes.

### (JAX-RS) Dependencies [#537](https://github.com/agrestio/agrest/issues/537)
* All JAX-RS specific code is now moved to a new module - `agrest-jaxrs`. It will need to be explicitly added to the 
application dependencies. 
* `agrest-openapi` got renamed to `agrest-jaxrs-openapi` and will require renaming in Maven
pom.xml or Gradle scripts.

### (JAX-RS) Starting the stack [#537](https://github.com/agrestio/agrest/issues/537)
As JAX-RS is no longer a part of Agrest core, and comes from a separate module, Agrest runtime startup in a JAX-RS 
environment looks different:
```java
// assuming Agrest with JAX-RS and Cayenne

ServerRuntime cayenneRuntime = ...;

Module cayenneModule = AgCayenneBuilder.build(cayenneRuntime);
AgRuntime runtime = AgRuntime.builder().module(cayenneModule).build();
AgJaxrsFeature feature = AgJaxrsFeature.runtime(runtime).build();

// register feature in a JAX-RS container
```

### (JAX-RS) Use of `Ag` [#537](https://github.com/agrestio/agrest/issues/537)
`Ag` class is no longer present in Agrest, as it was too tightly coupled with JAX-RS. You should replace it with 
`AgJaxrs`.

### (JAX-RS) SelectBuilder / UpdateBuilder "uri()" is replaced with "clientParams()" [#537](https://github.com/agrestio/agrest/issues/537)
To remove a direct dependency oin JAX-RS API, `SelectBuilder.uri(UriInfo)` and `UpdateBuilder.uri(UriInfo)`are replaced
with `SelectBuilder.clientParams(Map)` and `UpdateBuilder.clientParams(Map)`. Parameters map passed as an argument 
corresponds to `UriInfo.getQueryParameters()`.

### SelectStage.PARSE_REQUEST is gone [#537](https://github.com/agrestio/agrest/issues/537)
`SelectStage.PARSE_REQUEST` is no longer needed, as protocol parameter parsing happens outside of a stage, right in
the context. If you had stage callbacks referencing this stage, you may instead reference `SelectStage.START`.
