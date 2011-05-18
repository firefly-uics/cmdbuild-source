package org.cmdbuild.dao.driver;

import java.util.Collection;

import org.cmdbuild.dao.entry.DBEntry;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.QuerySpecs;

public interface DBDriver {

	static final String BASE_CLASS_NAME = "Class";

	public Collection<DBClass> findAllClasses();
	public DBClass findClassById(final Object id);
	public DBClass findClassByName(final String name);

	public DBClass createClass(final String name, final DBClass parent);
//	public void deleteClass(final CMClass cmClass);

	public void deleteClass(final DBClass dbClass);

	public Object create(DBEntry entry);
	public void update(DBEntry entry);
	public void delete(DBEntry entry);
	public CMQueryResult query(QuerySpecs query);
}
