package org.cmdbuild.common.template;

import static com.google.common.base.Suppliers.memoize;
import static com.google.common.base.Suppliers.synchronizedSupplier;

import com.google.common.base.Supplier;

public class IdentityTemplateResolver implements TemplateResolver {

	private static Supplier<IdentityTemplateResolver> instance = new Supplier<IdentityTemplateResolver>() {

		public IdentityTemplateResolver get() {
			return new IdentityTemplateResolver();
		};

	};

	private static Supplier<IdentityTemplateResolver> supplier = synchronizedSupplier(memoize(instance));

	public static IdentityTemplateResolver getInstance() {
		return supplier.get();
	}

	private IdentityTemplateResolver() {
		// use factory method
	}

	@Override
	public String simpleEval(final String template) {
		return template;
	}

}
