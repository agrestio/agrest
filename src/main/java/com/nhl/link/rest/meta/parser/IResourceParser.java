package com.nhl.link.rest.meta.parser;

import com.nhl.link.rest.meta.LrResource;

import java.util.Collection;

public interface IResourceParser {

    <T> Collection<LrResource> parse(Class<T> resourceClass);

}
