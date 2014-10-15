package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.size;
import static org.cmdbuild.service.rest.model.Builders.newMetadata;
import static org.cmdbuild.service.rest.model.Builders.newResponseMultiple;
import static org.cmdbuild.service.rest.model.Builders.newResponseSingle;

import java.util.Comparator;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.service.rest.Classes;
import org.cmdbuild.service.rest.cxf.serialization.ToFullClassDetail;
import org.cmdbuild.service.rest.cxf.serialization.ToSimpleClassDetail;
import org.cmdbuild.service.rest.model.ClassWithBasicDetails;
import org.cmdbuild.service.rest.model.ClassWithFullDetails;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.model.ResponseSingle;

import com.google.common.collect.Ordering;

public class CxfClasses implements Classes {

	private static final ToFullClassDetail TO_FULL_CLASS_DETAIL = ToFullClassDetail.newInstance().build();
	private static final ToSimpleClassDetail TO_SIMPLE_CLASS_DETAIL = ToSimpleClassDetail.newInstance().build();

	private static final Comparator<CMClass> NAME_ASC = new Comparator<CMClass>() {

		@Override
		public int compare(final CMClass o1, final CMClass o2) {
			return o1.getName().compareTo(o2.getName());
		}

	};

	private final ErrorHandler errorHandler;
	private final DataAccessLogic userDataAccessLogic;

	public CxfClasses(final ErrorHandler errorHandler, final DataAccessLogic userDataAccessLogic) {
		this.errorHandler = errorHandler;
		this.userDataAccessLogic = userDataAccessLogic;
	}

	@Override
	public ResponseMultiple<ClassWithBasicDetails> readAll(final boolean activeOnly, final Integer limit,
			final Integer offset) {
		// FIXME do all the following it within the same logic
		// <<<<<
		final Iterable<? extends CMClass> allClasses = userDataAccessLogic.findClasses(activeOnly);
		final Iterable<? extends CMClass> ordered = Ordering.from(NAME_ASC) //
				.sortedCopy(allClasses);
		final Iterable<ClassWithBasicDetails> elements = from(ordered) //
				.skip((offset == null) ? 0 : offset) //
				.limit((limit == null) ? Integer.MAX_VALUE : limit) //
				.transform(TO_SIMPLE_CLASS_DETAIL);
		// <<<<<
		return newResponseMultiple(ClassWithBasicDetails.class) //
				.withElements(elements) //
				.withMetadata(newMetadata() //
						.withTotal(Long.valueOf(size(ordered))) //
						.build()) //
				.build();
	}

	@Override
	public ResponseSingle<ClassWithFullDetails> read(final String classId) {
		final CMClass found = userDataAccessLogic.findClass(classId);
		if (found == null) {
			errorHandler.classNotFound(classId);
		}
		final ClassWithFullDetails element = TO_FULL_CLASS_DETAIL.apply(found);
		return newResponseSingle(ClassWithFullDetails.class) //
				.withElement(element) //
				.build();
	}

}
