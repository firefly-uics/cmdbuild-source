package org.cmdbuild.data.converter;

import static org.cmdbuild.logic.data.Utils.readBoolean;
import static org.cmdbuild.logic.data.Utils.readString;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.data.store.DataViewStore.BaseStorableConverter;
import org.cmdbuild.model.bim.BimMapperInfo;

public class BimMapperInfoConverter extends BaseStorableConverter<BimMapperInfo> {

	final String	TABLE_NAME = "_BimMapperInfo",
					CLASS_NAME = "ClassName",
					ACTIVE = "Active",
					BIM_ROOT = "BimRoot";

	@Override
	public String getClassName() {
		return TABLE_NAME;
	}

	@Override
	public String getIdentifierAttributeName() {
		return CLASS_NAME;
	}

	@Override
	public BimMapperInfo convert(CMCard card) {
		final BimMapperInfo bimMapperInfo = new BimMapperInfo(readString(card, CLASS_NAME));
		bimMapperInfo.setActive(readBoolean(card,ACTIVE));
		bimMapperInfo.setBimRoot(readBoolean(card, BIM_ROOT));
		return bimMapperInfo;
	}

	@Override
	public Map<String, Object> getValues(BimMapperInfo bimClassInfo) {
		final Map<String, Object> values = new HashMap<String, Object>();

		values.put(CLASS_NAME, bimClassInfo.getClassName());
		values.put(ACTIVE, bimClassInfo.isActive());
		values.put(BIM_ROOT, bimClassInfo.isBimRoot());
		return values;
	}

	@Override
	public String getUser(BimMapperInfo storable) {
		// TODO Auto-generated method stub
		return null;
	}

}
