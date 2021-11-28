## Upgrading to 4.8

### Bulk updates may return "201" status where previously they returned "200" [#503](https://github.com/agrestio/agrest/issues/503)

Now that we have more precise data about operation types on update, Agrest would return status 201 instead of 200 for 
bulk create operations (previously it would only return 201 for single-object create ops). Client code must be aware of 
this and act accordingly. A mix of create and update operations would still return 200, as before.

## Upgrading to 4.7

### `@ClientReadable` and `@ClientWritable` annotations are removed in favor of per-attribute access controls [#491](https://github.com/agrestio/agrest/issues/491)

As a part of an effort to unify constraint handling, `@ClientReadable` and `@ClientWritable` annotations have been 
removed, as a rather inelegant mechanism to reference class properties. Instead `@Ag*` annotations on getters 
(`@AgId`, `@AgAttribute` and `@AgRelationship`) now provide "readable" and "writable" properties. By default all
properties are both readable and writable. So you must review your code and replace `@ClientReadable` and 
`@ClientWritable` with explicit annotations on properties that you would like to **exclude** from either reading or
writing. E.g. if a property "a" was "client-readable", then `@ClientReadable` annotation should be removed, and 
properties "b", "c", etc. should be annotated with `@AgAttribute(readable=false)` / `@AgRelationship(readable=false)`

### Injectable EntityConstraint is removed in favor of per-attribute access controls [#491](https://github.com/agrestio/agrest/issues/491)

There was a little-known mechanism to programmatically define runtime-wide policy for entity read and write property
exclusion - an injectable `EntityConstraint`. List injection keys were `agrest.constraints.read.list` and `agrest.constraints.write.list`
for read and write constraints respectively. As a part of an effort to unify constrain handling, this mechanism was
removed. There are two alternatives: `@Ag*` annotations on attributes, relationships and ids now have "readable" and 
"writable" properties; also `AgEntityOverlay` API allows redefining existing property readability and writeability, 
either globally or per-request. 

### Updates may return "201" status where previously they returned "200" [#490](https://github.com/agrestio/agrest/issues/490)

All variants of "create or update" operations would now return 201 code instead of 200 when performed on a single object
and if the object was newly created. While this is the correct response code (and the previous behavior was incorrect),
this may affect existing assumptions made by Agrest consumers. Consumer code should be revisited to ensure that
201 response code is properly handled.

### AgException API changed [#495](https://github.com/agrestio/agrest/issues/495)

`AgException` API has changed. `javax.ws.rs.core.Response.Status` was replaced with an int inside `AgException`. 
So any code explicitly creating this exception, must pass standard HTTP status codes as integers. For convenience, they 
are defined in `io.agrest.HttpStatus`. 

Instead of public constructors, AgException now provides a set of static factory methods that allow to build exception
messages with String templates and parameters.

## Upgrading to 4.4

### 'Exp' constructors replaced with static factory methods  [#476](https://github.com/agrestio/agrest/issues/476)

`Exp` class was converted to an interface to allow to compose expression trees from parts. As a result all its 
constructors got replaced with convenience static methods - `Exp.simple()`, `Exp.withPositionalParams()`, 
`Exp.withNamedParams()`. If you see compilation errors, replace constructor calls accordingly.

## Upgrading to 4.2

### Legacy "io.agrest:agrest" module is removed [#472](https://github.com/agrestio/agrest/issues/472) 

If you imported legacy `io.agrest:agrest` module as a dependency, remove it, and import these two modules instead: 
`io.agrest:agrest-engine` and `io.agrest:agrest-cayenne` (of course Cayenne module is needed only if you are using 
Cayenne as Agrest backend).

## Upgrading to 4.1

### Metadata API is deprecated in favor of OpenAPI [#466](https://github.com/agrestio/agrest/issues/466) 

Since we now support integration with OpenAPI / Swagger, Agrest own less capable metadata API is deprecated. While
we plan to keep it around for a while, it is wise to stay awy from it, and if you are already using it, switch to
OpenAPI. 

## Upgrading to 4.0

### Cayenne 4.2

This release is integrated with Cayenne 4.2 and will not work with earlier Cayenne releases. 

## Upgrading to 3.7

### "SelectBuilder.property()" replaced with simpler "SelectBuilder.entityAttribute()" [#452](https://github.com/agrestio/agrest/issues/452) 

As a part of the effort to consolidate various customization APIs, two "SelectBuilder.property()" methods were replaced
with a single "SelectBuilder.entityAttribute()" method that relies on the standard entity overlay customization 
mechanism behind the scenes. It is more user-friendly, as it allows a user to provide `Function<T, V> reader` 
instead of an obscure `EntityProperty property`.

### "ResourceEntity.getSelect()" is not available [#453](https://github.com/agrestio/agrest/issues/453) 

`ResourceEntity.getSelect()` is removed, so that there's no direct Cayenne dependency in generic Agrest API. 
While there's still a way to access it using `CayenneProcessor` class, you will no longer be able to set an arbitrary
query as a "template" for Cayenne. So if you need to customize query parameters, such as expressions, orderings, includes,
look into using ResourceEntity API instead.

### Cayenne Expression is no longer part of ResourceEntity [#457](https://github.com/agrestio/agrest/issues/457) 

In a continuing effort to remove dependency on Cayenne, ResourceEntity tree is no longer using Expression "qualifier". Instead ir is replaced with a collection of Agrest `CayenneExp` objects. This may effect customization code if it attempted to change query conditions. Our recommendation is to replace Cayenne Expression class in any such code with CayenneExp. 

A special case is Sencha module. Callers of `SenchaOps.startsWithFilter(..)` must attach it to `SelectStage.APPLY_SERVER_PARAMS` stage, not `SelectStage.ASSEMBLE_QUERY`, or it will not be applied.

## Upgrading to 3.6

### Separate "commit" in its own UpdateStage [#446](https://github.com/agrestio/agrest/issues/446) 

If you customized `UpdateStage.UPDATE_DATA_STORE` stage on update, change the stage name from `UPDATE_DATA_STORE` 
to `COMMIT`. It should produce the same result. 

## Upgrading to 3.5

### Isolating a pluggable Cayenne backend [#433](https://github.com/agrestio/agrest/issues/433) 

Code related to Cayenne backend was isolated in its own module `agrest-cayenne`. The old `agrest` module
depends on it, so dependencies do not need to be upgraded. However bootstrapping Cayenne is now done differently.
`ServerRuntime` is not longer passed to `AgBuilder`. You must pass it to `AgCayenneBuilder`, and use it to 
create a module that is passed to `AgBuilder`:

```java
ServerRuntime runtime = ..;
AgCayenneModule cayenneExt = AgCayenneBuilder.build(runtime);
AgBuilder agBuilder = new AgBuilder().module(cayenneExt);
```
Also classes in `io.agrest.protocol` where moved to `io.agrest.base.protocol` package. Change your imports 
accordingly.

## Upgrading to 3.4

### Support for Case Insensitive Sorting [#428](https://github.com/agrestio/agrest/issues/428)

Support was added for case-insensitive sorting to the Agrest protocol and the backend framework. While this change 
does not require any upgrade actions and is fully backwards-compatible, since this is a rare protocol addition,
it is worth mentioning it here.

### EncoderFilter API Changes [#420](https://github.com/agrestio/agrest/issues/420)

* `EncoderFilter` was renamed to `EntityEncoderFilter`, as the new name points to the exact place where filtering occurs
 (per entity object).
* Previous `EntityEncoderFilter` that was used as an abstract superclass for the filters was removed. If you have code
that subclasses it, switch to `EntityEncoderFilter` static builder methods instead:

```java
var x = ..
EntityEncoderFilter filter = EntityEncoderFilter.forEntity(E4.class)
    .encoder((p, o, out, e) -> {
        out.writeStartObject();
        out.writeObjectField("x", x);
        out.writeEndObject();
        return true;
    })
    .build();
```

### Pluggable Resolvers make `MultiSelectBuilder` obsolete [#413](https://github.com/agrestio/agrest/issues/413)

This feature allows to install custom resolvers for root and nested entities in an Ag request, either globally or per-request.
This new capability allows to fetch data from multiple sources, aggregating it in a single response on the fly (all without
altering model objects for each source). This is very powerful, and it makes our earlier experiment with `AgMultiSource`
and `MultiSelectBuilder` obsolete. These two classes are deprecated. If you were using them, consider switching to
custom resolvers and `AgEntityOverlay`.

## Upgrading to 3.2

### Response with overlapping relationship / attribute "includes" is include order dependent [#406](https://github.com/agrestio/agrest/issues/406)

If you are using combinations of relationship and attribute includes (e.g. `include=e2&include=e2.id`), you may have received 
more fields in response than you should, depending on the include order. And your code may have relied on such an incorrect 
behavior. If suddenly now you are getting less data than you expected for a given request, you will need to adjust client-side 
includes to match the data that you need.

### @QueryParameter types changed to Strings and Integers [#408](https://github.com/agrestio/agrest/issues/408)

* Inspect your `@QueryParam` injection code and change Agrest types, such as `Include`, `Exclude`, etc. to Strings.
Change `Start` and `Limit` to `Integers`.
* Replace calls to `AgRequest.builder()` with `Ag.request(configuration)`.

If you are not directly injecting `Include`, `Exclude` and friends (injecting `UriInfo` instead), are you are not affected.

## Upgrading to 3.0

### Renaming "LinkRest" to "AgRest"

This is the release in which "LinkRest" got renamed to "AgRest", causing the renaming of
modules, packages , and class prefixes. When upgrading please change the following:

* Change dependency imports from `com.nhl.link.rest:link-rest-*` to `io.agrest:agrest-*`
* Change package imports from `com.nhl.link.rest.*` to `io.agrest.*`.
* Update your class references that start with `Lr` to `Ag` prefix.

### LinkRestAdapter is removed [#340](https://github.com/agrestio/agrest/issues/340)

The adapter was deprecated for a while in favor of `AgFeatureProvider` and `AgModuleProvider`. Now it is
finally removed.

### "link-rest-legacy-date-encoders" module is removed [#340](https://github.com/agrestio/agrest/issues/340)

We no longer support legacy date/time encoders. See upgrade instructions for version 2.11 for details on how to
update your code.


## Upgrading to 2.13

### Removed listeners and listener annotations [#300](https://github.com/agrestio/agrest/issues/300)

As a part of the effort cleaning up deprecated API, support for stage listeners and stage listener annotations was removed.
If you need to extend LinkRest processing chains, you should be using "stage" and "terminalStage" methods with custom lambdas.
Those are more flexible and easy to understand. See [2.7 upgrade notes](#replacing-listeners-with-functions-240-241-242-243) below in this document for hints on how to use stages.

### "query" protocol parameter was moved to "link-rest-sencha" [#301](https://github.com/agrestio/agrest/issues/301)

Support for "query" protocol parameter (doing case insensitive "starts with" search on a server-specified property) 
is now limited to the Sencha flavor of LinkRest. Base LinkRest no longer supports this operation. If you are using `link-rest-sencha`,
replace calls to `SelectBuilder.autocompleteOn` with calls to `.stage(SelectStage.PARSE_REQUEST, SenchaOps.startsWithFilter(T.NAME, uriInfo))`.
Either SelectStage.PARSE_REQUEST or SelectStage.ASSEMBLE_QUERY stages can be used.
If you are not using the Sencha module, you can inspect `SenchaOps` code implement a similar function on your own.

### PARSE_REQUEST stage got split in two [#309](https://github.com/agrestio/agrest/issues/309)

If you have callbacks attached to `SelectStage.PARSE_REQUEST` or `UpdateStage.PARSE_REQUEST` stages that rely on the presence of `ResourceEntity` in the context, reattach them to `SelectStage.CREATE_ENTITY` or `UpdateStage.CREATE_ENTITY` respectively, as `ResourceEntity` only becomes available after that new stage.

## Upgrading to 2.11

### JSON encoders stop rendering timezone for all date/time values [#275](https://github.com/agrestio/agrest/issues/275)

Encoding of local date/time values is now uniform for all attribute types and is based on the following rules:

- everything is formatted in server's default TZ
- TZ is never specified in the formatted string
- time is not truncated to seconds, so a fractional part may appear in the formatted string
- fractional part is truncated to milliseconds during encoding

To revert these changes and go back to the old behavior you may use `com.nhl.link.rest.LegacyDateEncodersModule`. In case
the modules auto-loading feature is not disabled, it should be sufficient to add the
`com.nhl.link.rest:link-rest-legacy-date-encoders` JAR on your application's classpath. Here's how to do it,
if you're using Maven build:

```xml
<dependency>
	<groupId>com.nhl.link.rest</groupId>
	<artifactId>link-rest-legacy-date-encoders</artifactId>
	<version>2.11</version>
</dependency>
```

## Upgrading to 2.10

### LinkRestAdapter is deprecated, replaced with LrFeatureProvider and LrModuleProvider [#245](https://github.com/agrestio/agrest/issues/245)

Instead of a monolithic adapter, LinkRest new extension mechanism is based on two separate interfaces, `LrFeatureProvider` and `LrModuleProvider`,
one to provide JAX-RS extensions, the other - for the LinkRest stack extensions. `LinkRestAdapter` got deprecated. Consider upgrading to the new
mechanism. Also note that both new providers can be configured for auto-loading using standard Java ServiceLoader mechanism .See 
[#245](https://github.com/agrestio/agrest/issues/245) for details.

### Renamed @Resource to @LrResource [#261](https://github.com/agrestio/agrest/issues/261)

`@Resource` annotation was used for annotating REST endpoints to build informative metadata resources. For naming 
consistency across LR annotations it was renamed to `@LrResource` and placed in `link-rest-annotations` module. 
While the old form is still operational, it is deprecated and will eventually be removed. When replacing it
with the new form (`@LrResource`) you will get an error about one of its attributes, namely the `LinkType` enum. `@LrResource`
uses LinkType from a different package. So be sure to replace `com.nhl.link.rest.meta.LinkType` import with 
`com.nhl.link.rest.annotation.LinkType`.

### SenchaAdapter made auto-loadable [#263](https://github.com/agrestio/agrest/issues/263)

If you are using LinkRest with Sencha/ExtJS, chances are you've activated `SenchaAdapter`. In 2.10 `SenchaAdapter` class was removed and replaced with auto-loadable `SenchaFeatureProvider` and `SenchaModuleProvider` pair. So you will see compilation errors. When you do, remove the adapter registration code and instead add a dependency on the new `com.nhl.link.rest:link-rest-sencha` module. After that your app should automatically find and install all Sencha-specific services.


## Upgrading to 2.7

### Replacing Listeners with Functions [#240](https://github.com/agrestio/agrest/issues/240), [#241](https://github.com/agrestio/agrest/issues/241), [#242](https://github.com/agrestio/agrest/issues/242), [#243](https://github.com/agrestio/agrest/issues/243)

To simplify writing LR extensions, an API alternative to annotated listeners was implemented for "select" and "update"
operations. Now you can register specific functions to be invoked after a given stage. E.g.:
 
```java
LinkRest.select(E.class, config)
    .uri(uri)
    .stage(SelectStage.PARSE_REQUEST, c -> c.getEntity().setQualifier(E.NAME.eq("John")))
    .get();

LinkRest.select(E.class, config)
    .uri(uri)
    .terminalStage(SelectStage.APPLY_SERVER_PARAMS, object::customBackend)
    .get();
```
Old-style listeners are still supported, but deprecated. While most existing listeners will work unchanged, there are
may be issues if the listeners do anything fancy with stage routing (beyond just returning unchanged "next" or null).
generally we would recommend to inspect the deprecation warnings and migrate to the new function-centric API.


## Upgrading to 2.4

### Immutable Constraints API [#214](https://github.com/agrestio/agrest/issues/214)
`ConstraintsBuilder` static factory methods were moved (with deprecation) to `Constraint` interface. It is advisable to
clean up all deprecation warnings.

The only subltly breaking change is that `ConstraintsBuilder` was made immutable, so whenever you are calling one of 
its builder methods, a new instance is created. So make sure you do not keep references to the intermediate builder 
results (unless this is intentional), and only use the ConstraintsBuilder instance returned from the last builder method. 
E.g. the following likely won't do what you'd expect:

```java
ConstraintsBuilder<E> c = Constraint.excludeAll();
c.attribute("a");

// Likely a bug!! "c" does not include attribute "a"
LinkRest.select(MyType.class, config).constraint(c);
```

while this will:

```java
ConstraintsBuilder<E> c = Constraint.excludeAll().attribute("a");
LinkRest.select(MyType.class, config).constraint(c);
```

## Upgrading to 1.23

### New Feature: support for Java 8 Dates [#154](https://github.com/agrestio/agrest/issues/154)
If you want to start using Java 8 date/time types, you will need to import an extra module:
```xml
<dependency>
	<artifactId>link-rest-java8</artifactId>
	<groupId>com.nhl.link.rest</groupId>
	<version>1.23</version>
</dependency>
```
and then add a ```com.nhl.link.rest.runtime.adapter.java8.Java8Adapter``` to LinkRest runtime.

## Upgrading to 1.19

###  Collection Document: remove "success":true key, keep it under SenchaAdapter [#114](https://github.com/agrestio/agrest/issues/114)

LinkRest no longer includes ```"success":true``` key in Collection Document responses, unless you are using SenchaAdapter. The hope is that nobody actually relied on the JSON body to check the status of response, and instead used HTTP status code for that purpose. ```"success":true``` is still available when SenchaAdapter is in use. 

###  UpdateBuilder / UpdateResponse / EntityUpdate refactoring [#113](https://github.com/agrestio/agrest/issues/113)

An update chain [can return](https://agrest.io/docs/protocol.html#json-documents) either a Simple Document or a full Collection Document. Before 1.19 this distinction wasn't clear from the API (```UpdateBuilder.process()``` always retruned UpdateResponse). This is made more explicit in 1.19. Instead of the 'process(String)' method (that was deprecated), there are 2 new methods:

```java
DataResponse<T> syncAndSelect(String);
SimpleResponse sync(String)
```

Additionally ```UpdateBuilder.includeData()/excludeData()``` methods were removed, as they make no sense any longer. The users must review compilation errors and deprecation warnings and replace those 3 methods with calls to ```syncAndSelect()``` or ```sync()```. Note that deprecated 'process(String)' method would work the same way as 'sync' with default adapter, and same way as 'syncAndSelect' with SenchaAdapter.

This change also forced us to kill ```UpdateFilter```. UpdateFilter users must change to annotated listeners available per [#111](https://github.com/agrestio/agrest/issues/111).


###   Externalizing Processor Chain invocation [#112](https://github.com/agrestio/agrest/issues/112)

Implementors of custom processing stages will need to update their API, or better, replace their custom stages with listeners per [#111](https://github.com/agrestio/agrest/issues/111).
