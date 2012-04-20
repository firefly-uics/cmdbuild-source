package org.cmdbuild.dms.alfresco.ftp;

import java.io.InputStream;
import java.util.List;

import javax.activation.DataHandler;

import org.cmdbuild.dms.exception.ConnectionException;
import org.cmdbuild.dms.exception.FtpOperationException;
import org.cmdbuild.dms.exception.InvalidLoginException;

interface FtpClient {

	void upload(String filename, InputStream is, List<String> path) throws ConnectionException, InvalidLoginException,
			FtpOperationException;

	void delete(String filename, List<String> path) throws ConnectionException, InvalidLoginException,
			FtpOperationException;

	DataHandler download(String filename, List<String> path) throws ConnectionException, InvalidLoginException,
			FtpOperationException;

}
