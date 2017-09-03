## Upgrading to 2.10

### Renamed @Resource to @LrResource [#261](https://github.com/nhl/link-rest/issues/261)

`@Resource` annotation was used for annotating REST endpoints to build informative metadata resources. For naming 
consistency across LR annotations it was renamed to `@LrResource` and placed in `link-rest-annotations` module. 
While the old form is still operational, it is deprecated and will eventually be removed. When replacing it
with the new form (`@LrResource`) you will get an error about one of its attributes, namely the `LinkType` enum. `@LrResource`
uses LinkType from a different package. So be sure to replace `com.nhl.link.rest.meta.LinkType` import with 
`com.nhl.link.rest.annotation.LinkType`.


## Upgrading to 2.7

### Replacing Listeners with Functions [#240](https://github.com/nhl/link-rest/issues/240), [#241](https://github.com/nhl/link-rest/issues/241), [#242](https://github.com/nhl/link-rest/issues/242), [#243](https://github.com/nhl/link-rest/issues/243)

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

### Immutable Constraints API [#214](https://github.com/nhl/link-rest/issues/214)
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

### New Feature: support for Java 8 Dates [#154](https://github.com/nhl/link-rest/issues/154)
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

###  Collection Document: remove "success":true key, keep it under SenchaAdapter [#114](https://github.com/nhl/link-rest/issues/114)

LinkRest no longer includes ```"success":true``` key in Collection Document responses, unless you are using SenchaAdapter. The hope is that nobody actually relied on the JSON body to check the status of response, and instead used HTTP status code for that purpose. ```"success":true``` is still available when SenchaAdapter is in use. 

###  UpdateBuilder / UpdateResponse / EntityUpdate refactoring [#113](https://github.com/nhl/link-rest/issues/113)

An update chain [can return](http://nhl.github.io/link-rest/docs/protocol.html#protocol-json-documents) either a Simple Document or a full Collection Document. Before 1.19 this distinction wasn't clear from the API (```UpdateBuilder.process()``` always retruned UpdateResponse). This is made more explicit in 1.19. Instead of the 'process(String)' method (that was deprecated), there are 2 new methods:

```java
DataResponse<T> syncAndSelect(String);
SimpleResponse sync(String)
```

Additionally ```UpdateBuilder.includeData()/excludeData()``` methods were removed, as they make no sense any longer. The users must review compilation errors and deprecation warnings and replace those 3 methods with calls to ```syncAndSelect()``` or ```sync()```. Note that deprecated 'process(String)' method would work the same way as 'sync' with default adapter, and same way as 'syncAndSelect' with SenchaAdapter.

This change also forced us to kill ```UpdateFilter```. UpdateFilter users must change to annotated listeners available per [#111](https://github.com/nhl/link-rest/issues/111).


###   Externalizing Processor Chain invocation [#112](https://github.com/nhl/link-rest/issues/112)

Implementors of custom processing stages will need to update their API, or better, replace their custom stages with listeners per [#111](https://github.com/nhl/link-rest/issues/111).
