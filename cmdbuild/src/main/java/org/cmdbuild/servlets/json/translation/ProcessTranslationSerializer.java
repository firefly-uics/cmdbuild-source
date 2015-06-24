package org.cmdbuild.servlets.json.translation;

import static com.google.common.collect.FluentIterable.from;

import org.cmdbuild.common.Constants;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.servlets.json.management.JsonResponse;

import com.google.common.base.Predicate;

public class ProcessTranslationSerializer extends ClassTranslationSerializer {

	public ProcessTranslationSerializer(final DataAccessLogic dataLogic, final boolean activeOnly,
			final TranslationLogic translationLogic) {
		super(dataLogic, activeOnly, translationLogic);
	}

	@Override
	public JsonResponse serialize() {
		final Iterable<? extends CMClass> allClasses = dataLogic.findAllClasses();
		final Iterable<? extends CMClass> onlyProcessess = from(allClasses).filter(new Predicate<CMClass>() {

			@Override
			public boolean apply(final CMClass input) {
				final CMClass processBaseClass = dataLogic.findClass(Constants.BASE_PROCESS_CLASS_NAME);
				return processBaseClass.isAncestorOf(input);
			}
		});
		return readStructure(onlyProcessess);
	}

}
