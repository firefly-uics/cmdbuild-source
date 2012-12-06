package org.cmdbuild.dao.view;

import static org.cmdbuild.dao.entrytype.Deactivable.IsActivePredicate.filterActive;

import java.util.List;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMAttribute.Mode;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBDomain;
import org.cmdbuild.dao.entrytype.DBEntryType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.QuerySpecs;

import com.google.common.collect.Lists;

public class DBDataView extends QueryExecutorDataView {

	public static interface DBClassDefinition extends CMClassDefinition {

		@Override
		public DBClass getParent();

	}

	public static interface DBAttributeDefinition extends CMAttributeDefinition {

		@Override
		DBEntryType getOwner();

	}

	private final DBDriver driver;

	public DBDataView(final DBDriver driver) {
		this.driver = driver;
	}

	@Override
	public DBClass findClassById(final Long id) {
		return driver.findClassById(id);
	}

	@Override
	public DBClass findClassByName(final String name) {
		return driver.findClassByName(name);
	}

	@Override
	public Iterable<DBClass> findClasses() {
		return filterActive(findAllClasses());
	}

	@Override
	public Iterable<DBClass> findAllClasses() {
		return driver.findAllClasses();
	}

	@Override
	public DBClass createClass(final CMClassDefinition definition) {
		return driver.createClass(adaptDefinition(definition));
	}

	@Override
	public DBClass updateClass(final CMClassDefinition definition) {
		return driver.updateClass(adaptDefinition(definition));
	}

	@Override
	public void deleteClass(final CMClass cmClass) {
		driver.deleteClass(cmToDbClass(cmClass));
	}

	private DBClassDefinition adaptDefinition(final CMClassDefinition definition) {
		return new DBClassDefinition() {

			@Override
			public Long getId() {
				return definition.getId();
			}

			@Override
			public String getName() {
				return definition.getName();
			}

			@Override
			public String getDescription() {
				return definition.getDescription();
			}

			@Override
			public DBClass getParent() {
				return cmToDbClass(definition.getParent());
			}

			@Override
			public boolean isSuperClass() {
				return definition.isSuperClass();
			}

			@Override
			public boolean isActive() {
				return definition.isActive();
			}

			@Override
			public boolean isHoldingHistory() {
				return definition.isHoldingHistory();
			}

		};
	}

	@Override
	public DBAttribute createAttribute(final CMAttributeDefinition definition) {
		return driver.createAttribute(adaptDefinition(definition));
	}

	@Override
	public DBAttribute updateAttribute(final CMAttributeDefinition definition) {
		return driver.updateAttribute(adaptDefinition(definition));
	}

	private DBAttributeDefinition adaptDefinition(final CMAttributeDefinition definition) {
		return new DBAttributeDefinition() {

			@Override
			public String getName() {
				return definition.getName();
			}

			@Override
			public DBClass getOwner() {
				return cmToDbClass(definition.getOwner());
			}

			@Override
			public CMAttributeType<?> getType() {
				return definition.getType();
			}

			@Override
			public String getDescription() {
				return definition.getDescription();
			}

			@Override
			public String getDefaultValue() {
				return definition.getDefaultValue();
			}

			@Override
			public boolean isDisplayableInList() {
				return definition.isDisplayableInList();
			}

			@Override
			public boolean isMandatory() {
				return definition.isMandatory();
			}

			@Override
			public boolean isUnique() {
				return definition.isUnique();
			}

			@Override
			public boolean isActive() {
				return definition.isActive();
			}

			@Override
			public Mode getMode() {
				return definition.getMode();
			}

		};
	}

	private DBClass cmToDbClass(final CMEntryType entryType) {
		final DBClass dbClass;
		if (entryType == null) {
			dbClass = null;
		} else if (entryType instanceof DBClass) {
			dbClass = DBClass.class.cast(entryType);
		} else {
			dbClass = findClassByName(entryType.getName());
			assert dbClass != null;
		}
		return dbClass;
	}

	@Override
	public void deleteAttribute(final CMAttribute attribute) {
		driver.deleteAttribute(cmToDbAttribute(attribute));
	}

	private DBAttribute cmToDbAttribute(final CMAttribute attribute) {
		final DBAttribute dbAttribute;
		if (attribute == null) {
			dbAttribute = null;
		} else if (attribute instanceof DBClass) {
			dbAttribute = DBAttribute.class.cast(attribute);
		} else {
			final DBClass owner = cmToDbClass(findClassByName(attribute.getOwner().getName()));
			dbAttribute = owner.getAttribute(attribute.getName());
			assert dbAttribute != null;
		}
		return dbAttribute;
	}

	@Override
	public Iterable<DBDomain> findDomains() {
		return filterActive(findAllDomains());
	}

	@Override
	public Iterable<DBDomain> findAllDomains() {
		return driver.findAllDomains();
	}

	@Override
	public Iterable<DBDomain> findDomainsFor(final CMClass cmClass) {
		final List<DBDomain> domainsForClass = Lists.newArrayList();
		for (final DBDomain d : findDomains()) {
			if (d.getClass1().isAncestorOf(cmClass) || d.getClass2().isAncestorOf(cmClass)) {
				domainsForClass.add(d);
			}
		}
		return domainsForClass;
	}

	@Override
	public DBDomain findDomainById(final Long id) {
		return driver.findDomainById(id);
	}

	@Override
	public DBDomain findDomainByName(final String name) {
		return driver.findDomainByName(name);
	}

	@Override
	public Iterable<? extends CMFunction> findAllFunctions() {
		return driver.findAllFunctions();
	}

	@Override
	public CMFunction findFunctionByName(final String name) {
		return driver.findFunctionByName(name);
	}

	@Override
	public DBCard newCard(final CMClass type) {
		final DBClass dbType = findClassById(type.getId());
		return DBCard.newInstance(driver, dbType);
	}

	@Override
	public DBCard modifyCard(final CMCard type) {
		throw new UnsupportedOperationException();
	}

	@Override
	public CMQueryResult executeNonEmptyQuery(final QuerySpecs querySpecs) {
		return driver.query(querySpecs);
	}

}
