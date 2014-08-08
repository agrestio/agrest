package com.nhl.link.rest.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.map.DataMap;
import org.junit.Before;
import org.junit.Test;

import com.nhl.link.rest.DataResponseConfig;
import com.nhl.link.rest.runtime.cayenne.ICayennePersister;
import com.nhl.link.rest.runtime.config.IConfigMerger;
import com.nhl.link.rest.runtime.encoder.IEncoderService;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.meta.MetadataService;
import com.nhl.link.rest.runtime.parser.IRequestParser;
import com.nhl.link.rest.unit.TestWithCayenneMapping;
import com.nhl.link.rest.unit.cayenne.E5;

public class EntityDaoLinkRestServiceTest extends TestWithCayenneMapping {

	private IRequestParser mockRequestParser;
	private IConfigMerger mockConfigMerger;
	private IEncoderService mockEncoderService;
	private ICayennePersister mockCayennePersister;
	private IMetadataService metadataService;

	@Before
	public void before() {
		this.mockRequestParser = mock(IRequestParser.class);
		this.mockConfigMerger = mock(IConfigMerger.class);
		this.mockEncoderService = mock(IEncoderService.class);

		ObjectContext sharedContext = runtime.newContext();
		mockCayennePersister = mock(ICayennePersister.class);
		when(mockCayennePersister.sharedContext()).thenReturn(sharedContext);
		when(mockCayennePersister.newContext()).thenReturn(runtime.newContext());
		when(mockCayennePersister.entityResolver()).thenReturn(sharedContext.getEntityResolver());

		this.metadataService = new MetadataService(Collections.<DataMap> emptyList(), mockCayennePersister);
	}

	@Test
	public void testNewConfig() {

		EntityDaoLinkRestService service = new EntityDaoLinkRestService(mockRequestParser, mockEncoderService,
				metadataService, mockCayennePersister, mockConfigMerger);

		DataResponseConfig defaultConfig = service.newConfig(E5.class);
		assertNotNull(defaultConfig);

		assertEquals(0, defaultConfig.getFetchOffset());
		assertEquals(0, defaultConfig.getFetchLimit());

		assertTrue(defaultConfig.getEntity().isIdIncluded());
		assertTrue(defaultConfig.getEntity().getChildren().isEmpty());
		assertEquals(2, defaultConfig.getEntity().getAttributes().size());
		assertTrue(defaultConfig.getEntity().getAttributes().contains(E5.DATE.getName()));
		assertTrue(defaultConfig.getEntity().getAttributes().contains(E5.NAME.getName()));

	}

}
