package org.cmdbuild.logic.data;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.view.CMAttributeDefinition;
import org.cmdbuild.dao.view.CMClassDefinition;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.Logic;
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

	public CMClass createOrUpdateClass(final ClassDTO classDTO) {
		logger.info("creating or updating class: {}", classDTO);

		// TODO check here privileges
		final CMClass existingClass = view.findClassByName(classDTO.getName());

		final Long parentId = classDTO.getParentId();
		final CMClass parentClass = (parentId == null) ? NO_PARENT : view.findClassById(parentId.longValue());

		final CMClass createdOrUpdatedClass;
		// FIXME how be sure that is data view is a system data view
		if (existingClass == null) {
			logger.info("class not already created, creating a new one");
			createdOrUpdatedClass = view.createClass(definitionForNew(classDTO, parentClass));
		} else {
			logger.info("class already created, updating existing one");
			createdOrUpdatedClass = view.updateClass(definitionForExisting(classDTO, existingClass));
		}
		return createdOrUpdatedClass;
	}

	private CMClassDefinition definitionForNew(final ClassDTO classDTO, final CMClass parentClass) {
		return new CMClassDefinition() {

			@Override
			public Long getId() {
				return null;
			}

			@Override
			public String getName() {
				return classDTO.getName();
			}

			@Override
			public String getDescription() {
				return classDTO.getDescription();
			}

			@Override
			public CMClass getParent() {
				return parentClass;
			}

			@Override
			public boolean isSuperClass() {
				return classDTO.isSuperClass();
			}

			@Override
			public boolean isHoldingHistory() {
				return classDTO.isHoldingHistory();
			}

			@Override
			public boolean isActive() {
				return classDTO.isActive();
			}

		};
	}

	private CMClassDefinition definitionForExisting(final ClassDTO classDTO, final CMClass existingClass) {
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
				return classDTO.getDescription();
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
				return classDTO.isActive();
			}

		};
	}

	public CMAttribute createOrUpdateAttribute(final AttributeDTO attributeDTO) {
		logger.info("creating or updating attribute: {}", attributeDTO.toString());

		// TODO check here privileges
		final CMClass owner = view.findClassById(attributeDTO.getOwner());
		final CMAttribute existingAttribute = owner.getAttribute(attributeDTO.getName());

		final CMAttribute createdOrUpdatedAttribute;
		// FIXME how be sure that is data view is a system data view
		if (existingAttribute == null) {
			logger.info("attribute not already created, creating a new one");
			createdOrUpdatedAttribute = view.createAttribute(definitionForNew(attributeDTO, owner));
		} else {
			logger.info("attribute already created, updating existing one");
			createdOrUpdatedAttribute = view.updateAttribute(definitionForExisting(attributeDTO, existingAttribute));
		}
		return createdOrUpdatedAttribute;
	}

	private CMAttributeDefinition definitionForNew(final AttributeDTO attributeDTO, final CMEntryType owner) {
		return new CMAttributeDefinition() {

			@Override
			public String getName() {
				return attributeDTO.getName();
			}

			@Override
			public CMEntryType getOwner() {
				return owner;
			}

			@Override
			public CMAttributeType<?> getType() {
				return attributeDTO.getType();
			}

			@Override
			public String getDescription() {
				return attributeDTO.getDescription();
			}

			@Override
			public String getDefaultValue() {
				return attributeDTO.getDefaultValue();
			}

			@Override
			public boolean isDisplayableInList() {
				return attributeDTO.isDisplayableInList();
			}

			@Override
			public boolean isMandatory() {
				return attributeDTO.isMandatory();
			}

			@Override
			public boolean isUnique() {
				return attributeDTO.isUnique();
			}

			@Override
			public boolean isActive() {
				return attributeDTO.isActive();
			}

		};
	}

	private CMAttributeDefinition definitionForExisting(final AttributeDTO attributeDTO,
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
				return attributeDTO.getDescription();
			}

			@Override
			public String getDefaultValue() {
				// TODO
				return null;
			}

			@Override
			public boolean isDisplayableInList() {
				return attributeDTO.isDisplayableInList();
			}

			@Override
			public boolean isMandatory() {
				return attributeDTO.isMandatory();
			}

			@Override
			public boolean isUnique() {
				return attributeDTO.isUnique();
			}

			@Override
			public boolean isActive() {
				return attributeDTO.isActive();
			}

		};
	}

}
