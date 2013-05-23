package org.cmdbuild.config;

import static org.apache.commons.lang.StringUtils.EMPTY;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class DefaultProperties extends Properties {

	private static final long serialVersionUID = -1L;

	private File file;

	public DefaultProperties(final Properties defaults) {
		super(defaults);
	}

	public DefaultProperties() {
		super();
	}

	public void load(final String file) throws IOException {
		this.file = new File(file);
		load(new FileInputStream(file));
	}

	public void store() throws IOException {
		store(new FileOutputStream(file), EMPTY);
	}

	public File getPath() {
		return file.getParentFile();
	}

	@Override
	public final synchronized Object setProperty(String key, String value) {
		return setProperty0(key, value);
	}

	protected Object setProperty0(String key, String value) {
		return super.setProperty(key, value);
	}

}
