package com.nhl.link.rest.it.fixture.cayenne;

import com.nhl.link.rest.annotation.ClientReadable;
import com.nhl.link.rest.annotation.ClientWritable;
import com.nhl.link.rest.it.fixture.cayenne.auto._E10;

@ClientReadable(id = true, value = { "cBoolean", "cInt", "e11s" })
@ClientWritable(id = false, value = { "cInt" })
public class E10 extends _E10 {

	private static final long serialVersionUID = 1L;

}
