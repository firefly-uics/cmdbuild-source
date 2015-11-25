package org.cmdbuild.servlets.json.serializers.translations.csv;

import static org.cmdbuild.servlets.json.serializers.translations.commons.Constants.NO_OWNER;

import java.util.Collection;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.servlets.json.schema.TranslatableElement;
import org.cmdbuild.servlets.json.translationtable.objects.csv.CsvTranslationRecord;

public class ClassSerializer extends DefaultElementSerializer {

	private final CMClass aClass;

	public static Builder newInstance() {
		return new Builder();
	}

	@Override
	public Collection<? extends CsvTranslationRecord> serialize() {
		final String className = aClass.getName();
		final TranslatableElement element = TranslatableElement.CLASS;
		return serializeFields(NO_OWNER, className, element);
	}

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ClassSerializer> {

		private DataAccessLogic dataLogic;
		private Iterable<String> enabledLanguages;
		private TranslationLogic translationLogic;
		public CMClass theClass;

		@Override
		public ClassSerializer build() {
			return new ClassSerializer(this);
		}

		public Builder withClass(final CMClass theClass) {
			this.theClass = theClass;
			return this;
		}

		public Builder withDataAccessLogic(DataAccessLogic dataLogic) {
			this.dataLogic = dataLogic;
			return this;
		}

		public Builder withEnabledLanguages(final Iterable<String> enabledLanguages) {
			this.enabledLanguages = enabledLanguages;
			return this;
		}

		public Builder withTranslationLogic(final TranslationLogic translationLogic) {
			this.translationLogic = translationLogic;
			return this;
		}

	}

	private ClassSerializer(final Builder builder) {
		super.dataLogic = builder.dataLogic;
		super.enabledLanguages = builder.enabledLanguages;
		super.translationLogic = builder.translationLogic;
		this.aClass = builder.theClass;
	}

}
