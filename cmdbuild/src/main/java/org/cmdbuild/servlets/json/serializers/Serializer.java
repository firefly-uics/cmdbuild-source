package org.cmdbuild.servlets.json.serializers;

import static org.cmdbuild.servlets.json.ComunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.ComunicationConstants.EMAIL;
import static org.cmdbuild.servlets.json.ComunicationConstants.IS_ACTIVE;
import static org.cmdbuild.servlets.json.ComunicationConstants.USER_ID;
import static org.cmdbuild.servlets.json.ComunicationConstants.USER_NAME;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.ArrayUtils;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.user.CMUser;
import org.cmdbuild.common.annotations.OldDao;
import org.cmdbuild.config.DmsProperties;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dms.Metadata;
import org.cmdbuild.dms.MetadataGroup;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.elements.AttributeValue;
import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.elements.interfaces.BaseSchema.Mode;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.exception.DmsException;
import org.cmdbuild.listeners.RequestListener;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.DmsLogic;
import org.cmdbuild.logic.auth.AuthenticationLogic.GroupInfo;
import org.cmdbuild.model.Report;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.services.auth.PrivilegeManager.PrivilegeType;
import org.cmdbuild.services.meta.MetadataService;
import org.cmdbuild.servlets.json.management.ActivityIdentifier;
import org.cmdbuild.servlets.json.serializers.JsonHistory.HistoryItem;
import org.cmdbuild.servlets.json.serializers.JsonHistory.ValueAndDescription;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Maps;

public class Serializer {

	// TODO use constants
	private static final SimpleDateFormat ATTACHMENT_DATE_FOMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

	public static final String AVAILABLE_CLASS = "availableclass";
	public static final String AVAILABLE_PROCESS_CLASS = "availableprocessclass";
	public static final String AVAILABLE_REPORT = "availablereport";
	public static final String AVAILABLE_DASHBOARDS = "availabledashboards";

	/**
	 * keep until the end of integration just to be sure that actual
	 * serialization has all what we need
	 */
	@OldDao
	public static JSONObject serializeCard(final ICard card, final boolean printReserved) {
		return serializeCard(card, printReserved, false, false);
	}

	private static JSONObject serializeCard(final ICard card, final boolean printReserved,
			final boolean printPrivileges, final boolean normalize) {
		final JSONObject jsoncard = new JSONObject();
		try {
			for (final String attributeName : card.getAttributeValueMap().keySet()) {
				final AttributeValue value = card.getAttributeValue(attributeName);
				if (value != null) {
					final IAttribute attribute = value.getSchema();
					if (!printReserved
							&& attribute.getMode().equals(Mode.RESERVED)
							&& !(attributeName.equals(ICard.CardAttributes.Id.toString())
									|| !attribute.getStatus().isActive() || // skip
							// inactive
							// attributes
							attributeName.equals(ICard.CardAttributes.Notes.toString()) // Notes
							// is
							// reserved!
							)) {
						continue;
					}
					final Integer id = value.getId();
					String valueString = value.toString();
					if (normalize) {
						valueString = valueString.replace("\n", " ");
					}
					if (id != null) {
						// jsoncard.put(attributeName, id);
						// jsoncard.put(attributeName+"_value", valueString);
						final JSONObject a = new JSONObject();
						a.put("id", id);
						a.put("description", valueString);
						jsoncard.put(attributeName, a);
					} else {
						jsoncard.put(attributeName, valueString);
					}
				}
			}
			jsoncard.put(ICard.CardAttributes.ClassId.toString(), card.getSchema().getId()); // put
			// classId
			jsoncard.put(ICard.CardAttributes.ClassId.toString() + "_value", card.getSchema().getDescription());
			if (printPrivileges) {
				addMetadataAndAccessPrivileges(jsoncard, card.getSchema());
			}
		} catch (final JSONException e) {
			Log.JSONRPC.error("Error serializing card", e);
		}
		return jsoncard;
	}

	public static JSONObject serializeAttachment(final StoredDocument attachment) {
		final JSONObject serializer = new JSONObject();
		try {
			serializer.put("Category", attachment.getCategory());
			serializer.put("CreationDate", ATTACHMENT_DATE_FOMAT.format(attachment.getCreated()));
			serializer.put("ModificationDate", ATTACHMENT_DATE_FOMAT.format(attachment.getModified()));
			serializer.put("Author", attachment.getAuthor());
			serializer.put("Version", attachment.getVersion());
			serializer.put("Filename", attachment.getName());
			serializer.put("Description", attachment.getDescription());
			serializer.put("Metadata", serialize(attachment.getMetadataGroups()));
		} catch (final JSONException e) {
			Log.JSONRPC.error("Error serializing attachment", e);
		}
		return serializer;
	}

	private static JSONObject serialize(final Iterable<MetadataGroup> metadataGroups) throws JSONException {
		final JSONObject jsonMetadata = new JSONObject();
		for (final MetadataGroup metadataGroup : metadataGroups) {
			final JSONObject jsonAllMetadata = new JSONObject();
			for (final Metadata metadata : metadataGroup.getMetadata()) {
				jsonAllMetadata.put(metadata.getName(), metadata.getValue());
			}
			jsonMetadata.put(metadataGroup.getName(), jsonAllMetadata);
		}
		return jsonMetadata;
	}

	protected static void addMetadataAndAccessPrivileges(final JSONObject serializer, final BaseSchema schema)
			throws JSONException {
		addMetadata(serializer, schema);
		addAccessPrivileges(serializer, schema);
	}

	/**
	 * @deprecated use addMetadata(JSONObject, CMClass) instead
	 */
	@Deprecated
	protected static void addMetadata(final JSONObject serializer, final BaseSchema schema) throws JSONException {
		final JSONObject jsonMetadata = new JSONObject();
		final TreeMap<String, Object> metadata = schema.getMetadata();
		for (final String key : metadata.keySet()) {
			jsonMetadata.put(key, metadata.get(key));
		}
		serializer.put("meta", jsonMetadata);
	}

	// FIXME: implement it reading the metadata for the class
	protected static void addMetadata(final JSONObject serializer, final CMClass cmClass) throws JSONException {
		final JSONObject jsonMetadata = new JSONObject();
		serializer.put("meta", jsonMetadata);
	}

	private static void addAccessPrivileges(final JSONObject serializer, final BaseSchema schema) throws JSONException {
		final Object privileges = schema.getMetadata().get(MetadataService.RUNTIME_PRIVILEGES_KEY);
		if (privileges != null) {
			final boolean writePriv = PrivilegeType.WRITE.equals(privileges)
					&& !schema.getMode().alwaysReadPrivileges();
			serializer.put("priv_write", writePriv);
			boolean createPriv = writePriv;
			if (schema instanceof ITable) {
				createPriv &= !((ITable) schema).isSuperClass();
			}
			serializer.put("priv_create", createPriv);
		}
	}

	public static JSONArray buildJsonAvaiableMenuItems() throws JSONException {
		final JSONArray jsonAvaiableItems = new JSONArray();

		final JSONObject jsonClassesFolder = new JSONObject();
		final JSONObject jsonReportsFolder = new JSONObject();
		final JSONObject jsonProcessFolder = new JSONObject();
		final JSONObject jsonDashboardsFolder = new JSONObject();

		jsonClassesFolder.put("text", "class");
		jsonClassesFolder.put("id", AVAILABLE_CLASS);
		jsonClassesFolder.put("iconCls", "cmdbuild-tree-folder-icon");
		jsonClassesFolder.put("cmIndex", 1);

		jsonProcessFolder.put("text", "processclass");
		jsonProcessFolder.put("id", AVAILABLE_PROCESS_CLASS);
		jsonProcessFolder.put("iconCls", "cmdbuild-tree-folder-icon");
		jsonProcessFolder.put("cmIndex", 2);

		jsonReportsFolder.put("text", "report");
		jsonReportsFolder.put("id", AVAILABLE_REPORT);
		jsonReportsFolder.put("iconCls", "cmdbuild-tree-folder-icon");
		jsonReportsFolder.put("cmIndex", 3);

		jsonDashboardsFolder.put("text", "dashboard");
		jsonDashboardsFolder.put("id", AVAILABLE_DASHBOARDS);
		jsonDashboardsFolder.put("iconCls", "cmdbuild-tree-folder-icon");
		jsonDashboardsFolder.put("cmIndex", 4);

		jsonAvaiableItems.put(jsonReportsFolder);
		jsonAvaiableItems.put(jsonClassesFolder);
		jsonAvaiableItems.put(jsonProcessFolder);
		jsonAvaiableItems.put(jsonDashboardsFolder);

		return jsonAvaiableItems;
	}

	public static JSONObject serializeReportForMenu(final Report report, final String type) throws JSONException {
		final JSONObject jsonReport = new JSONObject();
		jsonReport.put("text", report.getDescription());
		jsonReport.put("parent", AVAILABLE_REPORT);
		jsonReport.put("selectable", true);
		jsonReport.put("type", type);
		jsonReport.put("subtype", report.getType().toString().toLowerCase());
		jsonReport.put("objid", report.getId());
		jsonReport.put("id", report.getId() + type);
		jsonReport.put("leaf", true);
		return jsonReport;
	}

	public static JSONObject serialize(final CMGroup group) throws JSONException {
		final JSONObject jsonGroup = new JSONObject();
		jsonGroup.put("id", group.getId());
		jsonGroup.put("name", group.getName());
		jsonGroup.put("description", group.getDescription());
		jsonGroup.put("email", group.getEmail());
		jsonGroup.put("isAdministrator", group.isAdmin());
		jsonGroup.put("isCloudAdministrator", group.isRestrictedAdmin());
		// TODO check if missing
		jsonGroup.put("startingClass", group.getStartingClassId());
		jsonGroup.put("isActive", group.isActive());
		jsonGroup.put("text", group.getDescription());
		jsonGroup.put("selectable", true);
		jsonGroup.put("type", "group");
		return jsonGroup;
	}

	public static JSONArray serializeGroupsForUser(final CMUser user, final List<GroupInfo> groups)
			throws JSONException {
		final JSONArray jsonGroupList = new JSONArray();
		for (final GroupInfo group : groups) {
			final JSONObject row = new JSONObject();
			row.put("id", group.getId());
			row.put("description", group.getDescription());
			final String userDefaultGroupName = user.getDefaultGroupName();
			if (userDefaultGroupName != null && userDefaultGroupName.equalsIgnoreCase(group.getName())) {
				row.put("isdefault", true);
			} else {
				row.put("isdefault", false);
			}
			jsonGroupList.put(row);
		}
		return jsonGroupList;
	}

	public static JSONObject serialize(final CMUser user) throws JSONException {
		final JSONObject row = new JSONObject();
		row.put(USER_ID, user.getId());
		row.put(USER_NAME, user.getUsername());
		row.put(DESCRIPTION, user.getDescription());
		row.put(EMAIL, user.getEmail());
		row.put(IS_ACTIVE, user.isActive());
		return row;
	}

	public static JSONArray serializeUsers(final List<CMUser> users) throws JSONException {
		final JSONArray userList = new JSONArray();
		for (final CMUser user : users) {
			userList.put(Serializer.serialize(user));
		}
		return userList;
	}

	// public static JSONObject serializeProcessAttributeHistory(final ICard
	// card, final CardQuery cardQuery)
	// throws JSONException {
	// final JsonProcessAttributeHistoryFormatter formatter = new
	// JsonProcessAttributeHistoryFormatter();
	// formatter.addCard(card);
	// for (final ICard historyCard : cardQuery) {
	// final String processCode = historyCard.getCode();
	// if (processCode != null && processCode.length() != 0) {
	// formatter.addCard(historyCard);
	// }
	// }
	// final JSONObject jsonResponse = new JSONObject();
	// jsonResponse.put("rows", formatter.toJson());
	// return jsonResponse;
	// }

	public static void serializeCardAttributeHistory( //
			final CMClass targetClass, //
			final Card currentCard, //
			final Iterable<Card> historyCards, //
			final JSONObject jsonOutput //
	) throws JSONException {
		final JsonCardAttributeHistoryFormatter formatter = new JsonCardAttributeHistoryFormatter(targetClass);
		formatter.addCard(currentCard);
		for (final Card historyCard : historyCards) {
			formatter.addCard(historyCard);
		}
		final JSONArray rows = jsonOutput.getJSONArray("rows");
		formatter.addJsonHistoryItems(rows);
	}

	private static class CardHistoryItem extends AbstractJsonResponseSerializer implements HistoryItem {

		private final CMClass targetClass;
		protected final Card card;

		public CardHistoryItem(final CMClass targetClass, final Card card) {
			this.targetClass = targetClass;
			this.card = card;
		}

		@Override
		public Long getId() {
			return card.getId();
		}

		@Override
		public long getInstant() {
			return card.getBeginDate().getMillis();
		}

		@Override
		public Map<String, ValueAndDescription> getAttributes() {
			final Map<String, ValueAndDescription> map = Maps.newLinkedHashMap();
			for (final CMAttribute attribute : targetClass.getActiveAttributes()) {
				try {
					final String name = attribute.getName();
					final String description = attribute.getDescription();
					final Object value = javaToJsonValue(attribute.getType(), card.getAttribute(name));
					map.put(name, new ValueAndDescription(value, description));
				} catch (final JSONException ex) {
					// skip
				}
			}
			return map;
		}

		@Override
		public Map<String, Object> getExtraAttributes() {
			final Map<String, Object> map = Maps.newLinkedHashMap();
			map.put("_AttrHist", true);
			map.put("User", card.getUser());
			map.put("Code", card.getAttribute("Code"));
			map.put("BeginDate", formatDateTime(card.getBeginDate()));

			final Date endDateForSorting;
			if (card.getEndDate() != null) {
				final DateTime endDateTime = card.getEndDate();
				map.put("EndDate", formatDateTime(endDateTime));
				endDateForSorting = endDateTime.toDate();
			} else {
				endDateForSorting = new Date();
			}
			map.put("_EndDate", endDateForSorting.getTime());
			return map;
		}

		@Override
		public boolean isInOutput() {
			return true;
		}
	}

	private static class ProcessHistoryItem extends CardHistoryItem {

		private Card previousCard = null;

		/**
		 * 
		 * @param card
		 *            the card that you want to extract the history
		 * @param previousCard
		 *            the previous card in the cycle, the more recent
		 */
		public ProcessHistoryItem(final CMClass targetClass, final Card card, final Card previousCard) {
			super(targetClass, card);
			this.previousCard = previousCard;
		}

		@Override
		public Map<String, Object> getExtraAttributes() {
			final Map<String, Object> map = super.getExtraAttributes();

			// Add the performer
			if (previousCard != null) {
				final String[] currentActivities = getActivityInstanceIds(card);
				final String[] previousActivities = getActivityInstanceIds(previousCard);

				for (int i = 0; i < currentActivities.length; ++i) {
					final String id = currentActivities[i];
					if (ArrayUtils.contains(previousActivities, id)) {
						continue;
					} else {
						final String[] performers = getActivityInstancePerformers(card);
						map.put("Executor", performers[i]);
						break;
					}
				}
			}

			return map;
		}

		// FIXME!!!!
		private String[] getActivityInstanceIds(final Card card) {
			return new String[0];
			// return
			// card.get(ProcessAttributes.ActivityInstanceId.dbColumnName()).getStringArrayValue();
		}

		// FIXME!!!
		private String[] getActivityInstancePerformers(final Card card) {
			return new String[0];
			// return
			// card.get(ProcessAttributes.CurrentActivityPerformers.dbColumnName())
			// .getStringArrayValue();
		}
	}

	private static class JsonCardAttributeHistoryFormatter extends JsonHistory {

		public JsonCardAttributeHistoryFormatter(final CMClass targetClass) {
			super(targetClass);
		}

		public void addCard(final Card card) {
			addHistoryItem(new CardHistoryItem(targetClass, card));
		}

	}

	private static class JsonProcessAttributeHistoryFormatter extends JsonHistory {

		public JsonProcessAttributeHistoryFormatter(final CMClass targetClass) {
			super(targetClass);
		}

		private Card previousCard = null;

		public void addCard(final Card card) {
			addHistoryItem(new ProcessHistoryItem(targetClass, card, previousCard));
			previousCard = card;
		}
	}

	public static JSONObject serializeActivityIds(final ActivityIdentifier ai, final ICard processCard)
			throws JSONException {
		final JSONObject out = new JSONObject();
		out.put("Id", processCard.getId());
		out.put("IdClass", processCard.getIdClass());
		out.put("ProcessInstanceId", ai.getProcessInstanceId());
		out.put("WorkItemId", ai.getWorkItemId());
		return out;
	}

	public static void addAttachmentsData(final JSONObject jsonTable, final CMClass cmClass, final DmsLogic dmsLogic)
			throws JSONException {
		if (!DmsProperties.getInstance().isEnabled()) {
			return;
		}
		final Map<String, Map<String, String>> rulesByGroup = rulesByGroup(cmClass, dmsLogic);

		final JSONObject jsonGroups = new JSONObject();
		for (final String groupName : rulesByGroup.keySet()) {
			jsonGroups.put(groupName, rulesByGroup.get(groupName));
		}

		final JSONObject jsonAutocompletion = new JSONObject();
		jsonAutocompletion.put("autocompletion", jsonGroups);
		try {
			final JSONObject jsonMeta = jsonTable.getJSONObject("meta");
			jsonMeta.put("attachments", jsonAutocompletion);
		} catch (final JSONException ex) {
			// there is no meta key
		}
	}

	private static Map<String, Map<String, String>> rulesByGroup(final CMClass cmClass, final DmsLogic dmsLogic) {
		try {
			return dmsLogic.getAutoCompletionRulesByClass(cmClass.getIdentifier().getLocalName());
		} catch (final DmsException e) {
			RequestListener.getCurrentRequest().pushWarning(e);
			return Collections.emptyMap();
		}
	}

}
