package org.cmdbuild.dao.legacywrappers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryTypeVisitor;
import org.cmdbuild.elements.interfaces.BaseSchema.CMTableType;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.exception.NotFoundException;

public class ClassWrapper implements CMClass {

	final ITable table;

	public ClassWrapper(final ITable table) {
		this.table = table;
	}

	@Override
	public String getDescription() {
		return table.getDescription();
	}

	@Override
	public Long getId() {
		return Long.valueOf(table.getId());
	}

	@Override
	public String getName() {
		return table.getName();
	}

	@Override
	public boolean isSuperclass() {
		return table.isSuperClass();
	}

	@Override
	public Iterable<? extends CMAttribute> getAllAttributes() {
		Collection<IAttribute> iac = table.getAttributes().values();
		List<CMAttribute> cmac = new ArrayList<CMAttribute>(iac.size());
		for (IAttribute ia : iac) {
			if (ia.getMode().isCustom()) {
				cmac.add(new AttributeWrapper(ia));
			}
		}
		return cmac;
	}

	@Override
	public CMAttribute getAttribute(String name) {
		try {
			return new AttributeWrapper(table.getAttribute(name));
		} catch (NotFoundException e) {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public Iterable<? extends CMAttribute> getAttributes() {
		Collection<IAttribute> iac = table.getAttributes().values();
		List<CMAttribute> cmac = new ArrayList<CMAttribute>(iac.size());
		for (IAttribute ia : iac) {
			if (ia.getMode().isCustom() && ia.getStatus().isActive()) {
				cmac.add(new AttributeWrapper(ia));
			}
		}
		return cmac;
	}

	@Override
	public boolean isSystem() {
		return !table.getMode().isCustom();
	}

	@Override
	public boolean isActive() {
		return table.getStatus().isActive();
	}

	@Override
	public void accept(final CMEntryTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String getPrivilegeId() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterable<? extends CMClass> getChildren() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterable<? extends CMClass> getLeaves() {
		throw new UnsupportedOperationException();
	}

	@Override
	public CMClass getParent() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isAncestorOf(CMClass cmClass) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean holdsHistory() {
		return (table.getTableType() != CMTableType.SIMPLECLASS);
	}

}
