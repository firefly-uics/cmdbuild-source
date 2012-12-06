package org.cmdbuild.logic.data;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMAttribute.Mode;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.view.CMAttributeDefinition;
import org.cmdbuild.dao.view.CMClassDefinition;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.model.data.Attribute;
import org.cmdbuild.model.data.Class;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Business Logic Layer for data definition.
 */
@Component
public class DataDefinitionLogic implements Logic {

	private static CMClass NO_PARENT = null;

	private final CMDataView view;

	@Autowired
	public DataDefinitionLogic(@Qualifier("user") final CMDataView dataView) {
		this.view = dataView;
	}

	public CMClass createOrUpdate(final Class clazz) {
		logger.info("creating or updating class: {}", clazz);

		final CMClass existingClass = view.findClassByName(clazz.getName());

		final Long parentId = clazz.getParentId();
		final CMClass parentClass = (parentId == null) ? NO_PARENT : view.findClassById(parentId.longValue());

		final CMClass createdOrUpdatedClass;
		if (existingClass == null) {
			logger.info("class not already created, creating a new one");
			createdOrUpdatedClass = view.createClass(definitionForNew(clazz, parentClass));
		} else {
			logger.info("class already created, updating existing one");
			createdOrUpdatedClass = view.updateClass(definitionForExisting(clazz, existingClass));
		}
		return createdOrUpdatedClass;
	}

	private CMClassDefinition definitionForNew(final Class clazz, final CMClass parentClass) {
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

	private CMClassDefinition definitionForExisting(final Class clazz, final CMClass existingClass) {
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

	public void deleteOrDeactivate(final Class clazz) {
		logger.info("deleting class: {}", clazz.toString());
		final CMClass existingClass = view.findClassByName(clazz.getName());
		if (existingClass == null) {
			logger.warn("class '{}' not found", clazz.getName());
			return;
		}
		try {
			logger.warn("deleting existing class '{}'", clazz.getName());
			view.deleteClass(existingClass);
		} catch (final ORMException e) {
			logger.error("error deleting class", e);
			if (e.getExceptionType() == ORMExceptionType.ORM_CONTAINS_DATA) {
				logger.warn("class contains data");
				view.updateClass(unactive(existingClass));
			}
			throw e;
		}

	}

	public CMAttribute createOrUpdate(final Attribute attribute) {
		logger.info("creating or updating attribute: {}", attribute.toString());

		final CMClass owner = view.findClassById(attribute.getOwner());
		final CMAttribute existingAttribute = owner.getAttribute(attribute.getName());

		final CMAttribute createdOrUpdatedAttribute;
		if (existingAttribute == null) {
			logger.info("attribute not already created, creating a new one");
			createdOrUpdatedAttribute = view.createAttribute(definitionForNew(attribute, owner));
		} else {
			logger.info("attribute already created, updating existing one");
			createdOrUpdatedAttribute = view.updateAttribute(definitionForExisting(attribute, existingAttribute));
		}
		return createdOrUpdatedAttribute;
	}

	public void deleteOrDeactivate(final Attribute attribute) {
		logger.info("deleting attribute: {}", attribute.toString());
		final CMClass owner = view.findClassById(attribute.getOwner());
		final CMAttribute existingAttribute = owner.getAttribute(attribute.getName());
		if (existingAttribute == null) {
			logger.warn("attribute '{}' not found", attribute.getName());
			return;
		}
		try {
			logger.warn("deleting existing attribute '{}'", attribute.getName());
			view.deleteAttribute(existingAttribute);
		} catch (final ORMException e) {
			logger.error("error deleting attribute", e);
			if (e.getExceptionType() == ORMExceptionType.ORM_CONTAINS_DATA) {
				logger.warn("attribute contains data");
				view.updateAttribute(unactive(existingAttribute));
			}
			throw e;
		}
	}

	private CMAttributeDefinition definitionForNew(final Attribute attribute, final CMEntryType owner) {
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

		};
	}

	private CMAttributeDefinition definitionForExisting(final Attribute attribute, final CMAttribute existingAttribute) {
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
				// TODO
				return null;
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

		};
	}

	private CMClassDefinition unactive(final CMClass existingClass) {
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

	private CMAttributeDefinition unactive(final CMAttribute existingAttribute) {
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
				// TODO
				return null;
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

		};
	}

}
