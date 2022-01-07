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
Instead of endpoint geberation from the OpenAPI model, we offer code-first documentation approach via "agrest-openapi"
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
* `DeleteBuilder.id(Map)`
* `DeleteBuilder.parent(Class,Map,String)`