
## Full JPA support todo list: 
- [ ] `AgRest core` expression parser (big update in the core needed first)
- [ ] `AgRest core` EntityManager lifecycle management (most probably via some sort of a decorator for all processors, like exception mapper)
- [x] `POSTPONED` ordering by related properties (no path parsing for now, may be a problem somewhere else, not only in the ordering logic)
      - disabled for now, need advanced JPA-query builder or we will get CROSS JOIN by default 
- [x] `WON'T FIX` full support for the compound pk
  - do we need embedded PK to be in the output JSON? (attributes are serialized as JSON both in JPA and Cayenne)
  - need a proper property writer for the IdClass attributes (PK is ignored)
- [x] `WON'T FIX` `incompatibility` db path support (no such thing in the JPA, what to do with many-to-many relationships in particular)?
- [x] `WON'T FIX` `incompatibility` two way relationship on update (from the toMany side, can't handle for now if no FK is set directly)
- [ ] limit/offset (what is intended logic here?)
- [ ] `incompatibility` get id from the related objects (part of the two-way relationship problem), see PUT_Related_IT.testToMany_Join(),
  results in request not being idempotent
- [ ] review TODOs in the code
