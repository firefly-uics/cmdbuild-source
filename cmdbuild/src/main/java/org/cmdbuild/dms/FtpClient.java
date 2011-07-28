package org.cmdbuild.dms;

import java.io.InputStream;

import javax.activation.DataHandler;

public interface FtpClient {

	boolean upload(String filename, InputStream is, String[] path);

	boolean delete(String filename, String[] path);

	DataHandler download(String filename, String[] path);

}
