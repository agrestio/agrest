package io.agrest.jpa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

@Entity
@Table(name = "e19")
public class E19 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "big_decimal")
    protected BigDecimal bigDecimal;
    @Column(name = "big_integer")
    protected BigInteger bigInteger;
    @Column(name = "boolean_object")
    protected Boolean booleanObject;
    @Column(name = "boolean_primitive")
    protected Boolean booleanPrimitive;
    @Column(name = "byte_object")
    protected Byte byteObject;
    @Column(name = "byte_primitive")
    protected Byte bytePrimitive;
    @Column(name = "c_date")
    protected Date cDate;
    @Column(name = "c_string")
    protected String cString;
    @Column(name = "c_time")
    protected Time cTime;
    @Column(name = "c_timestamp")
    protected Timestamp cTimestamp;
    @Column(name = "char_object")
    protected Character charObject;
    @Column(name = "char_primitive")
    protected Character charPrimitive;
    @Column(name = "double_object")
    protected Double doubleObject;
    @Column(name = "double_primitive")
    protected Double doublePrimitive;
    @Column(name = "float_object")
    protected Float floatObject;
    @Column(name = "float_primitive")
    protected Float floatPrimitive;
    @Column(name = "guid")
    protected byte[] guid;
    @Column(name = "int_object")
    protected Integer intObject;
    @Column(name = "int_primitive")
    protected Integer intPrimitive;
    @Column(name = "long_object")
    protected Long longObject;
    @Column(name = "long_primitive")
    protected Long longPrimitive;
    @Column(name = "short_object")
    protected Short shortObject;
    @Column(name = "short_primitive")
    protected Short shortPrimitive;

    public Short getShortPrimitive() {
        return shortPrimitive;
    }

    public Short getShortObject() {
        return shortObject;
    }

    public Long getLongPrimitive() {
        return longPrimitive;
    }

    public Long getLongObject() {
        return longObject;
    }

    public Integer getIntPrimitive() {
        return intPrimitive;
    }

    public Integer getIntObject() {
        return intObject;
    }

    public byte[] getGuid() {
        return guid;
    }

    public Float getFloatPrimitive() {
        return floatPrimitive;
    }

    public Float getFloatObject() {
        return floatObject;
    }

    public Double getDoublePrimitive() {
        return doublePrimitive;
    }

    public Double getDoubleObject() {
        return doubleObject;
    }

    public Character getCharPrimitive() {
        return charPrimitive;
    }

    public Character getCharObject() {
        return charObject;
    }

    public Timestamp getCTimestamp() {
        return cTimestamp;
    }

    public Time getCTime() {
        return cTime;
    }

    public String getCString() {
        return cString;
    }

    public Date getCDate() {
        return cDate;
    }

    public Byte getBytePrimitive() {
        return bytePrimitive;
    }

    public Byte getByteObject() {
        return byteObject;
    }

    public Boolean getBooleanPrimitive() {
        return booleanPrimitive;
    }

    public Boolean getBooleanObject() {
        return booleanObject;
    }

    public BigInteger getBigInteger() {
        return bigInteger;
    }

    public BigDecimal getBigDecimal() {
        return bigDecimal;
    }

    public Long getId() {
        return id;
    }


}
