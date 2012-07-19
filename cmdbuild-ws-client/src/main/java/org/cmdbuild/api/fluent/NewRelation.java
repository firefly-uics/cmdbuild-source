package org.cmdbuild.api.fluent;

public class NewRelation extends ActiveRelation {

	public NewRelation(final FluentApiExecutor executor) {
		super(executor);
	}

	public NewRelation withDomainName(final String domainName) {
		super.setDomainName(domainName);
		return this;
	}

	public NewRelation withCard1(final String className, final int cardId) {
		super.setCard1(className, cardId);
		return this;
	}

	public NewRelation withCard2(final String className, final int cardId) {
		super.setCard2(className, cardId);
		return this;
	}

	public void create() {
		executor().create(this);
	}

}
