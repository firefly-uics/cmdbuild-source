package org.cmdbuild.logic.data;

import static org.cmdbuild.logic.data.Utils.definitionForClassOrdering;
import static org.cmdbuild.logic.data.Utils.definitionForExisting;
import static org.cmdbuild.logic.data.Utils.definitionForNew;
import static org.cmdbuild.logic.data.Utils.definitionForReordering;
import static org.cmdbuild.logic.data.Utils.unactive;

import java.util.List;
import java.util.Map;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.model.data.Attribute;
import org.cmdbuild.model.data.Class;
import org.cmdbuild.model.data.ClassOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

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
		logger.info("creating or updating class '{}'", clazz);

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

	public void deleteOrDeactivate(final Class clazz) {
		logger.info("deleting class '{}'", clazz.toString());
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
		logger.info("creating or updating attribute '{}'", attribute.toString());

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
		logger.info("deleting attribute '{}'", attribute.toString());
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

	public void reorder(final Attribute attribute) {
		logger.info("reordering attribute '{}'", attribute.toString());
		final CMClass owner = view.findClassById(attribute.getOwner());
		final CMAttribute existingAttribute = owner.getAttribute(attribute.getName());
		if (existingAttribute == null) {
			logger.warn("attribute '{}' not found", attribute.getName());
			return;
		}
		view.updateAttribute(definitionForReordering(attribute, existingAttribute));
	}

	public void changeClassOrders(final String className, final List<ClassOrder> classOrders) {
		logger.info("changing classorder for class '{}'", className);

		final Map<String, ClassOrder> mappedClassOrders = Maps.uniqueIndex(classOrders,
				new Function<ClassOrder, String>() {
					@Override
					public String apply(final ClassOrder input) {
						return input.attributeName;
					}
				});

		final CMClass owner = view.findClassByName(className);
		for (final CMAttribute attribute : owner.getAllAttributes()) {
			view.updateAttribute(definitionForClassOrdering(Attribute.newAttribute() //
					.withOwner(owner.getId()) //
					.withName(attribute.getName()) //
					.withClassOrder(valueOrDefaultIfNull(mappedClassOrders.get(attribute.getName()))) //
					.build(), //
					attribute));
		}
	}

	private int valueOrDefaultIfNull(final ClassOrder classOrder) {
		return (classOrder == null) ? 0 : classOrder.value;
	}

}
