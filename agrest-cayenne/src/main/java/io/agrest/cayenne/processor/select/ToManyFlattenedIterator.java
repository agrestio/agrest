package io.agrest.cayenne.processor.select;

import org.apache.cayenne.DataObject;

import java.util.Iterator;
import java.util.List;

/**
 * @param <T>
 * @since 3.4
 */
public class ToManyFlattenedIterator<T> extends ToOneFlattenedIterator<T> {

    private List<T> nextList;
    private int nextPos;

    public ToManyFlattenedIterator(Iterator<? extends DataObject> parentIt, String property) {
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
                DataObject parent = parentIt.next();
                if (parent != null) {
                    // TODO: handle Set or Map relationships
                    List<T> maybeNextList = (List<T>) parent.readProperty(property);
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
