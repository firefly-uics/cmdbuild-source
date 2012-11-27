package utils;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.driver.SelfVersioningDBDriver;
import org.cmdbuild.dao.entry.DBEntry;
import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBDomain;
import org.cmdbuild.dao.entrytype.DBEntryType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.function.DBFunction;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.view.DBDataView.DBAttributeDefinition;
import org.cmdbuild.dao.view.DBDataView.DBClassDefinition;

public class GenericRollbackDriver implements DBDriver {

	private interface Undoable {

		public void undo();

	}

	private abstract class Command<T> implements Undoable {

		public T exec() {
			final T out = execCommand();
			undoLog.add(this);
			return out;
		}

		@Override
		public void undo() {
			undoCommand();
			undoLog.remove(this);
		}

		protected abstract T execCommand();

		protected abstract void undoCommand();
	}

	private class CreateClass extends Command<DBClass> {

		private final DBClassDefinition definition;

		private DBClass createdClass;

		private CreateClass(final DBClassDefinition definition) {
			this.definition = definition;
		}

		@Override
		protected DBClass execCommand() {
			createdClass = innerDriver.createClass(definition);
			return createdClass;
		}

		@Override
		public void undoCommand() {
			innerDriver.deleteClass(createdClass);
		}

		public DBClass getCreatedClass() {
			return createdClass;
		}

	}

	private class UpdateClass extends Command<DBClass> {

		private final DBClassDefinition definition;

		private DBClassDefinition previousDefinition;
		private DBClass updatedClass;

		private UpdateClass(final DBClassDefinition definition) {
			this.definition = definition;
		}

		@Override
		protected DBClass execCommand() {
			storePreviousData();
			updatedClass = innerDriver.updateClass(definition);
			return updatedClass;
		}

		private void storePreviousData() {
			final DBClass existingClass = innerDriver.findClassByName(definition.getName());
			previousDefinition = new DBClassDefinition() {

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
				public DBClass getParent() {
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
					return existingClass.isActive();
				}

			};
		}

		@Override
		public void undoCommand() {
			innerDriver.updateClass(previousDefinition);
		}

		public DBClass getUpdatedClass() {
			return updatedClass;
		}

	}

	private class DeleteClass extends Command<Void> {

		private final DBClass classToDelete;

		public DeleteClass(final DBClass c) {
			this.classToDelete = c;
		}

		@Override
		protected Void execCommand() {
			innerDriver.deleteClass(classToDelete);
			return null;
		}

		@Override
		public void undoCommand() {
			// Remove the
			for (final Undoable undoableCommand : undoLog) {
				if (undoableCommand instanceof CreateClass) {
					final CreateClass createClassCommand = (CreateClass) undoableCommand;
					if (createClassCommand.getCreatedClass().equals(classToDelete)) {
						undoLog.remove(createClassCommand);
						return;
					}
				}
			}
			throw new UnsupportedOperationException(
					"Unsupported deletion of a class that has not been created in the test");
		}
	}

	private class CreateAttribute extends Command<DBAttribute> {

		private final DBAttributeDefinition definition;

		private DBAttribute createdAttribute;

		private CreateAttribute(final DBAttributeDefinition definition) {
			this.definition = definition;
		}

		@Override
		protected DBAttribute execCommand() {
			createdAttribute = innerDriver.createAttribute(definition);
			return createdAttribute;
		}

		@Override
		public void undoCommand() {
			// TODO
			// innerDriver.deleteAttribute(createdAttribute);
		}

		public DBAttribute getCreatedAttribute() {
			return createdAttribute;
		}

	}

	private class UpdateAttribute extends Command<DBAttribute> {

		private final DBAttributeDefinition definition;

		private DBAttributeDefinition previousDefinition;
		private DBAttribute updatedAttribute;

		private UpdateAttribute(final DBAttributeDefinition definition) {
			this.definition = definition;
		}

		@Override
		protected DBAttribute execCommand() {
			storePreviousData();
			updatedAttribute = innerDriver.updateAttribute(definition);
			return updatedAttribute;
		}

		private void storePreviousData() {
			final DBClass existingClass = innerDriver.findClassByName(definition.getOwner().getName());
			final DBAttribute existingAttribute = existingClass.getAttribute(definition.getName());
			previousDefinition = new DBAttributeDefinition() {

				@Override
				public String getName() {
					return existingAttribute.getName();
				}

				@Override
				public DBEntryType getOwner() {
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
					return existingAttribute.isActive();
				}

			};
		}

		@Override
		public void undoCommand() {
			innerDriver.updateAttribute(previousDefinition);
		}

		public DBAttribute getUpdatedAttribute() {
			return updatedAttribute;
		}

	}

	private class CreateDomain extends Command<DBDomain> {

		private final DomainDefinition domainDefinition;

		private DBDomain newDomain;

		private CreateDomain(final DomainDefinition domainDefinition) {
			this.domainDefinition = domainDefinition;
		}

		@Override
		protected DBDomain execCommand() {
			newDomain = innerDriver.createDomain(domainDefinition);
			return newDomain;
		}

		@Override
		public void undoCommand() {
			innerDriver.deleteDomain(newDomain);
		}
	}

	private class DeleteDomain extends Command<Void> {

		private final DBDomain domainToDelete;

		public DeleteDomain(final DBDomain d) {
			this.domainToDelete = d;
		}

		@Override
		protected Void execCommand() {
			innerDriver.deleteDomain(domainToDelete);
			return null;
		}

		@Override
		protected void undoCommand() {
			throw new UnsupportedOperationException("Not implemented");
		}
	}

	private class CreateEntry extends Command<Long> {

		private final DBEntry entry;

		public CreateEntry(final DBEntry entry) {
			this.entry = entry;
		}

		@Override
		protected Long execCommand() {
			return innerDriver.create(entry);
		}

		@Override
		public void undoCommand() {
			if (innerDriver instanceof SelfVersioningDBDriver) {
				final SelfVersioningDBDriver svd = (SelfVersioningDBDriver) innerDriver;
				svd.clearEntryType(entry.getType());
			} else {
				throw new UnsupportedOperationException();
			}
		}
	}

	/*
	 * Driver interface
	 */

	private final DBDriver innerDriver;
	private final Deque<Undoable> undoLog;

	public GenericRollbackDriver(final DBDriver driver) {
		this.innerDriver = driver;
		this.undoLog = new ArrayDeque<Undoable>();
	}

	public DBDriver getInnerDriver() {
		return innerDriver;
	}

	public void rollback() {
		while (!undoLog.isEmpty()) {
			undoLog.getLast().undo();
		}
	}

	@Override
	public Collection<DBClass> findAllClasses() {
		return innerDriver.findAllClasses();
	}

	@Override
	public DBClass findClassById(final Long id) {
		return innerDriver.findClassById(id);
	}

	@Override
	public DBClass findClassByName(final String name) {
		return innerDriver.findClassByName(name);
	}

	@Override
	public DBClass createClass(final DBClassDefinition definition) {
		return new CreateClass(definition).exec();
	}

	@Override
	public DBClass updateClass(final DBClassDefinition definition) {
		return new UpdateClass(definition).exec();
	}

	@Override
	public void deleteClass(final DBClass dbClass) {
		new DeleteClass(dbClass).exec();
	}

	@Override
	public DBAttribute createAttribute(final DBAttributeDefinition definition) {
		return new CreateAttribute(definition).exec();
	}

	@Override
	public DBAttribute updateAttribute(final DBAttributeDefinition definition) {
		return new UpdateAttribute(definition).exec();
	}

	@Override
	public Collection<DBDomain> findAllDomains() {
		return innerDriver.findAllDomains();
	}

	@Override
	public DBDomain createDomain(final DomainDefinition domainDefinition) {
		return new CreateDomain(domainDefinition).exec();
	}

	@Override
	public void deleteDomain(final DBDomain dbDomain) {
		new DeleteDomain(dbDomain).exec();
	}

	@Override
	public DBDomain findDomainById(final Long id) {
		return innerDriver.findDomainById(id);
	}

	@Override
	public DBDomain findDomainByName(final String name) {
		return innerDriver.findDomainByName(name);
	}

	@Override
	public Collection<DBFunction> findAllFunctions() {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public DBFunction findFunctionByName(final String name) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public Long create(final DBEntry entry) {
		return new CreateEntry(entry).exec();
	}

	@Override
	public void update(final DBEntry entry) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(final DBEntry entry) {
		throw new UnsupportedOperationException();
	}

	@Override
	public CMQueryResult query(final QuerySpecs query) {
		return innerDriver.query(query);
	}

}
