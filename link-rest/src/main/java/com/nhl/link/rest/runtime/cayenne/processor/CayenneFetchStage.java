package com.nhl.link.rest.runtime.cayenne.processor;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import javax.ws.rs.core.Response.Status;

import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.cayenne.CayenneLrAttribute;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.Ordering;
import org.apache.cayenne.query.PrefetchTreeNode;
import org.apache.cayenne.query.SelectQuery;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.annotation.listener.DataFetched;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.processor.BaseLinearProcessingStage;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.runtime.cayenne.ICayennePersister;
import com.nhl.link.rest.runtime.processor.select.SelectContext;

/**
 * @since 1.16
 */
public class CayenneFetchStage<T> extends BaseLinearProcessingStage<SelectContext<T>, T> {

	private ICayennePersister persister;

	public CayenneFetchStage(ProcessingStage<SelectContext<T>, ? super T> next, ICayennePersister persister) {
		super(next);
		this.persister = persister;
	}

	@Override
	public Class<? extends Annotation> afterStageListener() {
		return DataFetched.class;
	}

	@Override
	protected void doExecute(SelectContext<T> context) {
		DataResponse<T> response = context.getResponse();
		SelectQuery<T> select = buildQuery(context);

		List<T> objects = persister.sharedContext().select(select);

		if (context.isAtMostOneObject() && objects.size() != 1) {

			LrEntity<?> entity = context.getEntity().getLrEntity();

			if (objects.isEmpty()) {
				throw new LinkRestException(Status.NOT_FOUND,
						String.format("No object for ID '%s' and entity '%s'", context.getId(), entity.getName()));
			} else {
				throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, String.format(
						"Found more than one object for ID '%s' and entity '%s'", context.getId(), entity.getName()));
			}
		}

		response.withObjects(objects);
	}

	<X extends T> SelectQuery<X> buildQuery(SelectContext<X> context) {

		DataResponse<X> response = context.getResponse();
		ResourceEntity<X> entity = context.getEntity();

		SelectQuery<X> query = basicSelect(context);

		if (context.getParent() != null) {
			Expression qualifier = context.getParent().qualifier(persister.entityResolver());
			query.andQualifier(qualifier);
		}

		if (entity.getQualifier() != null) {
			query.andQualifier(entity.getQualifier());
		}

		for (Ordering o : entity.getOrderings()) {
			query.addOrdering(o);
		}

		if (!entity.getChildren().isEmpty()) {
			PrefetchTreeNode root = new PrefetchTreeNode();

			int prefetchSemantics = response.getPrefetchSemantics();
			if (prefetchSemantics <= 0) {
				// it makes more sense to use joint prefetches for single object
				// queries...
				prefetchSemantics = context.isById() && !context.getId().isCompound() ?
						PrefetchTreeNode.JOINT_PREFETCH_SEMANTICS : PrefetchTreeNode.DISJOINT_PREFETCH_SEMANTICS;
			}

			appendPrefetches(root, entity, prefetchSemantics);
			query.setPrefetchTree(root);
		}

		return query;
	}

	<X extends T> SelectQuery<X> basicSelect(SelectContext<X> context) {

		// selecting by ID overrides any explicit SelectQuery...
		if (context.isById()) {

			Class<X> root = context.getType();
			SelectQuery<X> query = new SelectQuery<>(root);
			Collection<LrAttribute> idAttributes = context.getEntity().getLrEntity().getIds();

			if (idAttributes.size() != context.getId().size()) {
				throw new LinkRestException(Status.BAD_REQUEST,
						"Wrong compound ID size: expected " + idAttributes.size() + ", got: " + context.getId().size());
			}
			for (LrAttribute idAttribute : idAttributes) {
				Object idValue = context.getId().get(idAttribute.getName());
				query.andQualifier(ExpressionFactory.matchDbExp(
						((CayenneLrAttribute) idAttribute).getDbAttribute().getName(), idValue
				));
			}

			return query;
		}

		return context.getSelect() != null ? context.getSelect() : new SelectQuery<>(context.getType());
	}

	private void appendPrefetches(PrefetchTreeNode root, ResourceEntity<?> entity, int prefetchSemantics) {
		for (Entry<String, ResourceEntity<?>> e : entity.getChildren().entrySet()) {

			PrefetchTreeNode child = root.addPath(e.getKey());

			// always full prefetch related entities... we can't use phantom as
			// this will hit object cache and hence won't be cache controlled
			// via query cache anymore...
			child.setPhantom(false);
			child.setSemantics(prefetchSemantics);
			appendPrefetches(child, e.getValue(), prefetchSemantics);
		}

		if (entity.getMapBy() != null) {
			appendPrefetches(root, entity.getMapBy(), prefetchSemantics);
		}
	}
}
