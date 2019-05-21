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
