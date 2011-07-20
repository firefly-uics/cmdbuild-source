package org.cmdbuild.dao.backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.cmdbuild.elements.Lookup;
import org.cmdbuild.elements.LookupType;
import org.cmdbuild.elements.TableTree;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.wrappers.MenuCard;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.NotFoundException.NotFoundExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.utils.tree.CNode;
import org.cmdbuild.utils.tree.CTree;

public class SchemaCache {

	private final CMBackend backend;

	private Map<Integer, IDomain> domainMap;
	private Map<Integer, CTree<MenuCard>> menuMap;
	private Map<Integer, Lookup> lookups;
	private Map<String, LookupType> lookupTypeMap;
	private CTree<LookupType> lookupTypeTree;
	private Map<Integer, CNode<ITable>> tableMap;
	private CTree<ITable> tableTree;

	private final Object tableSyncObject = new Object();
	private final Object lookupSyncObject = new Object();
	private final Object domainSyncObject = new Object();
	
	public SchemaCache(CMBackend backend) {
		clearDomains();
		clearLookups();
		clearTables();
		this.menuMap = new Hashtable<Integer, CTree<MenuCard>>();
		this.backend = backend;
	}

	public ITable getTable(String tableName) throws NotFoundException {
		for(CNode<ITable> tableNode : getTableMap().values()) {
			ITable table = tableNode.getData();
			if(table.getName().equals(tableName))
				return table;
		}
		throw NotFoundExceptionType.CLASS_NOTFOUND.createException(tableName);
	}

	public ITable getTable(Integer classId) throws NotFoundException {
		tableMap = getTableMap();
		if (tableMap.containsKey(classId))
			return tableMap.get(classId).getData();
		else
			throw NotFoundExceptionType.CLASS_NOTFOUND.createException(String.valueOf(classId));
	}

	public IDomain getDomain(String domainName) throws NotFoundException {
		synchronized (domainSyncObject) {
			for(IDomain domain : getDomainMap().values()) {
				if(domain.getName().equals(domainName))
					return domain;
			}
			throw NotFoundExceptionType.DOMAIN_NOTFOUND.createException(domainName);
		}
	}

	public IDomain getDomain(Integer domainId) throws NotFoundException {
		domainMap = getDomainMap();
		if (domainMap.containsKey(domainId))
			return domainMap.get(domainId);
		else
			throw NotFoundExceptionType.CLASS_NOTFOUND.createException(String.valueOf(domainId));
	}

	protected void setMenu(Integer idGroup, CTree<MenuCard> menuTree){
		menuMap.put(idGroup, menuTree);
	}

	public Lookup getLookup(Integer idLookup){
		Map<Integer, Lookup> lookups = getLookups();
		if (lookups!=null && idLookup!=null && lookups.containsKey(idLookup)) {
			return getLookups().get(idLookup);
		} else {
			return null;
		}
	}

	public Lookup getLookup(String type, String description) {
		for(Lookup lookup : getLookups().values()) {
			String ldescription = lookup.getDescription();
			String ltype = lookup.getType();
			if (ldescription != null && ldescription.equals(description) && ltype != null && ltype.equals(type)) { 
				return lookup;
			}
		}
		return null;
	}

	public List<Lookup> getLookupList(String type, String description) {
		List<Lookup> list = new ArrayList<Lookup>();
		for(Lookup lookup : getLookups().values()) {
			String ltype = lookup.getType();
			 if (ltype == null || !ltype.equals(type))
			 	continue;
			 if(description != null && !"".equals(description)) {
				 String ldescription = lookup.getDescription();
				 if (ldescription == null || !ldescription.equals(description))
					 continue;
			 }
			 list.add(lookup);
		}
		return list;
	}

	protected Map<Integer, Lookup> getLookups() {
		synchronized (lookupSyncObject) {
			if (null == lookups)
				loadLookups();
			return lookups;
		}
	}

	protected void loadLookups() {
		synchronized (lookupSyncObject) {
			Log.PERSISTENCE.info("Building lookup cache");
			lookups = new Hashtable<Integer, Lookup>();
			List<Lookup> lookupList = backend.findLookups();
			for (Lookup l: lookupList) {
				lookups.put(l.getId(), l);
			}
		}
	}

	public LookupType getLookupType(String type){
		synchronized (lookupSyncObject) {
			try {
				return getLookupTypeMap().get(type);
			} catch (NullPointerException e) {
				return null;
			}
		}
	}

	protected Map<String, LookupType> getLookupTypeMap() {
		synchronized (lookupSyncObject) {
			if (lookupTypeMap == null) {
				loadLookupTypeMap();
			}
			return lookupTypeMap;
		}
	}

	public Iterable<LookupType> getLookupTypeList() {
		List<LookupType> list = new ArrayList<LookupType>(); 
		for (LookupType lt : getLookupTypeMap().values()) {
			if (!"root".equals(lt.getType())) {
				list.add(lt);
			}
		}
		return list;
	}
	
	protected void loadLookupTypeMap() {
		synchronized (lookupSyncObject) {
			Log.PERSISTENCE.info("Building lookup type cache");
			lookupTypeMap = new HashMap<String, LookupType>();
			for(CNode<LookupType> node: getLookupTypeTree().toList()) {
				LookupType l = node.getData();
				lookupTypeMap.put(l.getType(), l);
			}
		}
	}

	public CTree<LookupType> getLookupTypeTree() {
		synchronized (lookupSyncObject) {
			if (lookupTypeTree == null) {
				lookupTypeTree = backend.loadLookupTypeTree();
			}
			return lookupTypeTree;
		}
	}

	public Iterable<ITable> getTableList() {
		List<ITable> list = new ArrayList<ITable>(); 
		for (CNode<ITable> node : getTableMap().values())
			list.add(node.getData());
		return list;
	}

	private Map<Integer, CNode<ITable>> getTableMap() {
		synchronized (tableSyncObject) {
			if (tableMap == null)
				loadTables();
			return tableMap;
		}
	}

	@Deprecated
	public TableTree getTableTree() {
		synchronized (tableSyncObject) {
			if(tableTree == null)
				loadTables();
			return new TableTree(tableTree);
		}
	}

	private void loadTables() {
		Log.PERSISTENCE.info("Building table cache");
		tableMap = backend.loadTableMap();
		tableTree = backend.buildTableTree(tableMap);
	}

	public Iterable<IDomain> getDomainList() {
		return getDomainMap().values();
	}

	private Map<Integer, IDomain> getDomainMap() {
		synchronized (domainSyncObject) {
			if (domainMap == null)
				loadDomains();
			return domainMap;
		}
	}

	private void loadDomains() {
		Log.PERSISTENCE.info("Building domain cache");
		domainMap = backend.loadDomainMap();
	}

	public void refreshTables() {
		clearTables();
	}

	public void clearTables() {
		tableMap = null;
		tableTree = null;
	}

	public void refreshLookups() {
		clearLookups();
	}

	public void clearLookups() {
		lookups = null;
		lookupTypeMap = null;
		lookupTypeTree = null;
	}

	public void refreshDomains() {
		clearDomains();
	}

	public void clearDomains() {
		domainMap = null;
	}

	public void addTable(int oid, ITable table) {
		CNode<ITable> childNode = new CNode<ITable>(table);
		CNode<ITable> parentNode = getTableMap().get(table.getParent().getId());
		tableMap.put(oid, childNode);
		parentNode.addChild(childNode);
	}

	public void addDomain(int oid, IDomain domain) {
		getDomainMap().put(oid, domain);
	}
}
