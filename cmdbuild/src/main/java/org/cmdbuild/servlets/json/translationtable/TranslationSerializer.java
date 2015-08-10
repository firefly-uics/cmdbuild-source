package org.cmdbuild.servlets.json.translationtable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;

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
	
	DataHandler serializeCsv() throws IOException;
	
}
