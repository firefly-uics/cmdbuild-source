package org.cmdbuild.cql.compiler.impl;

import org.cmdbuild.cql.CQLBuilderListener.DomainDirection;
import org.cmdbuild.cql.compiler.from.DomainDeclaration;
import org.cmdbuild.elements.DirectedDomain;
import org.cmdbuild.elements.TableImpl;
import org.cmdbuild.elements.TableTree;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserOperations;

@SuppressWarnings("unchecked")
public class DomainDeclarationImpl extends CQLElementImpl implements DomainDeclaration {
	String as;
	DomainDirection direction;

	int id = -1;
	String name;

	DomainDeclarationImpl subDomain = null;

	@Override
	public boolean equals(final Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof DomainDeclarationImpl))
			return false;
		final DomainDeclarationImpl o = (DomainDeclarationImpl) obj;
		if (direction != o.direction)
			return false;
		if (name != null) {
			if (!name.equals(o.name))
				return false;
		} else if (o.name != null)
			return false;
		if (id != o.id)
			return false;
		if (as != null) {
			if (!as.equals(o.as))
				return false;
		} else if (o.as != null)
			return false;
		if (subDomain != null) {
			if (!subDomain.equals(o.subDomain))
				return false;
		} else if (o.subDomain != null)
			return false;

		return true;
	}

	@Override
	public String getAs() {
		return as;
	}

	@Override
	public DomainDirection getDirection() {
		return direction;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public DomainDeclarationImpl getSubdomain() {
		return subDomain;
	}

	@Override
	public DomainDeclarationImpl searchDomain(final String nameOrRef) {
		if (this.name != null && this.name.equals(nameOrRef)) {
			return this;
		}
		if (this.as != null && this.as.equals(nameOrRef)) {
			return this;
		}
		if (this.subDomain != null) {
			return this.subDomain.searchDomain(nameOrRef);
		}
		return null;
	}

	@Override
	public void setAs(final String domainAs) {
		this.as = domainAs;
	}

	@Override
	public void setDirection(final DomainDirection direction) {
		this.direction = direction;
	}

	@Override
	public void setId(final int domainId) {
		this.id = domainId;
	}

	@Override
	public void setName(final String domainName) {
		this.name = domainName;
	}

	@Override
	public void setSubdomain(final DomainDeclaration subdomain) {
		this.subDomain = (DomainDeclarationImpl) subdomain;
	}

	private IDomain getIDomain(UserContext userCtx) {
		if (userCtx == null) {
			userCtx = UserContext.systemContext();
		}
		if (this.id > 0) {
			return UserOperations.from(userCtx).domains().get(id);
		} else {
			return UserOperations.from(userCtx).domains().get(name);
		}
	}

	public ITable getStartClassTable(final UserContext userCtx) {
		return getClassTable(true, userCtx);
	}

	private ITable getEndClassTable() {
		return getClassTable(false, null);
	}

	public ITable getEndClassTable(final UserContext userCtx) {
		return getClassTable(false, userCtx);
	}

	protected ITable getClassTable(final boolean start, final UserContext userCtx) {
		final IDomain domain = getIDomain(userCtx);
		ITable t = null;
		if (this.parent instanceof ClassDeclarationImpl) {
			final ClassDeclarationImpl p = parentAs();
			t = p.getClassTable(userCtx);
		} else {
			final DomainDeclarationImpl p = parentAs();
			t = p.getEndClassTable(userCtx);
		}

		final TableTree tree = TableImpl.tree();
		if (!tree.branch(domain.getClass1().getName()).contains(t.getId())
				&& !tree.branch(domain.getClass2().getName()).contains(t.getId())) {
			throw new RuntimeException("Table " + t.getName() + " not found for domain " + domain.getName());
		}

		if (start) {
			return t;
		}
		switch (direction) {
		case INVERSE:
			return domain.getClass1();
		default:
			try {
				if (domain.getDirectionFrom(t)) {
					return domain.getClass2();
				} else {
					return domain.getClass1();
				}
			} catch (final ORMException exc) {
				if (ORMException.ORMExceptionType.ORM_AMBIGUOUS_DIRECTION == exc.getExceptionType()) {
					return domain.getClass2();
				}
				throw exc;
			}
		}

	}

	public DirectedDomain getDirectedDomain(final UserContext userCtx) {
		DirectedDomain out = null;
		if (DomainDirection.INVERSE == direction) {
			out = DirectedDomain.create(getIDomain(userCtx), DirectedDomain.DomainDirection.I);
		} else {
			final IDomain domain = getIDomain(userCtx);
			ITable t = null;
			if (this.parent instanceof ClassDeclarationImpl) {
				final ClassDeclarationImpl p = parentAs();
				t = p.getClassTable(userCtx);
			} else {
				final DomainDeclarationImpl p = parentAs();
				t = p.getEndClassTable(userCtx);
			}

			final TableTree tree = TableImpl.tree();
			if (!tree.branch(domain.getClass1().getName()).contains(t.getId())
					&& !tree.branch(domain.getClass2().getName()).contains(t.getId())) {
				throw new RuntimeException("Table " + t.getName() + " not found for domain " + domain.getName());
			}
			try {
				final boolean isdirect = domain.getDirectionFrom(t);
				out = DirectedDomain.create(domain, isdirect);
			} catch (final ORMException exc) {
				if (ORMException.ORMExceptionType.ORM_AMBIGUOUS_DIRECTION == exc.getExceptionType()) {
					out = DirectedDomain.create(domain, true);
				}
				throw exc;
			}
		}
		return out;
	}

	public void check() {
		if (FactoryImpl.CmdbuildCheck) {
			getEndClassTable(); // this check the existence of the domain and
								// that the tables are consistent
			if (subDomain != null) {
				subDomain.check();
			}
		}
	}
}
