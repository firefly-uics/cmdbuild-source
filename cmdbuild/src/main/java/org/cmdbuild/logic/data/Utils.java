package org.cmdbuild.logic.data;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMAttribute.Mode;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMClass.CMClassDefinition;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMDomain.CMDomainDefinition;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.view.CMAttributeDefinition;
import org.cmdbuild.model.data.Attribute;
import org.cmdbuild.model.data.Class;
import org.cmdbuild.model.data.Domain;

class Utils {

	private Utils() {
		// prevents instantiation
	}

	public static CMClassDefinition definitionForNew(final Class clazz, final CMClass parentClass) {
		return new CMClassDefinition() {

			@Override
			public Long getId() {
				return null;
			}

			@Override
			public String getName() {
				return clazz.getName();
			}

			@Override
			public String getDescription() {
				return clazz.getDescription();
			}

			@Override
			public CMClass getParent() {
				return parentClass;
			}

			@Override
			public boolean isSuperClass() {
				return clazz.isSuperClass();
			}

			@Override
			public boolean isHoldingHistory() {
				return clazz.isHoldingHistory();
			}

			@Override
			public boolean isActive() {
				return clazz.isActive();
			}

		};
	}

	public static CMClassDefinition definitionForExisting(final Class clazz, final CMClass existingClass) {
		return new CMClassDefinition() {

			@Override
			public Long getId() {
				return existingClass.getId();
			}

			@Override
			public String getName() {
				return existingClass.getName();
			}

			@Override
			public String getDescription() {
				return clazz.getDescription();
			}

			@Override
			public CMClass getParent() {
				return existingClass.getParent();
			}

			@Override
			public boolean isSuperClass() {
				return existingClass.isSuperclass();
			}

			@Override
			public boolean isHoldingHistory() {
				return existingClass.holdsHistory();
			}

			@Override
			public boolean isActive() {
				return clazz.isActive();
			}

		};
	}

	public static CMClassDefinition unactive(final CMClass existingClass) {
		return new CMClassDefinition() {

			@Override
			public Long getId() {
				return existingClass.getId();
			}

			@Override
			public String getName() {
				return existingClass.getName();
			}

			@Override
			public String getDescription() {
				return existingClass.getDescription();
			}

			@Override
			public CMClass getParent() {
				return existingClass.getParent();
			}

			@Override
			public boolean isSuperClass() {
				return existingClass.isSuperclass();
			}

			@Override
			public boolean isHoldingHistory() {
				return existingClass.holdsHistory();
			}

			@Override
			public boolean isActive() {
				return false;
			}

		};
	}

	public static CMAttributeDefinition definitionForNew(final Attribute attribute, final CMEntryType owner) {
		return new CMAttributeDefinition() {

			@Override
			public String getName() {
				return attribute.getName();
			}

			@Override
			public CMEntryType getOwner() {
				return owner;
			}

			@Override
			public CMAttributeType<?> getType() {
				return attribute.getType();
			}

			@Override
			public String getDescription() {
				return attribute.getDescription();
			}

			@Override
			public String getDefaultValue() {
				return attribute.getDefaultValue();
			}

			@Override
			public boolean isDisplayableInList() {
				return attribute.isDisplayableInList();
			}

			@Override
			public boolean isMandatory() {
				return attribute.isMandatory();
			}

			@Override
			public boolean isUnique() {
				return attribute.isUnique();
			}

			@Override
			public boolean isActive() {
				return attribute.isActive();
			}

			@Override
			public Mode getMode() {
				return attribute.getMode();
			}

			@Override
			public int getIndex() {
				return attribute.getIndex();
			}

			@Override
			public String getGroup() {
				return attribute.getGroup();
			}

			@Override
			public int getClassOrder() {
				return attribute.getClassOrder();
			}

		};
	}

	public static CMAttributeDefinition definitionForExisting(final Attribute attribute,
			final CMAttribute existingAttribute) {
		return new CMAttributeDefinition() {

			@Override
			public String getName() {
				return existingAttribute.getName();
			}

			@Override
			public CMEntryType getOwner() {
				return existingAttribute.getOwner();
			}

			@Override
			public CMAttributeType<?> getType() {
				return existingAttribute.getType();
			}

			@Override
			public String getDescription() {
				return attribute.getDescription();
			}

			@Override
			public String getDefaultValue() {
				return existingAttribute.getDefaultValue();
			}

			@Override
			public boolean isDisplayableInList() {
				return attribute.isDisplayableInList();
			}

			@Override
			public boolean isMandatory() {
				return attribute.isMandatory();
			}

			@Override
			public boolean isUnique() {
				return attribute.isUnique();
			}

			@Override
			public boolean isActive() {
				return attribute.isActive();
			}

			@Override
			public Mode getMode() {
				return attribute.getMode();
			}

			@Override
			public int getIndex() {
				return existingAttribute.getIndex();
			}

			@Override
			public String getGroup() {
				return attribute.getGroup();
			}

			@Override
			public int getClassOrder() {
				return existingAttribute.getClassOrder();
			}

		};
	}

	public static CMAttributeDefinition definitionForReordering(final Attribute attribute,
			final CMAttribute existingAttribute) {
		return new CMAttributeDefinition() {

			@Override
			public String getName() {
				return existingAttribute.getName();
			}

			@Override
			public CMEntryType getOwner() {
				return existingAttribute.getOwner();
			}

			@Override
			public CMAttributeType<?> getType() {
				return existingAttribute.getType();
			}

			@Override
			public String getDescription() {
				return existingAttribute.getDescription();
			}

			@Override
			public String getDefaultValue() {
				return existingAttribute.getDefaultValue();
			}

			@Override
			public boolean isDisplayableInList() {
				return existingAttribute.isDisplayableInList();
			}

			@Override
			public boolean isMandatory() {
				return existingAttribute.isMandatory();
			}

			@Override
			public boolean isUnique() {
				return existingAttribute.isUnique();
			}

			@Override
			public boolean isActive() {
				return existingAttribute.isActive();
			}

			@Override
			public Mode getMode() {
				return existingAttribute.getMode();
			}

			@Override
			public int getIndex() {
				return attribute.getIndex();
			}

			@Override
			public String getGroup() {
				return existingAttribute.getGroup();
			}

			@Override
			public int getClassOrder() {
				return existingAttribute.getClassOrder();
			}

		};
	}

	public static CMAttributeDefinition definitionForClassOrdering(final Attribute attribute,
			final CMAttribute existingAttribute) {
		return new CMAttributeDefinition() {

			@Override
			public String getName() {
				return existingAttribute.getName();
			}

			@Override
			public CMEntryType getOwner() {
				return existingAttribute.getOwner();
			}

			@Override
			public CMAttributeType<?> getType() {
				return existingAttribute.getType();
			}

			@Override
			public String getDescription() {
				return existingAttribute.getDescription();
			}

			@Override
			public String getDefaultValue() {
				return existingAttribute.getDefaultValue();
			}

			@Override
			public boolean isDisplayableInList() {
				return existingAttribute.isDisplayableInList();
			}

			@Override
			public boolean isMandatory() {
				return existingAttribute.isMandatory();
			}

			@Override
			public boolean isUnique() {
				return existingAttribute.isUnique();
			}

			@Override
			public boolean isActive() {
				return existingAttribute.isActive();
			}

			@Override
			public Mode getMode() {
				return existingAttribute.getMode();
			}

			@Override
			public int getIndex() {
				return existingAttribute.getIndex();
			}

			@Override
			public String getGroup() {
				return existingAttribute.getGroup();
			}

			@Override
			public int getClassOrder() {
				return attribute.getClassOrder();
			}

		};
	}

	public static CMAttributeDefinition unactive(final CMAttribute existingAttribute) {
		return new CMAttributeDefinition() {

			@Override
			public String getName() {
				return existingAttribute.getName();
			}

			@Override
			public CMEntryType getOwner() {
				return existingAttribute.getOwner();
			}

			@Override
			public CMAttributeType<?> getType() {
				return existingAttribute.getType();
			}

			@Override
			public String getDescription() {
				return existingAttribute.getDescription();
			}

			@Override
			public String getDefaultValue() {
				return existingAttribute.getDefaultValue();
			}

			@Override
			public boolean isDisplayableInList() {
				return existingAttribute.isDisplayableInList();
			}

			@Override
			public boolean isMandatory() {
				return existingAttribute.isMandatory();
			}

			@Override
			public boolean isUnique() {
				return existingAttribute.isUnique();
			}

			@Override
			public boolean isActive() {
				return false;
			}

			@Override
			public Mode getMode() {
				return existingAttribute.getMode();
			}

			@Override
			public int getIndex() {
				return existingAttribute.getIndex();
			}

			@Override
			public String getGroup() {
				return existingAttribute.getGroup();
			}

			@Override
			public int getClassOrder() {
				return existingAttribute.getClassOrder();
			}

		};
	}

	public static CMDomainDefinition definitionForNew(final Domain domain, final CMClass class1, final CMClass class2) {
		return new CMDomainDefinition() {

			@Override
			public Long getId() {
				return null;
			}

			@Override
			public String getName() {
				return domain.getName();
			}

			@Override
			public CMClass getClass1() {
				return class1;
			}

			@Override
			public CMClass getClass2() {
				return class2;
			}

			@Override
			public String getDirectDescription() {
				return domain.getDirectDescription();
			}

			@Override
			public String getInverseDescription() {
				return domain.getInverseDescription();
			}

			@Override
			public String getCardinality() {
				return domain.getCardinality();
			}

			@Override
			public boolean isMasterDetail() {
				return domain.isMasterDetail();
			}

			@Override
			public String getMasterDetailDescription() {
				return domain.getMasterDetailDescription();
			}

		};
	}

	public static CMDomainDefinition definitionForExisting(final Domain domain, final CMDomain existing) {
		return new CMDomainDefinition() {

			@Override
			public Long getId() {
				return existing.getId();
			}

			@Override
			public String getName() {
				return existing.getName();
			}

			@Override
			public CMClass getClass1() {
				return existing.getClass1();
			}

			@Override
			public CMClass getClass2() {
				return existing.getClass2();
			}

			@Override
			public String getDirectDescription() {
				return domain.getDirectDescription();
			}

			@Override
			public String getInverseDescription() {
				return domain.getInverseDescription();
			}

			@Override
			public String getCardinality() {
				return existing.getCardinality();
			}

			@Override
			public boolean isMasterDetail() {
				return existing.isMasterDetail();
			}

			@Override
			public String getMasterDetailDescription() {
				return domain.getMasterDetailDescription();
			}

		};
	}

}
