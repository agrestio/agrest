## Release 5.0-RC1

* #650 Exp: scalar encoding is incompatible with the parser rules
* #681 Exp: regression in processing (not)in with empty list
* #691 Upgrade Cayenne to 4.2.2
* #692 Upgrade SLF4J to 2.0.17
* #693 Deprecate javax (JAX-RS 2) integration

## Release 5.0-M20

* #651 Exp: numeric scalars to support underscore
* #653 Exp syntax: do not support space in the named parameter token
* #654 Exp syntax: function names should not be keywords  
* #655 Exp syntax: deprecate case-insensitivity of NULL and booleans
* #657 Expression parser to support path aliases
* #658 DeleteBuilder: byIds(..), byMultiIds(..)
* #659 Idempotent DELETE by ids, optimizing performance
* #660 Builders: auto-detect multi-column IDs
* #662 Update expression parser with JavaCC 7.0.13
* #673 OpenAPI should have an "array of string" schema for "include" and "exclude"
* #676 Multi-column ObjectMapper
* #677 Upgrading JUnit to 5.10.2
* #678 Upgrading Swagger to 2.2.2
* #679 Upgrading Jackson to 2.15.4
* #680 Upgrading SLF4J to 2.0.7
* #683 Upgrade Cayenne to 4.2.1 
* #684 GET with includes can't match children to parent when parent has a compound key
* #687 Upgrade Swagger to 2.2.23 

## Release 5.0.M19

* #648 NPE in CayenneExpParser for a combination of null and positional parameter
* #649 AgExpression internal cleanup

## Release 5.0.M18

* #602 Expressions - immutable parameter binding
* #603 Expressions: compact and/or chains
* #638 EntityUpdate.getToMany(String) should return null if the key is not a part of update
* #640 DataResponse<T> to support "304 Not Modified"
* #641 Cayenne IPathResolver may get "poisoned" by per-request overlayed AgEntities
* #642 TokenMgrException is not handled by Agrest
* #643 Static factory methods in Exp for conditions
* #646 org.apache.cayenne.exp.ExpressionException: [v.4.2 May 16 2023 08:31:12] In: invalid parent - Equal
 
## Release 5.0.M17

* #630 POST/PUT breaks for Short and BigInteger types
* #631 POST/PUT - can't update multi-key relationships
* #632 POST/PUT gets confused when relationship is passed as object
* #633 Optimize and clean up update parser
* #634 Recursive EntityUpdate parser
* #635 Write constraint only applied to "id" in the path, but not in the body
* #636 Clean up the API of EntityUpdate

## Release 5.0.M16

* #601 Exp.keyValue() only works with a narrow range of value types
* #621 Problem with timestamp formatting
* #622 Upgrade Jackson to 2.14.2 
* #624 Optimization: "relatedViaQueryWithParentIds" strategy when limit is involved
* #629 Upgrade to Cayenne 4.2 GA

## Release 5.0.M15

* #614 Improve support for Java records as Agrest entities
* #618 Expand the definition of "getter" and "setter"
* #619 Swagger: do not expose UriInfo unless annotated with @Parameter

## Release 5.0.M14

* #613 Exception on include when entity id is unreadable
* #615 Swagger: ignore UriInfo if @Parameter(hidden=true) is set
* #616 Swagger: duplicate param properties
* #617 Proper handling of ObjectId annotated with @AgId

## Release 5.0.M13

* #607 OpenAPI docs for EntityUpdate should include relationships
* #610 OpenAPI descriptors should be able to resolve multi-column AgId
* #611 OpenAPI: properties must be sorted in same order as responses
* #612 OpenAPI descriptors - exclude inaccessible properties

## Release 5.0.M12

* #606 Jackson upgrade, dependency management
* #608 "Path exceeds the max allowed depth" exception for attribute sort and 0 depth
* #609 EntityUpdate-style endpoints behave differently from String and List<EntityUpdate>

## Release 5.0.M11

* #604 Allow to exclude null properties in responses
* #605 Hide deprecated protocol keys from OpenAPI descriptors

## Release 5.0.M10

* #582 AgRest own full-featured expression parser
* #594 Support for escape char in "like" expressions
* #597 Upgrade Cayenne to 4.2.RC2
* #598 Max path depth for "Include"
* #600 Max path depth for "mapBy", "sort"

## Release 5.0.M9

* #590 Can't use inherited relationships in includes

## Release 5.0.M8
 
* #583 Upgrade jackson-databind to 2.13.4.2
* #584 GET - Inheritance support - Cayenne backend
* #587 Inheritance-aware AgEntityOverlay

## Release 5.0.M7

* #575 Better contextual message for Cayenne ValidationException
* #577 Better default encoder for DataResponse
* #578 Overlaying AgIdPart access rules results in their renaming
* #579 Correct WebApplicationException interception
* #580 Cayenne properties annotated with @AgAttribute/@AgRelationship lose reader

## Release 5.0.M6

* #574 Can't turn unreadable/unwritable properties to readable/writable with an overlay

## Release 5.0.M5

* #573 Overlay "ignore" methods are not properly merged when combining overlays

## Release 5.0.M4

* #571 Allow AgEntityOverlay to clear previous access filters / authorizers
* #572 AgEntityOverlay - strip "redefine" prefix from builder methods

## Release 5.0.M3

* #570 Define String constants for protocol parameters

## Release 5.0.M2

* #568 Overlay "writablePropFilter" doesn't block relationships updates

## Release 5.0.M1

* #515 Switch to Java 11
* #516 Upgrade jackson to 2.13.1 
* #517 Remove "agrest-sencha"
* #518 Remove "agrest-openapi-designfirst"
* #519 Remove deprecated EncoderFilter and Constraint
* #520 Stop using Cayenne expression API in Ag core
* #521 Cayenne entity ID part represented by a DB column to be prefixed with "db:"
* #522 "id" appears twice in the response for Ag.create() of Cayenne entity with a mapped id called "id"
* #523 "Unrelate" operation must use AgIdParts to decode ids
* #524 Align APIs for passing ID to requests: "byId" vs "id"
* #525 UnrelateBuilder
* #526 Replace deprecated Cayenne SelectQuery with ObjectSelect/ColumnSelect
* #527 Remove "agrest-client"
* #529 Remove "agrest-openapi-designfirst"
* #530 JAX-RS free exception mapper
* #531 JAX-RS free "agrest-cayenne"
* #532 Encoder.willEncode can be removed
* #533 Look up converters by type, not by entity or attribute; value Encoders based on known converters
* #534 Remove "Encoder.visit()" / "DataResponse.getIncludedObjects()"
* #535 Immutable SimpleResponse/DataResponse
* #536 DataResponse object should resemble its encoded version
* #537 Split JAX-RS interface to Agrest in a separate module
* #538 "agrest-jaxrs3" module 
* #541 Remove deprecated metadata responses
* #549 OpenAPI to use format="partial-time" for time attributes
* #557 Upgrade Jackson to 2.13.2.x
* #558 JacksonService swallows parse exception cause
* #561 Protocol changes - v1.2 
* #562 Term refactoring: AgDataMap > AgSchema
* #563 Upgrade to Cayenne 4.2.RC1
* #565 Move static builder methods from AgCayenneBuilder to AgCayenneModule
* #567 Terminology refactoring: "reader", "resolver", "dataResolver", "nestedEntity"

## Release 4.10

* #540 Fetch by ID with filters will return 200 even if the filter excludes the object

## Release 4.9

* #513 AgEntityOverly with a single root resolver doesn't update the entity
* #514 A simpler way to redefine root data resolver

## Release 4.8

* #427 AgEntityOverlay support for POST/PUT requests
* #493 Replace Constraint with per-entity API based on AgEntityOverlay
* #497 Agrest fails to build on Java 11
* #498 Made JAX-RS dependency "provided"
* #500 Child relationship of a dynamic relationship fails to resolve
* #501 Replace EntityEncoderFilter with per-entity object filter
* #502 Per-entity CRUD filters and authorizers for UpdateBuilder
* #503 New update stage - MAP_CHANGES
* #504 Update processes result twice
* #505 Authorizer for DeleteBuilder
* #506 DeleteBuilder stage
* #507 Upgrade to Cayenne 4.2.B1
* #509 Removing ExecutorService and ShutdownManager
* #510 "by id" Cayenne resolver fetches entire table in the absence of pagination
* #512 New select/update stage: ENCODE


## Release 4.7

* #488 Bump to the latest version of openapi-generator (v5.2.0)
* #490 "createOrUpdate" and "idempotentCreateOrUpdate" must return 201 when a single object is created
* #491 Consolidate access control API: fold readable and writable flags into the Ag entity model
* #492 Annotation entity constraints block request-defined attributes
* #494 java.lang.Double cannot be cast to java.math.BigDecimal
* #495 Remove JAX-RS dependency from "agrest-base"
* #496 Replace JAX-RS Status references with int Status

## Release 4.6

* #487 Bootique dependency leaks to Agrest via BOM import

## Release 4.5

* #484 Upgrade Jackson to 2.11.3
* #485 Upgrade Cayenne 4.2 to M3

## Release 4.4

* #476 Composable Exp
* #479 Simple API for returning an empty DataResponse

## Release 4.3

* #473 Cayenne backend - prefetches do not work if id is excluded from parent AgEntity
* #474 Error encoding Json attributes if entity has more than one attribute

## Release 4.2

* #468 ClassCastException in combination Agrest 3.4 and Cayenne 4.1.
* #469 AgEntityModelConverter to use predictable alphabetic ordering 
* #470 Swagger model must include entities reachable from resource entities
* #472 remove legacy "agrest" module

## Release 4.1

* #459 "code-first" OpenAPI support and Swagger integration
* #460 Support for the new Cayenne "Json" type
* #462 "exp" key to replace "cayenneExp"
* #465 AgIdPart metadata object
* #466 Deprecate Agrest metadata API
* #467 Injectable AgDataMap, remove redundant IMetadataService

## Release 4.0

* #456 Upgrade to Cayenne 4.2

## Release 3.8

* #468 ClassCastException in combination Agrest 3.4 and Cayenne 4.1
* #477 Composable Exp 3.x

## Release 3.7

* #449 Excludes in overlays: support removing attributes and relationships via overlays
* #450 Minimize Cayenne footprint - remove references to "Property" from API signatures
* #451 Remove API deprecated in 3.4 or earlier
* #452 Replace SelectBuilder.property(..) with overlay-based API
* #453 Remove Cayenne SelectQuery from ResourceEntity
* #454 Remove property name from property reader signature
* #455 Replace Cayenne Ordering in ResourceEntity with Agrest "sort"
* #457 Replace Cayenne "Expression" in ResourceEntity with Agrest "CayenneExp"

## Release 3.6

* #444 Support id propagation on update from PK properties
* #446 Separate "commit" in its own UpdateStage to intercept uncommitted data
* #447 "CayenneResolvers.nested(..).viaQueryWithParentIds()" strategy must respect pagination settings

## Release 3.5

* #433 "agrest-cayenne" : isolate a pluggable Cayenne backend for Agrest
* #443 Null-safe AgRequestBuilder

## Release 3.4

* #413 Pluggable resolver for hierarchical fetches
* #420 Per-request EncoderFilters
* #421 Custom per-request Encoder is ignored on update
* #422 GET: Per-request AgEntityOverlay to customize properties based on request state
* #423 Wrong Encoder for target entities in overlayed relationships
* #424 Unwinding Cayenne dependencies - get rid of AgPersistent models
* #428 Add support for Case Insensitive Sorting
* #430 agrest-client: Use protocol "dir" enum instead of "SortDirection" enum
* #432 Cayenne resolvers reading their objects from parent must follow contract with child resolvers
* #434 Upgrade to Cayenne 4.0.2
* #435 Upgrade SLF4J to 1.7.25
* #436 Metadata pipeline throws for empty @Path("")
* #437 Remove "bootique-bom" references from "agrest-bom" POM
* #440 Upgrade to Jackson 2.10.3

Release 3.3

* #414 Metadata encoding related memory leak in EncoderService
* #415 Exposed id with name different from DB column: ExpressionException: Can't resolve path component
* #418 Obscure exception on entities with no ids 
* #419 Upgrade to Jackson 2.10.0.pr1

Release 3.2

* #262 Switch to bootique-test
* #405 Upgrade to Cayenne 4.0.1
* #406 Overlapping relationship / attribute includes are order dependent
* #407 Stop using Include / Exclude as dummy containers of other Includes / Excludes
* #408 Change parameter injection to Strings / Integers
* #409 Stop using Sort as a dummy container of other Sorts
* #410 "mapBy" doesn't work with relationship path 
* #412 "include" with exposed root id: ExpressionException: Can't resolve path component

Release 3.1

* #358 Protocol Enhancement: 'include/exclude' parameters should take an array of values 
* #384 Change "include" processing strategy from Cayenne prefetches to individual queries
* #392 Request include/exclude settings are ignored for dynamic request properties
* #394 Upgrade Jackson to 2.9.8
* #402 An Included relation is missed in case of update operation

Release 3.0

_LinkRest got renamed to Agrest in 3.0_

* #285 OpenApi integration: Design First Approach
* #331 LinkRest to Agrest rebranding: Rename packages and files
* #333 Actualize documentation
* #338 Update gh-pages static documentation
* #340 Removing API deprecated in 2.x
* #341 Create example of application
* #342 Add add an extra method to AgClient that takes a configuration callback

Release 2.13

* #284 Cayenne 4.0.RC1 is out... upgrading
* #300 Removing API deprecated as of 2.7 or earlier
* #301 Move "query" protocol parameter to "link-rest-sencha"
* #309 Capture request state in a new LrRequest object
* #313 Refactor Sencha extension to follow the new pipeline logic
* #314 Refactoring runtime.parser.cache.IPathCache to runtime.path.IPathDescriptorManager
* #315 Upgrade JAX-RS to 2.1
* #318 Add 'request' method to SelectBuilder and UpdateBuilder to process LrRequest with explicit query parameters
* #321 Create protocol value object converters and providers
* #322 Manage exception mappers via DI to simplify overriding
* #328 Upgrade to Cayenne 4.0 final

Release 2.12

* #276 Support OffsetDateTime
* #282 Default server side size limitation works incorrectly

Release 2.11

* #200 Automatic JSON to POJO deserialization in LR client
* #273 Intermittent error in UtcDateConverterTest.testConverter_javaUtilDate test
* #275 Stop rendering timezone for all date/time values
* #278 ISO encoding of Java 8 date/time in mapBy keys

Release 2.10

* #245 module auto-loading
* #253 meta: explicit base URL for misconfigured proxies
* #254 Upgrade to Jackson 2.6.4
* #255 Support for constraints in MetadataBuilder
* #256 Built-in JsonValueConverter for enums
* #258 "AdHoc" properties
* #259 NPE in metadata resource when the model has unknown attribute types
* #260 LrEntityOverlay.addAttribute(String) must detect attribute type
* #261 Rename @Resource to @LrResource and move to link-rest-annotations
* #263 link-rest-sencha module
* #267 POST: java.util.Date to Timestamp conversion
* #269 Upgrade to Cayenne 4.0.B2

Release 2.9

* #247 Split Lr annotations in a separate jar
* #252 link-rest-bom

Release 2.8

* #250 Fix stage method generics boundaries

Release 2.7

* #235 Allow Pojo attributes of type java.util.Collection
* #238 Dynamic relationships do not work with "mapBy"
* #239 Add ResourceEntity.setQualifier(..) method.
* #240 Functional select pipeline interceptors
* #241 Functional update pipeline interceptors
* #242 Functional delete pipeline
* #243 Functional pipeline for unrelate and meta chains
* #244 Remove API deprecated on or before 2.0

Release 2.6

* #234 Error reading relationships: Multi-join relationship propagation is not supported yet

Release 2.5

* #225 Support constraining FK updates
* #229 POST and PUT methods doesn't supports content type header with charset
* #230 Generic object API and type conversion problems
* #233 (2.x) Upgrade to Cayenne 4.0.B1

Release 2.4

* #212 Upgrade to Cayenne 4.0.M4
* #214 Immutable constraint objects
* #218 NPE in LR client when HTTP request fails with something different from LinkRestException
* #219 LinkRest 2.x upgrade to Cayenne 4.0.M5
* #220 Support multi-component paths for root "mapBy"
* #222 Support for Java 8 date time types in the metadata requests
* #223 Rename SelectBuilder.select() to get()

Release 2.3

* #208 NPE on deleting
* #209 Batch delete (Sencha-only)

Release 2.2

* #201 Change license from BSD to Apache 2.0
* #202 Sencha adapter "filter" key can't use "id" property
* #206 Cayenne exception on select by exposed ID

Release 2.1

* #191 Replace Joda Time with java.time in JSON encoders and converters
* #194 Refactor root encoder for easier customization
* #199 Support post/put/delete in LR Client

Release 2.0

* #166 Multiple backends with parallel fetch
* #167 Move to Java 8
* #175 Removing API deprecated prior to 2.0
* #176 LR client
* #178 Encoder "visitor" API
* #182 Support for "mapBy" at the top level of request
* #184 Metadata request: related entity type is NULL for POJO related entities
* #185 Store information about the target entity in LrRelationship
* #188 ExecutorService service
* #189 Shutdown service and API
* #190 "mapBy" breaks on POJOs

Release 1.24

* #104 Compile annotated POJO entities on the fly; no need for explicit entity declarations.
* #137 Change LrAttribute.getJavaType() to return Class object
* #157 Not enough diagnostics on misconfigured listeners
* #161 Unmapped PK causes response encoding to fail
* #163 Redefining public ID attribute
* #164 Auto-compile Lr-annotated properties of entity objects
* #168 Refactor DataResponse use
* #169 Deprecate LinkRest and ILinkRestService methods that take SelectQuery parameter
* #170 Support compound IDs in Create/Update JSON payload
* #172 Disallow null value for to-many relationships in update

Release 1.23

* #144 Merge link-rest-docs repo into link-rest
* #148 Optimizing paginated fetches
* #153 Treat byte[] properties as Base64-encoded when parsing request payload
* #154 Add JSON Converters for Java 8 Dates
* #156 Support encoding of entities with no IDs
* #158 POJO's with relationships can't be encoded.

Release 1.22

* #126 LinkRest JSON Path
* #132 Listener param and return type are forced to be BaseLinearProcessingStage instead of ProcessingStage
* #134 LinkRestException logging should include cause message
* #141 Implement `start`/`limit` for advanced `include`s
* #142 PUT fails when existing many-to-many relationship passed into JSON

Release 1.21

* #117 Sync: Linking with to-many relationships
* #120 Custom escape sequences for Unicode characters in JSON
* #121 PropertyMetadataEncoder should work with primitive types
* #123 Update dependency versions

Release 1.20

* #78  Parsed update payload objects as resource method parameters
* #101 Add supporting of id-value for foreign key value during post and put.
* #109 Support for compound IDs in LR service operations
* #115 Refactoring of the service layer
* #116 UpdateBuilder.mapper(String) and UpdateBuilder.mapper(Property)

Release 1.19

* #103 Support for annotation-based stage listeners - select listeners
* #110 Refactor Select stage names to be more meaningful
* #111 Annotation-based listeners for update chains
* #112 Externalizing Processor Chain invocation
* #113 UpdateBuilder / UpdateResponse / EntityUpdate refactoring
* #114 Collection Document: remove "success":true key, keep it under SenchaAdapter

Release 1.18

* #100 LR metadata
* #102 Remove DataResponse.queryProperty
* #105 SenchaAdapter: filter should disengage if “disabled":true option is passed
* #107 Select is run twice within CayenneFetchStage

Release 1.17

* #98 Start weeding out "id is a single attribue" assumption
* #99 Refactor RequestParser expression 'workers' to function-like injectable services

Release 1.16

* #93 Remove methods deprecated since 1.14
* #94 Chain-of-responsibility execution pipeline
* #95 refactoring DAO concept to IProcessorFactory
* #96 Switching IRequestParser to the use of contexts

Release 1.15

* #58 'cayenneExp' positional parameter binding
* #88 Error when using 'cayenneExp' with outer joins ending in relationship
* #89 ConstraintsBuilder to allow individual attribute and relationship excludes
* #91 Expand EntityDAO concept
* #92 Simplify POJO metadata compilation: LR annotations


Release 1.14 2015-03-16

* #79 Simplified API to start request processor builders
* #80 ILinkRestService and related builders API refactoring for consistency
* #81 LinkRestRuntime should implement JAX RS Feature
* #82 LinkRestBuilder: a static shortcut for simple LinkRest stack
* #83 Start deploying releases to Maven central
* #84 Problems with entity attributes of type java.sql.Time
* #87 Upgrade to release version of Cayenne

Release 1.13

* #73 cayenneExp with paths containing plus signs (outer joins) are reportedly broken in 1.12

Release 1.12 2015-01-22

* #63 Object matching with explicit ID fails when callers do not use correct ID type
* #67 Support for per-entity non-persistent properties
* #68 Rename Entity to ResourceEntity
* #69 Own metadata layer
* #70 Added JSON converters for java.sql date/time types
* #71 POJOs should be modeled as LrEntities, not ObjEntities
* #72 Refactor TreeConstraints into visitor-based ConstraintsBuilder

Release 1.11 2015-01-20

* #24 Sencha fake ids and Longs
* #66 LinkRestExceptionMapper should log exception stack traces at DEBUG level

Release 1.10 2014-12-08

* #55 Object matching fails when callers do not use correct implicit ID type
* #57 Support for Long IDs in updates
* #60 Ignore 'query' parameter when no property to match is specified
* #61 Upgrade to to Cayenne 4.0.M2.1ab1caa

Release 1.9 2014-11-24

* #52 Removing hard size limit for 'cayenneExp'
* #56 Upgrade to to Cayenne 4.0.M2.fba700d

Release 1.8 2014-10-14

* #43 upgrade to Jersey 2.12, jackson 2.4.2
* #46 Sencha: update objects order is important on bulk updates
* #47 support for Filter operator option
* #48 Support for implicit propagated ids on create/update
* #49 IRelationshipMapper should use relationship name by default

Release 1.7 2014-09-05

* #29 Batch lookup of relationship objects.
* #38 'idempotentFullSync' method to synchronize collections
* #40 Optionally suppress entity bodies on update requests
* #41 rename CreateOrUpdateBuilder to just UpdateBuilder

Release 1.6 2014-08-29

* #36 StackOverflow on compiling circular constraint annotations
* #37 Redesign default constraints as EntityConstraint's

Release 1.5 2014-08-28

* #9 Chatty merging of config
* #30 Add type generic parameter to TreeConstraint
* #31 Customizable request chain
* #32 Support for default per-entity constraints
* #33 EncoderService: Do not include related_id property by default
* #34 Remove @AnyRole annotation
* #35 Annotations for default constraints

Release 1.4 2014-08-22

* #25 forSelectRelated/constraints causes unqualified fetch ;
* #26 Refactor delete operation to DeleteBuilder with "delete by id" and "delete by parent" options.
* #27 ID that is not a PK can't be used in idempotent requests
	Refactor 'forSelectRelated' to be setup inside SelectBuilder 

Release 1.3 2014-08-20

* #7 Allow TreeConstraints to be applied to insert/update requests
* #17 Support "group" as an object
* #18 CreateOrUpdateBuilder
* #19 Backend support for batch updates of a single entity
* #20 IEncoderService to return Encoder instead of DataResponse
* #21 SelectBuilder.withEntity - rename to SelectBuilder.canRead.
* #22 SenchaAdapter with Incoming id filter
* #23 Refactor EntityConfig and friends into 'constraints'
* #24 LinkRestAdapter - a generic LR extension mechanism

Release 1.2 2014-08-01

* #8 EntityConfig API improvements
* #10 Support for char PK
* #11 Refactoring: rename NoRolesEntityAuthorizationEncoderFilter to EntityEncoderFilter
* #12 Add 'SelectBuilder.selectOne' method
* #13 'filter' processor improvements
* #14 ILinkRestService API for managing relationship operations
* #15 IMetadataService must throw LinkRestException on bad entities
* #16 DataResponseConfig should be attached to SelectBuilder

Release 1.1

* #1 Intercept Cayenne ValidationException
* #3 DataResponseConfig - a server-side request template
* #4 Responses for inserts should return '201 Created' instead of 200
* #5 Rename ClientEntity to Entity, ClientProperty to EntityProperty
* #6 Entity: convert setters to builder methods similar to EntityConfig
