package com.nhl.link.rest.runtime.processor.select;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.DefaultLrEntity;
import com.nhl.link.rest.meta.DefaultLrRelationship;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrRelationship;
import com.nhl.link.rest.runtime.fetcher.Fetcher;

public class ParallelFetchStageTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(ParallelFetchStageTest.class);

	private SelectContext<TreeNode> context;
	private ExecutorService executor;

	@Before
	public void before() {
		this.context = new SelectContext<>(TreeNode.class);
		this.context.setEntity(createEntityWithFetcher("n", null, null));
		this.executor = Executors.newFixedThreadPool(3);
	}

	@After
	public void after() {
		executor.shutdownNow();
	}

	private ResourceEntity<TreeNode> createEntityWithFetcher(String treeNodeName, ResourceEntity<TreeNode> parent,
			String name) {
		return createEntityWithFetcher(new TreeNodeFetcher(treeNodeName), parent, name);
	}

	private ResourceEntity<TreeNode> createEntityWithFetcher(Fetcher fetcher, ResourceEntity<TreeNode> parent,
			String name) {

		LrRelationship incoming = name != null ? new DefaultLrRelationship(name, TreeNode.class, false) : null;

		LrEntity<TreeNode> lrEntity = new DefaultLrEntity<>(TreeNode.class);
		ResourceEntity<TreeNode> e = new ResourceEntity<>(lrEntity, incoming);

		if (parent != null && name != null) {
			parent.getChildren().put(name, e);
		}

		e.setFetcher(fetcher);
		return e;
	}

	private ParallelFetchStage<TreeNode> createStage() {
		return new ParallelFetchStage<>(null, executor, 500, TimeUnit.MILLISECONDS);
	}

	private void assertContext(int expectedNodeCount, String expectedNodeAsString) {

		StringBuilder buffer = new StringBuilder();
		int actualNodeCount = objectsAsString(context.getEntity(), buffer);

		assertEquals(expectedNodeCount, actualNodeCount);
		assertEquals(expectedNodeAsString, buffer.toString());
	}

	private <U> int objectsAsString(ResourceEntity<U> entity, StringBuilder buffer) {

		int count = 0;

		Iterable<U> list = entity.getObjects();
		if (list != null) {

			String prefix = entity.getIncoming() != null ? entity.getIncoming().getName() + "." : "";

			for (U object : list) {

				count++;
				buffer.append(prefix).append(object).append(";");
			}
		}

		for (ResourceEntity<?> childEntity : entity.getChildren().values()) {
			count += objectsAsString(childEntity, buffer);
		}

		return count;
	}

	@Test
	public void testDoExecute_SingleFetcher() {
		createStage().doExecute(context);
		assertContext(1, "n;");
	}

	@Test
	public void testDoExecute_DependentFetchers() {

		createEntityWithFetcher("c1", context.getEntity(), "ec1");
		createEntityWithFetcher("c2", context.getEntity(), "ec2");

		createStage().doExecute(context);

		assertContext(3, "n;ec1.c1;ec2.c2;");
	}

	@Test
	public void testDoExecute_NestedDependentFetchers() {

		ResourceEntity<TreeNode> ec1 = createEntityWithFetcher("c1", context.getEntity(), "ec1");

		createEntityWithFetcher("c3", ec1, "ec3");
		createEntityWithFetcher("c4", ec1, "ec4");

		createEntityWithFetcher("c2", context.getEntity(), "ec2");

		createStage().doExecute(context);

		assertContext(5, "n;ec1.c1;ec3.c3;ec4.c4;ec2.c2;");
	}

	@Test(expected = LinkRestException.class)
	public void testDoExecute_TimeoutInRoot() {

		context.setEntity(createEntityWithFetcher(new TreeNodeFetcher("n") {
			@Override
			public <T> Iterable<T> fetch(SelectContext<T> context) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					LOGGER.info("interrupted: " + name);
				}
				return super.fetch(context);
			}
		}, null, null));

		createEntityWithFetcher("c1", context.getEntity(), "ec1");
		createEntityWithFetcher("c2", context.getEntity(), "ec2");

		createStage().doExecute(context);
		assertContext(-1, "expecting_an_error_not_this_string");
	}

	@Test(expected = LinkRestException.class)
	public void testDoExecute_TimeoutInChild() throws InterruptedException {
		createEntityWithFetcher("c1", context.getEntity(), "ec1");
		createEntityWithFetcher(new TreeNodeFetcher("c2") {
			@Override
			public <T> Iterable<T> fetch(SelectContext<T> context) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					LOGGER.info("interrupted: " + name);
				}
				return super.fetch(context);
			}
		}, context.getEntity(), "ec2");

		createStage().doExecute(context);
		assertContext(-1, "expecting_an_error_not_this_string");
	}

	@Test(expected = LinkRestException.class)
	public void testDoExecute_ErrorInChild() throws InterruptedException {

		createEntityWithFetcher("c1", context.getEntity(), "ec1");
		createEntityWithFetcher(new TreeNodeFetcher("c2") {
			
			@Override
			public <T> Iterable<T> fetch(SelectContext<T> context) {
				throw new UnsupportedOperationException("Can't fetch...");
			}
		}, context.getEntity(), "ec2");

		createStage().doExecute(context);
		assertContext(-1, "expecting_an_error_not_this_string");
	}

	static class TreeNodeFetcher implements Fetcher {
		protected String name;

		public TreeNodeFetcher(String name) {
			this.name = name;
		}
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public <T> Iterable<T> fetch(SelectContext<T> context) {
			LOGGER.info("fetched: " + name);
			List nodes = Collections.singletonList(new TreeNode(name));
			return nodes;
		}
	}

	static class TreeNode {
		private String name;

		TreeNode(String name) {
			this.name = Objects.requireNonNull(name);
		}

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return name;
		}
	}
}
