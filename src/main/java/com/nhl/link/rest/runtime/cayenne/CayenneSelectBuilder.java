package com.nhl.link.rest.runtime.cayenne;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.SelectBuilder;
import com.nhl.link.rest.meta.LrPersistentAttribute;
import com.nhl.link.rest.runtime.BaseSelectBuilder;
import com.nhl.link.rest.runtime.constraints.IConstraintsHandler;
import com.nhl.link.rest.runtime.encoder.IEncoderService;
import com.nhl.link.rest.runtime.parser.IRequestParser;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.Ordering;
import org.apache.cayenne.query.PrefetchTreeNode;
import org.apache.cayenne.query.SelectQuery;

class CayenneSelectBuilder<T> extends BaseSelectBuilder<T> implements SelectBuilder<T> {

	private SelectQuery<T> select;
	private ICayennePersister cayenneService;

	CayenneSelectBuilder(SelectQuery<T> select, Class<T> type, ICayennePersister cayenneService,
			IEncoderService encoderService, IRequestParser requestParser, IConstraintsHandler configMerger) {
		this(type, cayenneService, encoderService, requestParser, configMerger);
		this.select = select;
	}

	CayenneSelectBuilder(Class<T> type, ICayennePersister cayenneService, IEncoderService encoderService,
			IRequestParser requestParser, IConstraintsHandler configMerger) {
		super(type, encoderService, requestParser, configMerger);
		this.cayenneService = cayenneService;
	}

	@Override
	protected void fetchObjects(DataResponse<T> responseBuilder) {

		SelectQuery<T> select = buildQuery(responseBuilder);
		responseBuilder.withObjects(cayenneService.sharedContext().select(select));
	}

	protected SelectQuery<T> buildQuery(DataResponse<T> request) {

		SelectQuery<T> query = basicSelect(request);

		if (parent != null) {
			Expression qualifier = parent.qualifier(cayenneService.entityResolver());
			query.andQualifier(qualifier);
		}

		if (request.getEntity().getQualifier() != null) {
			query.andQualifier(request.getEntity().getQualifier());
		}

		for (Ordering o : request.getEntity().getOrderings()) {
			query.addOrdering(o);
		}

		if (request.getEntity() != null && !request.getEntity().getChildren().isEmpty()) {
			int prefetchSemantics = request.getPrefetchSemantics();
			if (prefetchSemantics <= 0) {
				// it makes more sense to use joint prefetches for single object
				// queries...
				prefetchSemantics = isById() ? PrefetchTreeNode.JOINT_PREFETCH_SEMANTICS
						: PrefetchTreeNode.DISJOINT_PREFETCH_SEMANTICS;
			}

			query.setPrefetchTree(
					Util.createPrefetch(request.getEntity(), prefetchSemantics)
			);
		}

		return query;
	}

	protected SelectQuery<T> basicSelect(DataResponse<T> request) {

		// selecting by ID overrides any explicit SelectQuery...
		if (isById()) {

			Class<T> root = getType();

			// TODO: compound PK
			LrPersistentAttribute idAttribute = (LrPersistentAttribute) request.getEntity().getLrEntity().getSingleId();

			SelectQuery<T> query = new SelectQuery<T>(root);
			query.andQualifier(ExpressionFactory.matchDbExp(idAttribute.getDbAttribute().getName(), id));
			return query;
		}

		if (select == null) {
			select = new SelectQuery<>(getType());
		}

		return select;
	}
}
