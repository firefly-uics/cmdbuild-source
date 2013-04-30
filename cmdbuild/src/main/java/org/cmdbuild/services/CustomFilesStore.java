package org.cmdbuild.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.fileupload.FileItem;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.utils.PatternFilenameFilter;

public class CustomFilesStore {
	private static final String ps = File.separator;
	private String relativeRootDirectory = "upload"+ps;
	private String absoluteRootDirectory = Settings.getInstance().getRootPath()+relativeRootDirectory;
	
	private static final String[] ALLOWED_IMAGE_TYPES = {
		"image/png", "image/gif", "image/jpeg", "image/pjpeg", "image/x-png"
	};
	
	public String[] list(String dir) {
		return list(dir, null);
	}

	public String[] list(String dir, String pattern) {
		File directory = new File(absoluteRootDirectory + dir);
		if (directory.exists()) {
			FilenameFilter filenameFilter = PatternFilenameFilter.build(pattern);
			return directory.list(filenameFilter);
		} else {
			return new String[0];
		}
	}

	public void remove(String filePath) {
		File theFile = new File(absoluteRootDirectory + filePath);
		if (theFile.exists()) {
			theFile.delete();
		} else {
			throw ORMExceptionType.ORM_ICONS_FILE_NOT_FOUND.createException();
		}
	}

	public void rename(String filePath, String newFilePath) {
		File theFile = new File(absoluteRootDirectory + filePath);
		if (theFile.exists()) {
			String extension = getExtension(theFile.getName());
			if (!"".equals(extension)) {
				newFilePath = newFilePath + extension;
			}

			File newFile = newFile(absoluteRootDirectory + newFilePath);
			theFile.renameTo(newFile);
		} else {
			throw ORMExceptionType.ORM_ICONS_FILE_NOT_FOUND.createException();
		}
	}

	public void save(FileItem file, String filePath) throws IOException {
		save(file.getInputStream(), filePath);
	}

	public void save(InputStream inputStream, String filePath) throws IOException {
		String destinationPath = absoluteRootDirectory+filePath;
		FileOutputStream outputStream = null;
		try {
			File destinationFile = newFile(destinationPath);
			File dir = destinationFile.getParentFile();
			dir.mkdirs();
			
			outputStream = new FileOutputStream(destinationFile);
			byte[] buf = new byte[1024];
			int i = 0;
			while ((i = inputStream.read(buf)) != -1) {
				outputStream.write(buf, 0, i);
			}
		} catch (FileNotFoundException e) {
			throw ORMExceptionType.ORM_ICONS_FILE_NOT_FOUND.createException();
		} catch (IOException e) {
			throw e;
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
			if (outputStream != null) {
				outputStream.close();
			}
		}
	}

	private File newFile(String destinationPath) {
		File destinationFile = new File(destinationPath);
		
		if (destinationFile.exists()) {
			throw ORMExceptionType.ORM_ICONS_FILE_ALREADY_EXISTS.createException(destinationFile.getName());
		}
		return destinationFile;
	}
	
	public String getRelativeRootDirectory() {
		return this.relativeRootDirectory;
	}
	
	public File getFile(String path) {
		File file = new File(path);
		if (file.exists()) {
			return file;
		} else {
			throw ORMExceptionType.ORM_ICONS_FILE_NOT_FOUND.createException();
		}
	}
	
	public boolean isImage(FileItem file) {
		boolean valid = false;
		for (String type: ALLOWED_IMAGE_TYPES) {
			if (type.equalsIgnoreCase(file.getContentType())) {
				valid = true;
				break;
			}
		}
		return valid;
	}
	
	public String getExtension(String fileName) {
		String ext = "";
		int lastIndexOfPoint = fileName.lastIndexOf(".");
		if (lastIndexOfPoint >=0) {
			ext = fileName.substring(lastIndexOfPoint);
		}
		return ext;
	}
	
	public String removeExtension(String fileName) {
		String cleanedFileName = fileName;
		int lastIndexOfPoint = fileName.lastIndexOf(".");
		if (lastIndexOfPoint >=0) {
			cleanedFileName = fileName.substring(0,lastIndexOfPoint);
		}
		return cleanedFileName;
	}
	
}
