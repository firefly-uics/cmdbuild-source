package org.cmdbuild.dao.entrytype;

public interface CMDomain extends CMEntryType {

	interface CMDomainDefinition extends CMEntryTypeDefinition {

		CMClass getClass1();

		CMClass getClass2();

		String getDirectDescription();

		String getInverseDescription();

		String getCardinality();

		boolean isMasterDetail();

		String getMasterDetailDescription();

	}

	CMClass getClass1();

	CMClass getClass2();

	String getDescription1();

	String getDescription2();

	String getCardinality();

	boolean isMasterDetail();

	String getMasterDetailDescription();

}
