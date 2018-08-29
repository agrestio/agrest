package com.nhl.link.rest.swagger.api.v1.service.stage;

import com.nhl.link.rest.it.fixture.cayenne.*;
import com.nhl.link.rest.it.fixture.cayenne.E21;

import java.util.*;
import javax.ws.rs.core.*;
import com.nhl.link.rest.runtime.processor.update.UpdateContext;
import com.nhl.link.rest.runtime.processor.select.SelectContext;
import org.apache.commons.collections.CollectionUtils;

public class E21ResourceStage {

    @Context
    private Configuration config;

    protected void getOneByCompoundIdImpl(SelectContext<E21> context, List<String> xyzs, Integer abc) {

            /**
             * Add custom logic here and specify path to this file in .swagger-codegen-ignore
             * to avoid overwriting
             *
             */

    }

    protected void updateByCompoundIdImpl(UpdateContext<E21> context, List<String> xyzs) {

            /**
             * Add custom logic here and specify path to this file in .swagger-codegen-ignore
             * to avoid overwriting
             *
             */

    }

}