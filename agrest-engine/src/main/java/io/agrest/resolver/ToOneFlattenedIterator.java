package io.agrest.resolver;

import io.agrest.reader.DataReader;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @since 4.8
 */
public class ToOneFlattenedIterator<T> implements Iterator<T> {

    protected final Iterator<?> parentIt;
    protected final DataReader parentProperty;
    protected T next;

    public ToOneFlattenedIterator(Iterator<?> parentIt, DataReader parentProperty) {
        this.parentIt = parentIt;
        this.parentProperty = parentProperty;
        rewind();
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public T next() {
        T next = this.next;

        if (next == null) {
            throw new NoSuchElementException("Past the end of the iterator");
        }

        rewind();
        return next;
    }

    protected void rewind() {

        if (parentIt.hasNext()) {

            T next = null;

            while (next == null && parentIt.hasNext()) {
                Object parent = parentIt.next();
                if (parent != null) {
                    next = (T) parentProperty.read(parent);
                }
            }

            this.next = next;

        } else {
            next = null;
        }
    }
}
