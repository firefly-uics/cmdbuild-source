package org.cmdbuild.config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class DefaultProperties extends Properties {

	private static final long serialVersionUID = -1L;
	
	private String file;
	
	public DefaultProperties(Properties defaults) {
		super(defaults);
	}
	
	public DefaultProperties() {
		super();
	}
	
	public void load(String file) throws IOException {
		load(new FileInputStream(file));
		this.file = file;
	}
	
	public void store() throws IOException {
		store(new FileOutputStream(file), "");
	}
}