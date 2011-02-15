package org.cmdbuild.elements;

import java.io.Serializable;

import org.cmdbuild.elements.interfaces.ObjectWithId;


public class Reference implements Serializable, ObjectWithId {

	private static final long serialVersionUID = 1L;

	private String description;
	
	private DirectedDomain directedDomain;
	private int idCard;
	private int idClass;

	protected Reference(DirectedDomain directedDomain, int id, String description, boolean isLoadedFromDB) {
		this.directedDomain = directedDomain;
		this.description = description;
		this.idCard = id;
		this.idClass = -1;
	}

	public Reference(DirectedDomain directedDomain, int id, String description) {
		this(directedDomain, id, description, false);
	}

	public int getId() {
		return idCard;
	}

	// WRONG! WHAT IF IT IS A SUBCLASS?
	public int getClassId() {
		if (idClass < 0) {
			idClass = directedDomain.getDestTable().cards().get(idCard).getIdClass();
		}
		return idClass;
	}

	public void setId(int id) {
		idCard = id;
		idClass = -1;
	}

	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public boolean equals(Object o) {
		if (o instanceof Reference)
			return this.getId() == ((Reference) o).getId();
		return false;
	}

}
