package org.cmdbuild.dao.backend.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.cmdbuild.dao.backend.postgresql.PGCMBackend;
import org.cmdbuild.elements.interfaces.DomainQuery;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.ITable;

public class TestCMBackend extends PGCMBackend {

	static int oidCounter = 0;

	@Override
	public int createTable(ITable table) {
		int oid = ++oidCounter;
		cache.addTable(oid, table);
		return oid;
	}

	@Override
	public int createDomain(IDomain domain) {
		int oid = ++oidCounter;
		cache.addDomain(oid, domain);
		return oid;
	}

	@Override
	public Iterator<IDomain> getDomainList(DomainQuery query) {
		List<IDomain> list = new ArrayList<IDomain>();
		for (IDomain d : cache.getDomainList()) {
			if (domainMatchesQuery(d, query)) {
				list.add(d);
			}
		}
		return list.iterator();
	}

	private boolean domainMatchesQuery(IDomain d, DomainQuery query) {
		final String tableName = query.getTableName();
		if (!d.getMode().isDisplayable()) {
			return false; // Map
		} else if (query.isInherited()) {
			return d.getClass1().treeBranch().contains(tableName)
				|| d.getClass2().treeBranch().contains(tableName);
		} else {
			return d.getClass1().getName().equals(tableName)
				|| d.getClass2().getName().equals(tableName);
		}
	}
}
