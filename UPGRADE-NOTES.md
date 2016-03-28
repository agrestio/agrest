## Upgrading to 1.23

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
