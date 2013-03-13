package org.cmdbuild.dao.legacywrappers;

import static com.google.common.collect.FluentIterable.from;
import static org.cmdbuild.dao.entrytype.DBIdentifier.fromName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryTypeVisitor;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.elements.interfaces.BaseSchema.CMTableType;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ICard.CardAttributes;
import org.cmdbuild.elements.interfaces.ITable;

import com.google.common.base.Function;

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
	public CMIdentifier getIdentifier() {
		return fromName(table.getName());
	}

	@Override
	public boolean isSuperclass() {
		return table.isSuperClass();
	}

	@Override
	public Iterable<? extends CMAttribute> getAllAttributes() {
		final Collection<IAttribute> iac = table.getAttributes().values();
		final List<CMAttribute> cmac = new ArrayList<CMAttribute>(iac.size());
		for (final IAttribute ia : iac) {
			if (ia.getMode().isCustom()) {
				cmac.add(new AttributeWrapper(ia));
			}
		}
		return cmac;
	}

	@Override
	public CMAttribute getAttribute(final String name) {
		return new AttributeWrapper(table.getAttribute(name));
	}

	@Override
	public Iterable<? extends CMAttribute> getAttributes() {
		final Collection<IAttribute> iac = table.getAttributes().values();
		final List<CMAttribute> cmac = new ArrayList<CMAttribute>(iac.size());
		for (final IAttribute ia : iac) {
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
	public boolean isBaseClass() {
		return !table.getMode().getModeString().equals("baseclass");
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
		return table.getPrivilegeId();
	}

	@Override
	public Iterable<? extends CMClass> getChildren() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterable<? extends CMClass> getLeaves() {
		return from(table.getChildren()).transform(new Function<ITable, CMClass>() {
			@Override
			public CMClass apply(final ITable input) {
				return new ClassWrapper(input);
			}
		});
	}

	@Override
	public CMClass getParent() {
		return new ClassWrapper(table.getParent());
	}

	@Override
	public boolean isAncestorOf(final CMClass cmClass) {
		for (CMClass parent = cmClass; parent != null; parent = parent.getParent()) {
			if (parent.equals(this)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean holdsHistory() {
		return (table.getTableType() != CMTableType.SIMPLECLASS);
	}

	@Override
	public String getKeyAttributeName() {
		return CardAttributes.Id.toString();
	}

	@Override
	public String getCodeAttributeName() {
		return CardAttributes.Code.toString();
	}

	@Override
	public String getDescriptionAttributeName() {
		return CardAttributes.Description.toString();
	}
}
