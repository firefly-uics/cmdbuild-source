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

@SuppressWarnings("unchecked")
public class DomainDeclarationImpl extends CQLElementImpl implements
		DomainDeclaration {
	String as;
	DomainDirection direction;
	
	int id = -1;
	String name;
	
	DomainDeclarationImpl subDomain = null;
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null)
			return false;
		if(!(obj instanceof DomainDeclarationImpl))
			return false;
		DomainDeclarationImpl o = (DomainDeclarationImpl)obj;
		if(direction != o.direction)
			return false;
		if(name != null){
			if(!name.equals(o.name))
				return false;
		} else if(o.name != null)
			return false;
		if(id != o.id)
			return false;
		if(as != null){
			if(!as.equals(o.as))
				return false;
		}else if(o.as != null)
			return false;
		if(subDomain != null) {
			if(!subDomain.equals(o.subDomain))
				return false;
		}else if(o.subDomain != null)
			return false;
		
		return true;
	}

	public String getAs() {
		return as;
	}

	public DomainDirection getDirection() {
		return direction;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public DomainDeclarationImpl getSubdomain() {
		return subDomain;
	}

	public DomainDeclarationImpl searchDomain(String nameOrRef) {
		if(this.name != null && this.name.equals(nameOrRef)) {
			return this;
		}
		if(this.as != null && this.as.equals(nameOrRef)) {
			return this;
		}
		if(this.subDomain != null) {
			return this.subDomain.searchDomain(nameOrRef);
		}
		return null;
	}

	public void setAs(String domainAs) {
		this.as = domainAs;
	}

	public void setDirection(DomainDirection direction) {
		this.direction = direction;
	}

	public void setId(int domainId) {
		this.id = domainId;
	}

	public void setName(String domainName) {
		this.name = domainName;
	}

	public void setSubdomain(DomainDeclaration subdomain) {
		this.subDomain = (DomainDeclarationImpl)subdomain;
	}

	private IDomain getIDomain(UserContext userCtx) {
		if (userCtx == null) {
			userCtx = UserContext.systemContext();
		}
		if (this.id > 0) {
			return userCtx.domains().get(id);
		} else {
			return userCtx.domains().get(name);
		}
	}

	public ITable getStartClassTable(UserContext userCtx) {
		return getClassTable(true, userCtx);
	}

	private ITable getEndClassTable() {
		return getClassTable(false, null);
	}

	public ITable getEndClassTable(UserContext userCtx) {
		return getClassTable(false, userCtx);
	}

	protected ITable getClassTable(boolean start, UserContext userCtx) {
		IDomain domain = getIDomain(userCtx);
		ITable t = null;
		if(this.parent instanceof ClassDeclarationImpl) {
			ClassDeclarationImpl p = parentAs();
			t = p.getClassTable(userCtx);
		} else {
			DomainDeclarationImpl p = parentAs();
			t = p.getEndClassTable(userCtx);
		}
		
		TableTree tree = TableImpl.tree();
		if( !tree.branch(domain.getClass1().getName()).contains(t.getId()) &&
			!tree.branch(domain.getClass2().getName()).contains(t.getId())) {
			throw new RuntimeException("Table " + t.getName() + " not found for domain " + domain.getName());
		}
		
		if(start) {
			return t;
		}
		switch(direction) {
		case INVERSE:
			return domain.getClass1();
		default:
			try {
				if(domain.getDirectionFrom(t)) {
					return domain.getClass2();
				} else {
					return domain.getClass1();
				}
			} catch(ORMException exc) {
				if(ORMException.ORMExceptionType.ORM_AMBIGUOUS_DIRECTION == exc.getExceptionType()) {
					return domain.getClass2();
				}
				throw exc;
			}
		}

	}
	public DirectedDomain getDirectedDomain(UserContext userCtx) {
		DirectedDomain out = null;
		if(DomainDirection.INVERSE == direction) {
			out = DirectedDomain.create(getIDomain(userCtx),DirectedDomain.DomainDirection.I);
		} else {
			IDomain domain = getIDomain(userCtx);
			ITable t = null;
			if(this.parent instanceof ClassDeclarationImpl) {
				ClassDeclarationImpl p = parentAs();
				t = p.getClassTable(userCtx);
			} else {
				DomainDeclarationImpl p = parentAs();
				t = p.getEndClassTable(userCtx);
			}
			
			TableTree tree = TableImpl.tree();
			if( !tree.branch(domain.getClass1().getName()).contains(t.getId()) &&
				!tree.branch(domain.getClass2().getName()).contains(t.getId())) {
				throw new RuntimeException("Table " + t.getName() + " not found for domain " + domain.getName());
			}
			try {
				boolean isdirect = domain.getDirectionFrom(t);
				out = DirectedDomain.create(domain, isdirect);
			} catch(ORMException exc) {
				if(ORMException.ORMExceptionType.ORM_AMBIGUOUS_DIRECTION == exc.getExceptionType()) {
					out = DirectedDomain.create(domain, true);
				}
				throw exc;
			}
		}
		return out;
	}

	public void check() {
		if(FactoryImpl.CmdbuildCheck) {
			getEndClassTable(); //this check the existence of the domain and that the tables are consistent
			if(subDomain != null) {
				subDomain.check();
			}
		}
	}
}
