_This document contains upgrade notes for Agrest 5.x and newer. Older versions are documented in 
[UPGRADE-NOTES-1-4](./UPGRADE-NOTES-1-to-4.md)._

## Upgrading to 5.0.M17

### No more array syntax for updating to-one relationships, and single-value - for to-many [#633](https://github.com/agrestio/agrest/issues/633)
A number of changes were implemented to improve consistency and functionality of the update pipeline. As a result
the array-based syntax for updating to-one relationships is no longer supported as it is semantically invalid. E.g. the 
following won't work anymore: `{"toOne":[1]}`, and should be changed to `{"toOne":1}`. Similarly, a single-value
syntax for updating to-many relationships should be replaced with arrays: `{"toMany":[1]}` -> `{"toMany":[1]}`

## Upgrading to 5.0.M16

### Got rid of custom date/time formatters [#621](https://github.com/agrestio/agrest/issues/621)

To fix formatting issues of the older dates (19th century and earlier), date/time parsing and encoding code was
refactored. Instead of a set of custom DateTimeFormatters, we are using the standard ISO formatters from the JDK. 
This introduced slight behavior changes (most are actually desirable in any sane codebase) :

* No more arbitrary truncation of time precision. E.g. a time with 1ns would render as `00:00:00.000000001`, where it would
previously render as `00:00:00`
* If a property is modeled as the old `java.sql.Time`, we will no longer allow to parse times starting with "T". So
`T00:00:00` will no longer work, while `00:00:00` will. This does not affect parsing of `java.time.LocalTime`, as it 
already disallowed the leading "T".

## Upgrading to 5.0.M15

### To include UriInfo in OpenAPI, an explicit annotation is required [#619](https://github.com/agrestio/agrest/issues/619)
This only affects projects that auto-generate OpenAPI documentation descriptors from Agrest endpoints.
Previously, the framework would automatically add every single Ag Protocol parameter to the endpoint signature
if an endpoint method had `@Context UriInfo` as one of the parameters. This proved to be rather inflexible
in many situations. So now by default `UriInfo` will be ignored for documentation purposes, and the way to
turn on the old behavior is to annotate it explicitly like this: `@Parameter UriInfo` (with or without `@Context`).

## Upgrading to 5.0.M1

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
Instead of endpoint generation from the OpenAPI model, we offer code-first documentation approach via 
"agrest-jaxrs2-openapi" and "agrest-jaxrs3-openapi" modules.

### Ag protocol v1.2 - protocol changes [#561](https://github.com/agrestio/agrest/issues/561)
This primarily affects client-side developers. There were some Agrest protocol changes introduced. The current version 
of the protocol is called "1.2". Most of the changes are backwards-compatible (i.e. the previous protocol v1.1 
assumptions should work). Though we still encourage client implementors to use the new flavor. One potentially breaking 
change is the removal of `"success":boolean` property from the "simple" response object returned from some POST/PUT and 
all DELETE requests. The clients should not be checking `"success":boolean` in the JSON body anymore, and should 
instead check the response HTTP status codes. This is common sense, and we hope most of the clients were doing that already. 

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

### Endpoint metadata API is removed [#541](https://github.com/agrestio/agrest/issues/541)
Agrest no longer includes an API for describing Agrest endpoints metadata (`Ag.metadata(..)`). Instead, it provides 
integration with OpenAPI / Swagger via `agrest-jaxrs2-openapi` and `agrest-jaxrs3-openapi`. Unfortunately, the two 
ways to document endpoints (the old Agrest metadata responses vs OpenAPI) are not directly compatible. Though we feel
that the OpenAPI approach is more powerful and, more importantly, can be used with or without Agrest. It
has become a de-facto standard for REST API documentation, and is the way forward for Agrest as well.

### Custom Exception mapping is no longer tied to JAX-RS [#530](https://github.com/agrestio/agrest/issues/530)

Now if you need a mapper for a custom Exception to be rendered in a response in a certain way, instead of 
using JAX-RS API (`binder.bindMap(ExceptionMapper.class)`), you would bind a special AgExceptionMapper 
(`binder.bindMap(AgExceptionMapper.class)`) that converts a custom exception to AgException instead of Response. 

In addition to changing the API above, if your code throws `CayenneRuntimeException`, `ValidationException` or any
custom exception other than `AgException` explicitly outside Agrest processing chain, you will need to replace 
those exceptions with `AgException` to ensure it is rendered cleanly in the response. Throwing within custom "stages"
works the same way as before and requires no changes.

### (JAX-RS) Dependencies [#537](https://github.com/agrestio/agrest/issues/537)
* All JAX-RS specific code is now moved to a new module - `agrest-jaxrs2`. It will need to be explicitly added to the 
application dependencies. 
* `agrest-openapi` got renamed to `agrest-jaxrs2-openapi` and will require renaming in Maven
pom.xml or Gradle scripts.

### (JAX-RS) Starting the stack [#537](https://github.com/agrestio/agrest/issues/537), [#565](https://github.com/agrestio/agrest/issues/565)
As JAX-RS is no longer a part of Agrest core, and comes from two separate modules (`agrest-jaxrs2` and `agrest-jaxrs3`), 
Agrest runtime startup in a JAX-RS environment looks different:
```java
// assuming Agrest with JAX-RS and Cayenne

ServerRuntime cayenneRuntime = ...;

AgCayenneModule cayenneModule = AgCayenneModule.build(cayenneRuntime);
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
`SelectStage.PARSE_REQUEST` is no longer needed, as protocol parameter parsing happens outside a stage, right in
the context. If you had stage callbacks referencing this stage, you may instead reference `SelectStage.START`.

### AgDataMap is renamed to AgSchema [#562](https://github.com/agrestio/agrest/issues/562)
Injectable `AgDataMap` is renamed to `AgSchema`. If you accessed this object directly (which is not typical to do in 
most apps), make sure you change the name in your code.
