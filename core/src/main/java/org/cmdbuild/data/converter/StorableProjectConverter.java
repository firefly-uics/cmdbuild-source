package org.cmdbuild.data.converter;

import static org.cmdbuild.logic.data.Utils.readBoolean;
import static org.cmdbuild.logic.data.Utils.readDateTime;
import static org.cmdbuild.logic.data.Utils.readString;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.data.store.DataViewStore.BaseStorableConverter;
import org.cmdbuild.model.bim.StorableProject;

public class StorableProjectConverter extends BaseStorableConverter<StorableProject> {

	public static final String TABLE_NAME = "_BimProject";

	final String NAME = "Code", DESCRIPTION = "Description", PROJECT_ID = "ProjectId", ACTIVE = "Active",
			LAST_CHECKIN = "LastCheckin", SYNCHRONIZED = "Synchronized", IMPORT_MAPPING = "ImportMapping",
			EXPORT_MAPPING = "ExportMapping";

	@Override
	public String getClassName() {
		return TABLE_NAME;
	}

	@Override
	public String getIdentifierAttributeName() {
		return PROJECT_ID;
	}

	@Override
	public StorableProject convert(CMCard card) {
		final StorableProject project = new StorableProject();
		project.setCardId(card.getId());
		project.setName(readString(card, NAME));
		project.setDescription(readString(card, DESCRIPTION));
		project.setProjectId(readString(card, PROJECT_ID));
		project.setActive(readBoolean(card, ACTIVE));
		project.setLastCheckin(readDateTime(card, LAST_CHECKIN));
		project.setSynch(readBoolean(card, SYNCHRONIZED));
		project.setImportMapping(readString(card, IMPORT_MAPPING));
		project.setExportMapping(readString(card, EXPORT_MAPPING));
		return project;
	}

	@Override
	public Map<String, Object> getValues(StorableProject bimProject) {
		final Map<String, Object> values = new HashMap<String, Object>();

		values.put(NAME, bimProject.getName());
		values.put(DESCRIPTION, bimProject.getDescription());
		values.put(PROJECT_ID, bimProject.getProjectId());
		values.put(ACTIVE, bimProject.isActive());
		values.put(LAST_CHECKIN, bimProject.getLastCheckin());
		values.put(SYNCHRONIZED, bimProject.isSynch());
		values.put(IMPORT_MAPPING, bimProject.getImportMapping());
		values.put(EXPORT_MAPPING, bimProject.getExportMapping());
		return values;
	}

	@Override
	public String getUser(StorableProject storable) {
		// TODO Auto-generated method stub
		return null;
	}

}
