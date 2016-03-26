package com.nhl.link.rest.runtime.adapter.java8;

import com.nhl.link.rest.runtime.adapter.LinkRestAdapter;
import com.nhl.link.rest.runtime.encoder.IAttributeEncoderFactory;
import com.nhl.link.rest.runtime.encoder.Java8AttributeEncoderFactory;
import com.nhl.link.rest.runtime.parser.converter.IJsonValueConverterFactory;
import com.nhl.link.rest.runtime.parser.converter.Java8JsonValueConverterFactory;
import org.apache.cayenne.di.Binder;

import javax.ws.rs.core.Feature;
import java.util.Collection;

public class Java8Adapter implements LinkRestAdapter {

    @Override
    public void contributeToRuntime(Binder binder) {
        binder.bind(IJsonValueConverterFactory.class).to(Java8JsonValueConverterFactory.class);
        binder.bind(IAttributeEncoderFactory.class).to(Java8AttributeEncoderFactory.class);
    }

    @Override
    public void contributeToJaxRs(Collection<Feature> features) {
        // nothing is contributed specifically for Java8 here...
    }
}
