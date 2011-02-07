package org.cmdbuild.elements;

import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.ITable;

public class DirectedDomain {

	public enum DomainDirection {
		D(true),
		I(false);

		private final boolean value;
		private final int hashOffset; 

		DomainDirection(boolean value) {
			this.value = value;
			this.hashOffset = value ? 0 : Integer.MAX_VALUE/2;
		}
		boolean getValue() {
			return this.value;
		}
		int getHashOffset() {
			return this.hashOffset;
		}
	}

	private IDomain domain;
	private DomainDirection direction;

	private DirectedDomain (IDomain domain, DomainDirection direction) {
		this.domain = domain;
		this.direction = direction;
	}

	public static DirectedDomain create(IDomain domain, boolean directionValue) {
		if (directionValue) {
			return create(domain, DomainDirection.D);
		} else {
			return create(domain, DomainDirection.I);
		}
	}

	public static DirectedDomain create(IDomain domain, DomainDirection direction) {
		return new DirectedDomain(domain, direction);
	}

	public IDomain getDomain() {
		return domain;
	}

	public DomainDirection getDirection() {
		return direction;
	}

	public boolean getDirectionValue() {
		return direction.getValue();
	}

	public ITable getDestTable() {
		return getDirectionValue() ? domain.getClass2() : domain.getClass1();
	}

	public ITable getSourceTable() {
		return getDirectionValue() ? domain.getClass1() : domain.getClass2();
	}

	public String getDescription() {
		return getDirectionValue() ? domain.getDescriptionDirect() : domain.getDescriptionInverse();
	}

	public boolean equals(Object o) {
		if (o instanceof DirectedDomain) {
			DirectedDomain d = ((DirectedDomain) o);
			return (this.domain == d.getDomain() && this.direction == d.getDirection());
		}
		return false;
	}

	public int hashCode() {
		return domain.getId() + direction.getHashOffset();
	}

	public String toString() {
		return String.valueOf(domain.getId())+"_"+direction.toString();
	}
}
