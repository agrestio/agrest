package com.nhl.link.rest.runtime.processor.select;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
			String relationshipName) {
		return createEntityWithFetcher(new TreeNodeFetcher(treeNodeName), parent, relationshipName);
	}

	private ResourceEntity<TreeNode> createEntityWithFetcher(Fetcher<TreeNode, TreeNode> fetcher,
			ResourceEntity<TreeNode> parent, String relationshipName) {

		LrRelationship incoming = relationshipName != null
				? new DefaultLrRelationship(relationshipName, TreeNode.class, false) : null;

		LrEntity<TreeNode> lrEntity = new DefaultLrEntity<>(TreeNode.class);
		ResourceEntity<TreeNode> e = new ResourceEntity<>(lrEntity, incoming);

		if (parent != null && relationshipName != null) {
			parent.getChildren().put(relationshipName, e);
		}

		e.setFetcher(fetcher);
		return e;
	}

	private ParallelFetchStage<TreeNode> createStage() {
		return new ParallelFetchStage<>(null, executor, 500, TimeUnit.MILLISECONDS, mock(Fetcher.class));
	}

	private void assertContext(int nodeCount, String... nodeStrings) {

		List<TreeNode> nodes = context.getObjects();
		assertNotNull(nodes);
		assertEquals(nodeStrings.length, nodes.size());
		assertEquals(nodeCount, countNodes(nodes));

		for (int i = 0; i < nodeStrings.length; i++) {
			assertEquals(nodeStrings[i], nodes.get(i).toString());
		}
	}

	private int countNodes(List<TreeNode> nodes) {

		int count = nodes.size();
		for (TreeNode n : nodes) {
			count += countNodes(n.getChildren());
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

		assertContext(3, "n;n.c1;n.c2;");
	}

	@Test
	public void testDoExecute_NestedDependentFetchers() {

		ResourceEntity<TreeNode> c1 = createEntityWithFetcher("c1", context.getEntity(), "ec1");

		createEntityWithFetcher("c3", c1, "ec3");
		createEntityWithFetcher("c4", c1, "ec4");

		createEntityWithFetcher("c2", context.getEntity(), "ec2");

		createStage().doExecute(context);

		assertContext(5, "n;n.c1;c1.c3;c1.c4;n.c2;");
	}

	@Test(expected = LinkRestException.class)
	public void testDoExecute_TimeoutInRoot() {

		context.setEntity(createEntityWithFetcher(new TreeNodeFetcher("n") {

			@Override
			public Iterable<TreeNode> fetch(SelectContext<TreeNode> context, Iterable<TreeNode> parents) {

				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					LOGGER.info("interrupted: " + name);
				}
				return super.fetch(context, parents);
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
			public Iterable<TreeNode> fetch(SelectContext<TreeNode> context, Iterable<TreeNode> parents) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					LOGGER.info("interrupted: " + name);
				}
				return super.fetch(context, parents);
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
			public Iterable<TreeNode> fetch(SelectContext<TreeNode> context, Iterable<TreeNode> parents) {
				throw new UnsupportedOperationException("Can't fetch...");
			}
		}, context.getEntity(), "ec2");

		createStage().doExecute(context);
		assertContext(-1, "expecting_an_error_not_this_string");
	}

	static class TreeNodeFetcher implements Fetcher<TreeNode, TreeNode> {
		protected String name;

		public TreeNodeFetcher(String name) {
			this.name = name;
		}

		@Override
		public Iterable<TreeNode> fetch(SelectContext<TreeNode> context, Iterable<TreeNode> parents) {

			LOGGER.info("fetched child: " + name);

			TreeNode c = new TreeNode(name);
			List<TreeNode> children = Collections.singletonList(c);

			if (parents != null) {
				((TreeNode) parents.iterator().next()).addChild(c);
			} else {
				context.setObjects(children);
			}

			return children;
		}
	}

	static class TreeNode {
		private String name;
		private Set<TreeNode> children;

		TreeNode(String name) {
			this.name = Objects.requireNonNull(name);
			this.children = ConcurrentHashMap.newKeySet();
		}

		public String getName() {
			return name;
		}

		public void addChild(TreeNode child) {
			children.add(child);
		}

		public List<TreeNode> getChildren() {

			// results may arrive in any order, so we need to sort children for
			// predictability...

			List<TreeNode> sorted = new ArrayList<>(children);
			Collections.sort(sorted, (t1, t2) -> t1.getName().compareTo(t2.getName()));
			return sorted;
		}

		@Override
		public String toString() {
			StringBuilder buffer = new StringBuilder();
			toString(buffer, null);
			return buffer.toString();
		}

		private void toString(StringBuilder buffer, String parentName) {

			if (parentName != null) {
				buffer.append(parentName).append(".");
			}
			buffer.append(name).append(";");

			for (TreeNode c : getChildren()) {
				c.toString(buffer, name);
			}
		}
	}
}
