package org.cmdbuild.elements.interfaces;

import org.cmdbuild.elements.DirectedDomain;

public interface IRelation extends IAbstractElement {

	public enum RelationAttributes {
		BeginDate("BeginDate"),
		IdObj1("IdObj1"),
		IdClass1("IdClass1"),
		IdObj2("IdObj2"),
		IdClass2("IdClass2");

		private final String descr;

		RelationAttributes(String descr) {
			this.descr = descr;
		}

		public String toString() {
			return descr;
		}
	}

	public void save();

	public ICard getCard1();
	public void setCard1(ICard card1);

	public ICard getCard2();
	public void setCard2(ICard card2);

	public IDomain getSchema();
	public void setSchema(IDomain schema);
	public boolean isReversed();
	public DirectedDomain getDirectedDomain();
}
