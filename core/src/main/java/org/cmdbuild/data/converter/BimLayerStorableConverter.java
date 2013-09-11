package org.cmdbuild.data.converter;

import static org.cmdbuild.logic.data.Utils.readBoolean;
import static org.cmdbuild.logic.data.Utils.readString;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.data.store.DataViewStore.BaseStorableConverter;
import org.cmdbuild.model.bim.BimLayer;

public class BimLayerStorableConverter extends BaseStorableConverter<BimLayer> {

	final String	TABLE_NAME = "_BimLayer",
					CLASS_NAME = "ClassName",
					ACTIVE = "Active",
					EXPORT = "Export",	
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
		final BimLayer layer = new BimLayer(readString(card, CLASS_NAME));
		layer.setActive(readBoolean(card,ACTIVE));
		layer.setRoot(readBoolean(card, BIM_ROOT));
		layer.setExport(readBoolean(card, EXPORT));
		return layer;
	}

	@Override
	public Map<String, Object> getValues(BimLayer layer) {
		final Map<String, Object> values = new HashMap<String, Object>();

		values.put(CLASS_NAME, layer.getClassName());
		values.put(ACTIVE, layer.isActive());
		values.put(BIM_ROOT, layer.isRoot());
		values.put(EXPORT, layer.isExport());
		return values;
	}

	@Override
	public String getUser(BimLayer storable) {
		// TODO Auto-generated method stub
		return null;
	}

}
