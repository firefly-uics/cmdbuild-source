package org.cmdbuild.logic.email;

import org.cmdbuild.common.template.TemplateResolver;
import org.cmdbuild.data.store.email.EmailAccount;
import org.cmdbuild.logic.Action;
import org.cmdbuild.logic.email.EmailTemplateLogic.Template;

import com.google.common.base.Supplier;

public interface EmailTemplateSenderFactory {

	interface Builder extends org.apache.commons.lang3.builder.Builder<EmailTemplateSender> {

		Builder withEmailAccountSupplier(Supplier<EmailAccount> emailAccountSupplier);

		Builder withEmailTemplateSupplier(Supplier<Template> emailTemplateSupplier);

		Builder withTemplateResolver(TemplateResolver templateResolver);

		Builder withReference(Long reference);

	}

	interface EmailTemplateSender extends Action {

	}

	Builder direct();

	Builder queued();

}
