package io.agrest.backend.util;


public final class ConversionUtil {

//    public static int toInt(Object object, int defaultValue) {
//        if (object == null) {
//            return defaultValue;
//        } else if (object instanceof Number) {
//            return ((Number) object).intValue();
//        } else if (object instanceof String) {
//            try {
//                return Integer.parseInt((String) object);
//            } catch (NumberFormatException ex) {
//                return defaultValue;
//            }
//        }
//
//        return defaultValue;
//    }
//
//    public static long toLong(Object object, long defaultValue) {
//        if (object == null) {
//            return defaultValue;
//        } else if (object instanceof Number) {
//            return ((Number) object).longValue();
//        } else if (object instanceof String) {
//            try {
//                return Long.parseLong((String) object);
//            } catch (NumberFormatException ex) {
//                return defaultValue;
//            }
//        }
//
//        return defaultValue;
//    }
//
//    public static double toDouble(Object object, double defaultValue) {
//        if (object == null) {
//            return defaultValue;
//        } else if (object instanceof Number) {
//            return ((Number) object).doubleValue();
//        } else if (object instanceof String) {
//            try {
//                return Double.parseDouble((String) object);
//            } catch (NumberFormatException ex) {
//                return defaultValue;
//            }
//        }
//
//        return defaultValue;
//    }

    public static boolean toBoolean(Object object) {
        if (object instanceof Boolean) {
            return ((Boolean) object).booleanValue();
        }

        if (object instanceof Number) {
            return ((Number) object).intValue() != 0;
        }

        return object != null;
    }

//    public static BigDecimal toBigDecimal(Object object) {
//
//        if (object == null) {
//            return null;
//        } else if (object instanceof BigDecimal) {
//            return (BigDecimal) object;
//        } else if (object instanceof BigInteger) {
//            return new BigDecimal((BigInteger) object);
//        } else if (object instanceof Number) {
//            return new BigDecimal(((Number) object).doubleValue());
//        }
//
//        throw new RuntimeException("Can't convert to BigDecimal: " + object);
//    }
//
//    /**
//     * Attempts to convert an object to Comparable instance.
//     */
//    public static Comparable toComparable(Object object) {
//        if (object == null) {
//            return null;
//        } else if (object instanceof Comparable) {
//            return (Comparable) object;
//        } else if (object instanceof StringBuilder) {
//            return object.toString();
//        } else if (object instanceof StringBuffer) {
//            return object.toString();
//        } else if (object instanceof char[]) {
//            return new String((char[]) object);
//        } else {
//            throw new ClassCastException(
//                    "Invalid Comparable class:" + object.getClass().getName());
//        }
//    }
//
//    /**
//     * Attempts to convert an object to Comparable instance.
//     */
//    public static String toString(Object object) {
//        if (object == null) {
//            return null;
//        } else if (object instanceof String) {
//            return (String) object;
//        } else if (object instanceof StringBuffer) {
//            return object.toString();
//        } else if (object instanceof char[]) {
//            return new String((char[]) object);
//        } else {
//            throw new ClassCastException(
//                    "Invalid class for String conversion:" + object.getClass().getName());
//        }
//    }
//
//    /**
//     * Attempts to convert an object to an uppercase string.
//     */
//    public static Object toUpperCase(Object object) {
//        if ((object instanceof String) || (object instanceof StringBuffer)) {
//            return object.toString().toUpperCase();
//        } else if (object instanceof char[]) {
//            return new String((char[]) object).toUpperCase();
//        } else {
//            return object;
//        }
//    }

    /**
     * Compares two objects similar to "Object.equals(Object)". Unlike
     * Object.equals(..), this method doesn't throw an exception if any of the
     * two objects is null.
     */
    public static boolean nullSafeEquals(Object o1, Object o2) {

        if (o1 == null) {
            return o2 == null;
        }

        // Arrays must be handled differently since equals() only does
        // an "==" for an array and ignores equivalence. If an array, use
        // the Jakarta Commons Language component EqualsBuilder to determine
        // the types contained in the array and do individual comparisons.
        if (o1.getClass().isArray()) {
            EqualsBuilder builder = new EqualsBuilder();
            builder.append(o1, o2);
            return builder.isEquals();
        } else { // It is NOT an array, so use regular equals()
            return o1.equals(o2);
        }
    }

    private ConversionUtil() {
    }
}
