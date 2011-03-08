package org.cmdbuild.connector.differ;

public abstract class AbstractItemDiffer<T> extends AbstractDiffer<T, T> {

	public AbstractItemDiffer(final T customerRelation, final T cmdbuildRelation) {
		super(customerRelation, cmdbuildRelation);
	}

	@Override
	public void diff() throws DifferException {
		diff(customerItem, cmdbuildItem);
	}

	protected abstract void diff(final T customerItem, final T cmdbuildItem);

}
