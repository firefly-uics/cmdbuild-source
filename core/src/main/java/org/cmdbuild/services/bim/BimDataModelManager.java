package org.cmdbuild.services.bim;

public interface BimDataModelManager {

	void createBimTableIfNeeded(String className);

	void deleteBimDomainOnClass(String oldClass);

	void createBimDomainOnClass(String className);

}