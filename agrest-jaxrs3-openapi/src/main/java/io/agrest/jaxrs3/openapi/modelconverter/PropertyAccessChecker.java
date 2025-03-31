package io.agrest.jaxrs3.openapi.modelconverter;

import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgIdPart;
import io.agrest.meta.AgRelationship;

interface PropertyAccessChecker {

    static PropertyAccessChecker allowAll() {
        return AllowAll.instance;
    }

    static PropertyAccessChecker checkRead() {
        return CheckRead.instance;
    }

    static PropertyAccessChecker checkWrite() {
        return CheckWrite.instance;
    }

    boolean allow(AgAttribute attribute);

    boolean allow(AgRelationship relationship);

    boolean allow(AgIdPart idPart);

    class AllowAll implements PropertyAccessChecker {

        static final AllowAll instance = new AllowAll();

        @Override
        public boolean allow(AgAttribute attribute) {
            return true;
        }

        @Override
        public boolean allow(AgRelationship relationship) {
            return true;
        }

        @Override
        public boolean allow(AgIdPart idPart) {
            return true;
        }
    }

    class CheckRead implements PropertyAccessChecker {

        static final CheckRead instance = new CheckRead();

        @Override
        public boolean allow(AgAttribute attribute) {
            return attribute.isReadable();
        }

        @Override
        public boolean allow(AgRelationship relationship) {
            return relationship.isReadable();
        }

        @Override
        public boolean allow(AgIdPart idPart) {
            return idPart.isReadable();
        }
    }

    class CheckWrite implements PropertyAccessChecker {

        static final CheckWrite instance = new CheckWrite();

        @Override
        public boolean allow(AgAttribute attribute) {
            return attribute.isWritable();
        }

        @Override
        public boolean allow(AgRelationship relationship) {
            return relationship.isWritable();
        }

        @Override
        public boolean allow(AgIdPart idPart) {
            return idPart.isWritable();
        }
    }
}
