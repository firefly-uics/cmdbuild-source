package org.cmdbuild.service.rest.dto.adapter;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public abstract class MapAdapter<K, V> extends XmlAdapter<MapAdapter.AdaptedMap<K, V>, Map<K, V>> {

	public static class AdaptedMap<K, V> {

		public List<Entry<K, V>> entry = Lists.newArrayList();

	}

	public static class Entry<K, V> {

		public K key;

		public V value;

	}

	@Override
	public Map<K, V> unmarshal(final AdaptedMap<K, V> adaptedMap) throws Exception {
		final Map<K, V> map = Maps.newHashMap();
		for (final Entry<K, V> entry : adaptedMap.entry) {
			map.put(entry.key, entry.value);
		}
		return map;
	}

	@Override
	public AdaptedMap<K, V> marshal(final Map<K, V> map) throws Exception {
		final AdaptedMap<K, V> adaptedMap = new AdaptedMap<K, V>();
		for (final Map.Entry<K, V> mapEntry : map.entrySet()) {
			final Entry<K, V> entry = new Entry<K, V>();
			entry.key = mapEntry.getKey();
			entry.value = mapEntry.getValue();
			adaptedMap.entry.add(entry);
		}
		return adaptedMap;
	}

}
