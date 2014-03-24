package org.cmdbuild.services.email;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.services.email.EmailTemplateResolver.Configuration;
import org.cmdbuild.services.email.EmailTemplateResolver.DataFacade;

/**
 * {@link DefaultEmailTemplateResolver} factory.
 */
public class DefaultEmailTemplateResolverFactory implements EmailTemplateResolverFactory {

	private static class DefaultConfiguration implements Configuration {

		private final DataFacade dataFacade;

		public DefaultConfiguration(final DataFacade dataFacade) {
			this.dataFacade = dataFacade;
		}

		@Override
		public DataFacade dataFacade() {
			return dataFacade;
		}

		@Override
		public String multiSeparator() {
			return null;
		}

	}

	private final DataFacade dataFacade;

	/**
	 * Creates a new {@link DefaultEmailTemplateResolverFactory} with the
	 * specified {@link DataFacade}.
	 * 
	 * @param dataFacade
	 */
	public DefaultEmailTemplateResolverFactory(final DataFacade dataFacade) {
		this.dataFacade = dataFacade;
	}

	@Override
	public EmailTemplateResolver create() {
		Validate.notNull(dataFacade, "null data facade");
		return new DefaultEmailTemplateResolver(new DefaultConfiguration(dataFacade));
	}
	
	@Override
	public EmailTemplateResolver create(Configuration configuration) {
		Validate.notNull(configuration, "null configuration");
		return new DefaultEmailTemplateResolver(configuration);
	}

}
