package io.agrest.resolver;

import io.agrest.NestedResourceEntity;
import io.agrest.property.PropertyReader;
import io.agrest.runtime.processor.select.SelectContext;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * @since 3.4
 */
public class ParentPropertyDataResolvers {

    public static <T> NestedDataResolver<T> forReader(Function<?, T> reader) {
        // lose generics. PropertyReader is not parameterized
        Function plainReader = reader;
        return new ReaderBasedResolver<>((o, n) -> plainReader.apply(o));
    }

    public static <T> NestedDataResolver<T> forListReader(Function<?, List<T>> reader) {
        // lose generics. PropertyReader is not parameterized
        Function plainReader = reader;
        return new ReaderBasedResolver<>((o, n) -> plainReader.apply(o));
    }

    public static <T> NestedDataResolver<T> forReader(PropertyReader reader) {
        return new ReaderBasedResolver<>(reader);
    }

    public static <T> NestedDataResolver<T> forReaderFactory(Function<NestedResourceEntity<T>, PropertyReader> readerFactory) {
        return new ReaderFactoryBasedResolver<>(readerFactory);
    }

    static class ReaderFactoryBasedResolver<T> extends BaseNestedDataResolver<T> {

        private Function<NestedResourceEntity<T>, PropertyReader> readerFactory;

        protected ReaderFactoryBasedResolver(Function<NestedResourceEntity<T>, PropertyReader> readerFactory) {
            this.readerFactory = readerFactory;
        }

        @Override
        protected void doOnParentQueryAssembled(NestedResourceEntity<T> entity, SelectContext<?> context) {
            // do nothing .. parent entity will query our data for us
        }


        @Override
        protected Iterable<T> doOnParentDataResolved(NestedResourceEntity<T> entity, Iterable<?> parentData, SelectContext<?> context) {
            // do nothing .. parent entity will carry our data for us

            // TODO: create an iterable<T> for the sake of children
            return Collections.emptyList();
        }

        @Override
        public PropertyReader reader(NestedResourceEntity<T> entity) {
            return readerFactory.apply(entity);
        }
    }

    static class ReaderBasedResolver<T> extends BaseNestedDataResolver<T> {

        private PropertyReader reader;

        protected ReaderBasedResolver(PropertyReader reader) {
            this.reader = reader;
        }

        @Override
        protected void doOnParentQueryAssembled(NestedResourceEntity<T> entity, SelectContext<?> context) {
            // do nothing .. parent entity will query our data for us
        }

        @Override
        protected Iterable<T> doOnParentDataResolved(NestedResourceEntity<T> entity, Iterable<?> parentData, SelectContext<?> context) {
            // do nothing .. parent entity will carry our data for us
            // TODO: create an iterable<T> for the sake of children
            return Collections.emptyList();
        }

        @Override
        public PropertyReader reader(NestedResourceEntity<T> entity) {
            return reader;
        }
    }
}
