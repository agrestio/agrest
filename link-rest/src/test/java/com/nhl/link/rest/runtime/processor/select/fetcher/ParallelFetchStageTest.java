package com.nhl.link.rest.runtime.processor.select.fetcher;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.runtime.processor.select.SelectContext;

public class ParallelFetchStageTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(ParallelFetchStageTest.class);

	private SelectContext<TreeNode> context;
	private ExecutorService executor;

	@Before
	public void before() {
		this.context = new SelectContext<>(TreeNode.class);
		this.executor = Executors.newFixedThreadPool(3);
	}

	@After
	public void after() {
		executor.shutdownNow();
	}

	private <T> ParallelFetchStage<T> createStage(Function<SelectContext<T>, RootFetcher<T>> partitioner) {
		return new ParallelFetchStage<>(null, partitioner, executor, 500, TimeUnit.MILLISECONDS);
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
		Function<SelectContext<TreeNode>, RootFetcher<TreeNode>> partitioner = c -> () -> Collections
				.singletonList(new TreeNode("n"));
		createStage(partitioner).doExecute(context);

		assertContext(1, "n;");
	}

	@Test
	public void testDoExecute_DependentFetchers() {
		RootFetcher<TreeNode> rootFetcher = new SingleRootFetcher("n", new SingleChildFetcher("c1"),
				new SingleChildFetcher("c2"));
		Function<SelectContext<TreeNode>, RootFetcher<TreeNode>> partitioner = c -> rootFetcher;
		createStage(partitioner).doExecute(context);

		assertContext(3, "n;n.c1;n.c2;");
	}

	@Test
	public void testDoExecute_NestedDependentFetchers() {
		ChildFetcher<TreeNode, TreeNode> c1 = new SingleChildFetcher("c1", new SingleChildFetcher("c3"),
				new SingleChildFetcher("c4"));

		RootFetcher<TreeNode> rootFetcher = new SingleRootFetcher("n", c1, new SingleChildFetcher("c2"));
		Function<SelectContext<TreeNode>, RootFetcher<TreeNode>> partitioner = c -> rootFetcher;
		createStage(partitioner).doExecute(context);

		assertContext(5, "n;n.c1;c1.c3;c1.c4;n.c2;");
	}

	@Test(expected = LinkRestException.class)
	public void testDoExecute_TimeoutInRoot() {
		RootFetcher<TreeNode> rootFetcher = new SingleRootFetcher("n", new SingleChildFetcher("c1"),
				new SingleChildFetcher("c2")) {
			@Override
			public List<TreeNode> fetch() {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					LOGGER.info("interrupted: " + name);
				}
				return super.fetch();
			}
		};
		Function<SelectContext<TreeNode>, RootFetcher<TreeNode>> partitioner = c -> rootFetcher;
		createStage(partitioner).doExecute(context);
	}

	@Test(expected = LinkRestException.class)
	public void testDoExecute_TimeoutInChild() throws InterruptedException {
		ChildFetcher<TreeNode, TreeNode> c1 = new SingleChildFetcher("c1") {

			@Override
			public List<TreeNode> fetch(Future<List<TreeNode>> parents) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					LOGGER.info("interrupted: " + name);
				}
				return super.fetch(parents);
			}
		};

		RootFetcher<TreeNode> rootFetcher = new SingleRootFetcher("n", c1, new SingleChildFetcher("c2"));
		Function<SelectContext<TreeNode>, RootFetcher<TreeNode>> partitioner = c -> rootFetcher;
		createStage(partitioner).doExecute(context);
	}

	@Test(expected = LinkRestException.class)
	public void testDoExecute_ErrorInChild() throws InterruptedException {
		ChildFetcher<TreeNode, TreeNode> c1 = new SingleChildFetcher("c1") {

			@Override
			public List<TreeNode> fetch(Future<List<TreeNode>> parents) {
				throw new UnsupportedOperationException("Can't fetch...");
			}
		};

		RootFetcher<TreeNode> rootFetcher = new SingleRootFetcher("n", c1, new SingleChildFetcher("c2"));
		Function<SelectContext<TreeNode>, RootFetcher<TreeNode>> partitioner = c -> rootFetcher;
		createStage(partitioner).doExecute(context);
	}

	static class SingleRootFetcher implements RootFetcher<TreeNode> {
		protected String name;
		private Collection<ChildFetcher<?, TreeNode>> children;

		@SafeVarargs
		public SingleRootFetcher(String name, ChildFetcher<?, TreeNode>... children) {
			this.name = name;
			this.children = asList(children);
		}

		@Override
		public Collection<ChildFetcher<?, TreeNode>> subFetchers() {
			return children;
		}

		@Override
		public List<TreeNode> fetch() {
			LOGGER.info("fetched root: " + name);
			return Collections.singletonList(new TreeNode(name));
		}
	}

	static class SingleChildFetcher implements ChildFetcher<TreeNode, TreeNode> {

		protected String name;
		private Collection<ChildFetcher<?, TreeNode>> children;

		@SafeVarargs
		public SingleChildFetcher(String name, ChildFetcher<?, TreeNode>... children) {
			this.name = name;
			this.children = asList(children);
		}

		@Override
		public Collection<ChildFetcher<?, TreeNode>> subFetchers() {
			return children;
		}

		@Override
		public List<TreeNode> fetch(Future<List<TreeNode>> parents) {

			LOGGER.info("fetched child: " + name);

			TreeNode c = new TreeNode(name);
			try {
				parents.get(1, TimeUnit.SECONDS).get(0).addChild(c);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				throw new RuntimeException(e);
			}

			return Collections.singletonList(c);
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
