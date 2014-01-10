package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.size;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.service.rest.Schema;
import org.cmdbuild.service.rest.dto.ClassDetail;
import org.cmdbuild.service.rest.dto.ClassDetailResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.google.common.base.Function;

public class CxfSchema implements Schema {

	private static Function<CMClass, ClassDetail> TO_CLASS_DETAILS = new Function<CMClass, ClassDetail>() {

		@Override
		public ClassDetail apply(final CMClass input) {
			return ClassDetail.newInstance() //
					.withName(input.getName()) //
					.withDescription(input.getDescription()) //
					.build();
		}

	};

	@Autowired
	private ApplicationContext applicationContext;

	@Override
	public ClassDetailResponse getClasses() {
		final Iterable<? extends CMClass> allClasses = applicationContext.getBean(DBDataView.class).findClasses();
		final Iterable<ClassDetail> details = from(allClasses) //
				.transform(TO_CLASS_DETAILS);
		return ClassDetailResponse.newInstance() //
				.withDetails(details) //
				.withTotal(size(details)) //
				.build();
	}

}
