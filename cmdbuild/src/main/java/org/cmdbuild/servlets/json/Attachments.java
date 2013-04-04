package org.cmdbuild.servlets.json;

import static com.google.common.collect.FluentIterable.from;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringUtils;
import org.cmdbuild.common.annotations.CheckIntegration;
import org.cmdbuild.data.store.lookup.LookupDto;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.data.store.lookup.LookupTypeDto;
import org.cmdbuild.dms.DefaultDefinitionsFactory;
import org.cmdbuild.dms.DefinitionsFactory;
import org.cmdbuild.dms.DocumentTypeDefinition;
import org.cmdbuild.dms.Metadata;
import org.cmdbuild.dms.MetadataDefinition;
import org.cmdbuild.dms.MetadataGroup;
import org.cmdbuild.dms.MetadataGroupDefinition;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.exception.DmsException;
import org.cmdbuild.listeners.RequestListener;
import org.cmdbuild.logic.DmsLogic;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.servlets.json.management.JsonResponse;
import org.cmdbuild.servlets.json.serializers.Attachments.JsonAttachmentsContext;
import org.cmdbuild.servlets.json.serializers.Attachments.JsonCategoryDefinition;
import org.cmdbuild.servlets.json.serializers.Serializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

public class Attachments extends JSONBase {

	private static final Predicate<? super LookupDto> ACTIVE_ONLY = new Predicate<LookupDto>() {
		@Override
		public boolean apply(final LookupDto input) {
			return input.active;
		}
	};

	private static final ObjectMapper mapper = new ObjectMapper();

	private final DefinitionsFactory definitionsFactory;

	public Attachments() {
		definitionsFactory = new DefaultDefinitionsFactory();
	}

	@JSONExported
	public JsonResponse getAttachmentsContext() {
		final LookupTypeDto lookupType = LookupTypeDto.newInstance() //
				.withName(dmsLogic().getCategoryLookupType()) //
				.build();
		final Iterable<LookupDto> lookups = lookupStore().listForType(lookupType);
		final List<JsonCategoryDefinition> jsonCategories = Lists.newArrayList();
		for (final LookupDto lookup : from(lookups).filter(ACTIVE_ONLY)) {
			final DocumentTypeDefinition categoryDefinition = categoryDefinition(lookup.description);
			jsonCategories.add(JsonCategoryDefinition.from(lookup, categoryDefinition));
		}
		return JsonResponse.success(JsonAttachmentsContext.from(jsonCategories));
	}

	/*
	 * Legacy calls
	 */

	// TODO: replace ICard parameter with two parameters className and cardId
	@CheckIntegration
	@JSONExported
	public JSONObject getAttachmentList( //
			final JSONObject serializer, //
			final ICard card) throws JSONException, CMDBException {
		final List<StoredDocument> attachments = dmsLogic().search(card.getSchema().getName(), card.getId());
		final JSONArray rows = new JSONArray();
		for (final StoredDocument attachment : attachments) {
			rows.put(Serializer.serializeAttachment(attachment));
		}
		serializer.put("rows", rows);
		return serializer;
	}

	// TODO: replace ICard parameter with two parameters className and cardId
	@CheckIntegration
	@JSONExported
	public DataHandler downloadAttachment( //
			@Parameter("Filename") final String filename, //
			final ICard card) throws JSONException, CMDBException {
		return dmsLogic().download(card.getSchema().getName(), card.getId(), filename);
	}

	// TODO: replace ICard parameter with two parameters className and cardId
	@CheckIntegration
	@JSONExported
	public void uploadAttachment( //
			@Parameter("File") final FileItem file, //
			@Parameter("Category") final String category, //
			@Parameter("Description") final String description, //
			@Parameter("Metadata") final String jsonMetadataValues, //
			final ICard card) throws JSONException, CMDBException, IOException {
		final Map<String, Map<String, Object>> metadataValues = metadataValuesFromJson(jsonMetadataValues);
		final String username = TemporaryObjectsBeforeSpringDI.getOperationUser().getAuthenticatedUser().getUsername();
		dmsLogic().upload( //
				username, //
				card.getSchema().getName(), //
				card.getId(), //
				file.getInputStream(), //
				removeFilePath(file.getName()), //
				category, //
				description, //
				metadataGroupsFrom(categoryDefinition(category), metadataValues));
	}

	/**
	 * Needed by Internet Explorer that uploads the file with full path
	 */
	private String removeFilePath(final String name) {
		final int backslashIndex = name.lastIndexOf("\\");
		final int slashIndex = name.lastIndexOf("/");
		final int fileNameIndex = Math.max(slashIndex, backslashIndex) + 1;
		return name.substring(fileNameIndex);
	}

	// TODO: replace ICard parameter with two parameters className and cardId
	@CheckIntegration
	@JSONExported
	public JSONObject modifyAttachment( //
			final JSONObject serializer, //
			@Parameter("Filename") final String filename, //
			@Parameter("Category") final String category, //
			@Parameter("Description") final String description, //
			@Parameter("Metadata") final String jsonMetadataValues, //
			final ICard card) throws JSONException, CMDBException, IOException {
		final Map<String, Map<String, Object>> metadataValues = metadataValuesFromJson(jsonMetadataValues);
		dmsLogic().updateDescriptionAndMetadata( //
				card.getSchema().getName(), //
				card.getId(), //
				filename, //
				description, //
				metadataGroupsFrom(categoryDefinition(category), metadataValues));
		return serializer;
	}

	private List<MetadataGroup> metadataGroupsFrom(final DocumentTypeDefinition documentTypeDefinition,
			final Map<String, Map<String, Object>> metadataValues) {
		final List<MetadataGroup> metadataGroups = Lists.newArrayList();
		for (final MetadataGroupDefinition metadataGroupDefinition : documentTypeDefinition
				.getMetadataGroupDefinitions()) {
			final String groupMame = metadataGroupDefinition.getName();
			final Map<String, Object> allMetadataMap = metadataValues.get(groupMame);
			if (allMetadataMap == null) {
				continue;
			}

			metadataGroups.add(new MetadataGroup() {

				@Override
				public String getName() {
					return groupMame;
				}

				@Override
				public Iterable<Metadata> getMetadata() {
					final List<Metadata> metadata = Lists.newArrayList();
					for (final MetadataDefinition metadataDefinition : metadataGroupDefinition.getMetadataDefinitions()) {
						final String metadataName = metadataDefinition.getName();
						final Object rawValue = allMetadataMap.get(metadataName);
						metadata.add(new Metadata() {

							@Override
							public String getName() {
								return metadataName;
							}

							@Override
							public String getValue() {
								return (rawValue == null) ? StringUtils.EMPTY : rawValue.toString();
							}

						});
					}
					return metadata;
				}
			});

		}
		return metadataGroups;
	}

	// TODO: replace ICard parameter with two parameters className and cardId
	@CheckIntegration
	@JSONExported
	public JSONObject deleteAttachment( //
			final JSONObject serializer, //
			@Parameter("Filename") final String filename, //
			final ICard card) throws JSONException, CMDBException, IOException {
		dmsLogic().delete(card.getSchema().getName(), card.getId(), filename);
		return serializer;
	}

	/*
	 * Utilities
	 */

	/**
	 * At the first level there are the metadataGroups For each metadataGroups,
	 * there is another map with the values for the group
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Map<String, Object>> metadataValuesFromJson(final String jsonMetadataValues)
			throws IOException, JsonParseException, JsonMappingException {
		return mapper.readValue(jsonMetadataValues, Map.class);
	}

	private DocumentTypeDefinition categoryDefinition(final String category) {
		try {
			return dmsLogic().getCategoryDefinition(category);
		} catch (final DmsException e) {
			RequestListener.getCurrentRequest().pushWarning(e);
			return definitionsFactory.newDocumentTypeDefinitionWithNoMetadata(category);
		}
	}

	private DmsLogic dmsLogic() {
		return TemporaryObjectsBeforeSpringDI.getDmsLogic();
		// return applicationContext.getBean(DmsLogic.class);
	}

	private LookupStore lookupStore() {
		return TemporaryObjectsBeforeSpringDI.getLookupStore();
	}

}
