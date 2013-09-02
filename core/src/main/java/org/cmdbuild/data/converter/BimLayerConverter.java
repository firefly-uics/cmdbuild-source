package org.cmdbuild.data.converter;

import static org.cmdbuild.logic.data.Utils.readBoolean;
import static org.cmdbuild.logic.data.Utils.readString;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.data.store.DataViewStore.BaseStorableConverter;
import org.cmdbuild.model.bim.BimLayer;

public class BimLayerConverter extends BaseStorableConverter<BimLayer> {

	final String	TABLE_NAME = "_BimLayer",
					CLASS_NAME = "ClassName",
					ACTIVE = "Active",
					BIM_ROOT = "Root";

	@Override
	public String getClassName() {
		return TABLE_NAME;
	}

	@Override
	public String getIdentifierAttributeName() {
		return CLASS_NAME;
	}

	@Override
	public BimLayer convert(CMCard card) {
		final BimLayer bimMapperInfo = new BimLayer(readString(card, CLASS_NAME));
		bimMapperInfo.setActive(readBoolean(card,ACTIVE));
		bimMapperInfo.setRoot(readBoolean(card, BIM_ROOT));
		return bimMapperInfo;
	}

	@Override
	public Map<String, Object> getValues(BimLayer bimClassInfo) {
		final Map<String, Object> values = new HashMap<String, Object>();

		values.put(CLASS_NAME, bimClassInfo.getClassName());
		values.put(ACTIVE, bimClassInfo.isActive());
		values.put(BIM_ROOT, bimClassInfo.isRoot());
		return values;
	}

	@Override
	public String getUser(BimLayer storable) {
		// TODO Auto-generated method stub
		return null;
	}

}
