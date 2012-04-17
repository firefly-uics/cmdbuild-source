package utils;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.driver.SelfVersioningDBDriver;
import org.cmdbuild.dao.entry.DBEntry;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBDomain;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.QuerySpecs;

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
		public void undo() {
			undoCommand();
			undoLog.remove(this);
		}
		protected abstract T execCommand();
		protected abstract void undoCommand();
	}

	private class CreateClass extends Command<DBClass> {
		private final String name;
		private final DBClass parent;
		private DBClass newClass;

		private CreateClass(final String name, final DBClass parent) {
			this.name = name;
			this.parent = parent;
		}

		@Override
		protected DBClass execCommand() {
			newClass = innerDriver.createClass(name, parent);
			return newClass;
		}

		@Override
		public void undoCommand() {
			innerDriver.deleteClass(newClass);
		}

		public DBClass getCreatedClass() {
			return newClass;
		}
	}
	
	private class CreateDomain extends Command<DBDomain> {
		private final String name;
		private final DBClass class1;
		private final DBClass class2;
		
		private DBDomain newDomain;

		private CreateDomain(final String name, final DBClass class1, final DBClass class2) {
			this.name = name;
			this.class1 = class1;
			this.class2 = class2;
		}

		@Override
		protected DBDomain execCommand() {
			newDomain = innerDriver.createDomain(name, class1, class2);
			return newDomain;
		}

		@Override
		public void undoCommand() {
			innerDriver.deleteDomain(newDomain);
		}
	}

	private class CreateEntry extends Command<Object> {

		private final DBEntry entry;

		public CreateEntry(DBEntry entry) {
			this.entry = entry;
		}

		@Override
		protected Object execCommand() {
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

	private class DeleteClass extends Command<Void> {

		private final DBClass classToDelete;

		public DeleteClass(DBClass c) {
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
			for (Undoable undoableCommand : undoLog) {
				if (undoableCommand instanceof CreateClass) {
					final CreateClass createClassCommand = (CreateClass) undoableCommand;
					if (createClassCommand.getCreatedClass().equals(classToDelete)) {
						undoLog.remove(createClassCommand);
						return;
					}
				}
			}
			throw new UnsupportedOperationException("Unsupported deletion of a class that has not been created in the test");
		}
	}

	private class DeleteDomain extends Command<Void> {

		private final DBDomain domainToDelete;

		public DeleteDomain(DBDomain d) {
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

	/*
	 * Driver interface
	 */

	private final Deque<Undoable> undoLog;
	private final DBDriver innerDriver;

	public GenericRollbackDriver(final DBDriver driver) {
		this.innerDriver = driver;
		this.undoLog = new ArrayDeque<Undoable>();
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
	public DBClass createClass(final String name, final DBClass parent) {
		return new CreateClass(name, parent).exec();
	}

	@Override
	public void deleteClass(final DBClass dbClass) {
		new DeleteClass(dbClass).exec();
	}

	@Override
	public DBClass findClassById(Object id) {
		return innerDriver.findClassById(id);
	}

	@Override
	public DBClass findClassByName(final String name) {
		return innerDriver.findClassByName(name);
	}

	@Override
	public Collection<DBDomain> findAllDomains() {
		return innerDriver.findAllDomains();
	}

	@Override
	public DBDomain createDomain(String name, DBClass class1, DBClass class2) {
		return new CreateDomain(name, class1, class2).exec();
	}

	@Override
	public void deleteDomain(DBDomain dbDomain) {
		new DeleteDomain(dbDomain).exec();
	}

	@Override
	public DBDomain findDomainById(final Object id) {
		return innerDriver.findDomainById(id);
	}

	@Override
	public DBDomain findDomainByName(final String name) {
		return innerDriver.findDomainByName(name);
	}

	@Override
	public Object create(DBEntry entry) {
		return new CreateEntry(entry).exec();
	}

	@Override
	public void update(DBEntry entry) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(DBEntry entry) {
		throw new UnsupportedOperationException();
	}

	@Override
	public CMQueryResult query(QuerySpecs query) {
		return innerDriver.query(query);
	}
}
