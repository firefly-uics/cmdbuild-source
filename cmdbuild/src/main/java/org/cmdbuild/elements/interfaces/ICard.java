package org.cmdbuild.elements.interfaces;

import java.util.Date;

import org.cmdbuild.common.annotations.OldDao;

@OldDao
@Deprecated
public interface ICard extends IAbstractElement {

	public enum CardAttributes {
		ClassId("IdClass"), Id("Id"), Code("Code", true), Description("Description", true), Status("Status"), User(
				"User"), BeginDate("BeginDate"), Notes("Notes", true);

		private final String descr;
		private final boolean visibleByUsers;

		CardAttributes(String descr) {
			this(descr, false);
		}

		CardAttributes(String descr, boolean visibleByUsers) {
			this.descr = descr;
			this.visibleByUsers = visibleByUsers;
		}

		public String toString() {
			return descr;
		}

		public String dbColumnName() {
			return descr;
		}

		public boolean isVisibleByUsers() {
			return visibleByUsers;
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
