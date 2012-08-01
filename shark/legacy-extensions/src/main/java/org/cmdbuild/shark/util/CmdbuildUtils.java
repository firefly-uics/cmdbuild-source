package org.cmdbuild.shark.util;

import org.cmdbuild.workflow.ConfigurationHelper;
import org.cmdbuild.workflow.api.SchemaApi;
import org.cmdbuild.workflow.api.SchemaApi.ClassInfo;
import org.enhydra.shark.api.internal.working.CallbackUtilities;
import org.enhydra.shark.toolagent.BshToolAgent;

/**
 * 
 * Needed only for supporting the toolagent "createReferenceObj" (interpreted by
 * {@link BshToolAgent}).
 * 
 */
public class CmdbuildUtils {

	public interface CmdbuildTableStruct {

		int getId();

	}

	private static CmdbuildUtils instance = new CmdbuildUtils();

	public static CmdbuildUtils getInstance() {
		return instance;
	}

	private ConfigurationHelper configurationHelper;
	private SchemaApi schemaApi;

	private CmdbuildUtils() {
	}

	public void configure(final CallbackUtilities cus) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		configurationHelper = new ConfigurationHelper(cus);
		// TODO SchemaApi will be replaced by FluentApi
		schemaApi = configurationHelper.newSharkWorkflowApi().schemaApi();
	}

	public CmdbuildTableStruct getStructureFromName(final String name) throws Exception {
		final SchemaApi.ClassInfo classInfo = schemaApi.findClass(name);
		return tableStructFrom(classInfo);
	}

	private CmdbuildTableStruct tableStructFrom(final ClassInfo classInfo) {
		return new CmdbuildTableStruct() {
			@Override
			public int getId() {
				return classInfo.getId();
			}
		};
	}

}
