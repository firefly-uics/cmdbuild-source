package org.cmdbuild.elements.interfaces;

import java.util.Date;

public interface ICard extends IAbstractElement {

	public enum CardAttributes {
		ClassId("IdClass"),
		Id("Id"), Code("Code"),
		Description("Description"),
		Status("Status"),
		User("User"),
		BeginDate("BeginDate"),
		Notes("Notes");

		private final String descr;

		CardAttributes(String descr) {
			this.descr = descr;
		}

		public String toString() {
			return descr;
		}
	}

	public ITable getSchema();

	public int getIdClass();
	public void setIdClass(Integer idClass);

	public String getCode();
	public void setCode(String code);

	public String getDescription();
	public void setDescription(String description);

	public String getUser();
	public void setUser(String user);

	public Date getBeginDate();
	public void setBeginDate(Date date);

	public String getNotes();
	public void setNotes(String notes);	

	public String toString();
}
