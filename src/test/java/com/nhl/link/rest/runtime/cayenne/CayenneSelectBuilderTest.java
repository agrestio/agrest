package com.nhl.link.rest.runtime.cayenne;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

import java.util.Collections;

import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.query.Ordering;
import org.apache.cayenne.query.PrefetchTreeNode;
import org.apache.cayenne.query.SelectQuery;
import org.junit.Before;
import org.junit.Test;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.encoder.EncoderFilter;
import com.nhl.link.rest.it.fixture.cayenne.E1;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrRelationship;
import com.nhl.link.rest.runtime.constraints.IConstraintsHandler;
import com.nhl.link.rest.runtime.encoder.AttributeEncoderFactory;
import com.nhl.link.rest.runtime.encoder.EncoderService;
import com.nhl.link.rest.runtime.encoder.IAttributeEncoderFactory;
import com.nhl.link.rest.runtime.encoder.IEncoderService;
import com.nhl.link.rest.runtime.encoder.IStringConverterFactory;
import com.nhl.link.rest.runtime.parser.IRequestParser;
import com.nhl.link.rest.runtime.semantics.RelationshipMapper;
import com.nhl.link.rest.unit.TestWithCayenneMapping;

public class CayenneSelectBuilderTest extends TestWithCayenneMapping {

	private IEncoderService encoderService;
	private ICayennePersister cayenneServiceMock;
	private IRequestParser requestParserMock;
	private IConstraintsHandler configMergerMock;

	@Before
	public void setUp() {

		IAttributeEncoderFactory attributeEncoderFactory = new AttributeEncoderFactory();
		IStringConverterFactory stringConverterFactory = mock(IStringConverterFactory.class);

		this.cayenneServiceMock = mock(ICayennePersister.class);
		this.requestParserMock = mock(IRequestParser.class);
		this.encoderService = new EncoderService(Collections.<EncoderFilter> emptyList(), attributeEncoderFactory,
				stringConverterFactory, new RelationshipMapper());
		this.configMergerMock = mock(IConstraintsHandler.class);
	}

	@Test
	public void testBuildQuery_Ordering() {

		Ordering o1 = E1.NAME.asc();
		Ordering o2 = E1.NAME.desc();

		SelectQuery<E1> query = new SelectQuery<E1>(E1.class);
		query.addOrdering(o1);

		DataResponse<E1> request = DataResponse.forType(E1.class).withClientEntity(getClientEntity(E1.class));
		request.getEntity().getOrderings().add(o2);

		CayenneSelectBuilder<E1> builder = new CayenneSelectBuilder<>(query, E1.class, cayenneServiceMock,
				encoderService, requestParserMock, configMergerMock);

		SelectQuery<E1> amended = builder.buildQuery(request);
		assertSame(query, amended);
		assertEquals(2, amended.getOrderings().size());
		assertSame(o1, amended.getOrderings().get(0));
		assertSame(o2, amended.getOrderings().get(1));
	}

	@Test
	public void testBuildQuery_Prefetches() {
		SelectQuery<E2> query = new SelectQuery<E2>(E2.class);

		ResourceEntity<E2> resultFilter = getClientEntity(E2.class);
		LrRelationship incoming = resultFilter.getLrEntity().getRelationship(E2.E3S.getName());
		@SuppressWarnings("unchecked")
		LrEntity<E3> target = mock(LrEntity.class);
		resultFilter.getChildren().put(E2.E3S.getName(), new ResourceEntity<E3>(target, incoming));

		DataResponse<E2> request = DataResponse.forType(E2.class).withClientEntity(resultFilter);

		CayenneSelectBuilder<E2> builder = new CayenneSelectBuilder<>(query, E2.class, cayenneServiceMock,
				encoderService, requestParserMock, configMergerMock);

		SelectQuery<E2> amended = builder.buildQuery(request);
		assertSame(query, amended);
		PrefetchTreeNode rootPrefetch = amended.getPrefetchTree();

		assertNotNull(rootPrefetch);
		assertEquals(1, rootPrefetch.getChildren().size());

		PrefetchTreeNode child1 = rootPrefetch.getChildren().iterator().next();
		assertEquals(E2.E3S.getName(), child1.getPath());
	}

	@Test
	public void testBuildQuery_Pagination() {

		DataResponse<E1> request = DataResponse.forType(E1.class).withClientEntity(getClientEntity(E1.class));

		request.withFetchLimit(10);
		request.withFetchOffset(0);
		SelectQuery<E1> q1 = new CayenneSelectBuilder<>(E1.class, cayenneServiceMock, encoderService,
				requestParserMock, configMergerMock).buildQuery(request);

		assertEquals("No pagination in the query for paginated request is expected", 0, q1.getPageSize());
		assertEquals(0, q1.getFetchOffset());
		assertEquals(0, q1.getFetchLimit());

		request.withFetchLimit(0);
		request.withFetchOffset(0);
		SelectQuery<E1> q2 = new CayenneSelectBuilder<>(E1.class, cayenneServiceMock, encoderService,
				requestParserMock, configMergerMock).buildQuery(request);
		assertEquals(0, q2.getPageSize());
		assertEquals(0, q2.getFetchOffset());
		assertEquals(0, q2.getFetchLimit());

		request.withFetchLimit(0);
		request.withFetchOffset(5);
		SelectQuery<E1> q3 = new CayenneSelectBuilder<>(E1.class, cayenneServiceMock, encoderService,
				requestParserMock, configMergerMock).buildQuery(request);
		assertEquals(0, q3.getPageSize());
		assertEquals(0, q3.getFetchOffset());
		assertEquals(0, q3.getFetchLimit());
	}

	@Test
	public void testBuildQuery_Qualfier() {
		Expression extraQualifier = E1.NAME.eq("X");
		DataResponse<E1> request = DataResponse.forType(E1.class).withClientEntity(getClientEntity(E1.class));
		request.getEntity().andQualifier(extraQualifier);

		SelectQuery<E1> query = new CayenneSelectBuilder<>(E1.class, cayenneServiceMock, encoderService,
				requestParserMock, configMergerMock).buildQuery(request);
		assertEquals(extraQualifier, query.getQualifier());

		SelectQuery<E1> query2 = new SelectQuery<E1>(E1.class);
		query2.setQualifier(E1.NAME.in("a", "b"));

		SelectQuery<E1> query2Amended = new CayenneSelectBuilder<>(query2, E1.class, cayenneServiceMock,
				encoderService, requestParserMock, configMergerMock).buildQuery(request);
		assertEquals(E1.NAME.in("a", "b").andExp(E1.NAME.eq("X")), query2Amended.getQualifier());
	}

	@Test
	public void testFactoryMethods() {

		CayenneSelectBuilder<E1> b1 = new CayenneSelectBuilder<>(E1.class, cayenneServiceMock, encoderService,
				requestParserMock, configMergerMock);
		assertSame(E1.class, b1.getType());

		SelectQuery<E1> select = new SelectQuery<E1>(E1.class);
		CayenneSelectBuilder<E1> b2 = new CayenneSelectBuilder<>(select, E1.class, cayenneServiceMock, encoderService,
				requestParserMock, configMergerMock);
		assertSame(select, b2.basicSelect(DataResponse.forType(E1.class).withClientEntity(getClientEntity(E1.class))));
		assertSame(E1.class, b2.getType());
	}

	@Test
	public void testById() {

		CayenneSelectBuilder<E1> b1 = new CayenneSelectBuilder<E1>(E1.class, cayenneServiceMock, encoderService,
				requestParserMock, configMergerMock);
		b1.byId(1);
		assertSame(E1.class, b1.getType());
		SelectQuery<E1> s1 = b1.basicSelect(DataResponse.forType(E1.class).withClientEntity(getClientEntity(E1.class)));
		assertNotNull(s1);
		assertSame(E1.class, s1.getRoot());

		SelectQuery<E1> select = new SelectQuery<E1>(E1.class);
		CayenneSelectBuilder<E1> b2 = new CayenneSelectBuilder<E1>(select, E1.class, cayenneServiceMock,
				encoderService, requestParserMock, configMergerMock);
		b2.byId(1);
		SelectQuery<E1> s2 = b2.basicSelect(DataResponse.forType(E1.class).withClientEntity(getClientEntity(E1.class)));
		assertNotNull(s2);
		assertNotSame(select, s2);
		assertSame(E1.class, s2.getRoot());
	}
}
