package org.cmdbuild.data.converter;

import static org.cmdbuild.logic.data.Utils.readString;
import static org.cmdbuild.logic.data.Utils.readBoolean;
import static org.cmdbuild.logic.data.Utils.readDateTime;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.data.store.DataViewStore.BaseStorableConverter;
import org.cmdbuild.model.bim.BimProjectInfo;

public class BIMProjectStorableConverter extends BaseStorableConverter<BimProjectInfo> {

	final String	TABLE_NAME = "_BIMProject",
					NAME = "Name",
					DESCRIPTION = "Description",
					PROJECT_ID = "ProjectId",
					ACTIVE = "Active",
					LAST_CHECKIN = "LastCheckin";

	@Override
	public String getClassName() {
		return TABLE_NAME;
	}

	@Override
	public String getIdentifierAttributeName() {
		return PROJECT_ID;
	}

	@Override
	public BimProjectInfo convert(CMCard card) {
		final BimProjectInfo bimProject = new BimProjectInfo();

		bimProject.setName(readString(card, NAME));
		bimProject.setDescription(readString(card, DESCRIPTION));
		bimProject.setProjectId(readString(card, PROJECT_ID));
		bimProject.setActive(readBoolean(card, ACTIVE));
		bimProject.setLastCheckin(readDateTime(card, LAST_CHECKIN));

		return bimProject;
	}

	@Override
	public Map<String, Object> getValues(BimProjectInfo bimProject) {
		final Map<String, Object> values = new HashMap<String, Object>();

		values.put(NAME, bimProject.getName());
		values.put(DESCRIPTION, bimProject.getDescription());
		values.put(PROJECT_ID, bimProject.getProjectId());
		values.put(ACTIVE, bimProject.isActive());
		values.put(LAST_CHECKIN, bimProject.getLastCheckin());

		return values;
	}

	@Override
	public String getUser(BimProjectInfo storable) {
		// TODO Auto-generated method stub
		return null;
	}

}
