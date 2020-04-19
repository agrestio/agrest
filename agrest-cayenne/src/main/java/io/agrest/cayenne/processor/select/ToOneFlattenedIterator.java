package io.agrest.cayenne.processor.select;

import org.apache.cayenne.DataObject;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @since 3.4
 */
public class ToOneFlattenedIterator<T> implements Iterator<T> {

    protected Iterator<? extends DataObject> parentIt;
    protected String property;
    protected T next;

    public ToOneFlattenedIterator(Iterator<? extends DataObject> parentIt, String property) {
        this.parentIt = parentIt;
        this.property = property;
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
                DataObject parent = parentIt.next();
                if (parent != null) {
                    next = (T) parent.readProperty(property);
                }
            }

            this.next = next;

        } else {
            next = null;
        }
    }
}
