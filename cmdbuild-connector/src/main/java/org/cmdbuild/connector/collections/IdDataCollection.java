package org.cmdbuild.connector.collections;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.connector.data.ConnectorClass;
import org.cmdbuild.connector.data.Key;

public class IdDataCollection {

	private final Map<ConnectorClass, Map<Integer, Key>> idsMap;

	public IdDataCollection() {
		idsMap = new HashMap<ConnectorClass, Map<Integer, Key>>();
	}

	public void addCardId(final ConnectorClass clazz, final Integer id, final Key key) {
		Map<Integer, Key> idMap;
		if (idsMap.containsKey(clazz))
			idMap = idsMap.get(clazz);
		else {
			idMap = new HashMap<Integer, Key>();
			idsMap.put(clazz, idMap);
		}
		idMap.put(id, key);
	}

	public Key getCardKey(final ConnectorClass clazz, final int id) {
		if (idsMap.containsKey(clazz)) {
			final Map<Integer, Key> idMap = idsMap.get(clazz);
			if (idMap.containsKey(id)) {
				return idMap.get(id);
			}
		}
		return null;
	}

	// FIXME useless if we want to send the diffs to CMDBuild without ids!
	public Map<Integer, Key> getClassKeys(final ConnectorClass clazz) {
		if (idsMap.containsKey(clazz)) {
			return idsMap.get(clazz);
		}
		return null;
	}

}
