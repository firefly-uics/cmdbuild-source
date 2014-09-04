package org.cmdbuild.servlets.json;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.logic.translation.DefaultTranslationLogic.DESCRIPTION_FOR_CLIENT;
import static org.cmdbuild.servlets.json.CommunicationConstants.DEFAULT_DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.fileupload.FileItem;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logic.translation.GisIconTranslation;
import org.cmdbuild.services.CustomFilesStore;
import org.cmdbuild.services.FilesStore;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Icon extends JSONBaseWithSpringContext {

	private static final String ps = File.separator;
	private static final String UPLOADED_FILE_RELATIVE_PATH = "images" + ps + "gis";
	private static final FilesStore iconsFileStore = new CustomFilesStore();

	@JSONExported
	public JSONObject list(final JSONObject serializer) throws JSONException, AuthException {

		final String[] iconsFileList = iconsFileStore.list(UPLOADED_FILE_RELATIVE_PATH);
		final JSONArray rows = new JSONArray();
		for (final String iconFileName : iconsFileList) {
			rows.put(toJSON(iconFileName));
		}
		serializer.put("rows", rows);
		return serializer;
	}

	@JSONExported
	@Admin
	public JSONObject upload(@Parameter(value = "file", required = true) final FileItem file,
			@Parameter(value = "description", required = true) final String fileName, final JSONObject serializer)
			throws ORMException, FileNotFoundException, IOException {

		final String relativePath = getRelativePath(fileName) + iconsFileStore.getExtension(file.getName());

		if (iconsFileStore.isImage(file)) {
			iconsFileStore.save(file, relativePath);
		} else {
			throw ORMExceptionType.ORM_ICONS_UNSUPPORTED_TYPE.createException();
		}

		return serializer;
	}

	@JSONExported
	@Admin
	public JSONObject update(@Parameter(value = "file", required = false) final FileItem file,
			@Parameter(value = "name", required = true) final String fileName,
			@Parameter(value = "description", required = true) final String newFileName, final JSONObject serializer)
			throws JSONException, AuthException, ORMException, IOException {

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
	public JSONObject remove(final JSONObject serializer, @Parameter("name") final String fileName)
			throws JSONException {

		iconsFileStore.remove(getRelativePath(fileName));
		return serializer;
	}

	private String getRelativePath(final String fileName) {
		return UPLOADED_FILE_RELATIVE_PATH + ps + fileName;
	}

	private JSONObject toJSON(final String iconFileName) throws JSONException {
		final JSONObject jsonIcon = new JSONObject();
		jsonIcon.put("name", iconFileName);

		final String description = iconsFileStore.removeExtension(iconFileName);

		final GisIconTranslation translationObject = GisIconTranslation.newInstance() //
				.withName(description) //
				.withField(DESCRIPTION_FOR_CLIENT) //
				.build();
		translationLogic().create(translationObject);
		final String translatedDescription = translationFacade().read(translationObject);

		jsonIcon.put(DESCRIPTION, defaultIfNull(translatedDescription, description));
		jsonIcon.put(DEFAULT_DESCRIPTION, description);
		final String path = iconsFileStore.getRelativeRootDirectory() + getRelativePath(iconFileName);
		jsonIcon.put("path", path.replace(File.separator, "/")); // because is
																	// used as
																	// URL
		return jsonIcon;
	}
}
