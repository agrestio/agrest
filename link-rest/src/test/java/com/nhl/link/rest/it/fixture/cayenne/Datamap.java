package com.nhl.link.rest.it.fixture.cayenne;

import com.nhl.link.rest.it.fixture.cayenne.auto._Datamap;

public class Datamap extends _Datamap {

    private static Datamap instance;

    private Datamap() {}

    public static Datamap getInstance() {
        if(instance == null) {
            instance = new Datamap();
        }

        return instance;
    }
}
