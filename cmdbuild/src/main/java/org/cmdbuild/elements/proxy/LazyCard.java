package org.cmdbuild.elements.proxy;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserOperations;

public class LazyCard extends CardForwarder implements Serializable {

	private static final long serialVersionUID = 1L;

	private int classId;
	private int cardId;
	private ITable lazyTable;

	/**
	 * With this constructor the behaviour is that of a CardForwarder
	 * 
	 * @param c
	 *            Card to be wrapped
	 */
	public LazyCard(final ICard c) {
		super(c);
		this.lazyTable = null;
	}

	/**
	 * Real Lazy initialization
	 * 
	 * @param classId
	 * @param id
	 */
	public LazyCard(final ITable table, final int id) {
		this.lazyTable = table;
		this.cardId = id;
	}

	public LazyCard(final int classId, final int id) {
		this.classId = classId;
		this.cardId = id;
	}

	/**
	 * Used to access the real object
	 */
	@Override
	protected ICard get() {
		if (c == null) {
			final ITable superClass = getSchemaForLoading();
			Log.PERSISTENCE.debug("Lazy loaded card requested: " + superClass.getId() + ", " + getId());
			try {
				c = superClass.cards().list().id(getId()).get();
			} catch (final NotFoundException e) {
				Log.PERSISTENCE.error("Cannot lazy load card: " + superClass.getId() + ", " + getId());
				throw e;
			}
		}
		// it might have been a superclass
		lazyTable = c.getSchema();
		classId = lazyTable.getId();
		return c;
	}

	private ITable getSchemaForLoading() {
		if (lazyTable != null)
			return lazyTable;
		else
			return UserOperations.from(UserContext.systemContext()).tables().get(classId);
	}

	@Override
	public int getId() {
		if (c != null)
			return c.getId();
		return cardId;
	}

	@Override
	public int getIdClass() {
		if (c != null)
			return c.getIdClass();
		if (cardId <= 0) {
			if (lazyTable != null) {
				return lazyTable.getId();
			} else {
				return classId;
			}
		}
		return getSchema().getId(); // it might be a superclass
	}

	@Override
	public ITable getSchema() {
		if (c != null)
			return c.getSchema();
		if (cardId <= 0) {
			return getSchemaForLoading();
		}
		if (lazyTable == null) {
			lazyTable = getSchemaForLoading();
		}
		if (lazyTable.isSuperClass()) {
			lazyTable = get().getSchema();
		}
		return lazyTable;
	}

	@Override
	public boolean equals(final Object o) {
		if (o instanceof ICard) {
			final ICard c = (ICard) o;
			return (classId == c.getIdClass() && cardId == c.getId());
		}
		return false;
	};

	@Override
	public int hashCode() {
		return String.valueOf(getIdClass()).hashCode() + String.valueOf(getId()).hashCode();
	}

	/**
	 * Serialization
	 */

	private void writeObject(final ObjectOutputStream out) throws IOException {
		out.writeInt(getIdClass());
		out.writeInt(getId());
	}

	private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
		this.classId = in.readInt();
		this.cardId = in.readInt();
	}
}
