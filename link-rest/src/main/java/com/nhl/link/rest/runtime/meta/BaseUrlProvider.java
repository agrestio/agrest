package com.nhl.link.rest.runtime.meta;

import java.util.Optional;

/**
 * @since 2.10
 */
public class BaseUrlProvider {

    public static BaseUrlProvider forUrl(Optional<String> url) {
        return new BaseUrlProvider(url.map(u -> u.endsWith("/") ? u : u + "/"));
    }

    private Optional<String> baseUrl;

    BaseUrlProvider(Optional<String> baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Optional<String> getBaseUrl() {
        return baseUrl;
    }
}
