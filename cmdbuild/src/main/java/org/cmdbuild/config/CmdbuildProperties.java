package org.cmdbuild.config;

import org.cmdbuild.services.Settings;

public class CmdbuildProperties extends DefaultProperties {

	private static final long serialVersionUID = 1L;
	
	private static final String MODULE_NAME = "cmdbuild";
	
	private static final String REFERENCE_COMBO_LIMIT = "referencecombolimit";
	private static final String STARTING_CLASS = "startingclass";
	private static final String RELATION_LIMIT = "relationlimit";
	private static final String LANGUAGE = "language";
	private static final String POPUP_PERCENTAGE_HEIGHT = "popuppercentageheight";
	private static final String POPUP_PERCENTAGE_WIDTH = "popuppercentagewidth";
	private static final String GRID_CARD_RATIO = "grid_card_ratio";
	private static final String ROW_LIMIT = "rowlimit";
	private static final String LANGUAGE_PROMPT = "languageprompt";
	private static final String SESSION_TIMEOUT = "session.timeout";
	private static final String INSTANCE_NAME = "instance_name";

	private static final String DEMO_MODE_ADMIN = "demomode";
 	
	public CmdbuildProperties () {
		super();
		setProperty(REFERENCE_COMBO_LIMIT, "500");
		setProperty(STARTING_CLASS, "");
		setProperty(RELATION_LIMIT, "20");
		setProperty(LANGUAGE,"it");
		setProperty(POPUP_PERCENTAGE_HEIGHT, "80");
		setProperty(POPUP_PERCENTAGE_WIDTH, "80");
		setProperty(GRID_CARD_RATIO, "50");
		setProperty(ROW_LIMIT, "20");
		setProperty(LANGUAGE_PROMPT, String.valueOf(true));
		setProperty(SESSION_TIMEOUT, "");
		setProperty(INSTANCE_NAME, "");
	}
	
	public static CmdbuildProperties getInstance() {
		return (CmdbuildProperties)Settings.getInstance().getModule(MODULE_NAME);
	}

	public String getLanguage(){
		return getProperty(LANGUAGE);
	}

	public void setLanguage(String language){
		setProperty(LANGUAGE, language);
	}

	public boolean useLanguagePrompt(){
		return Boolean.parseBoolean(getProperty(LANGUAGE_PROMPT));
	}

	public void setLanguagePrompt(boolean languagePrompt){		
		setProperty(LANGUAGE_PROMPT, String.valueOf(languagePrompt));
	}

	public String getStartingClassName(){
		return getProperty(STARTING_CLASS);
	}

	public void setStartingClass(String startingClass){
		setProperty(STARTING_CLASS, startingClass);
	}

	public String getDemoModeAdmin(){
		return getProperty(DEMO_MODE_ADMIN, "");
	}
	
	public void setInstanceName(String instanceName){
		setProperty(INSTANCE_NAME, instanceName);
	}

	public String getInstanceName(){
		return getProperty(INSTANCE_NAME, "");
	}	
	
	public int getSessionTimoutOrZero() {
		try {
			return Integer.parseInt(getProperty(SESSION_TIMEOUT));
		} catch (Exception e) {
			return 0;
		}
	}
}
