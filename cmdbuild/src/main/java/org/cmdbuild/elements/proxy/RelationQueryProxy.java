package org.cmdbuild.elements.proxy;

import org.cmdbuild.elements.interfaces.IRelation;
import org.cmdbuild.elements.interfaces.RelationQuery;
import org.cmdbuild.elements.proxy.iterator.ProxyIterable;
import org.cmdbuild.elements.utils.CountedValue;
import org.cmdbuild.services.auth.UserContext;

public class RelationQueryProxy extends RelationQueryForwarder {

	public static class ProxedIterableRelation extends ProxyIterable<IRelation> {
		private UserContext uc;

		public ProxedIterableRelation(Iterable<IRelation> i, UserContext uc) {
			super(i);
			this.uc = uc;
		}
		protected boolean isValid(IRelation r) {
			return uc.privileges().hasReadPrivilege(r.getSchema());
		}
		protected IRelation createProxy(IRelation r) {
			return new RelationProxy(r, uc);
		}
	}

	private UserContext userCtx;

	public RelationQueryProxy(RelationQuery relationQuery, UserContext userCtx) {
		super(relationQuery);
		this.userCtx = userCtx;
	}

	// FIXME Add priviege filters for subclasses
	@Override
	public Iterable<CountedValue<IRelation>> getCountedIterable() {
		return new ProxyIterable<CountedValue<IRelation>>(super.getCountedIterable()) {
			protected boolean isValid(CountedValue<IRelation> cr) {
				return userCtx.privileges().hasReadPrivilege(cr.getValue().getSchema());
			}
			protected CountedValue<IRelation> createProxy(CountedValue<IRelation> cr) {
				IRelation r = new RelationProxy(cr.getValue(), userCtx);
				return new CountedValue<IRelation>(cr.getCount(), r);
			}
		};
	}

	// FIXME Add priviege filters for subclasses
	@Override
	public Iterable<IRelation> getIterable() {
		return new ProxedIterableRelation(super.getIterable(), userCtx);
	}
}
