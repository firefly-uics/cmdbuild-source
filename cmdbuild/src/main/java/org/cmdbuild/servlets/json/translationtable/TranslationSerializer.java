package org.cmdbuild.servlets.json.translationtable;

import org.cmdbuild.servlets.json.management.JsonResponse;

public interface TranslationSerializer {

	static final String FIELD = "field";
	static final String ELEMENT = "element";
	static final String DIRECTION = "direction";
	static final String PROCESS = "process";
	static final String CLASS = "class";
	static final String DOMAIN = "domain";
	static final String ATTRIBUTE = "attribute";

	JsonResponse serialize();

}
