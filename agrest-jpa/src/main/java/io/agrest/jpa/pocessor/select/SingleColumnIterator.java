package io.agrest.jpa.pocessor.select;

import java.util.Iterator;

/**
 * @since 5.0
 */
public class SingleColumnIterator<T> implements Iterator<T> {

    private final Iterator<Object[]> arraysIterator;
    private final int index;

    public SingleColumnIterator(Iterator<Object[]> arraysIterator, int index) {
        this.arraysIterator = arraysIterator;
        this.index = index;
    }

    @Override
    public boolean hasNext() {
        return arraysIterator.hasNext();
    }

    @SuppressWarnings("unchecked")
    @Override
    public T next() {
        return (T) arraysIterator.next()[index];
    }
}
