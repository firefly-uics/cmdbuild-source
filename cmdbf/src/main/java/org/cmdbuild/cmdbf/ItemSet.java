package org.cmdbuild.cmdbf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ItemSet<T extends CMDBfItem> implements Set<T> {
	
	private List<T> items;
	private Map<CMDBfId, T> idMap;
	
	public ItemSet() {
		items = new ArrayList<T>();
		idMap = new HashMap<CMDBfId, T>();
	}
	
	public T get(Object o) {
		T item = idMap.get(o);
		if(item==null && o instanceof CMDBfItem){
			Iterator<CMDBfId> iterator = ((CMDBfItem)o).instanceIds().iterator();
			while(item==null && iterator.hasNext())
				item = idMap.get(iterator.next());
		}
		return item;
	}

	@Override
	public int size() {
		return items.size();
	}

	@Override
	public boolean isEmpty() {
		return items.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return get(o) != null;
	}

	@Override
	public Iterator<T> iterator() {
		return items.iterator();
	}

	@Override
	public Object[] toArray() {
		return items.toArray();
	}

	@Override
	public <E> E[] toArray(E[] a) {
		return items.toArray(a);
	}

	@Override
	public boolean add(T item) {
		boolean modified = false;
		T setItem = get(item);
		if(setItem != null) {
			modified = setItem.merge(item);
		}
		else {
			modified = items.add(item);
			setItem = item;
		}
		if(modified) {
			for(CMDBfId id : setItem.instanceIds()) {
				T old = idMap.put(id, setItem);
				assert(old==null || old==setItem);
			}
		}
		return modified;
	}

	@Override
	public boolean remove(Object o) {
		boolean modified = false;
		T setItem = get(o);
		if(setItem != null)
			modified = items.remove(setItem);
		if(modified) {
			for(CMDBfId id : setItem.instanceIds()) {
				T old = idMap.remove(id);
				assert(old == setItem);
			}
		}
		return modified;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		boolean contains = true;
		Iterator<?> iterator = c.iterator();
		while(contains && iterator.hasNext())
			contains = contains(iterator.next());
		return contains;
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		boolean modified = false;
		for(T item : c) {
			modified = add(item) | modified;
		}
		return modified;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean retainAll(Collection<?> c) {
		boolean modified = false;
		Set<T> toRemove = new HashSet<T>();
		toRemove.addAll(items);
		for(Object o : c) {
			T item = get(o);
			if(item != null) {
				toRemove.remove(item);
				if(o instanceof CMDBfItem)
					modified = add((T)o) | modified;
			}
		}
		modified = removeAll(toRemove) | modified;
		return modified;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean modified = false;
		for(Object o : c)
			modified = remove(o) | modified;
		return modified;
	}

	@Override
	public void clear() {
		items.clear();
		idMap.clear();		
	}
	
	public Set<CMDBfId> idSet() {
		return idMap.keySet();
	}
}
