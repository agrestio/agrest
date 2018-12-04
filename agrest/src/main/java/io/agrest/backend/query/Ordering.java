package io.agrest.backend.query;

import io.agrest.backend.exp.Expression;
import io.agrest.backend.exp.ExpressionException;
import io.agrest.backend.exp.parser.ASTDbPath;
import io.agrest.backend.exp.parser.ASTObjPath;
import io.agrest.backend.util.ConversionUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Defines object sorting criteria, used either for in-memory sorting of object
 * lists or as a specification for building <em>ORDER BY</em> clause of a
 * SelectQuery query. Note that in case of in-memory sorting, Ordering can be
 * used with any JavaBeans, not just DataObjects.
 */
public class Ordering implements Comparator<Object> {

    protected String sortSpecString;
    protected SortOrder sortOrder;
    protected transient Expression sortSpec;
    protected boolean nullSortedFirst = true;


//    /**
//     * Orders a given list of objects, using a List of Orderings applied
//     * according the default iteration order of the Orderings list. I.e. each
//     * Ordering with lower index is more significant than any other Ordering
//     * with higher index. List being ordered is modified in place.
//     *
//     * @param objects elements to sort
//     * @param orderings list of Orderings to be applied
//     */
//    @SuppressWarnings("unchecked")
//    public static void orderList(List<?> objects, List<? extends Ordering> orderings) {
//        if(objects == null || orderings == null || orderings.isEmpty()) {
//            return;
//        }
//        Comparator<Object> comparator = (Comparator<Object>) orderings.get(0);
//        for(int i=1; i<orderings.size(); i++) {
//            comparator = comparator.thenComparing((Comparator<? super Object>) orderings.get(i));
//        }
//        objects.sort(comparator);
//    }

    /**
     * Orders the given list of objects according to the ordering that this
     * object specifies. List is modified in-place.
     *
     * @param objects
     *            a List of objects to be sorted
     */
    public void orderList(List<?> objects) {
        Collections.sort(objects, this);
    }

    public Ordering(String sortPathSpec, SortOrder sortOrder) {
        setSortSpecString(sortPathSpec);
        setSortOrder(sortOrder);
    }

    public void setSortSpecString(String sortSpecString) {
        if (sortSpecString != null && !sortSpecString.equalsIgnoreCase(this.sortSpecString)) {
            this.sortSpecString = sortSpecString;
        }
    }

    /**
     * Sets the sort order for this ordering.
     *
     */
    public void setSortOrder(SortOrder order) {
        this.sortOrder = order;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    /**
     * Returns sortSpec string representation.34
     *
     */
    public String getSortSpecString() {
        return sortSpecString;
    }

    /**
     * Comparable interface implementation. Can compare two Java Beans based on
     * the stored expression.
     */
    @Override
    public int compare(Object o1, Object o2) {
        Expression exp = getSortSpec();

        Object value1 = null;
        Object value2 = null;
        try {
            value1 = Arrays.stream(o1.getClass().getMethods())
                            .filter(m -> ("get" + exp.toString()).equalsIgnoreCase(m.getName()))
                            .findAny()
                            .get()
                            .invoke(o1);
        } catch (IllegalAccessException | InvocationTargetException e) {
                // do nothing, we expect this
        }

        try {
            value2 = Arrays.stream(o2.getClass().getMethods())
                            .filter(m -> ("get" + exp.toString()).equalsIgnoreCase(m.getName()))
                            .findAny()
                            .get()
                            .invoke(o2);
        } catch (IllegalAccessException | InvocationTargetException e) {
            // do nothing, we expect this
        }

        if (value1 == null && value2 == null) {
            return 0;
        } else if (value1 == null) {
            return nullSortedFirst ? -1 : 1;
        } else if (value2 == null) {
            return nullSortedFirst ? 1 : -1;
        }

        if (isCaseInsensitive()) {
            // TODO: to upper case should probably be defined as a separate
            // expression
            // type
            value1 = ConversionUtil.toUpperCase(value1);
            value2 = ConversionUtil.toUpperCase(value2);
        }

        int compareResult = ConversionUtil.toComparable(value1).compareTo(ConversionUtil.toComparable(value2));
        return (isAscending()) ? compareResult : -compareResult;
    }

    /** Returns true if the sorting is case insensitive */
    public boolean isCaseInsensitive() {
        return !isCaseSensitive();
    }

    /**
     * Returns true if the sorting is case sensitive.
     */
    public boolean isCaseSensitive() {
        return sortOrder == null || sortOrder == SortOrder.ASCENDING || sortOrder == SortOrder.DESCENDING;
    }

    /** Returns true if sorting is done in ascending order. */
    public boolean isAscending() {
        return sortOrder == null || sortOrder == SortOrder.ASCENDING || sortOrder == SortOrder.ASCENDING_INSENSITIVE;
    }

    /**
     * Returns true if the sorting is done in descending order.
     */
    public boolean isDescending() {
        return !isAscending();
    }

    /**
     * Returns the expression defining a ordering Java Bean property.
     */
    public Expression getSortSpec() {
        if (sortSpecString == null) {
            return null;
        }

        // compile on demand .. since orderings can only be paths, avoid the
        // overhead of
        // Expression.fromString, and parse them manually
        if (sortSpec == null) {

            if (sortSpecString.startsWith(ASTDbPath.DB_PREFIX)) {
                sortSpec = new ASTDbPath(sortSpecString.substring(ASTDbPath.DB_PREFIX.length()));
            } else if (sortSpecString.startsWith(ASTObjPath.OBJ_PREFIX)) {
                sortSpec = new ASTObjPath(sortSpecString.substring(ASTObjPath.OBJ_PREFIX.length()));
            } else {
                sortSpec = new ASTObjPath(sortSpecString);
            }
        }

        return sortSpec;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof Ordering)) {
            return false;
        }

        Ordering o = (Ordering) object;

        if (!ConversionUtil.nullSafeEquals(sortSpecString, o.sortSpecString)) {
            return false;
        }

        if (sortOrder != o.sortOrder) {
            return false;
        }

//        if (pathExceptionSuppressed != o.pathExceptionSuppressed) {
//            return false;
//        }

        if (nullSortedFirst != o.nullSortedFirst) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = sortSpecString != null ? sortSpecString.hashCode() : 0;
        result = 31 * result + (sortOrder != null ? sortOrder.hashCode() : 0);
//        result = 31 * result + (pathExceptionSuppressed ? 1 : 0);
        result = 31 * result + (nullSortedFirst ? 1 : 0);
        return result;
    }

}
