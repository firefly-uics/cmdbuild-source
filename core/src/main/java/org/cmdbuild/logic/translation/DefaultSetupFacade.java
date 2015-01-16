package org.cmdbuild.logic.translation;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.logic.setup.SetupLogic;

import com.google.common.collect.Lists;

public class DefaultSetupFacade implements SetupFacade {

	private static final String SEPARATOR = ",";
	private static final String MODULE_NAME = "cmdbuild";
	private static final Object ENABLED_LANGUAGES = "enabled_languages";

	private final SetupLogic setupLogic;

	public DefaultSetupFacade(final SetupLogic setupLogic) {
		this.setupLogic = setupLogic;
	}

	@Override
	public Iterable<String> getEnabledLanguages() {
		Map<String, String> config;
		String[] enabledLanguagesArray = null;
		String enabledLanguagesConfiguration = EMPTY;
		try {
			config = setupLogic.load(MODULE_NAME);
			enabledLanguagesConfiguration = config.get(ENABLED_LANGUAGES);
			enabledLanguagesConfiguration = enabledLanguagesConfiguration.replaceAll("\\s", "");
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (isNotBlank(enabledLanguagesConfiguration)) {
			enabledLanguagesArray = enabledLanguagesConfiguration.split(SEPARATOR);
		}
		Iterable<String> enabledLanguages = Lists.newArrayList();
		if (enabledLanguagesArray != null) {
			enabledLanguages = Arrays.asList(enabledLanguagesArray);
		}
		return enabledLanguages;
	}

}
