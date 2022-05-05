-- noinspection SqlNoDataSourceInspectionForFile

-- noinspection SqlDialectInspectionForFile

-- main

CREATE TABLE e1 (age INTEGER , description VARCHAR (100), id INTEGER  NOT NULL GENERATED BY DEFAULT AS IDENTITY, name VARCHAR (100), PRIMARY KEY (id))
;

CREATE TABLE e2 (address VARCHAR (255), id INTEGER  NOT NULL GENERATED BY DEFAULT AS IDENTITY, name VARCHAR (100), PRIMARY KEY (id))
;

CREATE TABLE e3 (e2_id INTEGER , e5_id INTEGER , id INTEGER  NOT NULL GENERATED BY DEFAULT AS IDENTITY (START WITH 2000, INCREMENT BY 1), name VARCHAR (200), phone_number VARCHAR (12), PRIMARY KEY (id))
;

CREATE TABLE e4 (c_boolean BOOLEAN , c_date DATE , c_decimal DECIMAL (10, 2), c_int INTEGER , c_time TIME , c_timestamp TIMESTAMP , c_varchar VARCHAR (100), id INTEGER  NOT NULL GENERATED BY DEFAULT AS IDENTITY, PRIMARY KEY (id))
;

CREATE TABLE e5 (date DATE , id INTEGER  NOT NULL, name VARCHAR (50), PRIMARY KEY (id))
;

CREATE TABLE e6 (char_column VARCHAR (100), char_id VARCHAR (100) NOT NULL, PRIMARY KEY (char_id))
;

CREATE TABLE e7 (e8_id INTEGER , id INTEGER  NOT NULL, name VARCHAR (100), PRIMARY KEY (id))
;

CREATE TABLE e8 (id INTEGER  NOT NULL, name VARCHAR (100), PRIMARY KEY (id))
;

CREATE TABLE e9 (e8_id INTEGER  NOT NULL, PRIMARY KEY (e8_id))
;

CREATE TABLE e10 (c_boolean BOOLEAN , c_date DATE , c_decimal DECIMAL (10, 2), c_int INTEGER , c_time TIME , c_timestamp TIMESTAMP , c_varchar VARCHAR (100), id INTEGER  NOT NULL GENERATED BY DEFAULT AS IDENTITY, PRIMARY KEY (id))
;

CREATE TABLE e11 (address VARCHAR (255), e10_id INTEGER , id INTEGER  NOT NULL GENERATED BY DEFAULT AS IDENTITY, name VARCHAR (100), PRIMARY KEY (id))
;

CREATE TABLE e12 (id INTEGER  NOT NULL, PRIMARY KEY (id))
;

CREATE TABLE e12_e13 (e12_id INTEGER  NOT NULL, e13_id INTEGER  NOT NULL, PRIMARY KEY (e12_id, e13_id))
;

CREATE TABLE e13 (id INTEGER  NOT NULL, PRIMARY KEY (id))
;

CREATE TABLE e14 (e15_id BIGINT , long_id BIGINT  NOT NULL, name VARCHAR (100), PRIMARY KEY (long_id))
;

CREATE TABLE e15 (long_id BIGINT  NOT NULL, name VARCHAR (100), PRIMARY KEY (long_id))
;

CREATE TABLE e15_e1 (e15_id BIGINT  NOT NULL, e1_id INTEGER  NOT NULL, PRIMARY KEY (e15_id, e1_id))
;

CREATE TABLE e15_e5 (e15_id BIGINT  NOT NULL, e5_id INTEGER  NOT NULL, PRIMARY KEY (e15_id, e5_id))
;

CREATE TABLE e16 (c_date DATE , c_time TIME , c_timestamp TIMESTAMP , id INTEGER  NOT NULL GENERATED BY DEFAULT AS IDENTITY, PRIMARY KEY (id))
;

CREATE TABLE e17 (id1 INTEGER  NOT NULL, id2 INTEGER  NOT NULL, name VARCHAR (100), PRIMARY KEY (id1, id2))
;

CREATE TABLE e18 (e17_id1 INTEGER , e17_id2 INTEGER , id INTEGER  NOT NULL GENERATED BY DEFAULT AS IDENTITY, name VARCHAR (100), PRIMARY KEY (id))
;

CREATE TABLE e19 (big_decimal DECIMAL (18, 2), big_integer DECIMAL (18), boolean_object SMALLINT , boolean_primitive SMALLINT , byte_object SMALLINT , byte_primitive SMALLINT , c_date DATE , c_string VARCHAR (45), c_time TIME , c_timestamp TIMESTAMP , char_object CHAR (1), char_primitive CHAR (1), double_object DOUBLE PRECISION , double_primitive DOUBLE PRECISION , float_object DOUBLE PRECISION , float_primitive DOUBLE PRECISION , guid VARCHAR(16) FOR BIT DATA, id INTEGER  NOT NULL GENERATED BY DEFAULT AS IDENTITY, int_object INTEGER , int_primitive INTEGER , long_object BIGINT , long_primitive BIGINT , short_object SMALLINT , short_primitive SMALLINT , PRIMARY KEY (id))
;

CREATE TABLE e20 (age INTEGER , description VARCHAR (100), e21_id INTEGER , id INTEGER  NOT NULL GENERATED BY DEFAULT AS IDENTITY, name_col VARCHAR (100), PRIMARY KEY (id))
;

CREATE TABLE e21 (age INTEGER , description VARCHAR (100), id INTEGER  NOT NULL GENERATED BY DEFAULT AS IDENTITY, name VARCHAR (100), PRIMARY KEY (id))
;

CREATE TABLE e22 (id INTEGER  NOT NULL, name VARCHAR (100), PRIMARY KEY (id))
;

CREATE TABLE e23 (id INTEGER  NOT NULL, name VARCHAR (100), PRIMARY KEY (id))
;

CREATE TABLE e24 (TYPE INTEGER  NOT NULL, name VARCHAR (100), PRIMARY KEY (TYPE))
;

CREATE TABLE e26 (e23_id INTEGER , id INTEGER  NOT NULL, PRIMARY KEY (id))
;

CREATE TABLE e25 (e22_id INTEGER , id INTEGER  NOT NULL, name VARCHAR (100), PRIMARY KEY (id))
;

CREATE TABLE e27_nopk (name VARCHAR (100))
;

CREATE TABLE e28 (id INTEGER  NOT NULL, json VARCHAR (250), PRIMARY KEY (id))
;

CREATE TABLE e29 (id1 INTEGER  NOT NULL, id2 INTEGER  NOT NULL, PRIMARY KEY (id1, id2))
;

CREATE TABLE e30 (id INTEGER  NOT NULL GENERATED BY DEFAULT AS IDENTITY, e29_id1 INTEGER , e29_id2 INTEGER , PRIMARY KEY (id))
;

CREATE TABLE e31 (id1 INTEGER  NOT NULL, id2 INTEGER  NOT NULL, name VARCHAR (100), PRIMARY KEY (id1, id2))
;

CREATE TABLE e32 (id1 INTEGER  NOT NULL, id2 INTEGER  NOT NULL, name VARCHAR (100), PRIMARY KEY (id1, id2))
;

ALTER TABLE e14 ADD FOREIGN KEY (e15_id) REFERENCES e15 (long_id)
;

ALTER TABLE e26 ADD FOREIGN KEY (e23_id) REFERENCES e23 (id)
;

ALTER TABLE e9 ADD FOREIGN KEY (e8_id) REFERENCES e8 (id)
;

ALTER TABLE e18 ADD FOREIGN KEY (e17_id1, e17_id2) REFERENCES e17 (id1, id2)
;

-- ALTER TABLE e7 ADD FOREIGN KEY (e8_id) REFERENCES e8 (id)
-- ;

ALTER TABLE e11 ADD FOREIGN KEY (e10_id) REFERENCES e10 (id)
;

ALTER TABLE e12_e13 ADD FOREIGN KEY (e12_id) REFERENCES e12 (id)
;

ALTER TABLE e12_e13 ADD FOREIGN KEY (e13_id) REFERENCES e13 (id)
;

ALTER TABLE e20 ADD FOREIGN KEY (e21_id) REFERENCES e21 (id)
;

ALTER TABLE e15_e5 ADD FOREIGN KEY (e15_id) REFERENCES e15 (long_id)
;

ALTER TABLE e15_e5 ADD FOREIGN KEY (e5_id) REFERENCES e5 (id)
;

ALTER TABLE e25 ADD FOREIGN KEY (e22_id) REFERENCES e22 (id)
;

ALTER TABLE e15_e1 ADD FOREIGN KEY (e1_id) REFERENCES e1 (id)
;

ALTER TABLE e15_e1 ADD FOREIGN KEY (e15_id) REFERENCES e15 (long_id)
;

ALTER TABLE e3 ADD FOREIGN KEY (e2_id) REFERENCES e2 (id)
;

ALTER TABLE e3 ADD FOREIGN KEY (e5_id) REFERENCES e5 (id)
;

ALTER TABLE e30 ADD FOREIGN KEY (e29_id1, e29_id2) REFERENCES e29 (id1, id2)
;

CREATE SEQUENCE PK_E1 AS BIGINT START WITH 200 INCREMENT BY 20 NO MAXVALUE NO CYCLE
;

CREATE SEQUENCE PK_E10 AS BIGINT START WITH 200 INCREMENT BY 20 NO MAXVALUE NO CYCLE
;

CREATE SEQUENCE PK_E11 AS BIGINT START WITH 200 INCREMENT BY 20 NO MAXVALUE NO CYCLE
;

CREATE SEQUENCE PK_E12 AS BIGINT START WITH 200 INCREMENT BY 20 NO MAXVALUE NO CYCLE
;

CREATE SEQUENCE PK_E13 AS BIGINT START WITH 200 INCREMENT BY 20 NO MAXVALUE NO CYCLE
;

CREATE SEQUENCE PK_E14 AS BIGINT START WITH 200 INCREMENT BY 20 NO MAXVALUE NO CYCLE
;

CREATE SEQUENCE PK_E15 AS BIGINT START WITH 200 INCREMENT BY 20 NO MAXVALUE NO CYCLE
;

CREATE SEQUENCE PK_E16 AS BIGINT START WITH 200 INCREMENT BY 20 NO MAXVALUE NO CYCLE
;

CREATE SEQUENCE PK_E17 AS BIGINT START WITH 200 INCREMENT BY 20 NO MAXVALUE NO CYCLE
;

CREATE SEQUENCE PK_E18 AS BIGINT START WITH 200 INCREMENT BY 20 NO MAXVALUE NO CYCLE
;

CREATE SEQUENCE PK_E19 AS BIGINT START WITH 200 INCREMENT BY 20 NO MAXVALUE NO CYCLE
;

CREATE SEQUENCE PK_E2 AS BIGINT START WITH 200 INCREMENT BY 20 NO MAXVALUE NO CYCLE
;

CREATE SEQUENCE PK_E20 AS BIGINT START WITH 200 INCREMENT BY 20 NO MAXVALUE NO CYCLE
;

CREATE SEQUENCE PK_E21 AS BIGINT START WITH 200 INCREMENT BY 20 NO MAXVALUE NO CYCLE
;

CREATE SEQUENCE PK_E22 AS BIGINT START WITH 200 INCREMENT BY 20 NO MAXVALUE NO CYCLE
;

CREATE SEQUENCE PK_E23 AS BIGINT START WITH 200 INCREMENT BY 20 NO MAXVALUE NO CYCLE
;

CREATE SEQUENCE PK_E24 AS BIGINT START WITH 200 INCREMENT BY 20 NO MAXVALUE NO CYCLE
;

CREATE SEQUENCE PK_E25 AS BIGINT START WITH 200 INCREMENT BY 20 NO MAXVALUE NO CYCLE
;

CREATE SEQUENCE PK_E26 AS BIGINT START WITH 200 INCREMENT BY 20 NO MAXVALUE NO CYCLE
;

CREATE SEQUENCE PK_E3 AS BIGINT START WITH 200 INCREMENT BY 20 NO MAXVALUE NO CYCLE
;

CREATE SEQUENCE PK_E4 AS BIGINT START WITH 200 INCREMENT BY 20 NO MAXVALUE NO CYCLE
;

CREATE SEQUENCE PK_E5 AS BIGINT START WITH 200 INCREMENT BY 20 NO MAXVALUE NO CYCLE
;

CREATE SEQUENCE PK_E6 AS BIGINT START WITH 200 INCREMENT BY 20 NO MAXVALUE NO CYCLE
;

CREATE SEQUENCE PK_E7 AS BIGINT START WITH 200 INCREMENT BY 20 NO MAXVALUE NO CYCLE
;

CREATE SEQUENCE PK_E8 AS BIGINT START WITH 200 INCREMENT BY 20 NO MAXVALUE NO CYCLE
;

CREATE SEQUENCE PK_E28 AS BIGINT START WITH 200 INCREMENT BY 20 NO MAXVALUE NO CYCLE
;

CREATE SEQUENCE PK_E30 AS BIGINT START WITH 200 INCREMENT BY 20 NO MAXVALUE NO CYCLE
;

-- iso
CREATE TABLE SQL_DATE_TEST (Date DATE , ID INTEGER  NOT NULL, Time TIME , Timestamp TIMESTAMP , PRIMARY KEY (ID))
;

CREATE TABLE UTIL_DATE_TEST (Date DATE , ID INTEGER  NOT NULL, Time TIME , Timestamp TIMESTAMP , PRIMARY KEY (ID))
;

CREATE SEQUENCE PK_SQL_DATE_TEST AS BIGINT START WITH 200 INCREMENT BY 20 NO MAXVALUE NO CYCLE
;

CREATE SEQUENCE PK_UTIL_DATE_TEST AS BIGINT START WITH 200 INCREMENT BY 20 NO MAXVALUE NO CYCLE
;


