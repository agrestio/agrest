package io.agrest.it.fixture.cayenne;

import io.agrest.annotation.ClientReadable;
import io.agrest.annotation.ClientWritable;
import io.agrest.it.fixture.cayenne.auto._E10;

@ClientReadable(id = true, value = { "cBoolean", "cInt", "e11s" })
@ClientWritable(id = false, value = { "cInt" })
public class E10 extends _E10 {

	private static final long serialVersionUID = 1L;

}
