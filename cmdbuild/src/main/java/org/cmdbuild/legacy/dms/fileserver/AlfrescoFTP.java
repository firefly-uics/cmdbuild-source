package org.cmdbuild.legacy.dms.fileserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.cmdbuild.config.LegacydmsProperties;
import org.cmdbuild.exception.AuthException.AuthExceptionType;
import org.cmdbuild.legacy.dms.AbstractAlfrescoFileServer;
import org.cmdbuild.legacy.dms.AlfrescoCredential;
import org.cmdbuild.logger.Log;
import org.cmdbuild.utils.TempDataSource;

@SuppressWarnings("restriction")
public class AlfrescoFTP extends AbstractAlfrescoFileServer {

	AlfrescoCredential credential;
	String url;
	String basePath;
	int port;
	boolean error = false;

	public AlfrescoFTP() {
		super();
	}

	@Override
	protected synchronized boolean init(LegacydmsProperties properties, AlfrescoCredential credential) {
		url = properties.getFtpHost();
		basePath = properties.getRepositoryFSPath();
		port = Integer.parseInt(properties.getFtpPort());
		this.credential = credential;
		return true;
	}

	@Override
	protected synchronized boolean delete(String filename, String[] path) {
		boolean ok = false;
		FTPClient ftp = new FTPClient();
		try {
			int reply;
			ftp.connect(url, port);
			reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				return false;
			}
			if (!ftp.login(credential.getUser(), credential.getPassword())) {
				throw AuthExceptionType.AUTH_LOGIN_WRONG.createException();
			}

			if (!ftp.changeWorkingDirectory(basePath)) {
				Log.DMS.info("FTP server refused to chg dir.1", new Exception());
				throw new Exception();
			}

			for (String dir : path) {
				if (!ftp.changeWorkingDirectory(dir)) {
					Log.DMS.info("Cannot go into dir : " + dir + ", create");
					if (!ftp.makeDirectory(dir)) {
						Log.DMS.info("Cannot create dir : " + dir);
						throw new Exception("Cannot create dir : " + dir);
					} else if (!ftp.changeWorkingDirectory(dir)) {
						Log.DMS.info("FTP server refused to change dir : " + dir);
						throw new Exception("FTP server refused to chg dir : " + dir);
					}
				}
			}

			ok = ftp.deleteFile(filename);
		} catch (Exception e) {
			ok = true;
			e.printStackTrace();
		} finally {
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (IOException ioe) {
					// do nothing
				}
			}
		}
		return ok;
	}

	@Override
	protected DataHandler download(String filename, String[] path) {

		FTPClient ftp = new FTPClient();
		try {
			int reply;
			ftp.connect(url, port);
			reply = ftp.getReplyCode();

			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				Log.DMS.info("FTP server refused connection.");
				return null;
			}

			if (!ftp.login(credential.getUser(), credential.getPassword())) {
				Log.DMS.info("FTP server refused login.", AuthExceptionType.AUTH_LOGIN_WRONG.createException());
			}

			if (!ftp.changeWorkingDirectory(basePath)) {
				Log.DMS.info("FTP server refused to change dir.1");
				throw new Exception("FTP server refused to chg dir.1");
			}

			for (String dir : path) {
				if (!ftp.changeWorkingDirectory(dir)) {
					Log.DMS.info("Cannot go into dir : " + dir + ", create");
					if (!ftp.makeDirectory(dir)) {
						Log.DMS.info("Cannot create dir : " + dir);
						throw new Exception("Cannot create dir : " + dir);
					} else if (!ftp.changeWorkingDirectory(dir)) {
						Log.DMS.info("FTP server refused to chg dir : " + dir);
						throw new Exception("FTP server refused to chg dir : " + dir);
					}
				}
			}

			/*
			 * this could be ugly: download the file from the ftp then return
			 * the inputstream, based on the retrieved file. i'd like to use the
			 * ftp.retrieveStreamFile, but i don't see an easy way to return the
			 * inputstream, return it to the user via a download action, and
			 * then close the stream and the ftp connection.
			 */

			ftp.setFileType(FTPClient.BINARY_FILE_TYPE);

			/*
			 * WHAT CAN BE DONE:: create a tmp file in local filesystem, the
			 * return the inputstream from THAT file.
			 */

			DataSource dataSource = TempDataSource.create(filename);
			OutputStream os = dataSource.getOutputStream();
			if (ftp.retrieveFile(filename, os)) {
				os.flush();
				os.close();
				DataHandler attachment = new DataHandler(dataSource);
				return attachment;
			}

			return null;

		} catch (Exception e) {
			error = true;
			e.printStackTrace();
		} finally {
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (IOException ioe) {
					// do nothing
				}
			}
		}
		return null;
	}

	@Override
	protected boolean upload(String filename, InputStream is, String[] path) {
		boolean error = false;
		FTPClient ftp = new FTPClient();
		try {
			int reply;
			ftp.connect(url, port);

			// After connection attempt, you should check the reply code to
			// verify success.
			reply = ftp.getReplyCode();

			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				Log.DMS.info("FTP server refused connection.");
				return false;
			}

			if (!ftp.login(credential.getUser(), credential.getPassword())) {
				Log.DMS.info("FTP Server refused login.", AuthExceptionType.AUTH_LOGIN_WRONG.createException());
			}

			if (!ftp.changeWorkingDirectory(basePath)) {
				Log.DMS.info("FTP server refused to chg dir.1");
				throw new Exception("FTP server refused to chg dir.1");
			}

			for (String dir : path) {
				if (!ftp.changeWorkingDirectory(dir)) {
					Log.DMS.info("Cannot go into dir : " + dir + ", create");
					if (!ftp.makeDirectory(dir)) {
						Log.DMS.info("Cannot create dir : " + dir);
						throw new Exception("Cannot create dir : " + dir);
					} else if (!ftp.changeWorkingDirectory(dir)) {
						Log.DMS.info("FTP server refused to chg dir : " + dir);
						throw new Exception("FTP server refused to chg dir : " + dir);
					}
				}
			}

			ftp.setFileType(FTPClient.IMAGE_FILE_TYPE);

			if (!ftp.storeFile(filename, is)) {
				if (is != null)
					try {
						is.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				throw new Exception("FTP failed to upload.");
			}
			// transfer files
			ftp.logout();
		} catch (Exception e) {
			error = true;
			e.printStackTrace();
			Log.DMS.info("Exception while uploading a file", e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (Exception e) {
				}
			}
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (IOException ioe) {
					// do nothing
				}
			}

		}

		return !error;
	}
}
