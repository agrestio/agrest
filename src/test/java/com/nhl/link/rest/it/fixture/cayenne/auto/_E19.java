package com.nhl.link.rest.it.fixture.cayenne.auto;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import org.apache.cayenne.CayenneDataObject;
import org.apache.cayenne.exp.Property;

/**
 * Class _E19 was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _E19 extends CayenneDataObject {

    private static final long serialVersionUID = 1L;

    public static final String ID_PK_COLUMN = "id";

    public static final Property<BigDecimal> BIG_DECIMAL = new Property<BigDecimal>("bigDecimal");
    public static final Property<BigInteger> BIG_INTEGER = new Property<BigInteger>("bigInteger");
    public static final Property<Boolean> BOOLEAN_OBJECT = new Property<Boolean>("booleanObject");
    public static final Property<Boolean> BOOLEAN_PRIMITIVE = new Property<Boolean>("booleanPrimitive");
    public static final Property<Byte> BYTE_OBJECT = new Property<Byte>("byteObject");
    public static final Property<Byte> BYTE_PRIMITIVE = new Property<Byte>("bytePrimitive");
    public static final Property<Date> C_DATE = new Property<Date>("cDate");
    public static final Property<String> C_STRING = new Property<String>("cString");
    public static final Property<Time> C_TIME = new Property<Time>("cTime");
    public static final Property<Timestamp> C_TIMESTAMP = new Property<Timestamp>("cTimestamp");
    public static final Property<Character> CHAR_OBJECT = new Property<Character>("charObject");
    public static final Property<Character> CHAR_PRIMITIVE = new Property<Character>("charPrimitive");
    public static final Property<Double> DOUBLE_OBJECT = new Property<Double>("doubleObject");
    public static final Property<Double> DOUBLE_PRIMITIVE = new Property<Double>("doublePrimitive");
    public static final Property<Float> FLOAT_OBJECT = new Property<Float>("floatObject");
    public static final Property<Float> FLOAT_PRIMITIVE = new Property<Float>("floatPrimitive");
    public static final Property<byte[]> GUID = new Property<byte[]>("guid");
    public static final Property<Integer> INT_OBJECT = new Property<Integer>("intObject");
    public static final Property<Integer> INT_PRIMITIVE = new Property<Integer>("intPrimitive");
    public static final Property<Long> LONG_OBJECT = new Property<Long>("longObject");
    public static final Property<Long> LONG_PRIMITIVE = new Property<Long>("longPrimitive");
    public static final Property<Short> SHORT_OBJECT = new Property<Short>("shortObject");
    public static final Property<Short> SHORT_PRIMITIVE = new Property<Short>("shortPrimitive");

    public void setBigDecimal(BigDecimal bigDecimal) {
        writeProperty("bigDecimal", bigDecimal);
    }
    public BigDecimal getBigDecimal() {
        return (BigDecimal)readProperty("bigDecimal");
    }

    public void setBigInteger(BigInteger bigInteger) {
        writeProperty("bigInteger", bigInteger);
    }
    public BigInteger getBigInteger() {
        return (BigInteger)readProperty("bigInteger");
    }

    public void setBooleanObject(Boolean booleanObject) {
        writeProperty("booleanObject", booleanObject);
    }
    public Boolean getBooleanObject() {
        return (Boolean)readProperty("booleanObject");
    }

    public void setBooleanPrimitive(boolean booleanPrimitive) {
        writeProperty("booleanPrimitive", booleanPrimitive);
    }
	public boolean isBooleanPrimitive() {
        Boolean value = (Boolean)readProperty("booleanPrimitive");
        return (value != null) ? value.booleanValue() : false;
    }

    public void setByteObject(Byte byteObject) {
        writeProperty("byteObject", byteObject);
    }
    public Byte getByteObject() {
        return (Byte)readProperty("byteObject");
    }

    public void setBytePrimitive(byte bytePrimitive) {
        writeProperty("bytePrimitive", bytePrimitive);
    }
    public byte getBytePrimitive() {
        Object value = readProperty("bytePrimitive");
        return (value != null) ? (Byte) value : 0;
    }

    public void setCDate(Date cDate) {
        writeProperty("cDate", cDate);
    }
    public Date getCDate() {
        return (Date)readProperty("cDate");
    }

    public void setCString(String cString) {
        writeProperty("cString", cString);
    }
    public String getCString() {
        return (String)readProperty("cString");
    }

    public void setCTime(Time cTime) {
        writeProperty("cTime", cTime);
    }
    public Time getCTime() {
        return (Time)readProperty("cTime");
    }

    public void setCTimestamp(Timestamp cTimestamp) {
        writeProperty("cTimestamp", cTimestamp);
    }
    public Timestamp getCTimestamp() {
        return (Timestamp)readProperty("cTimestamp");
    }

    public void setCharObject(Character charObject) {
        writeProperty("charObject", charObject);
    }
    public Character getCharObject() {
        return (Character)readProperty("charObject");
    }

    public void setCharPrimitive(char charPrimitive) {
        writeProperty("charPrimitive", charPrimitive);
    }
    public char getCharPrimitive() {
        Object value = readProperty("charPrimitive");
        return (value != null) ? (Character) value : 0;
    }

    public void setDoubleObject(Double doubleObject) {
        writeProperty("doubleObject", doubleObject);
    }
    public Double getDoubleObject() {
        return (Double)readProperty("doubleObject");
    }

    public void setDoublePrimitive(double doublePrimitive) {
        writeProperty("doublePrimitive", doublePrimitive);
    }
    public double getDoublePrimitive() {
        Object value = readProperty("doublePrimitive");
        return (value != null) ? (Double) value : 0;
    }

    public void setFloatObject(Float floatObject) {
        writeProperty("floatObject", floatObject);
    }
    public Float getFloatObject() {
        return (Float)readProperty("floatObject");
    }

    public void setFloatPrimitive(float floatPrimitive) {
        writeProperty("floatPrimitive", floatPrimitive);
    }
    public float getFloatPrimitive() {
        Object value = readProperty("floatPrimitive");
        return (value != null) ? (Float) value : 0;
    }

    public void setGuid(byte[] guid) {
        writeProperty("guid", guid);
    }
    public byte[] getGuid() {
        return (byte[])readProperty("guid");
    }

    public void setIntObject(Integer intObject) {
        writeProperty("intObject", intObject);
    }
    public Integer getIntObject() {
        return (Integer)readProperty("intObject");
    }

    public void setIntPrimitive(int intPrimitive) {
        writeProperty("intPrimitive", intPrimitive);
    }
    public int getIntPrimitive() {
        Object value = readProperty("intPrimitive");
        return (value != null) ? (Integer) value : 0;
    }

    public void setLongObject(Long longObject) {
        writeProperty("longObject", longObject);
    }
    public Long getLongObject() {
        return (Long)readProperty("longObject");
    }

    public void setLongPrimitive(long longPrimitive) {
        writeProperty("longPrimitive", longPrimitive);
    }
    public long getLongPrimitive() {
        Object value = readProperty("longPrimitive");
        return (value != null) ? (Long) value : 0;
    }

    public void setShortObject(Short shortObject) {
        writeProperty("shortObject", shortObject);
    }
    public Short getShortObject() {
        return (Short)readProperty("shortObject");
    }

    public void setShortPrimitive(short shortPrimitive) {
        writeProperty("shortPrimitive", shortPrimitive);
    }
    public short getShortPrimitive() {
        Object value = readProperty("shortPrimitive");
        return (value != null) ? (Short) value : 0;
    }

}
