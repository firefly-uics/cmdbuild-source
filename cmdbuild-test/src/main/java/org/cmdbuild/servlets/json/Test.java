package org.cmdbuild.servlets.json;

import org.cmdbuild.dao.backend.CMBackend;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.BaseSchema.CMTableType;
import org.cmdbuild.elements.interfaces.BaseSchema.Mode;
import org.cmdbuild.elements.interfaces.BaseSchema.SchemaStatus;
import org.cmdbuild.services.auth.AuthenticationFacade;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.servlets.utils.Parameter;


public class Test extends JSONBase {

	@JSONExported
	@Unauthorized
	public String login(
			@Parameter(value="username", required=true) String username) {
		AuthenticationFacade.loginAs(username);
		return "Logged in as " + username;
	}

	@JSONExported
	@Unauthorized
	public String resetDatabase() {
		CMBackend.INSTANCE.clearCache();
		return "Database reset";
	}

	@JSONExported
	@Unauthorized
	public String createBasicStructure() {
		createClass("A");
		createClass("B");
		createSuperclass("S");
		createChildClass("S", "S1");
		createChildClass("S", "S2");
		createDomain("AB", "A", "B");
		createDomain("AS", "A", "S");
		return "Created basic structure";
	}

	private void createClass(final String name) {
		createClass(ITable.BaseTable, name, false);
	}

	private void createSuperclass(final String name) {
		createClass(ITable.BaseTable, name, true);
	}

	private void createChildClass(final String parentName, final String name) {
		createClass(parentName, name, false);
	}

	private void createClass(final String parentName, final String name, boolean isSuperClass) {
		ITable t = UserContext.systemContext().tables().create();
		t.setName(name);
		t.setDescription(name);
		if (!parentName.isEmpty()) {
			t.setParent(parentName);
		}
		t.setSuperClass(isSuperClass);
		t.setTableType(CMTableType.CLASS);
		t.setMode(Mode.WRITE.toString());
		t.setStatus(SchemaStatus.ACTIVE);
		t.save();
	}
	
	private void createDomain(final String domainName, final String class1, final String class2) {
		IDomain d = UserContext.systemContext().domains().create();
		ITable t1 = UserContext.systemContext().tables().get(class1);
		ITable t2 = UserContext.systemContext().tables().get(class2);
		d.setName(domainName);
		d.setDescription(domainName);
		d.setClass1(t1);
		d.setClass2(t2);
		d.setCardinality(IDomain.CARDINALITY_N1);
		d.setDescriptionDirect("->");
		d.setDescriptionInverse("<-");
		d.setMode(Mode.WRITE.toString());
		d.setStatus(SchemaStatus.ACTIVE);
		d.save();
	}
}
