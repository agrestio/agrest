package com.nhl.link.rest.sencha;

import com.nhl.link.rest.protocol.Dir;
import com.nhl.link.rest.protocol.Sort;
import com.nhl.link.rest.runtime.processor.select.SelectContext;
import com.nhl.link.rest.sencha.protocol.Filter;

import java.util.Collections;
import java.util.List;

/**
 * Sencha extensions of the standard {@link com.nhl.link.rest.LrRequest}.
 *
 * @since 2.13
 */
public class SenchaRequest {

    private static final String ATTRIBUTE_KEY = SenchaRequest.class.getName();

    private Sort group;
    private Dir groupDirection;
    private List<Filter> filters;

    protected SenchaRequest() {
    }

    public static SenchaRequest get(SelectContext<?> context) {
        return (SenchaRequest) context.getAttribute(ATTRIBUTE_KEY);
    }

    public static void set(SelectContext<?> context, SenchaRequest request) {
        context.setAttribute(ATTRIBUTE_KEY, request);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Dir getGroupDirection() {
        return groupDirection;
    }

    public Sort getGroup() {
        return group;
    }

    public List<Filter> getFilters() {
        return filters != null ? filters : Collections.emptyList();
    }

    public static class Builder {
        private SenchaRequest request;

        public Builder() {
            this.request = new SenchaRequest();
        }

        public SenchaRequest build() {
            return request;
        }

        public Builder group(Sort group) {
            this.request.group = group;
            return this;
        }

        public Builder groupDirection(Dir direction) {
            this.request.groupDirection = direction;
            return this;
        }

        public Builder filters(List<Filter> filters) {
            this.request.filters = filters;
            return this;
        }
    }
}
