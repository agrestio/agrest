package com.nhl.link.rest.unit.pojo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.query.Ordering;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.runtime.BaseSelectBuilder;
import com.nhl.link.rest.runtime.config.IConfigMerger;
import com.nhl.link.rest.runtime.encoder.IEncoderService;
import com.nhl.link.rest.runtime.parser.IRequestParser;

class PojoSelectBuilder<T> extends BaseSelectBuilder<T> {

	private Map<Object, T> typeBucket;

	public PojoSelectBuilder(Class<T> type, IEncoderService encoderService, IRequestParser requestParser,
			IConfigMerger configMerger, Map<Object, T> typeBucket) {
		super(type, encoderService, requestParser, configMerger);
		this.typeBucket = typeBucket;
	}

	@Override
	protected void fetchObjects(DataResponse<T> request) {

		if (isById()) {
			T object = typeBucket.get(id);
			request.withObjects(object != null ? Collections.<T> singletonList(object) : Collections.<T> emptyList());
			return;
		}

		// clone the list and then filter/sort it as needed
		List<T> list = new ArrayList<>(typeBucket.values());

		Expression filter = request.getEntity().getQualifier();
		if (filter != null) {

			Iterator<T> it = list.iterator();
			while (it.hasNext()) {
				T t = it.next();
				if (!filter.match(t)) {
					it.remove();
				}
			}
		}

		for (Ordering o : request.getEntity().getOrderings()) {
			o.orderList(list);
		}

		request.withObjects(list);
	}
}
