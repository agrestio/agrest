package io.agrest.runtime.cayenne.processor.select;

import java.util.Iterator;

/**
 * @since 3.4
 */
public class SingleColumnIterator<T> implements Iterator<T> {

    private Iterator<Object[]> arraysIterator;
    private int index;

    public SingleColumnIterator(Iterator<Object[]> arraysIterator, int index) {
        this.arraysIterator = arraysIterator;
        this.index = index;
    }

    @Override
    public boolean hasNext() {
        return arraysIterator.hasNext();
    }

    @Override
    public T next() {
        return (T) arraysIterator.next()[index];
    }
}
