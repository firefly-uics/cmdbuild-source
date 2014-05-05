package org.cmdbuild.logic.translation;

import java.util.Map;

public interface EnabledLanguagesLogic {

	Map<String, String> read();

	void write(final Map<String, String> requestParams);

}