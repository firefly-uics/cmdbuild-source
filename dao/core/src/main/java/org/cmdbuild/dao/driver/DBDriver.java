package org.cmdbuild.dao.driver;

import java.util.Collection;

import org.cmdbuild.dao.entry.DBEntry;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBDomain;
import org.cmdbuild.dao.function.DBFunction;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.QuerySpecs;

public interface DBDriver {

	Collection<DBClass> findAllClasses();
	DBClass findClassById(Long id);
	DBClass findClassByName(String name);

	DBClass createClass(String name, DBClass parent);
	void deleteClass(DBClass dbClass);

	Collection<DBDomain> findAllDomains();
	DBDomain findDomainById(Long id);
	DBDomain findDomainByName(String name);

	Collection<DBFunction> findAllFunctions();
	DBFunction findFunctionByName(String name);

	DBDomain createDomain(String name, DBClass class1, DBClass class2); // TODO Allow more than two classes
	void deleteDomain(DBDomain dbDomain);

	Long create(DBEntry entry);
	void update(DBEntry entry);
	void delete(DBEntry entry);
	CMQueryResult query(QuerySpecs query);
}
