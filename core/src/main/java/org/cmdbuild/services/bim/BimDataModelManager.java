package org.cmdbuild.services.bim;

public interface BimDataModelManager {

	void createBimTable(String className, String value);

	void deleteBimDomainOnClass(String oldClass);

	void createBimDomainOnClass(String className);

}