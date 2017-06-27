package com.nhl.link.rest.it.fixture;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.EJBQLQuery;
import org.apache.cayenne.query.QueryChain;
import org.apache.cayenne.query.RefreshQuery;
import org.apache.cayenne.query.SQLSelect;
import org.junit.rules.ExternalResource;

public class DbCleaner extends ExternalResource {

	private ObjectContext context;

	public DbCleaner(ObjectContext context) {
		this.context = context;
	}

	@Override
	public void before() {

		// this is to prevent shared caches from returning bogus data between
		// test runs
		context.performQuery(new RefreshQuery());

		QueryChain chain = new QueryChain();

		// ordering is important to avoid FK constraint failures on delete
		chain.addQuery(new EJBQLQuery("delete from E15E1"));
		chain.addQuery(SQLSelect.scalarQuery(Object.class, "delete from utest.e15_e5"));

		chain.addQuery(new EJBQLQuery("delete from E4"));
		chain.addQuery(new EJBQLQuery("delete from E3"));
		chain.addQuery(new EJBQLQuery("delete from E2"));
		chain.addQuery(new EJBQLQuery("delete from E5"));
		chain.addQuery(new EJBQLQuery("delete from E6"));
		chain.addQuery(new EJBQLQuery("delete from E7"));
		chain.addQuery(new EJBQLQuery("delete from E9"));
		chain.addQuery(new EJBQLQuery("delete from E8"));
		chain.addQuery(new EJBQLQuery("delete from E11"));
		chain.addQuery(new EJBQLQuery("delete from E10"));
		chain.addQuery(new EJBQLQuery("delete from E12E13"));
		chain.addQuery(new EJBQLQuery("delete from E12"));
		chain.addQuery(new EJBQLQuery("delete from E13"));
		chain.addQuery(new EJBQLQuery("delete from E14"));
		chain.addQuery(new EJBQLQuery("delete from E15"));
		chain.addQuery(new EJBQLQuery("delete from E18"));
		chain.addQuery(new EJBQLQuery("delete from E17"));
		chain.addQuery(new EJBQLQuery("delete from E19"));
		chain.addQuery(new EJBQLQuery("delete from E20"));
		chain.addQuery(new EJBQLQuery("delete from E21"));
		chain.addQuery(new EJBQLQuery("delete from E25"));
		chain.addQuery(new EJBQLQuery("delete from E22"));
		chain.addQuery(new EJBQLQuery("delete from E23"));
		chain.addQuery(new EJBQLQuery("delete from E24"));

		context.performGenericQuery(chain);
	}
}
