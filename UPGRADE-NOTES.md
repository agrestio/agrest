This document contains upgrade notes for Agrest 5.x and newer. Older versions are documented in 
[UPGRADE-NOTES-1-4](./UPGRADE-NOTES-1-to-4.md).

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