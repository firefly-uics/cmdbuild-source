package org.cmdbuild.dms.alfresco;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.cmdbuild.config.LegacydmsProperties;
import org.cmdbuild.dms.FtpClient;
import org.cmdbuild.exception.AuthException.AuthExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.utils.TempDataSource;

public class AlfrescoFtpClient implements FtpClient {

	private final LegacydmsProperties properties;
	boolean error = false;

	public AlfrescoFtpClient(final LegacydmsProperties properties) {
		this.properties = properties;
	}

	public synchronized boolean delete(final String filename, final String[] path) {
		boolean ok = false;
		final FTPClient ftp = new FTPClient();
		try {
			int reply;
			ftp.connect(properties.getFtpHost(), Integer.parseInt(properties.getFtpPort()));
			reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				return false;
			}
			final String username = properties.getAlfrescoUser();
			final String password = properties.getAlfrescoPassword();
			if (!ftp.login(username, password)) {
				throw AuthExceptionType.AUTH_LOGIN_WRONG.createException();
			}

			if (!ftp.changeWorkingDirectory(properties.getRepositoryFSPath())) {
				Log.DMS.info("FTP server refused to chg dir.1", new Exception());
				throw new Exception();
			}

			for (final String dir : path) {
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
		} catch (final Exception e) {
			ok = true;
			e.printStackTrace();
		} finally {
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (final IOException ioe) {
					// do nothing
				}
			}
		}
		return ok;
	}

	public DataHandler download(final String filename, final String[] path) {

		final FTPClient ftp = new FTPClient();
		try {
			int reply;
			ftp.connect(properties.getFtpHost(), Integer.parseInt(properties.getFtpPort()));
			reply = ftp.getReplyCode();

			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				Log.DMS.info("FTP server refused connection.");
				return null;
			}

			final String username = properties.getAlfrescoUser();
			final String password = properties.getAlfrescoPassword();
			if (!ftp.login(username, password)) {
				Log.DMS.info("FTP server refused login.", AuthExceptionType.AUTH_LOGIN_WRONG.createException());
			}

			if (!ftp.changeWorkingDirectory(properties.getRepositoryFSPath())) {
				Log.DMS.info("FTP server refused to change dir.1");
				throw new Exception("FTP server refused to chg dir.1");
			}

			for (final String dir : path) {
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

			final DataSource dataSource = TempDataSource.create(filename);
			final OutputStream os = dataSource.getOutputStream();
			if (ftp.retrieveFile(filename, os)) {
				os.flush();
				os.close();
				final DataHandler attachment = new DataHandler(dataSource);
				return attachment;
			}

			return null;

		} catch (final Exception e) {
			error = true;
			e.printStackTrace();
		} finally {
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (final IOException ioe) {
					// do nothing
				}
			}
		}
		return null;
	}

	public boolean upload(final String filename, final InputStream is, final String[] path) {
		boolean error = false;
		final FTPClient ftp = new FTPClient();
		try {
			int reply;
			ftp.connect(properties.getFtpHost(), Integer.parseInt(properties.getFtpPort()));

			// After connection attempt, you should check the reply code to
			// verify success.
			reply = ftp.getReplyCode();

			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				Log.DMS.info("FTP server refused connection.");
				return false;
			}

			final String username = properties.getAlfrescoUser();
			final String password = properties.getAlfrescoPassword();
			if (!ftp.login(username, password)) {
				Log.DMS.info("FTP Server refused login.", AuthExceptionType.AUTH_LOGIN_WRONG.createException());
			}

			if (!ftp.changeWorkingDirectory(properties.getRepositoryFSPath())) {
				Log.DMS.info("FTP server refused to chg dir.1");
				throw new Exception("FTP server refused to chg dir.1");
			}

			for (final String dir : path) {
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
					} catch (final Exception e) {
						e.printStackTrace();
					}
				throw new Exception("FTP failed to upload.");
			}
			// transfer files
			ftp.logout();
		} catch (final Exception e) {
			error = true;
			e.printStackTrace();
			Log.DMS.info("Exception while uploading a file", e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (final Exception e) {
				}
			}
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (final IOException ioe) {
					// do nothing
				}
			}

		}

		return !error;
	}
}
