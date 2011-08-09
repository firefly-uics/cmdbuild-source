package org.cmdbuild.plugins;

import java.util.Map.Entry;

import javax.servlet.ServletContext;

import org.cmdbuild.config.DefaultProperties;
import org.cmdbuild.logger.Log;
import org.cmdbuild.plugins.CMDBInitListener.CmdbuildModuleLoader;
import org.cmdbuild.services.Settings;
import org.cmdbuild.servlets.utils.MethodParameterResolver;
import org.cmdbuild.servlets.utils.ParameterBuilder;
import org.cmdbuild.servlets.utils.ParameterTransformer;
import org.cmdbuild.servlets.utils.Transformer;

public class ParameterTransformerInit implements CmdbuildModuleLoader {

	@SuppressWarnings("unchecked")
	public void init(ServletContext ctxt) throws Exception {
		DefaultProperties props = Settings.getInstance().getModule("transformers");
		Log.OTHER.info("Initialize custom ParameterTransformers");
		for(Entry entry : props.entrySet()) {
			String targetClass = (String)entry.getKey();
			String transformerClass = (String)entry.getValue();
			Log.OTHER.info("Transformer for: " + targetClass + " : " + transformerClass );
			try{
				ParameterTransformer.getInstance().addTransformer( 
						(Transformer)Class.forName(transformerClass).newInstance() );
			} catch(Exception e){
				e.printStackTrace();
				Log.OTHER.error("Cannot load ParameterTransformer for '" + targetClass + "' ('" + transformerClass + "')");
			}
		}
		
		props = Settings.getInstance().getModule("builders");
		Log.OTHER.info("Initializer custom ParameterBuilders");
		for(Entry entry : props.entrySet()) {
			String targetClass = (String)entry.getKey();
			String builderClass = (String)entry.getValue();
			Log.OTHER.info("Builder for: " + targetClass + ": " + builderClass);
			try{
				MethodParameterResolver.getInstance().putAutoloadParameter(
						(ParameterBuilder)Class.forName(builderClass).newInstance());
			} catch(Exception e){
				e.printStackTrace();
				Log.OTHER.error("Cannot load ParameterBuilder for '" + targetClass + "' ('" + builderClass + "')");
			}
		}
	}

}
