package org.cmdbuild.plugins;

import javax.servlet.ServletContext;

import org.cmdbuild.logger.Log;
import org.cmdbuild.plugins.CMDBInitListener.CmdbuildModuleLoader;
import org.cmdbuild.servlets.utils.MethodParameterResolver;
import org.cmdbuild.servlets.utils.ParameterBuilder;
import org.cmdbuild.servlets.utils.ParameterTransformer;
import org.cmdbuild.servlets.utils.Transformer;

public class ParameterTransformerInit implements CmdbuildModuleLoader {

	private static Transformer<?>[] TRANSFORMERS = {
		new org.cmdbuild.servlets.utils.transformer.FileItemTransformer(),
		new org.cmdbuild.servlets.utils.transformer.JSONObjectTransformer(),
		new org.cmdbuild.servlets.utils.transformer.JSONArrayTransformer()
	};

	private static ParameterBuilder<?>[] BUILDERS = {};

	@SuppressWarnings("unchecked")
	public void init(ServletContext ctxt) throws Exception {
		Log.OTHER.info("Initializing ParameterTransformers");
		for (Transformer transformer : TRANSFORMERS) {
			try {
				ParameterTransformer.getInstance().addTransformer(transformer);
				Log.OTHER.info("Transformer for " + transformer.getTransformedClass().getName() + ": " + transformer.getClass().getName());
			} catch (Exception e) {
				Log.OTHER.error("Cannot load ParameterTransformer " + transformer.getClass().getCanonicalName());
			}
		}

		Log.OTHER.info("Initializer custom ParameterBuilders");
		for (ParameterBuilder<?> builder : BUILDERS) {
			try {
				MethodParameterResolver.getInstance().putAutoloadParameter(builder);
				Log.OTHER.info("Builder for " + builder.getBindedClass().getName() + ": " + builder.getClass().getName());
			} catch (Exception e) {
				Log.OTHER.error("Cannot load ParameterBuilder " + builder.getClass().getCanonicalName());
			}
		}
	}
}
