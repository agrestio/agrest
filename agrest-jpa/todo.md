
## Full JPA support todo list: 
- [ ] `AgRest core` expression parser (big update in the core needed first)
- [ ] `AgRest core` EntityManager lifecycle management (most probably via some sort of a decorator for all processors, like exception mapper)
- [ ] ordering by related properties (no path parsing for now, may be a problem somewhere else, not only in the ordering logic)
- [ ] full support for the compound pk
  - do we need embedded PK to be in the output JSON?
  - need a proper property writer for the IdClass attributes
- [ ] `incompatibility` db path support (no such thing in the JPA, what to do with many-to-many relationships in particular)?
- [ ] `incompatibility` two way relationship on update (from the toMany side, can't handle for now if no FK is set directly)
- [ ] limit/offset (what is intended logic here?)
- [ ] `incompatibility` get id from the related objects (part of the two-way relationship problem), see PUT_Related_IT.testToMany_Join(),
  results in request not being idempotent
- review TODOs in the code