package org.cmdbuild.servlets.json.serializers;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.ArrayUtils;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.user.CMUser;
import org.cmdbuild.config.DmsProperties;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dms.Metadata;
import org.cmdbuild.dms.MetadataGroup;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.elements.AttributeValue;
import org.cmdbuild.elements.DirectedDomain;
import org.cmdbuild.elements.Lookup;
import org.cmdbuild.elements.LookupType;
import org.cmdbuild.elements.TableImpl;
import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.elements.interfaces.BaseSchema.Mode;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.IRelation;
import org.cmdbuild.elements.interfaces.IRelation.RelationAttributes;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.ProcessType;
import org.cmdbuild.elements.utils.CountedValue;
import org.cmdbuild.elements.wrappers.GroupCard;
import org.cmdbuild.elements.wrappers.PrivilegeCard.PrivilegeType;
import org.cmdbuild.elements.wrappers.ReportCard;
import org.cmdbuild.exception.DmsException;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.listeners.RequestListener;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.DmsLogic;
import org.cmdbuild.logic.auth.AuthenticationLogic.GroupInfo;
import org.cmdbuild.logic.commands.GetCardHistory.GetCardHistoryResponse;
import org.cmdbuild.logic.privileges.SecurityLogic.PrivilegeInfo;
import org.cmdbuild.services.meta.MetadataService;
import org.cmdbuild.servlets.json.management.ActivityIdentifier;
import org.cmdbuild.servlets.json.serializers.JsonHistory.HistoryItem;
import org.cmdbuild.servlets.json.serializers.JsonHistory.ValueAndDescription;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Serializer {

	// TODO use constants
	private static final SimpleDateFormat ATTACHMENT_DATE_FOMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

	public static final String AVAILABLE_CLASS = "availableclass";
	public static final String AVAILABLE_PROCESS_CLASS = "availableprocessclass";
	public static final String AVAILABLE_REPORT = "availablereport";
	public static final String AVAILABLE_DASHBOARDS = "availabledashboards";

	public static JSONObject serializeCard(final ICard card, final boolean printReserved) {
		return serializeCard(card, printReserved, false, false);
	}

	public static JSONObject serializeCardNormalized(final ICard card) {
		return serializeCard(card, false, false, true);
	}

	public static JSONObject serializeCardWithPrivileges(final ICard card, final boolean printReserved) {
		return serializeCard(card, printReserved, true, false);
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
							))
						continue;
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

	public static JSONObject serializeRelation(final CountedValue<IRelation> countedRelation) {
		return serializeRelation(countedRelation.getValue(), countedRelation.getCount());
	}

	public static JSONObject serializeRelation(final IRelation relation) {
		return serializeRelation(relation, 0);
	}

	public static JSONObject serializeRelation(final IRelation relation, final int count) {
		final JSONObject serializer = new JSONObject();
		ICard destCard, card1, card2;
		try {
			final DirectedDomain directedDomain = relation.getDirectedDomain();
			serializer.put("Domain", directedDomain.toString());
			serializer.put("DomainDesc", directedDomain.getDescription());
			serializer.put("DomainDir", directedDomain.getDirectionValue());
			if (count != 0)
				serializer.put("DomainCount", count);
			destCard = relation.getCard2();
			if (relation.isReversed()) {
				serializer.put("DomainDestClassId", relation.getSchema().getClass1().getId());
				card1 = relation.getCard2();
				card2 = relation.getCard1();
			} else {
				serializer.put("DomainDestClassId", relation.getSchema().getClass2().getId());
				card1 = relation.getCard1();
				card2 = relation.getCard2();
			}
			if (destCard != null) {
				final ITable destTable = destCard.getSchema();
				// relation key
				serializer.put("Id", relation.getId());
				serializer.put("DomainId", relation.getSchema().getId());
				serializer.put("Class1Id", card1.getIdClass());
				serializer.put("Card1Id", card1.getId());
				serializer.put("Class2Id", card2.getIdClass());
				serializer.put("Card2Id", card2.getId());
				serializer.put("BeginDate", relation.getAttributeValue(RelationAttributes.BeginDate.toString()));
				serializer.put("EndDate", relation.getAttributeValue("EndDate"));
				serializer.put("User", relation.getAttributeValue("User"));

				serializer.put("Class", destCard.getSchema().toString());
				serializer.put("ClassType", getClassType(destCard.getSchema().getName()));
				serializer.put("ClassId", destTable.getId());
				addMetadataAndAccessPrivileges(serializer, destTable);

				serializer.put("CardId", destCard.getId());
				serializer.put("CardCode", destCard.getCode());
				serializer.put("CardDescription", destCard.getDescription());
			}
		} catch (final JSONException e) {
			Log.JSONRPC.error("Error serializing relation", e);
		}
		return serializer;
	}

	/**
	 * @deprecated This is awful: a Table should know it is in a tree!
	 */
	@Deprecated
	protected static String getClassType(final String className) {
		// TODO This is awful: a Table should know it is in a tree!
		if (TableImpl.tree().branch(ProcessType.BaseTable).contains(className))
			return "processclass";
		else
			return "class";
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

	public static JSONObject serializeLookup(final Lookup lookup) throws JSONException {
		return serializeLookup(lookup, false);
	}

	public static JSONObject serializeLookup(final Lookup lookup, final boolean shortForm) throws JSONException {
		JSONObject serializer = null;
		if (lookup != null) {
			serializer = new JSONObject();
			serializer.put("Id", lookup.getId());
			serializer.put("Description", lookup.getDescription());

			if (!shortForm) {
				serializer.put("Type", lookup.getType());
				serializer.put("Code", lookup.getCode() != null ? lookup.getCode() : "");
				serializer.put("Number", lookup.getNumber());
				serializer.put("Notes", lookup.getNotes());
				serializer.put("Default", lookup.getIsDefault());
				serializer.put("Active", lookup.getStatus().isActive());
			}

			final Lookup parent = lookup.getParent();
			if (parent != null) {
				serializer.put("ParentId", parent.getId());
				if (!shortForm) {
					serializer.put("ParentDescription", parent.getDescription());
					serializer.put("ParentType", parent.getType());
				}
			}
		}
		return serializer;
	}

	public static JSONObject serializeLookupType(final LookupType lookupType) throws JSONException {
		final JSONObject row = new JSONObject();
		row.put("description", lookupType.getType());
		row.put("parent", lookupType.getParentTypeName());
		row.put("orig_type", lookupType.getType()); // used if someone want to
		// modify the type name
		return row;
	}

	public static JSONObject serializeLookupParent(final Lookup lookup) throws JSONException {
		JSONObject serializer = null;
		if (lookup != null) {
			serializer = new JSONObject();
			serializer.put("ParentId", lookup.getId());
			serializer.put("ParentDescription", lookup.getDescription());
		}
		return serializer;
	}

	public static JSONObject serializeLookupTable(final LookupType lookupType) throws JSONException {
		final JSONObject serializer = new JSONObject();
		serializer.put("id", lookupType.getType());
		serializer.put("text", lookupType.getType());
		serializer.put("type", "lookuptype");
		serializer.put("selectable", true);

		if (lookupType.getParentTypeName() != null) {
			serializer.put("parent", lookupType.getParentTypeName());
		}
		return serializer;
	}

	protected static void addMetadataAndAccessPrivileges(final JSONObject serializer, final BaseSchema schema)
			throws JSONException {
		addMetadata(serializer, schema);
		addAccessPrivileges(serializer, schema);
	}

	protected static void addMetadata(final JSONObject serializer, final BaseSchema schema) throws JSONException {
		final JSONObject jsonMetadata = new JSONObject();
		final TreeMap<String, Object> metadata = schema.getMetadata();
		for (final String key : metadata.keySet()) {
			jsonMetadata.put(key, metadata.get(key));
		}
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

	public static JSONObject serializeReportForMenu(final ReportCard report, final String type) throws JSONException {
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

	/**
	 * @deprecated use serialize(CMGroup) instead.
	 */
	@Deprecated
	public static JSONObject serializeGroupCard(final GroupCard groupCard) throws JSONException {
		final JSONObject jsonGroup = new JSONObject();
		jsonGroup.put("id", groupCard.getId());
		jsonGroup.put("name", groupCard.getName());
		jsonGroup.put("description", groupCard.getDescription());
		jsonGroup.put("email", groupCard.getEmail());
		jsonGroup.put("isAdministrator", groupCard.isAdmin());
		jsonGroup.put("isCloudAdministrator", groupCard.isCloudAdmin());
		if (groupCard.hasStartingClassId()) {
			jsonGroup.put("startingClass", groupCard.getStartingClassId());
		}
		jsonGroup.put("isActive", groupCard.getStatus().isActive());
		jsonGroup.put("text", groupCard.getDescription());
		jsonGroup.put("selectable", true);
		jsonGroup.put("type", "group");
		return jsonGroup;
	}

	public static JSONObject serialize(final CMGroup group) throws JSONException {
		final JSONObject jsonGroup = new JSONObject();
		jsonGroup.put("id", group.getId());
		jsonGroup.put("name", group.getName());
		jsonGroup.put("description", group.getDescription());
		jsonGroup.put("email", group.getEmail());
		jsonGroup.put("isAdministrator", group.isAdmin());
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

	public static JSONArray serializeGroupList(final boolean onlyActive, final String type) throws JSONException {
		final JSONArray jsonGroups = new JSONArray();
		Iterable<GroupCard> list = new LinkedList<GroupCard>();

		if (onlyActive) {
			list = GroupCard.allActive();
		} else {
			list = GroupCard.all();
		}

		for (final GroupCard group : list) {
			final JSONObject jsonGroup = new JSONObject();
			jsonGroup.put("id", group.getId());
			jsonGroup.put("text", group.getDescription());
			jsonGroup.put("leaf", true);
			jsonGroup.put("selectable", true);
			jsonGroup.put("type", type);

			jsonGroups.put(jsonGroup);
		}
		return jsonGroups;
	}

	public static JSONObject serializePrivilege(final PrivilegeInfo privilege) throws JSONException {
		final JSONObject row = new JSONObject();
		row.put("groupId", privilege.getGroupId());
		if (privilege.mode.equals("w")) {
			row.put("privilege_mode", "write_privilege");
			row.put("write_privilege", true);
		} else if (privilege.mode.equals("r")) {
			row.put("privilege_mode", "read_privilege");
			row.put("read_privilege", true);
		} else {
			row.put("privilege_mode", "none_privilege");
			row.put("none_privilege", true);
		}
		row.put("classname", privilege.getPrivilegedObjectName());
		row.put("classid", privilege.getPrivilegeObjectId());
		return row;
	}

	public static JSONArray serializePrivilegeList(final List<PrivilegeInfo> privileges) throws JSONException {
		final JSONArray privilegeList = new JSONArray();
		for (final PrivilegeInfo privilege : privileges) {
			try {
				privilegeList.put(Serializer.serializePrivilege(privilege));
			} catch (final NotFoundException e) {
				Log.PERSISTENCE.warn("Class OID not found (" + privilege.getPrivilegeObjectId()
						+ ") while searching for grant for group " + privilege.getGroupId());
			}
		}
		return privilegeList;
	}

	public static JSONObject serialize(final CMUser user) throws JSONException {
		final JSONObject row = new JSONObject();
		row.put("userid", user.getId());
		row.put("username", user.getName());
		row.put("description", user.getDescription());
		row.put("email", user.getEmail());
		row.put("isactive", user.isActive());
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

	public static void serializeCardAttributeHistory(final CMCard activeCard,
			final GetCardHistoryResponse cardHistoryResponse, final JSONObject jsonOutput) throws JSONException {
		final JsonCardAttributeHistoryFormatter formatter = new JsonCardAttributeHistoryFormatter();
		formatter.addCard(activeCard);
		for (final CMCard historyCard : cardHistoryResponse) {
			formatter.addCard(historyCard);
		}
		final JSONArray rows = jsonOutput.getJSONArray("rows");
		formatter.addJsonHistoryItems(rows);
	}

	private static class CardHistoryItem extends AbstractJsonResponseSerializer implements HistoryItem {
		protected CMCard card;

		public CardHistoryItem(final CMCard card) {
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
			final Map<String, ValueAndDescription> map = new HashMap<String, ValueAndDescription>();
			for (final CMAttribute attribute : card.getType().getAttributes()) {
				try {
					final String name = attribute.getName();
					final String description = attribute.getDescription();
					final Object value = javaToJsonValue(attribute.getType(), card.get(name));
					map.put(name, new ValueAndDescription(value, description));
				} catch (final JSONException ex) {
					// skip
				}
			}
			return map;
		}

		@Override
		public Map<String, Object> getExtraAttributes() {
			final Map<String, Object> map = new HashMap<String, Object>();
			map.put("_AttrHist", true);
			map.put("User", card.getUser());
			map.put("Code", card.getCode());
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
		private CMCard previousCard = null;

		/**
		 * 
		 * @param card
		 *            the card that you want to extract the history
		 * @param previousCard
		 *            the previous card in the cycle, the more recent
		 */
		public ProcessHistoryItem(final CMCard card, final CMCard previousCard) {
			super(card);
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
		private String[] getActivityInstanceIds(final CMCard card) {
			return new String[0];
			// return
			// card.get(ProcessAttributes.ActivityInstanceId.dbColumnName()).getStringArrayValue();
		}

		// FIXME!!!
		private String[] getActivityInstancePerformers(final CMCard card) {
			return new String[0];
			// return
			// card.get(ProcessAttributes.CurrentActivityPerformers.dbColumnName())
			// .getStringArrayValue();
		}
	}

	private static class JsonCardAttributeHistoryFormatter extends JsonHistory {
		public void addCard(final CMCard card) {
			addHistoryItem(new CardHistoryItem(card));
		}
	}

	private static class JsonProcessAttributeHistoryFormatter extends JsonHistory {
		private CMCard previousCard = null;

		public void addCard(final CMCard card) {
			addHistoryItem(new ProcessHistoryItem(card, previousCard));
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

		final JSONObject jsonMeta = (JSONObject) jsonTable.get("meta");
		jsonMeta.put("attachments", jsonAutocompletion);
	}

	private static Map<String, Map<String, String>> rulesByGroup(final CMClass cmClass, final DmsLogic dmsLogic) {
		try {
			return dmsLogic.getAutoCompletionRulesByClass(cmClass.getName());
		} catch (final DmsException e) {
			RequestListener.getCurrentRequest().pushWarning(e);
			return Collections.emptyMap();
		}
	}

}
