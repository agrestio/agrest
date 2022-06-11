package io.agrest.resolver;

import io.agrest.reader.DataReader;

import java.util.Iterator;
import java.util.List;

/**
 * @param <T>
 * @since 4.8
 */
public class ToManyFlattenedIterator<T> extends ToOneFlattenedIterator<T> {

    private List<T> nextList;
    private int nextPos;

    public ToManyFlattenedIterator(Iterator<?> parentIt, DataReader property) {
        super(parentIt, property);
    }

    @Override
    protected void rewind() {

        if (nextList == null || nextList.size() == nextPos) {
            rewindFromParent();
        } else {
            this.next = nextList.get(nextPos++);
        }
    }

    protected void rewindFromParent() {

        this.nextList = null;
        this.nextPos = -1;
        this.next = null;

        if (parentIt.hasNext()) {

            List<T> nextList = null;

            while (nextList == null && parentIt.hasNext()) {
                Object parent = parentIt.next();
                if (parent != null) {
                    // TODO: handle Set or Map relationships
                    List<T> maybeNextList = (List<T>) parentProperty.read(parent);
                    if (maybeNextList != null && !maybeNextList.isEmpty()) {
                        nextList = maybeNextList;
                    }
                }
            }

            if (nextList != null) {
                this.nextList = nextList;
                this.nextPos = 1;
                this.next = nextList.get(0);
            }
        }
    }
}
