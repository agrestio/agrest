package io.agrest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgRelationship;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

public abstract class PropertyMetadataEncoder extends AbstractEncoder {

    private static final Encoder instance = new PropertyMetadataEncoder() {

        @Override
        protected String getPropertyName(Object property) {
            if (property instanceof AgAttribute) {
                return ((AgAttribute) property).getName();
            } else if (property instanceof AgRelationship) {
                return ((AgRelationship) property).getName();
            } else {
                return null;
            }
        }

        @Override
        protected TypeDescription getPropertyType(Object property) {
            if (property instanceof AgAttribute) {
                return getAttributeType((AgAttribute) property);
            } else if (property instanceof AgRelationship) {
                return getRelationshipType((AgRelationship) property);
            }

            throw new UnsupportedOperationException("Unsupported property type, must be attribute or relationship: " + property);
        }

        protected TypeDescription getRelationshipType(AgRelationship relationship) {
            String entityName = relationship.getTargetEntity().getName();
            return new TypeDescription(entityName);
        }

        protected TypeDescription getAttributeType(AgAttribute attribute) {
            Class<?> type = attribute.getType();
            switch (type.getName()) {
                case "byte":
                case "short":
                case "int":
                    return TypeDescription.int32();
                case "long":
                    return TypeDescription.int64();
                case "float":
                    return TypeDescription.float32();
                case "double":
                    return TypeDescription.float64();
                case "[B":
                    return TypeDescription.base64();
                case "char":
                case "java.lang.Character":
                case "java.lang.String":
                    return TypeDescription.string();
                case "boolean":
                case "java.lang.Boolean":
                    return new TypeDescription("boolean");
            }
            if (Number.class.isAssignableFrom(type)) {
                if (Byte.class.equals(type) || Short.class.equals(type) || Integer.class.equals(type)) {
                    return TypeDescription.int32();
                } else if (Long.class.equals(type)) {
                    return TypeDescription.int64();
                } else if (Float.class.equals(type)) {
                    return TypeDescription.float32();
                } else if (Double.class.equals(type)) {
                    return TypeDescription.float64();
                }
                return TypeDescription.number();
            } else if (Date.class.isAssignableFrom(type)) {
                if (java.sql.Date.class.equals(type) || java.sql.Timestamp.class.equals(type)) {
                    return TypeDescription.datetime();
                } else if (java.sql.Time.class.equals(type)) {
                    return TypeDescription.time();
                }
                return TypeDescription.datetime();
            } else if (LocalDate.class.equals(type)) {
                return TypeDescription.date();
            } else if (LocalDateTime.class.equals(type)) {
                return TypeDescription.datetime();
            } else if (LocalTime.class.equals(type)) {
                return TypeDescription.time();
            }

            return TypeDescription.unknown();
        }

        @Override
        protected void doEncode(Object property, JsonGenerator out) throws IOException {
            if (property instanceof AgRelationship) {
                out.writeBooleanField("relationship", true);
                if (((AgRelationship) property).isToMany()) {
                    out.writeBooleanField("collection", true);
                }
            }
        }
    };

    public static Encoder encoder() {
        return instance;
    }

    @Override
    protected boolean encodeNonNullObject(Object property, JsonGenerator out) throws IOException {
        if (property == null) {
            return false;
        }

        out.writeStartObject();

        out.writeStringField("name", getPropertyName(property));

        TypeDescription type = getPropertyType(property);
        Objects.requireNonNull(type, () -> "Could not determine meta encoder type for property: " + property);

        out.writeStringField("type", type.getTypeName());
        Optional<String> format = type.getFormat();
        if (format.isPresent()) {
            out.writeStringField("format", format.get());
        }

        doEncode(property, out);

        out.writeEndObject();

        return true;
    }

    protected abstract String getPropertyName(Object property);

    protected abstract TypeDescription getPropertyType(Object property);

    protected abstract void doEncode(Object object, JsonGenerator out) throws IOException;

    private static class TypeDescription {
        static final String NUMBER_TYPE = "number";
        static final String STRING_TYPE = "string";
        static final String DATE_TYPE = "date";
        static final String UNKNOWN_TYPE = "unknown";

        static TypeDescription int32() {
            return new TypeDescription(NUMBER_TYPE, "int32");
        }

        static TypeDescription int64() {
            return new TypeDescription(NUMBER_TYPE, "int64");
        }

        static TypeDescription float32() {
            return new TypeDescription(NUMBER_TYPE, "float");
        }

        static TypeDescription float64() {
            return new TypeDescription(NUMBER_TYPE, "double");
        }

        static TypeDescription number() {
            return new TypeDescription(NUMBER_TYPE);
        }

        static TypeDescription base64() {
            return new TypeDescription(STRING_TYPE, "byte");
        }

        static TypeDescription string() {
            return new TypeDescription(STRING_TYPE);
        }

        static TypeDescription date() {
            return new TypeDescription(DATE_TYPE, "full-date");
        }

        static TypeDescription datetime() {
            return new TypeDescription(DATE_TYPE, "date-time");
        }

        static TypeDescription time() {
            return new TypeDescription(DATE_TYPE, "full-time");
        }

        static TypeDescription unknown() {
            return new TypeDescription(UNKNOWN_TYPE);
        }

        private String typeName;
        private Optional<String> format;

        TypeDescription(String typeName, String format) {
            this.typeName = typeName;
            this.format = Optional.of(format);
        }

        TypeDescription(String typeName) {
            this.typeName = typeName;
            this.format = Optional.empty();
        }

        String getTypeName() {
            return typeName;
        }

        Optional<String> getFormat() {
            return format;
        }
    }
}
