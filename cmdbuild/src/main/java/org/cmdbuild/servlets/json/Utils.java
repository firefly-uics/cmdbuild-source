package org.cmdbuild.servlets.json;

import java.lang.reflect.Constructor;
import java.util.Map;

import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.services.CacheManager;
import org.cmdbuild.services.DBTemplateService;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.services.TranslationService;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONException;
import org.json.JSONObject;

public class Utils extends JSONBase {

	@JSONExported
	@Unauthorized
	public String getTranslationObject() {
		String lang = new SessionVars().getLanguage();
		String transFile = TranslationService.getInstance()
							.getTranslationObject(lang)
							.toString();
		return "CMDBuild.Translation = " + transFile;
	}

	@JSONExported
	@Unauthorized
	public JSONObject listAvailableTranslations(
			JSONObject serializer ) throws JSONException {

		Map<String, String> trs = TranslationService.getInstance().getTranslationList();

		for(String lang : trs.keySet()) {
			JSONObject j = new JSONObject();
    		j.put("name", lang);
    		j.put("value", trs.get(lang));
    		serializer.append("translations", j);
		}

		return serializer;
	}

	@JSONExported
	@Unauthorized
	public void success() throws JSONException {
	}

	/**
	 * @param exceptionType
	 * @param exceptionCodeString
	 */
	@JSONExported
	@Unauthorized
	public void failure(
			@Parameter("type") String exceptionType,
			@Parameter(value="code", required=false) String exceptionCodeString
			) {
		try {
			Class<? extends CMDBException> classDefinition =
			    Class.forName("org.cmdbuild.exception."+exceptionType).asSubclass(CMDBException.class);
			if (exceptionCodeString == null) {
				Constructor<? extends CMDBException> constructorDefinition = classDefinition.getDeclaredConstructor();
				throw constructorDefinition.newInstance();
			} else {
				for (Class<?> subClass : classDefinition.getClasses()) {
					if (subClass.isEnum()) { 
						for (Object enumConst : subClass.getEnumConstants()) {
						    if (exceptionCodeString.equals(enumConst.toString())) {
						    	Constructor<? extends CMDBException> constructorDefinition = classDefinition.getDeclaredConstructor(enumConst.getClass());	
						    	throw constructorDefinition.newInstance(enumConst);
						    }
						}
					}
				}
			}
		} catch (CMDBException ex) {
			throw ex;
		} catch (Exception ex) {
			// Returns success if no error can be instantiated
		}
	}

	@JSONExported
	@Admin
	public void clearCache() {
		new CacheManager().clearAllCaches();
		new DBTemplateService().reload();
	}
}
