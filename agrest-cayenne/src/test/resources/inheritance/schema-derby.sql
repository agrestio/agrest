CREATE TABLE "ie2" ("id" INTEGER  NOT NULL GENERATED BY DEFAULT AS IDENTITY, PRIMARY KEY ("id"))
;

CREATE TABLE "ie1" ("a0" VARCHAR (100), "a1" VARCHAR (100), "a2" VARCHAR (100), "a3" VARCHAR (100), "e2_id" INTEGER , "id" INTEGER  NOT NULL GENERATED BY DEFAULT AS IDENTITY, "type" INTEGER  NOT NULL, PRIMARY KEY ("id"))
;

CREATE TABLE "ie3" ("e1_id" INTEGER  NOT NULL, "id" INTEGER  NOT NULL GENERATED BY DEFAULT AS IDENTITY, PRIMARY KEY ("id"))
;

ALTER TABLE "ie1" ADD FOREIGN KEY ("e2_id") REFERENCES "ie2" ("id")
;

ALTER TABLE "ie3" ADD FOREIGN KEY ("e1_id") REFERENCES "ie1" ("id")
;

CREATE SEQUENCE "PK_IE1" AS BIGINT START WITH 200 INCREMENT BY 20 NO MAXVALUE NO CYCLE
;

CREATE SEQUENCE "PK_IE3" AS BIGINT START WITH 200 INCREMENT BY 20 NO MAXVALUE NO CYCLE
;

CREATE SEQUENCE "PK_IE2" AS BIGINT START WITH 200 INCREMENT BY 20 NO MAXVALUE NO CYCLE
;

