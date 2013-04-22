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
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.CharAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateTimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DoubleAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.EntryTypeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IpAddressAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringArrayAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.converter.MetadataConverter;
import org.cmdbuild.data.store.DataViewStore;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.logic.data.DataDefinitionLogic.MetadataAction.Visitor;
import org.cmdbuild.logic.data.DataDefinitionLogic.MetadataActions.Create;
import org.cmdbuild.logic.data.DataDefinitionLogic.MetadataActions.Delete;
import org.cmdbuild.logic.data.DataDefinitionLogic.MetadataActions.Update;
import org.cmdbuild.model.data.Attribute;
import org.cmdbuild.model.data.ClassOrder;
import org.cmdbuild.model.data.Domain;
import org.cmdbuild.model.data.EntryType;
import org.cmdbuild.model.data.Metadata;
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
	
	// TODO create and enum...
	public static final String CARDINALITY_11 = "1:1";
	public static final String CARDINALITY_N1 = "N:1";
	public static final String CARDINALITY_1N = "1:N";
	public static final String CARDINALITY_NN = "N:N";

	public static interface MetadataAction {

		public interface Visitor {

			void visit(Create action);

			void visit(Update action);

			void visit(Delete action);
		}

		void accept(Visitor visitor);

	}

	public static class MetadataActions {

		public static class Create implements MetadataAction {

			@Override
			public void accept(final Visitor visitor) {
				visitor.visit(this);
			}

		}

		public static class Update implements MetadataAction {

			@Override
			public void accept(final Visitor visitor) {
				visitor.visit(this);
			}

		}

		public static class Delete implements MetadataAction {

			@Override
			public void accept(final Visitor visitor) {
				visitor.visit(this);
			}

		}

		public static final MetadataAction CREATE = new Create();
		public static final MetadataAction UPDATE = new Update();
		public static final MetadataAction DELETE = new Delete();

	}

	private static CMClass NO_PARENT = null;

	private final CMDataView view;

	@Autowired
	public DataDefinitionLogic(@Qualifier("user") final CMDataView dataView) {
		this.view = dataView;
	}

	public CMDataView getView() {
		return view;
	}

	public CMClass createOrUpdate(final EntryType clazz) {
		logger.info("creating or updating class '{}'", clazz);

		final CMClass existingClass = view.findClass(clazz.getName());

		final Long parentId = clazz.getParentId();
		final CMClass parentClass = (parentId == null) ? NO_PARENT : view.findClass(parentId.longValue());

		final CMClass createdOrUpdatedClass;
		if (existingClass == null) {
			logger.info("class not already created, creating a new one");
			createdOrUpdatedClass = view.create(definitionForNew(clazz, parentClass));
		} else {
			logger.info("class already created, updating existing one");
			createdOrUpdatedClass = view.update(definitionForExisting(clazz, existingClass));
		}
		return createdOrUpdatedClass;
	}

	/**
	 * TODO: delete also privileges that refers to the deleted class
	 */
	public void deleteOrDeactivate(final String className) {
		logger.info("deleting class '{}'", className);
		final CMClass existingClass = view.findClass(className);
		if (existingClass == null) {
			logger.warn("class '{}' not found", className);
			return;
		}
		try {
			logger.warn("deleting existing class '{}'", className);
			view.delete(existingClass);
		} catch (final ORMException e) {
			logger.error("error deleting class", e);
			if (e.getExceptionType() == ORMExceptionType.ORM_CONTAINS_DATA) {
				logger.warn("class contains data");
				view.update(unactive(existingClass));
			}
			throw e;
		}

	}

	public CMAttribute createOrUpdate(final Attribute attribute) {
		logger.info("creating or updating attribute '{}'", attribute.toString());

		final CMEntryType owner = getOwnerByName(attribute.getOwner());
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

		logger.info("setting metadata for attribute '{}'", attribute.getName());
		final Map<MetadataAction, List<Metadata>> elementsByAction = attribute.getMetadata();
		final Store<Metadata> store = new DataViewStore<Metadata>(view,
				new MetadataConverter(createdOrUpdatedAttribute));
		for (final MetadataAction action : elementsByAction.keySet()) {
			final Iterable<Metadata> elements = elementsByAction.get(action);
			for (final Metadata element : elements) {
				action.accept(new Visitor() {

					@Override
					public void visit(final Create action) {
						store.create(element);
					}

					@Override
					public void visit(final Update action) {
						store.update(element);
					}

					@Override
					public void visit(final Delete action) {
						store.delete(element);
					}

				});
			}
		}

		return createdOrUpdatedAttribute;
	}

	private CMEntryType getOwnerByName(final String ownerName) {
		logger.debug("getting entry type with name '{}'", ownerName);
		CMEntryType entryType;

		// try with classes
		entryType = view.findClass(ownerName);
		if (entryType != null) {
			logger.debug("class '{}'", ownerName);
			return entryType;
		}

		// try with domains
		entryType = view.findDomain(ownerName);
		if (entryType != null) {
			logger.debug("domain '{}'", ownerName);
			return entryType;
		}

		logger.warn("owner with name '{}' not found", ownerName);
		throw ORMExceptionType.ORM_TYPE_ERROR.createException();
	}

	private void validate(final Attribute attribute) {
		new CMAttributeTypeVisitor() {

			@Override
			public void visit(final BooleanAttributeType attributeType) {
			}

			@Override
			public void visit(final CharAttributeType attributeType) {
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
				final CMIdentifier identifier = attributeType.getIdentifier();
				Validate.isTrue(identifier.getNameSpace() == CMIdentifier.DEFAULT_NAMESPACE,
						"non-default namespaces not supported at this level");
				final CMDomain domain = view.findDomain(identifier.getLocalName());
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

			@Override
			public void visit(final StringArrayAttributeType attributeType) {
			}

		} //
		.validate(attribute);
	}

	public void deleteOrDeactivate(final Attribute attribute) {
		logger.info("deleting attribute '{}'", attribute.toString());
		final CMEntryType owner = getOwnerByName(attribute.getOwner());
		final CMAttribute existingAttribute = owner.getAttribute(attribute.getName());
		if (existingAttribute == null) {
			logger.warn("attribute '{}' not found", attribute.getName());
			return;
		}
		try {
			logger.info("deleting metadata for attribute '{}'", attribute.getName());
			final Store<Metadata> store = new DataViewStore<Metadata>(view, new MetadataConverter(existingAttribute));
			final Iterable<Metadata> allMetadata = store.list();
			for (final Metadata metadata : allMetadata) {
				store.delete(metadata);
			}

			logger.info("deleting existing attribute '{}'", attribute.getName());
			view.delete(existingAttribute);
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
		final CMClass owner = view.findClass(attribute.getOwner());
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

		final CMClass owner = view.findClass(className);
		for (final CMAttribute attribute : owner.getAttributes()) {
			view.updateAttribute(definitionForClassOrdering(Attribute.newAttribute() //
					.withOwner(owner.getName()) //
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

		final CMDomain existing = view.findDomain(domain.getName());

		final CMDomain createdOrUpdated;
		if (existing == null) {
			logger.info("domain not already created, creating a new one");
			final CMClass class1 = view.findClass(domain.getIdClass1());
			final CMClass class2 = view.findClass(domain.getIdClass2());
			createdOrUpdated = view.create(definitionForNew(domain, class1, class2));
		} else {
			logger.info("domain already created, updating existing one");
			createdOrUpdated = view.update(definitionForExisting(domain, existing));
		}
		return createdOrUpdated;
	}

	public void deleteDomainByName(final String name) {
		logger.info("deleting domain '{}'", name);

		final CMDomain domain = view.findDomain(name);
		if (domain == null) {
			logger.warn("domain '{}' not found", name);
		} else {
			final boolean hasReference;
			final String cardinality = domain.getCardinality();
			if (asList(CARDINALITY_11, CARDINALITY_1N).contains(cardinality)) {
				final CMClass table = view.findClass(domain.getClass2().getName());
				hasReference = searchReference(table, domain);
			} else if (asList(CARDINALITY_11, CARDINALITY_N1).contains(cardinality)) {
				final CMClass table = view.findClass(domain.getClass1().getName());
				hasReference = searchReference(table, domain);
			} else {
				hasReference = false;
			}

			if (hasReference) {
				throw ORMExceptionType.ORM_DOMAIN_HAS_REFERENCE.createException();
			} else {
				view.delete(domain);
			}
		}
	}

	private static boolean searchReference(final CMClass table, final CMDomain domain) {
		for (final CMAttribute attribute : table.getAttributes()) {
			final CMAttributeType<?> attributeType = attribute.getType();
			if (attributeType instanceof ReferenceAttributeType) {
				final ReferenceAttributeType referenceAttributeType = ReferenceAttributeType.class.cast(attributeType);
				final String referenceDomainName = referenceAttributeType.getIdentifier().getLocalName();
				if (referenceDomainName.equals(domain.getIdentifier().getLocalName())) {
					return true;
				}
			}
		}
		return false;
	}

}
