package org.cmdbuild.dao.backend;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cmdbuild.config.DatabaseProperties;
import org.cmdbuild.dao.backend.postgresql.CardQueryBuilder;
import org.cmdbuild.elements.CardQueryImpl;
import org.cmdbuild.elements.DirectedDomain;
import org.cmdbuild.elements.Lookup;
import org.cmdbuild.elements.LookupType;
import org.cmdbuild.elements.TableTree;
import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.interfaces.DomainQuery;
import org.cmdbuild.elements.interfaces.IAbstractElement;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.IRelation;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.wrappers.ReportCard;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.logger.Log;
import org.cmdbuild.utils.tree.CNode;
import org.cmdbuild.utils.tree.CTree;

public abstract class CMBackend {

	/*
	 * FIXME: It should be replaced with Spring Dependency Injection
	 */
	public static CMBackend INSTANCE;

	static {
		final String classname = DatabaseProperties.getInstance().getDatabaseBackendClass();
		try {
			@SuppressWarnings("unchecked")
			Class<CMBackend> backendClass = (Class<CMBackend>) Class.forName(classname);
			INSTANCE = backendClass.getConstructor().newInstance();
		} catch (Exception e) {
			Log.PERSISTENCE.error("Cannot instantiate the database backend " + classname, e);
		}
	}

	public abstract Map<String, String> parseComment(String comment);

	/*
	 * Classes
	 */
	
	public abstract void deleteTable(ITable table) throws ORMException;

	public abstract int createTable(ITable table) throws ORMException;

	public abstract void modifyTable(ITable table) throws ORMException;

	/**
	 * @return A hash map of class IDs and table nodes (with no parent set)
	 */
	public abstract Map<Integer, CNode<ITable>> loadTableMap();

	/**
	 * Generates the table tree and sets the parent to the table nodes
	 * 
	 * @param map
	 *            Hash map of class IDs and table nodes (with no parent set)
	 * @return Table tree nodes
	 */
	public abstract CTree<ITable> buildTableTree(Map<Integer, CNode<ITable>> map);

	public abstract void deleteAttribute(IAttribute attribute) throws ORMException;

	public abstract void createAttribute(IAttribute attribute) throws ORMException;

	public abstract void modifyAttribute(IAttribute attribute) throws ORMException;

	public abstract Map<String, IAttribute> findAttributes(BaseSchema schema);

	public abstract void modifyDomain(IDomain domain);

	public abstract int createDomain(IDomain domain);

	public abstract void deleteDomain(IDomain domain);

	public abstract Iterable<IDomain> getDomainList();
	public abstract Iterator<IDomain> getDomainList(DomainQuery query);

	public abstract Map<Integer, IDomain> loadDomainMap();

	public abstract List<String> getReportTypes();

	public abstract boolean insertReport(ReportCard bean) throws SQLException, IOException;

	public abstract boolean updateReport(ReportCard bean) throws SQLException, IOException;

	public abstract void createLookupType(LookupType lookupType);

	public abstract void modifyLookupType(LookupType lookupType);

	public abstract void deleteLookupType(LookupType lookupType);

	public abstract CTree<LookupType> loadLookupTypeTree();

	/*
	 * Lookup
	 */
	public abstract void modifyLookup(Lookup lookup);

	public abstract int createLookup(Lookup lookup);

	public abstract List<Lookup> findLookups();

	/*
	 * Relation
	 */
	public abstract int createRelation(IRelation relation);

	public abstract void modifyRelation(IRelation relation);

	public abstract IRelation getRelation(IDomain domain, ICard card1, ICard card2);

	public abstract IRelation getRelation(IDomain domain, int id);

	/*
	 * Card
	 */
	public abstract int createCard(ICard card);

	public abstract void modifyCard(ICard card);

	public abstract List<ICard> getCardList(CardQueryImpl cardQuery);

	public abstract String cardQueryToSQL(CardQuery cardQuery, CardQueryBuilder qb);

	public abstract int getCardPosition(CardQuery query, int cardId);

	public abstract void updateCardsFromTemplate(CardQuery cardQuery, ICard cardTemplate);

	public abstract void deleteElement(IAbstractElement element);

	/*
	 * From the schema cache
	 */

	public abstract ITable getTable(String tableName);
	public abstract ITable getTable(Integer classId);
	public abstract IDomain getDomain(String domainName);
	public abstract IDomain getDomain(Integer domainId);
	public abstract Lookup getLookup(Integer idLookup);
	public abstract Lookup getLookup(String type, String description);
	public abstract Lookup getFirstLookupByCode(String type, String code);
	public abstract List<Lookup> getLookupList(String type, String description);
	public abstract LookupType getLookupType(final String type);
	public abstract LookupType getLookupTypeOrDie(final String type);
	public abstract Iterable<LookupType> getLookupTypeList();
	public abstract CTree<LookupType> getLookupTypeTree();
	public abstract Iterable<ITable> getTableList();
	@Deprecated public abstract TableTree getTableTree();

	/*
	 * FIXME
	 */

	public abstract void clearCache();

	public abstract Iterable<IRelation> getRelationList(DirectedDomain domain, int sourceId);
}
