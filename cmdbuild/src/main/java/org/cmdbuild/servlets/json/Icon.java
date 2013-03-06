package org.cmdbuild.servlets.json;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.fileupload.FileItem;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.services.CustomFilesStore;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Icon extends JSONBase {

	private static final String ps = File.separator;
	private static final String UPLOADED_FILE_RELATIVE_PATH = "images" + ps + "gis";
	private static final CustomFilesStore iconsFileStore = new CustomFilesStore();

	@JSONExported
	public JSONObject list(JSONObject serializer) 
			throws JSONException, AuthException {

		String[] iconsFileList = iconsFileStore.list(UPLOADED_FILE_RELATIVE_PATH);
		JSONArray rows = new JSONArray();
		for (String iconFileName : iconsFileList) {
			rows.put(toJSON(iconFileName));
		}
		serializer.put("rows", rows);
		return serializer;
	}

	@JSONExported
	@Admin
	public JSONObject upload(
			@Parameter(value = "file", required = true) FileItem file,
			@Parameter(value = "description", required = true) String fileName,
			JSONObject serializer) throws ORMException, FileNotFoundException, IOException {

		String relativePath = getRelativePath(fileName) + iconsFileStore.getExtension(file.getName());

		if (iconsFileStore.isImage(file)) {
			iconsFileStore.save(file, relativePath);
		} else {
			throw ORMExceptionType.ORM_ICONS_UNSUPPORTED_TYPE.createException();
		}

		return serializer;
	}

	@JSONExported
	@Admin
	public JSONObject update(
			@Parameter(value = "file", required = false) FileItem file,
			@Parameter(value = "name", required = true) String fileName,
			@Parameter(value = "description", required = true) String newFileName,
			JSONObject serializer) throws JSONException, AuthException, ORMException, IOException {

		if (!"".equals(file.getName())) { // replace the file
			if (iconsFileStore.isImage(file)) {
				iconsFileStore.remove(getRelativePath(fileName));
				iconsFileStore.save(file, getRelativePath(newFileName));
			} else {
				throw ORMExceptionType.ORM_ICONS_UNSUPPORTED_TYPE.createException();
			}
		} else { // rename the existing file
			iconsFileStore.rename(getRelativePath(fileName), getRelativePath(newFileName));
		}

		return serializer;
	}

	@JSONExported
	@Admin
	public JSONObject remove(JSONObject serializer,
			@Parameter("name") String fileName) throws JSONException {

		iconsFileStore.remove(getRelativePath(fileName));
		return serializer;
	}

	private String getRelativePath(String fileName) {
		return UPLOADED_FILE_RELATIVE_PATH + ps + fileName;
	}

	private JSONObject toJSON(String iconFileName) throws JSONException {
		JSONObject jsonIcon = new JSONObject();
		jsonIcon.put("name", iconFileName);
		jsonIcon.put("description", iconsFileStore.removeExtension(iconFileName));
		String path = iconsFileStore.getRelativeRootDirectory() + getRelativePath(iconFileName);
		jsonIcon.put("path", path.replace(File.separator, "/")); // because is used as URL
		return jsonIcon;
	}
}