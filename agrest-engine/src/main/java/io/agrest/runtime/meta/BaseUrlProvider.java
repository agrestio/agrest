package io.agrest.runtime.meta;

import java.util.Optional;

/**
 * @since 2.10
 * @deprecated since 5.0 as a part of the metadata API that is going away
 */
@Deprecated
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
