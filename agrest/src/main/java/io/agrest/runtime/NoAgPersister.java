package io.agrest.runtime;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * A placeholder for {@link IAgPersister} for Agrest containers that work
 * with POJOs.
 */
public final class NoAgPersister implements IAgPersister<Context, Context> {

    private static final IAgPersister INSTANCE = new NoAgPersister();

    public static IAgPersister instance() {
        return INSTANCE;
    }

    private Context emptyResolver;

    public NoAgPersister() {
        try {
            this.emptyResolver = new InitialContext();
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Context entityResolver() {

        return emptyResolver;
    }

    @Override
    public Context newContext() {
        throw new UnsupportedOperationException("This service does not support interaction");
    }

    @Override
    public Context sharedContext() {
        throw new UnsupportedOperationException("This service does not support interaction");
    }
}
