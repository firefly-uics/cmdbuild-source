package org.cmdbuild.dms.cmis;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

import org.apache.chemistry.opencmis.commons.data.ContentStream;

public class CmisDataSource implements DataSource {
	
	private ContentStream contentStream;

	public CmisDataSource(ContentStream contentStream) {
		this.contentStream = contentStream;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return contentStream.getStream();
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return null;
	}

	@Override
	public String getContentType() {
		return contentStream.getMimeType();
	}

	@Override
	public String getName() {
		return contentStream.getFileName();
	}

}
