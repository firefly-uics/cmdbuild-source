package org.cmdbuild.logic.data;

import static java.util.Arrays.asList;
import static org.cmdbuild.logic.data.Utils.definitionForClassOrdering;
import static org.cmdbuild.logic.data.Utils.definitionForExisting;
import static org.cmdbuild.logic.data.Utils.definitionForNew;
import static org.cmdbuild.logic.data.Utils.definitionForReordering;
import static org.cmdbuild.logic.data.Utils.unactive;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateTimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DoubleAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.EntryTypeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.GeometryAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IpAddressAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.model.data.Attribute;
import org.cmdbuild.model.data.Class;
import org.cmdbuild.model.data.ClassOrder;
import org.cmdbuild.model.data.Domain;
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
			validate(attribute);
			createdOrUpdatedAttribute = view.createAttribute(definitionForNew(attribute, owner));
		} else {
			logger.info("attribute already created, updating existing one");
			createdOrUpdatedAttribute = view.updateAttribute(definitionForExisting(attribute, existingAttribute));
		}
		return createdOrUpdatedAttribute;
	}

	private void validate(final Attribute attribute) {
		new CMAttributeTypeVisitor() {

			@Override
			public void visit(final BooleanAttributeType attributeType) {
			}

			@Override
			public void visit(final EntryTypeAttributeType attributeType) {
			}

			@Override
			public void visit(final DateTimeAttributeType attributeType) {
			}

			@Override
			public void visit(final DateAttributeType attributeType) {
			}

			@Override
			public void visit(final DecimalAttributeType attributeType) {
			}

			@Override
			public void visit(final DoubleAttributeType attributeType) {
			}

			@Override
			public void visit(final ForeignKeyAttributeType attributeType) {
			}

			@Override
			public void visit(final GeometryAttributeType attributeType) {
			}

			@Override
			public void visit(final IntegerAttributeType attributeType) {
			}

			@Override
			public void visit(final IpAddressAttributeType attributeType) {
			}

			@Override
			public void visit(final LookupAttributeType attributeType) {
			}

			@Override
			public void visit(final ReferenceAttributeType attributeType) {
				final CMDomain domain = view.findDomainByName(attributeType.domain);
				// TODO do it better, maybe using an enum for define cardinality
				Validate.isTrue(Arrays.asList("1:N", "N:1").contains(domain.getCardinality()));
			}

			@Override
			public void visit(final StringAttributeType attributeType) {
			}

			@Override
			public void visit(final TextAttributeType attributeType) {
			}

			@Override
			public void visit(final TimeAttributeType attributeType) {
			}

			public void validate(final Attribute attribute) {
				attribute.getType().accept(this);
			}

		} //
		.validate(attribute);
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
		logger.info("changing classorders '{}' for class '{}'", classOrders, className);

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

	public CMDomain createOrUpdate(final Domain domain) {
		logger.info("creating or updating domain '{}'", domain);

		final CMDomain existing = view.findDomainByName(domain.getName());

		final CMDomain createdOrUpdated;
		if (existing == null) {
			logger.info("domain not already created, creating a new one");
			final CMClass class1 = view.findClassById(domain.getIdClass1());
			final CMClass class2 = view.findClassById(domain.getIdClass2());
			createdOrUpdated = view.createDomain(definitionForNew(domain, class1, class2));
		} else {
			logger.info("domain already created, updating existing one");
			createdOrUpdated = view.updateDomain(definitionForExisting(domain, existing));
		}
		return createdOrUpdated;
	}

	public void deleteDomainByName(final String name) {
		logger.info("deleting domain '{}'", name);

		final CMDomain domain = view.findDomainByName(name);
		if (domain == null) {
			logger.warn("domain '{}' not found", name);
		} else {
			final boolean hasReference;
			final String cardinality = domain.getCardinality();
			if (asList(IDomain.CARDINALITY_11, IDomain.CARDINALITY_1N).contains(cardinality)) {
				final CMClass table = domain.getClass2();
				hasReference = searchReference(table, domain);
			} else if (asList(IDomain.CARDINALITY_11, IDomain.CARDINALITY_N1).contains(cardinality)) {
				final CMClass table = domain.getClass1();
				hasReference = searchReference(table, domain);
			} else {
				hasReference = false;
			}

			if (hasReference) {
				throw ORMExceptionType.ORM_DOMAIN_HAS_REFERENCE.createException();
			} else {
				view.deleteDomain(domain);
			}
		}
	}

	private static boolean searchReference(final CMClass table, final CMDomain domain) {
		for (final CMAttribute attribute : table.getAllAttributes()) {
			final CMAttributeType<?> attributeType = attribute.getType();
			if (attributeType instanceof ReferenceAttributeType) {
				final ReferenceAttributeType referenceAttributeType = ReferenceAttributeType.class.cast(attributeType);
				// TODO need to implement reference type
				// final IDomain attributeDom = attribute.getReferenceDomain();
				// if (attributeDom != null &&
				// (attributeDom.getName()).equals(domain.getName())) {
				// return true;
				// }
			}
		}
		return false;
	}

}
