package org.cmdbuild.api.fluent;

public class ExistingRelation extends ActiveRelation {

	public ExistingRelation(final FluentApiExecutor executor) {
		super(executor);
	}

	public ExistingRelation withDomainName(final String domainName) {
		super.setDomainName(domainName);
		return this;
	}

	public ExistingRelation withCard1(final String className, final int cardId) {
		super.setCard1(className, cardId);
		return this;
	}

	public ExistingRelation withCard2(final String className, final int cardId) {
		super.setCard2(className, cardId);
		return this;
	}

	public void delete() {
		executor().delete(this);
	}

}
