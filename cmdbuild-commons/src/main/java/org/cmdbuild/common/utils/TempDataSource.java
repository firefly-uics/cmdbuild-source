package org.cmdbuild.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;
import javax.activation.MimetypesFileTypeMap;

@SuppressWarnings("restriction")
public class TempDataSource implements DataSource {

	File file;
	String name;
	String contentType;

	public static DataSource create(final String name, final String contentType) throws IOException {
		return new TempDataSource(name, contentType);
	}

	public static DataSource create(final String name) throws IOException {
		return new TempDataSource(name, null);
	}

	private TempDataSource(final String name, final String contentType) throws IOException {
		this.name = name;
		this.contentType = contentType;
		this.file = File.createTempFile("tempdatasource", name);
		file.deleteOnExit();
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			file.delete();
		} finally {
			super.finalize();
		}
	}

	public String getName() {
		return name;
	}

	public String getContentType() {
		if (contentType == null) {
			contentType = new MimetypesFileTypeMap().getContentType(file);
		}
		return contentType;
	}

	public InputStream getInputStream() throws IOException {
		return new FileInputStream(file);
	}

	public OutputStream getOutputStream() throws IOException {
		return new FileOutputStream(file);
	}
}
