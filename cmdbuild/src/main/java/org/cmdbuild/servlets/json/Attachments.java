package org.cmdbuild.servlets.json;

import static com.google.common.collect.Collections2.filter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.activation.DataHandler;

import org.apache.commons.fileupload.FileItem;
import org.cmdbuild.dms.DocumentTypeDefinition;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.elements.Lookup;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.logic.DmsLogic;
import org.cmdbuild.operation.management.LookupOperation;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.servlets.json.management.JsonResponse;
import org.cmdbuild.servlets.json.serializers.Attachments.JsonAttachmentsContext;
import org.cmdbuild.servlets.json.serializers.Attachments.JsonCategoryDefinition;
import org.cmdbuild.servlets.json.serializers.Serializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

public class Attachments extends JSONBase {

	@JSONExported
	public JsonResponse getAttachmentsContext(final UserContext userCtx) {
		final DmsLogic dmsLogic = dmsLogicFor(userCtx);
		final String categoryLookupType = dmsLogic.getCategoryLookupType();
		final LookupOperation lookupOperation = lookupOperationFor(userCtx);
		final List<Lookup> lookups = lookupOperation.getLookupList(categoryLookupType);
		final List<JsonCategoryDefinition> jsonCategories = Lists.newArrayList();
		for (final Lookup lookup : activeOnly(lookups)) {
			final DocumentTypeDefinition categoryDefinition = dmsLogic.getCategoryDefinition(lookup.getCode());
			jsonCategories.add(JsonCategoryDefinition.from(lookup, categoryDefinition));
		}
		return JsonResponse.success(JsonAttachmentsContext.from(jsonCategories));
	}

	private final Collection<Lookup> activeOnly(final List<Lookup> lookups) {
		return filter(lookups, activeLookups());
	}

	private Predicate<? super Lookup> activeLookups() {
		return new Predicate<Lookup>() {

			@Override
			public boolean apply(final Lookup input) {
				return input.getStatus().isActive();
			}

		};
	}

	private DmsLogic dmsLogicFor(final UserContext userCtx) {
		final DmsLogic dmsLogic = applicationContext.getBean(DmsLogic.class);
		dmsLogic.setUserContext(userCtx);
		return dmsLogic;
	}

	private LookupOperation lookupOperationFor(final UserContext userCtx) {
		final LookupOperation lookupOperation = new LookupOperation(userCtx);
		return lookupOperation;
	}

	/*
	 * Legacy calls
	 */

	@JSONExported
	public JSONObject getAttachmentList( //
			final JSONObject serializer, //
			final UserContext userCtx, //
			final ICard card) throws JSONException, CMDBException {
		final DmsLogic dmsLogic = applicationContext.getBean(DmsLogic.class);
		dmsLogic.setUserContext(userCtx);
		final List<StoredDocument> attachments = dmsLogic.search(card.getSchema().getName(), card.getId());
		final JSONArray rows = new JSONArray();
		for (final StoredDocument attachment : attachments) {
			rows.put(Serializer.serializeAttachment(attachment));
		}
		serializer.put("rows", rows);
		return serializer;
	}

	@JSONExported
	public DataHandler downloadAttachment( //
			final UserContext userCtx, //
			@Parameter("Filename") final String filename, //
			final ICard card) throws JSONException, CMDBException {
		final DmsLogic dmsLogic = applicationContext.getBean(DmsLogic.class);
		dmsLogic.setUserContext(userCtx);
		return dmsLogic.download(card.getSchema().getName(), card.getId(), filename);
	}

	@JSONExported
	public void uploadAttachment( //
			final UserContext userCtx, //
			@Parameter("File") final FileItem file, //
			@Parameter("Category") final String category, //
			@Parameter("Description") final String description, //
			// TODO add Json for metadata
			final ICard card) throws JSONException, CMDBException, IOException {
		final DmsLogic dmsLogic = applicationContext.getBean(DmsLogic.class);
		dmsLogic.setUserContext(userCtx);
		dmsLogic.upload(userCtx.getUsername(), card.getSchema().getName(), card.getId(), file.getInputStream(),
				removeFilePath(file.getName()), category, description);
	}

	// Needed by Internet Explorer that uploads the file with full path
	private String removeFilePath(final String name) {
		final int backslashIndex = name.lastIndexOf("\\");
		final int slashIndex = name.lastIndexOf("/");
		final int fileNameIndex = Math.max(slashIndex, backslashIndex) + 1;
		return name.substring(fileNameIndex);
	}

	@JSONExported
	public JSONObject deleteAttachment( //
			final JSONObject serializer, //
			final UserContext userCtx, //
			@Parameter("Filename") final String filename, //
			final ICard card) throws JSONException, CMDBException, IOException {
		final DmsLogic dmsLogic = applicationContext.getBean(DmsLogic.class);
		dmsLogic.setUserContext(userCtx);
		dmsLogic.delete(card.getSchema().getName(), card.getId(), filename);
		return serializer;
	}

	@JSONExported
	public JSONObject modifyAttachment( //
			final JSONObject serializer, //
			final UserContext userCtx, //
			@Parameter("Filename") final String filename, //
			@Parameter("Description") final String description, //
			// TODO add Json for metadata
			final ICard card) throws JSONException, CMDBException, IOException {
		final DmsLogic dmsLogic = applicationContext.getBean(DmsLogic.class);
		dmsLogic.setUserContext(userCtx);
		dmsLogic.updateDescription(card.getSchema().getName(), card.getId(), filename, description);
		return serializer;
	}

}
