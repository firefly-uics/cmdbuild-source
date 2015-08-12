package org.cmdbuild.servlets.json.serializers.translations.table;

import java.io.IOException;

import javax.activation.DataHandler;

import org.cmdbuild.servlets.json.translationtable.objects.GenericTableEntry;

public interface TranslationSerializer {

	static final String FIELD = "field";
	static final String ELEMENT = "element";
	static final String DIRECTION = "direction";
	static final String PROCESS = "process";
	static final String CLASS = "class";
	static final String DOMAIN = "domain";
	static final String ATTRIBUTE = "attribute";

	Iterable<GenericTableEntry> serialize();

	DataHandler exportCsv() throws IOException;

}
