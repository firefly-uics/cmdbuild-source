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

public class LazyCard extends CardForwarder implements Serializable {

	private static final long serialVersionUID = 1L;

	private int classId;
	private int cardId;
	private ITable lazyTable;

	/**
	 * With this constructor the behaviour is that of a CardForwarder
	 * @param c Card to be wrapped
	 */
	public LazyCard(ICard c) {
		super(c);
		this.lazyTable = null;
	}

	/**
	 * Real Lazy initialization
	 * 
	 * @param classId
	 * @param id
	 */
	public LazyCard(ITable table, int id) {
		this.lazyTable = table;
		this.cardId = id;
	}

	public LazyCard(int classId, int id) {
		this.classId = classId;
		this.cardId = id;
	}

	/**
	 * Used to access the real object
	 */
	protected ICard get() {
		if (c == null) {
			ITable superClass = getSchemaForLoading();
			Log.PERSISTENCE.debug("Lazy loaded card requested: " + superClass.getId() + ", " + getId());
			try {
				c = superClass.cards().list().id(getId()).get();
			} catch (NotFoundException e) {
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
			return UserContext.systemContext().tables().get(classId);
	}

	public int getId() {
		if (c != null)
			return c.getId();
		return cardId;
	}

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

	public ITable getSchema() {
		if (c != null)
			return c.getSchema();
		if (cardId <= 0) {
			return getSchemaForLoading();
		}
		if (lazyTable == null) {
			lazyTable = getSchemaForLoading();
			if (lazyTable.isSuperClass()) { // It should be done out of this if clause
				lazyTable = get().getSchema();
			}
		}
		return lazyTable;
	}

	public boolean equals(Object o) {
		if (o instanceof ICard) {
			ICard c = (ICard) o;
			return (classId == c.getIdClass() && cardId == c.getId());
		}
		return false;
	};

	public int hashCode() {
		return String.valueOf(getIdClass()).hashCode()+String.valueOf(getId()).hashCode();
	}

	/**
	 * Serialization
	 */

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(getIdClass());
		out.writeInt(getId());
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		this.classId = in.readInt();
		this.cardId = in.readInt();
	}
}
