LinkRest
=========
LinkRest is a server-side Java REST framework for easy access of backend data stores. Its goal is to simplify backend interaction of client applications, such as JavaScript apps or a native mobile apps. It is closely integrated with [JAX-RS 2.0](http://en.wikipedia.org/wiki/Java_API_for_RESTful_Web_Services) REST containers and minimizes the amount of the server-side code needed to expose your data.

LinkRest was originally written with Sencha/ExtJS clients in mind, but it is not specific to a any single client technology. It can be used from iOS and Android applications, jQuery, etc.

LinkRest is written on top of [Apache Cayenne ORM](http://cayenne.apache.org/) and supports any relational data store. Alternative data stores can be provided with relative ease for NoSQL databases, flat files, etc.

LinkRest is open source and is distributed under the terms of [BSD license](https://github.com/nhl/link-rest/blob/master/LICENSE.txt).

LinkRest Protocol
-------------
LinkRest defines a simple protocol for writing REST endpoints. The protocol is based on HTTP and JSON. Client applications use it to read or modify data entities. The main feature of LinkRest Protocol is the ability of the client to customize fine details of the response JSON. The clients can explicitly request inclusion in response of any given set of attributes (including related entities and their attributes), specify filtering criteria, sort ordering, pagination, etc. This allows to implement general purpose REST API with very little to no effort.

Getting Started
-----------
TODO
