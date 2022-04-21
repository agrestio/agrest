package io.agrest.jpa.query;

import java.util.ArrayList;
import java.util.List;

import io.agrest.jpa.exp.JpaExpression;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

/**
 * Simple query builder that simplifies JPA query construction
 *
 * @since 5.0
 */
public class JpaQueryBuilder {

    private List<String> selectSpec;
    private List<String> from;
    private List<String> join;
    private JpaExpression where;
    private List<String> ordering;

    private int limit;

    private int offset;

    private JpaQueryBuilder() {
        this.selectSpec = new ArrayList<>();
        this.from = new ArrayList<>();
    }

    public static JpaQueryBuilder select(String select) {
        JpaQueryBuilder builder = new JpaQueryBuilder();
        return builder.selectSpec(select);
    }

    public JpaQueryBuilder selectSpec(String select) {
        selectSpec.add(select);
        return this;
    }

    public JpaQueryBuilder from(String from) {
        this.from.add(from);
        return this;
    }

    public JpaQueryBuilder join(String join) {
        if(this.join == null) {
            this.join = new ArrayList<>();
        }
        this.join.add(join);
        return this;
    }

    public JpaQueryBuilder where(JpaExpression where) {
        if(where.isEmpty()) {
            return this;
        }

        if(this.where != null) {
            this.where = this.where.and(where);
        } else {
            this.where = where;
        }
        return this;
    }

    public JpaQueryBuilder where(String where) {
        if(where == null || where.length() == 0) {
            return this;
        }
        if(this.where == null) {
            this.where = new JpaExpression(where);
        } else {
            // TODO: solve this case somehow?
        }
        return this;
    }

    public JpaQueryBuilder orderBy(String orderBy) {
        if(this.ordering == null) {
            this.ordering = new ArrayList<>();
        }

        this.ordering.add(orderBy);
        return this;
    }

    public JpaQueryBuilder limit(int limit) {
        this.limit = limit;
        return this;
    }

    public JpaQueryBuilder offset(int offset) {
        this.offset = offset;
        return this;
    }

    public Query build(EntityManager entityManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("select ").append(getSelectSpec()).append(" from ").append(getFrom());

        if(hasJoins()) {
            sb.append(" ").append(getJoins());
        }

        if(hasWhere()) {
            sb.append(" where ").append(getWhere().getExp());
        }
        if(hasOrdering()) {
            sb.append(" order by ").append(getOrdering());
        }
        Query query = entityManager.createQuery(sb.toString());

        if(limit != 0) {
            query.setMaxResults(limit);
        }
        if(offset != 0) {
            query.setFirstResult(offset);
        }
        if(hasWhere()) {
            int i = 0;
            for(Object param : where.getParams()) {
                query.setParameter(i++, param);
            }
        }

        return query;
    }

    boolean hasJoins() {
        return join != null && !join.isEmpty();
    }

    public boolean hasWhere() {
        return where != null && !where.isEmpty();
    }

    boolean hasOrdering() {
        return ordering != null && !ordering.isEmpty();
    }

    public String getSelectSpec() {
        return String.join(", ", selectSpec);
    }

    public String getFrom() {
        return String.join(" ", from);
    }

    public String getJoins() {
        return String.join(" ", join);
    }

    public JpaExpression getWhere() {
        return where;
    }

    public String getOrdering() {
        return String.join(", ", ordering);
    }
}
