package org.cmdbuild.api.fluent;

public class Relation {

	private String domainName;
	private CardDescriptor card1;
	private CardDescriptor card2;

	public String getDomainName() {
		return domainName;
	}

	public Relation setDomainName(final String domainName) {
		this.domainName = domainName;
		return this;
	}

	public String getClassName1() {
		return card1.getClassName();
	}

	public int getClassId1() {
		return card1.getId();
	}

	public Relation setCard1(final String className, final int id) {
		this.card1 = CardDescriptor.newInstance(className, id);
		return this;
	}

	public String getClassName2() {
		return card2.getClassName();
	}

	public int getClassId2() {
		return card2.getId();
	}

	public Relation setCard2(final String className, final int id) {
		this.card2 = CardDescriptor.newInstance(className, id);
		return this;
	}

}
