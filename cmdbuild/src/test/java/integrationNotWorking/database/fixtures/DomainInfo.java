package integrationNotWorking.database.fixtures;

import org.cmdbuild.elements.interfaces.IDomain;

public class DomainInfo {
	String name;
	String class1;
	String class2;
	String cardinality;

	public DomainInfo(final String name, final String class1Name, final String class2Name, final String cardinality) {
		this.name = name;
		this.class1 = class1Name;
		this.class2 = class2Name;
		this.cardinality = cardinality;
	}

	public String getName() {
		return name;
	}

	public String getDBName() {
		if (name == null || name.isEmpty()) {
			return "Map";
		} else {
			return "Map_" + name;
		}
	}

	public String getClass1() {
		return class1;
	}

	public String getClass2() {
		return class2;
	}

	public String getCardinality() {
		return cardinality;
	}

	public boolean getReferenceDirection() {
		return IDomain.CARDINALITY_N1.equals(cardinality);
	}

	public String getReferenceTarget() {
		return getReferenceDirection() ? class2 : class1;
	}
}